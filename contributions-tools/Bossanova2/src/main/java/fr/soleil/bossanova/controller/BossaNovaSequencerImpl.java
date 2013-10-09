package fr.soleil.bossanova.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import javax.swing.JOptionPane;
import ptolemy.actor.Director;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.actor.v3.Actor;
import com.isencia.passerelle.core.ControlPort;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.director.PasserelleDirector;
import com.isencia.passerelle.hmi.action.ModelExecutor;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import fr.soleil.bossanova.bossaNovaData.BossaNovaData;
import fr.soleil.bossanova.configuration.RepositoryManager;
import fr.soleil.bossanova.model.Batch;
import fr.soleil.bossanova.model.DirectorFactory;
import fr.soleil.bossanova.model.DirectorType;
import fr.soleil.bossanova.model.Step;
import fr.soleil.bossanova.model.StepBlock;
import fr.soleil.bossanova.model.StepType;
import fr.soleil.passerelle.actor.TransformerV3;

public class BossaNovaSequencerImpl extends Observable implements Runnable, Sequencer {

  private final static Workspace WORKSPACE = new Workspace("Bossanova");

  private Thread thread;
  private Batch batch;
  private int currentRunningStepIndex = -1;
  private ModelExecutor modelExecutor = null;
  private final static Random random = new Random();
  private final static FlowManager flowManager = new FlowManager();
  private List<ExecutionListener> executionListeners = new ArrayList<ExecutionListener>();
  private boolean stopRequested = false;
  // private boolean useBossaNovaDirector = false;
  private boolean useNexusStorage = false;

  // This director instance is used as "template" for the directors of all actor-type steps.
  // Each actor-type step gets a clone of this director.
  private Director batchDirector = null;

  // Bug 17628
  private static boolean currentBatchSaved = true;

  // Bug 17570
  private static int currentBlockInd = 1;

  private String stopReason = null;

  // --------------------------------------------------------------------------
  // CONSTRUCTOR
  // --------------------------------------------------------------------------
  public BossaNovaSequencerImpl() {
    super();
    batch = new Batch();
    executionListeners = new ArrayList<ExecutionListener>();
  }

  // --------------------------------------------------------------------------
  // RUNNABLE IMPLEMENTATION
  // --------------------------------------------------------------------------
  @Override
  public void run() {
    // Can't use iterator in loop because it may cause
    // ConcurrentModificationException
    final List<Step> list = batch.getSteps();
    Step step = null;
    StepBlock currentBlock = null;

    for (int i = 0; i < list.size(); i++) {
      step = list.get(i);
      try {
        // if stop is requested : we stop execution of batch
        if (!isStopRequested()) {
          if (step.getBlockId() == 0) {
            executeStep(i, step);
          } else {
            if (currentBlock == null) {
              currentBlock = new StepBlock(step.getBlockIterationCount());
            }
            currentBlock.addStep(step);

            if ((i == list.size() - 1) || (step.getBlockId() != list.get(i + 1).getBlockId())) {
              executeBlock(i, currentBlock);
              currentBlock = null;
            }
          }
        }
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
    resetRunningStepIndex();

    // Bug 17641
    // displayViewComp(null, null);
  }

  private void resetRunningStepIndex() {
    currentRunningStepIndex = -1;
    // Erwin : 18205
    final boolean stoppedInError = (stopReason != null);
    if (isStopRequested()) {
      final String stopMsg = stoppedInError ? stopReason : "Batch stopped by user";
      BossaNovaData.getSingleton().getApplication().getTraceComponent().trace((Director) null, stopMsg);
    }
    BossaNovaData.getSingleton().getApplication().getMainScreen().getDirectorPanel().setVisible(true);

    setStopRequested(false);
    // Notify listeners
    setChangedAndNotify();
    fireExecutionFinished(stoppedInError);
  }

  /**
   * Check the step if it's valid or not
   * 
   * @param step
   * @return
   */
  private boolean validateStep(final Step step) {
    if (step == null) {
      return false;
    }

    if (step.getIterationCount() < 1) {
      return false;
    }

    if (step.getName() != null && !"".equals(step.getName().trim()) && step.isEnable()) {
      return true;
    }

    return false;
  }

  // --------------------------------------------------------------------------
  // BATCH EXECUTION
  // --------------------------------------------------------------------------
  public boolean isRunning() {
    return (currentRunningStepIndex != -1);
  }

  public void executeBatch(final ModelExecutor e) throws PasserelleException {
    setStopRequested(false);
    stopReason = null;
    thread = new Thread(this, "Sequencer");

    modelExecutor = e;
    thread.start();
  }

  // TODO REFACTOR
  // Bug 17641
  /***
   * private void displayViewComp(Flow flow, Step step) { MainScreen mainScreen = BossaNovaData.getSingleton().getApplication().getMainScreen(); // TODO QUICK
   * HACK // if (entity instanceof ISpecificBeans) { // ISpecificBeans beans = (ISpecificBeans) entity; // mainScreen.displayViewComponent(beans.getViewBean(),
   * step); // } else { mainScreen.displayViewComponent(null, step); // } }
   * 
   * @param stepIndex
   ***/

  private void executeStep(final int stepIndex, final Step step) throws Exception {
    currentRunningStepIndex++;
    // before executing the step we check if all parameters will be ok

    if (validateStep(step)) {
      setChangedAndNotify();

      // Complete if...else block commented out to avoid Sonar critical
      // violation
      // Block will be kept inside comment for future reference JC Pret
      // Jan 2011
      /***
       * Flow flow = step.getFlow(); if (flow != null) { // flow.setDirector(batch.getBatchDirector()); // step.applyFlowParametersToStep(); } else { // Fixing
       * Checkstyle bug JC Pret flow = createFlow(step, false); // End JC Pret }
       ***/
      // End Complete if...else block commented out...
      // Bug 17641
      // displayViewComp(flow, step);
      try {
        Flow flow = step.getFlow();
        if (flow == null) {
          flow = createFlow(step, false);
          step.setFlow(flow);
        }

        // END change SPJZ

        for (int i = 0; i < step.getIterationCount(); i++) {
          // if stop is requested we need to stop execution
          if (isStopRequested()) {
            return;
          }
          BossaNovaData
              .getSingleton()
              .getApplication()
              .getTraceComponent()
              .trace((Director) null,
                  "Executing " + step.getName() + "(" + (currentRunningStepIndex + 1) + ") - iteration " + (i + 1) + " on " + step.getIterationCount());
          // Need to do this, so that for each step, the events from the toolbar buttons
          // get directed to the right executing model.
          BossaNovaData.getSingleton().getApplication().setSelectedStep(stepIndex, step);
          // BossaNovaData.getSingleton().getApplication().setCurrentModel(flow, null,
          // null, false);
          StateMachine.getInstance().transitionTo(modelExecutor.getSuccessState());
          BossaNovaData.getSingleton().getApplication().launchModel(flow, modelExecutor, true, false);
          // if stop is requested we need to stop execution
          if (isStopRequested()) {
            return;
          }
        }
      } catch (final Exception e) {
        e.printStackTrace();
        step.setOnFault(true);
        // setStopRequested(true);
      }
    } else {
      if (step != null && step.getName() != null && !step.isBlockDelimiter()) {
        BossaNovaData.getSingleton().getApplication().getTraceComponent()
            .trace((Director) null, step.getName() + "(" + (currentRunningStepIndex + 1) + ") is NOT executing");
      }
    }
  }

  private void executeBlock(final int blockIndex, final StepBlock block) throws Exception {
    final int iterationCount = block.getIterationCount();
    int iterationInd, stepInd;
    final List<Step> list = block.getSteps();
    Step step = null;

    for (iterationInd = 0; iterationInd < iterationCount; iterationInd++) {
      for (stepInd = 0; stepInd < list.size(); stepInd++) {
        step = list.get(stepInd);
        try {
          // if stop is requested : we stop execution of batch
          if (!isStopRequested()) {
            executeStep(blockIndex, step);
          }
        } catch (final Exception e) {
          e.printStackTrace();
          throw e;
        }
      }
    }
  }

  /**
   * This method creates a new "template" batch director. Each creation of a FLow for an actor-type step should then assign a clone of this director, using the
   * method getBatchDirectorClone().
   * 
   * @return
   * @throws Exception
   */
  public Director createBatchDirector(final DirectorType directorType) throws Exception {
    final Director d = DirectorFactory.createNewDirector(directorType, WORKSPACE);
    batchDirector = d;
    return d;
  }

  public Director getBatchDirectorClone() throws CloneNotSupportedException {
    final Director d = (Director) batchDirector.clone(WORKSPACE);
    // batch directors should not appear in the cfg panels
    // GenericHMI.showModelForm(...) checks for the presence of this attribute.
    // when present : cfg panel is not created
    try {
      PasserelleDirector passD = (PasserelleDirector)d;
      ((Attribute) passD.getAdapter(null)).setContainer(null);
      DirectorUtils.getAdapter(d, null);
      new Attribute(d, "__not_configurable");
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return d;
  }

  public Flow createFlow(final Step step, final boolean forceCreate) throws Exception {
    Flow result = step.getFlow();
    if (result == null || forceCreate) {
      final String stepName = step.getName();
      if (stepName != null && !"".equals(stepName.trim()) && !step.isBlockDelimiter()) {
        // Bug 17624
        // Added try..catch block to detect sequence load failure

        try {
          // -------------------------------------------------------------------------------
          // ACTORS
          // -------------------------------------------------------------------------------
          if (StepType.ACTOR.equals(step.getType())) {
            Integer stepkey = step.getId();
            if (stepkey == null) {
              stepkey = random.nextInt();
              step.setId(stepkey);
            }

            result = new Flow(WORKSPACE, null);
            result.setName(stepName + "_" + stepkey);
            // trick to store the "plain" model name as well,
            // which can be used for model prefs mgmt etc
            result.setDisplayName(stepName);
            result.setDirector(getBatchDirectorClone());

            final TypedAtomicActor actorInstance = RepositoryManager.getActorRepository().getActorForName(step.getName());

            final TypedAtomicActor clone = (TypedAtomicActor) actorInstance.clone(result.workspace());
            clone.setContainer(result);
            if (clone instanceof TransformerV3) {
              final TransformerV3 transClone = (TransformerV3) clone;
              final TransformerV3 transInstance = (TransformerV3) actorInstance;
              // Field is PUBLIC !!
              if (transClone.input == null) {
                transClone.input = PortFactory.getInstance().createInputPort(transClone, transInstance.input.getName() + " ",
                    transInstance.input.getExpectedMessageContentType());
              }
              if (transClone.output == null) {
                transClone.output = PortFactory.getInstance().createInputPort(transClone, transInstance.output.getName() + " ",
                    transInstance.output.getExpectedMessageContentType());
              }
            }
            connectAllActorInputs(result, clone);

            // -------------------------------------------------------------------------------
            // SEQUENCE
            // -------------------------------------------------------------------------------
          } else if (StepType.SEQUENCE.equals(step.getType())) {
            final File f = RepositoryManager.getSequenceRepository().getSequenceForName(stepName);
            result = FlowManager.readMoml(f.toURL());
            result.setName(stepName + "_" + random.nextInt());
            // trick to store the "plain" model name as well,
            // which can be used for model prefs mgmt etc
            result.setDisplayName(stepName);
          }
        } catch (final Exception e) {
          // bug 17631
          // Added popup in case of sequence loading failure
          JOptionPane.showMessageDialog(BossaNovaData.getSingleton().getApplication().getDialogHookComponent(),
              "Error loading " + step.getType() + " " + step.getName());
          step.setOnFault(true);
          // throw(e);
        }
        // End Bug 17624
        flowManager.applyParameterSettings(result, step.getParameters(), step.getContexts());
      }
    } else {
      step.applyFlowParametersToStep();
    }

    step.setFlow(result);
    return result;
  }

  private void connectAllActorInputs(final Flow flow, final TypedAtomicActor actor) {
    if (actor instanceof Actor) {
      for (final Iterator<IOPort> iterator = actor.inputPortList().iterator(); iterator.hasNext();) {
        final IOPort inputPort = iterator.next();
        connectPortWithConstant(flow, inputPort);
      }
    }
  }

  private void connectPortWithConstant(final Flow flow, final IOPort inputPort) {
    try {
      // In order to avoid confusing:
      // do not display ControlPort ports
      if (!(inputPort instanceof ControlPort)) {
        if (inputPort != null) {
          final ptolemy.actor.Actor trigger = flow.addActor(com.isencia.passerelle.actor.general.Const.class, inputPort.getName());
          final IOPort triggerOutput = (IOPort) ((ComponentEntity) trigger).getPort("output");
          flow.connect(triggerOutput, inputPort);
        }
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  // --------------------------------------------------------------------------
  // ACCESSORS
  // --------------------------------------------------------------------------
  public Batch getBatch() {
    return batch;
  }

  public void setBatch(final Batch batch) {
    this.batch = batch;
    BossaNovaData.getSingleton().getApplication().setSelectedStep(0, batch.getStep(0));
    setChangedAndNotify();
  }

  public void setCurrentRunningStep(final int currentRunningStepIndex) {
    this.currentRunningStepIndex = currentRunningStepIndex;
  }

  public int getCurrentRunningStepIndex() {
    return currentRunningStepIndex;
  }

  // --------------------------------------------------------------------------
  // BATCH CONSTRUCTION
  // --------------------------------------------------------------------------
  @Override
  public void cleanAll() {
    this.batch = new Batch();
    BossaNovaData.getSingleton().getApplication().setSelectedStep(0, null);
    setChangedAndNotify();
  }

  @Override
  public boolean addEmptyStep(final String comment) {
    final Step step = new Step(StepType.ACTOR, "");
    step.setComment(comment);
    return addStep(step, false);
  }

  public boolean addStep(final Step step) {
    return addStep(step, true);
  }

  private boolean addStep(final Step step, final boolean checkStep) {
    boolean result = false;
    if (batch != null) {
      result = batch.addStep(step, checkStep);
    }
    setCurrentBatchSaved(false);
    setChangedAndNotify();
    StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
    return result;
  }

  /*
   * (non-Javadoc)
   * @see fr.soleil.bossanova.controller.Sequencer#addStep(java.lang.String)
   */
  @Override
  public boolean addSequenceStep(final String stepName, final String comment, final Map<String, String> parameters) {
    return true;
  }

  @Override
  public boolean addSequenceStep(final String stepName) {
    final Step step = new Step(StepType.SEQUENCE, stepName);
    return addStep(step, true);
  }

  @Override
  public boolean addActorStep(final String stepName) {
    return addActorStep(stepName, null, null);
  }

  @Override
  public boolean addActorStep(final String stepName, final String comment, final Map<String, String> parameters) {
    final Step step = new Step(StepType.ACTOR, stepName);
    step.setParameters(parameters);
    step.setComment(comment);
    return addStep(step, true);
  }

  /*
   * (non-Javadoc)
   * @see fr.soleil.bossanova.controller.Sequencer#getPossibleStepsNames()
   */
  @Override
  public List<String> getPossibleStepsNames() {
    return RepositoryManager.getElementNames();
  }

  @Override
  public List<String> getPossibleActorStepsNames() {
    return RepositoryManager.getActorRepository().getEnabledActorNames();
  }

  @Override
  public List<String> getPossibleSequenceStepsNames() {
    return RepositoryManager.getSequenceRepository().getSequenceNames();
  }

  /**
   * Insert a step in the batch
   * 
   * @param step
   * @param index
   */
  public void insertStepAt(final Step step, final int index) {
    if (batch != null) {
      batch.insertStepAt(step, index);
    }
    setCurrentBatchSaved(false);
    setChangedAndNotify();
  }

  public void insertStepsAt(final List<Step> steps, final int index) {
    if (batch != null) {
      for (int i = 0; i < steps.size(); i++) {
        final Step step = steps.get(i);
        batch.insertStepAt(step, index + i);
      }
    }
    setCurrentBatchSaved(false);
    setChangedAndNotify();
  }

  public void removeStep(final Step step) {
    if (batch != null) {
      batch.removeStep(step);
      final boolean emptyBatch = batch.size() == 0;
      setCurrentBatchSaved(emptyBatch);
    }
    setChangedAndNotify();
  }

  public void removeStepAt(final int index) {
    if (batch != null) {
      if (index <= batch.size() - 1) {
        batch.removeStepAt(index);
      }
      final boolean emptyBatch = batch.size() == 0;
      setCurrentBatchSaved(emptyBatch);
    }
    setChangedAndNotify();
  }

  public void moveStepUp(final int stepIndex) {
    if (stepIndex > 0 && stepIndex < batch.size()) {
      final Step stepToMove = batch.getStep(stepIndex);
      if (stepToMove != null) {
        batch.removeStepAt(stepIndex);
        batch.insertStepAt(stepToMove, stepIndex - 1);
        setCurrentBatchSaved(false);
        setChangedAndNotify();
      }
    }
  }

  public void moveStepdown(final int stepIndex) {
    if (stepIndex < batch.size() - 1) {
      final Step stepToMove = batch.getStep(stepIndex);
      if (stepToMove != null) {
        batch.removeStepAt(stepIndex);
        batch.insertStepAt(stepToMove, stepIndex + 1);
        setCurrentBatchSaved(false);
        setChangedAndNotify();
      }
    }
  }

  public void modifyIterationCountForStepAt(final int stepIndex, final int iterationCount) {
    final Step step = getBatch().getStep(stepIndex);

    if (step != null) {
      if (step.isBlockDelimiter()) {
        step.setBlockIterationCount(iterationCount);
      } else {
        step.setIterationCount(iterationCount);
      }
      setCurrentBatchSaved(false);
      setChangedAndNotify();
    }
  }

  public void modifyTypeAndNameForStepAt(final int stepIndex, final String name, final StepType stepType) {
    final Step step = getBatch().getStep(stepIndex);

    if (step != null) {
      if (stepType == null) {
        step.setOnFault(true);
        JOptionPane.showMessageDialog(BossaNovaData.getSingleton().getApplication().getDialogHookComponent(),
            "Error in Step configuration - unknown Step type or data " + step.getName());
      } else {

        // we apply changes only if it's a real changes. for example if the current step is
        // a Traccer and user click on combobox step to select a Tracer (same actor) then
        // there are nothing to change. But if he select an AttributeReader it's a real
        // change and we apply it.
        //
        // cf bug 0018737
        if ((!step.getName().equals(name)) || (step.getType() != stepType)) {
          step.setOnFault(false);
          step.setFlow(null);
          step.setName(name);
          step.setType(stepType);

          // TODO REFACTOR that's weird
          BossaNovaData.getSingleton().getApplication().getMainScreen().changeStep();
          setCurrentBatchSaved(false);
          setChangedAndNotify();
        }

      }
    }
  }

  public void modifyCommentForStepAt(final int stepIndex, final String comment) {
    final Step step = getBatch().getStep(stepIndex);

    if (step != null) {
      step.setComment(comment);
    }
  }

  public void modifyEnableForStepAt(final int stepIndex, final boolean enable) {
    final Step step = getBatch().getStep(stepIndex);

    if (step != null) {
      step.setEnable(enable);
    }
  }

  /**
   * Enable or Disable all step
   * 
   * @param enable
   */
  public void modifyEnableForAllStep(final boolean enable) {
    for (int i = 0; i < getBatch().size(); i++) {
      final Step step = getBatch().getStep(i);
      step.setEnable(enable);
    }
    setCurrentBatchSaved(false);
    setChangedAndNotify();
  }

  public void checkBatchSteps() {
    for (int i = 0; i < getBatch().size(); i++) {
      final Step step = getBatch().getStep(i);
      if (StepType.SEQUENCE.equals(step.getType())) {
        final File f = RepositoryManager.getSequenceRepository().getSequenceForName(step.getName());
        if (f == null) {
          step.setOnFault(true);
          setChangedAndNotify();
        }
      }
    }

  }

  private void setChangedAndNotify() {
    setChanged();
    notifyObservers();
  }

  public void addExecutionListener(final ExecutionListener execListener) {
    this.executionListeners.add(execListener);
  }

  private void fireExecutionFinished(final boolean stoppedInError) {
    // Erwin : 18205
    for (final ExecutionListener executionListener : executionListeners) {
      final ExecutionListener listener = executionListener;
      if (!stoppedInError) {
        listener.executionFinished(null);
      } else {
        listener.executionError(null, null);
      }
    }
  }

  public synchronized boolean isStopRequested() {
    return stopRequested;
  }

  public synchronized void stop() {
    // duplicated from end of run()
    // resetRunningStepIndex();
    setStopRequested(true);
    stopReason = null;
  }

  // Erwin 18205
  public synchronized void stopWithError(final String stopReason) {
    stop();
    this.stopReason = stopReason;
  }

  public synchronized void setStopRequested(final boolean stopRequested) {
    this.stopRequested = stopRequested;
  }

  public ModelExecutor getModelExecutor() {
    return modelExecutor;
  }

  public boolean useNexusStorage() {
    return this.useNexusStorage;
  }

  public void setUseNexusStorage(final boolean useNexusStorage) {
    this.useNexusStorage = useNexusStorage;
  }

  // Bug 17628
  public static boolean getCurrentBatchSaved() {
    return currentBatchSaved;
  }

  public static void setCurrentBatchSaved(final boolean batchSavedValue) {
    currentBatchSaved = batchSavedValue;
  }

  // End Bug 17628

  // Bug 17570
  public static int getCurrentBlockInd() {
    return currentBlockInd;
  }

  public static void incCurrentBlockInd() {
    currentBlockInd++;
  }

  public static void resetCurrentBlockInd() {
    currentBlockInd = 1;
  }

  public void applyDirectorConfigChangeToAllSteps() {
    final List<Step> list = batch.getSteps();
    for (final Step step : list) {
      if (StepType.ACTOR.equals(step.getType())) {
        final Flow flow = step.getFlow();
        if (flow != null) {
          try {
            if (flow.getDirector() != null) {
              flow.getDirector().setContainer(null);
            }
            flow.setDirector(getBatchDirectorClone());
          } catch (final Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
    BossaNovaData.getSingleton().getApplication().refreshCurrentlySelectedStep();
  }

}
