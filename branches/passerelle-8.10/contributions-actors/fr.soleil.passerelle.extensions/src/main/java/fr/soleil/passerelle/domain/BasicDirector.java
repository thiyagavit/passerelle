package fr.soleil.passerelle.domain;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.ext.ExecutionTracer;
import com.isencia.passerelle.ext.impl.DefaultDirectorAdapter;
import com.isencia.passerelle.ext.impl.DefaultExecutionTracer;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoDs.TangoConst;
import fr.soleil.passerelle.actor.IActorFinalizer;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.Properties;

/**
 * Director implementation which adds a choice of error control strategies.
 * 
 * @author erwin
 */
@SuppressWarnings("serial")
public class BasicDirector extends com.isencia.passerelle.domain.cap.Director {

    private final static Logger logger = LoggerFactory.getLogger(BasicDirector.class);

    private String modelName;

    private final List<IActorFinalizer> finalizers = new ArrayList<IActorFinalizer>();

    public StringParameter selectedErrCtrlStrategyParameter;
    public StringParameter nbRetryParameter;
    private int nbRetry;
    public StringParameter pausingTimeParameter;
    private double pausingTime;
    private ExecutionTracer tracer;

    enum ErrorStrategy {
        DEFAULT, RETRY;
    }

    ErrorStrategy errorStrategy;

    private boolean interactiveExecution;

    private void init() throws IllegalActionException, NameDuplicationException {
        DirectorAdapter adapter = getAdapter(null);

        if (adapter instanceof DefaultDirectorAdapter) {
            ((Parameter) getAttribute(DirectorAdapter.STOP_FOR_UNHANDLED_ERROR_PARAM)).setToken(BooleanToken.TRUE);
        }

        propsFileParameter.setVisibility(Settable.EXPERT);
        boolean firstTime = true;
        selectedErrCtrlStrategyParameter = new StringParameter(this, "Error Control");
        selectedErrCtrlStrategyParameter.addChoice(ErrorStrategy.DEFAULT.name());
        selectedErrCtrlStrategyParameter.addChoice(ErrorStrategy.RETRY.name());
        if (firstTime) {
            selectedErrCtrlStrategyParameter.setExpression(ErrorStrategy.DEFAULT.name());
            firstTime = false;
        }

        nbRetryParameter = new StringParameter(this, "Nb Retries");
        nbRetryParameter.setExpression("3");
        pausingTimeParameter = new StringParameter(this, "Pausing time before retrying (s)");
        pausingTimeParameter.setExpression("1");
        tracer = new DefaultExecutionTracer();

        final URL url = this.getClass().getResource("/fr/soleil/soleil.jpg");
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"0\" y=\"0\" width=\"160\" "
                + "height=\"80\" style=\"fill:white;stroke:black\"/>\n"
                + " <image x=\"0\" y=\"0\" width =\"150\" height=\"70\" xlink:href=\"" + url + "\"/>\n" + "</svg>\n");
    }

    /**
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public BasicDirector() throws IllegalActionException, NameDuplicationException {
        init();
    }

    /**
     * @param workspace
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public BasicDirector(final Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace);
        init();
    }

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public BasicDirector(final CompositeEntity container, final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        init();
    }

    public boolean isInteractiveExecution() {
        return interactiveExecution;
    }

    public void setInteractiveExecution(final boolean interactiveExecution) {
        this.interactiveExecution = interactiveExecution;
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == nbRetryParameter) {
            nbRetry = Integer.parseInt(nbRetryParameter.getExpression());
        } else if (attribute == pausingTimeParameter) {
            pausingTime = PasserelleUtil.getParameterDoubleValue(pausingTimeParameter);
        } else if (attribute == selectedErrCtrlStrategyParameter) {
            final String val = PasserelleUtil.getParameterValue(selectedErrCtrlStrategyParameter);
            if (val.equalsIgnoreCase(ErrorStrategy.RETRY.toString())) {
                errorStrategy = ErrorStrategy.RETRY;
            } else {
                errorStrategy = ErrorStrategy.DEFAULT;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    public void initialize() throws IllegalActionException {

        ExecutionTracerService.registerTracer(tracer);

        // set parameters for retry error strategy
        if (errorStrategy.equals(ErrorStrategy.DEFAULT)) {
            logger.info("Error Stategy is DEFAULT");
            Properties.setDelay(0);
            Properties.setRetries(1);
        } else if (errorStrategy.equals(ErrorStrategy.RETRY)) {
            Properties.setDelay((int) (pausingTime * 1000));
            Properties.setRetries(nbRetry);
            logger.info("Error Stategy is RETRY : nb {} and pausing {}", nbRetry, pausingTime);
        } else {
            Properties.setDelay(0);
            Properties.setRetries(0);
        }

        modelName = getFullName().substring(1, getFullName().lastIndexOf("."));

        if (getAdapter(null).isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - ###START " + modelName + "###");
        } else {
            // get tango host from file if needed
            // if (tangoHost == null && propsFileParameter.getExpression() !=
            // null) {
            // try {
            // final InputStream propsInput = new
            // FileInputStream(propsFileParameter
            // .getExpression());
            // System.getProperties().load(propsInput);
            // } catch (final FileNotFoundException e) {
            //
            // } catch (final IOException e) {
            //
            // }
            // }
            logger.debug("TANGO_HOST is " + System.getProperty("TANGO_HOST"));
            ExecutionTracerService.trace(this,
                    "###START " + modelName + "### with strategy " + errorStrategy.toString());
            // allow all access on tangodb
            Database db;
            try {
                db = ApiUtil.get_db_obj();
                db.setAccessControl(TangoConst.ACCESS_WRITE);
            } catch (final DevFailed e) {
                // System.out.println("Connection to TANGO failed");
                throw new IllegalActionException(TangoToPasserelleUtil.getDevFailedString(e, this));
            }
        }

        super.initialize();

    }

    @Override
    public void wrapup() throws IllegalActionException {
        if (getAdapter(null).isMockMode()) {
            ExecutionTracerService.trace(this, "MOCK - ###END " + modelName + "###");
        } else {
            for (final IActorFinalizer finalizer : finalizers) {
                finalizer.doFinalAction();
            }
            ExecutionTracerService.trace(this, "###END " + modelName + "###");
        }
        ExecutionTracerService.removeTracer(tracer);
        super.wrapup();
    }

    public void registerFinalizer(final IActorFinalizer finalizer) {
        if (!finalizers.contains(finalizer)) {
            finalizers.add(finalizer);
        }
    }

    public void removeFinalizer(final IActorFinalizer finalizer) {
        if (finalizers.contains(finalizer)) {
            finalizers.remove(finalizer);
        }
    }
}
