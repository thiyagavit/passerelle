package fr.soleil.passerelle.actor.tango;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.doc.generator.ParameterName;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

@SuppressWarnings("serial")
public abstract class ATangoCommandActor extends ATangoDeviceActor {

    private static final String COMMAND_NAME = "Command Name";
    private final static Logger logger = LoggerFactory.getLogger(ATangoCommandActor.class);

    /**
     * The command name
     */
    @ParameterName(name = COMMAND_NAME)
    public Parameter commandNameParam;
    private String commandName;

    private TangoCommand tangoCommand;

    public ATangoCommandActor(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        commandNameParam = new StringParameter(this, COMMAND_NAME);
        commandNameParam.setExpression(commandName);
    }

    @Override
    /*
     * @throws IllegalActionException
     */
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == commandNameParam) {
            commandName = PasserelleUtil.getParameterValue(commandNameParam);
        }
        else {
            super.attributeChanged(arg0);
        }
    }

    @Override
    /*
     * When not in mock mode, create the command proxy with the attribute name
     * 
     * @throws InitializationException
     */
    protected void validateInitialization() throws ValidationException {

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " validateInitialization() - entry");
        }
        if (!isMockMode()) {
            try {
                tangoCommand = new TangoCommand(getDeviceName(), commandName);
            }
            catch (final DevFailed e) {
                ExceptionUtil.throwValidationException(this, e);
            }
        }
        super.validateInitialization();
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " validateInitialization() - exit");
        }
    }

    /**
     * 
     * @return The TangoCommand which is initialized in {@link ATangoCommandActor#doInitialize()}
     * @throws PasserelleException
     */
    public TangoCommand getTangoCommand() throws PasserelleException {
        if (tangoCommand == null) {
            ExceptionUtil.throwPasserelleException("field not initialized", commandName);
        }
        return tangoCommand;
    }

    @Override
    public Object clone(final Workspace workspace) throws CloneNotSupportedException {
        final ATangoCommandActor copy = (ATangoCommandActor) super.clone(workspace);
        copy.tangoCommand = null;
        return copy;
    }

}
