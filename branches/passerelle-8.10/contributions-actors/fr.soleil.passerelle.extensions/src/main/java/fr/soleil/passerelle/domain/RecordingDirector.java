package fr.soleil.passerelle.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.recording.DataRecorder;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class RecordingDirector extends BasicDirector {

  private final static Logger logger = LoggerFactory.getLogger(RecordingDirector.class);

  private boolean asyncRecording = true;
  public Parameter asyncRecordingParam;

  // private Parameter saveParam;
  // private boolean save = true;

  private boolean autoChangeNxEntry = false;
  public Parameter autoChangeNxEntryParam;

  private String dataRecorderName;
  private final Parameter dataRecorderNameParam;
  
  public RecordingDirector() throws IllegalActionException, NameDuplicationException {
    super();

    dataRecorderNameParam = new StringParameter(this, "datarecorderName");

    autoChangeNxEntryParam = new Parameter(this, "Auto Change NXEntry", new BooleanToken(false));
    autoChangeNxEntryParam.setTypeEquals(BaseType.BOOLEAN);

    asyncRecordingParam = new Parameter(this, "Asynchronous Recording", new BooleanToken(true));
    asyncRecordingParam.setTypeEquals(BaseType.BOOLEAN);
  }

  public RecordingDirector(final CompositeEntity container, final String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    dataRecorderNameParam = new StringParameter(this, "datarecorderName");

    autoChangeNxEntryParam = new Parameter(this, "Auto Change NXEntry", new BooleanToken(false));
    autoChangeNxEntryParam.setTypeEquals(BaseType.BOOLEAN);

    asyncRecordingParam = new Parameter(this, "Asynchronous Recording", new BooleanToken(true));
    asyncRecordingParam.setTypeEquals(BaseType.BOOLEAN);
  }

  public RecordingDirector(final Workspace workspace) throws IllegalActionException, NameDuplicationException {
    super(workspace);

    dataRecorderNameParam = new StringParameter(this, "datarecorderName");

    autoChangeNxEntryParam = new Parameter(this, "Auto Change NXEntry", new BooleanToken(false));
    autoChangeNxEntryParam.setTypeEquals(BaseType.BOOLEAN);

    asyncRecordingParam = new Parameter(this, "Asynchronous Recording", new BooleanToken(true));
    asyncRecordingParam.setTypeEquals(BaseType.BOOLEAN);
  }

  @Override
  public void attributeChanged(final Attribute attribute) throws IllegalActionException {
    if (attribute == autoChangeNxEntryParam) {
      autoChangeNxEntry = Boolean.valueOf(autoChangeNxEntryParam.getExpression());
    } else if (attribute == asyncRecordingParam) {
      asyncRecording = Boolean.valueOf(asyncRecordingParam.getExpression());
    } else {
      super.attributeChanged(attribute);
    }
  }

  public String getDataRecorderName() {
    return dataRecorderName;
  }

  @Override
  public void initialize() throws IllegalActionException {
    if (!getAdapter(null).isMockMode()) {
      try {
        // if (!useRecordTest) {
        // dataRecorderName = SoleilUtilities
        // .getDevicesFromClass("DataRecorder")[0];
        // }
        dataRecorderName = PasserelleUtil.getParameterValue(dataRecorderNameParam);
        logger.debug("using datarecorder {} ", dataRecorderName);
        if(DataRecorder.getInstance().isRecordingStarted(dataRecorderName)){
            throw new IllegalActionException("DataRecorder session is already Running. Stop it before starting a new one.");
        }
        DataRecorder.getInstance().startSession();       
        DataRecorder.getInstance().setAsyncMode(dataRecorderName, asyncRecording);
        if (asyncRecording) {
          ExecutionTracerService.trace(this, "using asynchronous recording");
        } else {
          ExecutionTracerService.trace(this, "using synchronous recording");
        }
      } catch (final DevFailed e) {
        throw new IllegalActionException(TangoToPasserelleUtil.getDevFailedString(e, this));
      }
    }

    super.initialize();
  }

  public boolean isAsyncRecording() {
    return asyncRecording;
  }

  public boolean isAutoChangeNxEntry() {
    return autoChangeNxEntry;
  }

  public void setRecorderName(final String name) {
    dataRecorderName = name;
  }

  @Override
  public void stopFire() {
    try {
      if (!getAdapter(null).isMockMode()) {
        DataRecorder.getInstance().cancel();
        ExecutionTracerService.trace(this,dataRecorderName +  "/endRecording called");
        DataRecorder.getInstance().endRecording(dataRecorderName);
      }
    } catch (final Exception e) {
        ExecutionTracerService.trace(this, e.getMessage());
    }
    super.stopFire();
  }

  @Override
  public void wrapup() throws IllegalActionException {
    if (!getAdapter(null).isMockMode()) {
      try {
        DataRecorder.getInstance().cancel();
        if(DataRecorder.getInstance().isStartRecording()){
            ExecutionTracerService.trace(this,dataRecorderName +  "/endRecording called");
            DataRecorder.getInstance().endRecording(dataRecorderName);
        }
      } catch (final DevFailed e) {
          ExecutionTracerService.trace(this,TangoToPasserelleUtil.getDevFailedString(e, this));
      }
    }
    super.wrapup();
  }
}
