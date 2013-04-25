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
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.doc.generator.ParameterName;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.util.DevFailedValidationException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

/**
 * @author ABEILLE
 */
@SuppressWarnings("serial")
public abstract class ATangoDeviceActorV5 extends ATangoActorV5 {

    public static final String DEVICE_NAME = "Device Name";
    public static final String ERROR_DEVICE_NAME_EMPTY = DEVICE_NAME
            + "  parameter can not be empty";
    private static final Logger logger = LoggerFactory.getLogger(ATangoDeviceActorV5.class);
    /**
     * The Tango device name (e.g domain/family/member).
     */
    @ParameterName(name = DEVICE_NAME)
    public Parameter deviceNameParam;
    /**
     * The string corresponding to the value of deviceNameParam.
     */
    private String deviceName = "domain/family/member";
    /**
     * The device proxy construct from the device name in doInitialize.
     */
    private DeviceProxy deviceProxy;
    private boolean createDeviceProxy = true;

    /**
     * @throws ptolemy.kernel.util.NameDuplicationException
     * 
     * @throws ptolemy.kernel.util.IllegalActionException
     * 
     */
    public ATangoDeviceActorV5(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        deviceNameParam = new StringParameter(this, DEVICE_NAME);
        deviceNameParam.setExpression(deviceName);
    }

    /*
     * @throws IllegalActionException
     */
    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == deviceNameParam) {
            // deviceName is trimmed by getParameterValue
            deviceName = PasserelleUtil.getParameterValue(deviceNameParam);
            if (deviceName.isEmpty()) {
                throw new IllegalActionException(ERROR_DEVICE_NAME_EMPTY);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * It's a wrapper of attributeChanged that "convert" IllegalActionException in
     * ValidationException. This method is design to be used in validateInitialization() method to
     * do the static verification on parameters
     * 
     * @param attribute the parameter to check
     * 
     * @throws ValidationException if the parameter is invalid then a ValidationException is raised
     */
    protected void validateAttribute(Attribute attribute) throws ValidationException {
        try {
            attributeChanged(attribute);
        }
        catch (IllegalActionException e) {
            throw new ValidationException(ErrorCode.FLOW_VALIDATION_ERROR, e.getMessage(), this, e);
        }
    }

    /*
     * When not in mock mode, create the device proxy with the device name
     *
     * @throws InitializationException
     */
    @Override
    protected void validateInitialization() throws ValidationException {

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " validateInitialization() - entry");
        }

        if (!isMockMode() && createDeviceProxy) {
            validateAttribute(deviceNameParam);

            try {
                // see bug 22954 : The deviceProxy is still created here because the daughter
                // classes need of it
                deviceProxy = ProxyFactory.getInstance().createDeviceProxy(deviceName);
                new TangoCommand(deviceName, "State").execute();

            }
            catch (final DevFailed e) {
                throw new DevFailedValidationException(e, this);
            }
            catch (Exception e) {
                new ValidationException(ErrorCode.FLOW_VALIDATION_ERROR, e.getMessage(), this, e); // TODO
                                                                                                   // add
                                                                                                   // LOG
            }
        }
        super.validateInitialization();

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " validateInitialization() - exit");
        }
    }

    /**
     * @return The parameter for device name
     * 
     * @deprecated not used and parameter is public
     */
    @Deprecated
    protected final Parameter getDeviceNameParam() {
        return deviceNameParam;
    }

    /**
     * @return The device name
     */
    protected final String getDeviceName() {
        return deviceName;
    }

    /**
     * @return The device proxy (initialized in
     *         {@link fr.soleil.passerelle.actor.tango.ATangoDeviceActorV5#doInitialize()}
     * 
     * @throws com.isencia.passerelle.core.PasserelleException
     * 
     */
    protected final DeviceProxy getDeviceProxy() throws PasserelleException {
        if (deviceProxy == null) {
            throw new PasserelleException(ErrorCode.ERROR, "device proxy not initialized", this,
                    null);
        }
        return deviceProxy;
    }

    public boolean isCreateDeviceProxy() {
        return createDeviceProxy;
    }

    public void setCreateDeviceProxy(final boolean createDeviceProxy) {
        this.createDeviceProxy = createDeviceProxy;
    }

    @Override
    public Object clone(final Workspace workspace) throws CloneNotSupportedException {
        final ATangoDeviceActorV5 copy = (ATangoDeviceActorV5) super.clone(workspace);
        copy.deviceProxy = null;
        return copy;
    }
}
