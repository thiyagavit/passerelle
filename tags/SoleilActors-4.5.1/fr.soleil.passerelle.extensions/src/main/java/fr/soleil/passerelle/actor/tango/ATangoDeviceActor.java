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
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.core.PasserelleException;

import com.isencia.passerelle.doc.generator.ParameterName;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.util.DevFailedInitializationException;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

/**
 *
 * @author ABEILLE
 *
 */
@SuppressWarnings("serial")
public abstract class ATangoDeviceActor extends ATangoActor {

    private static final String DEVICE_NAME = "Device Name";

    private static final Logger logger = LoggerFactory.getLogger(ATangoDeviceActor.class);

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
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public ATangoDeviceActor(final CompositeEntity container, final String name)
	    throws NameDuplicationException, IllegalActionException {
	super(container, name);
	deviceNameParam = new StringParameter(this, DEVICE_NAME);
	deviceNameParam.setExpression(deviceName);
    }

    @Override
    /*
     * @throws IllegalActionException
     */
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
	if (arg0 == deviceNameParam) {
	    deviceName = PasserelleUtil.getParameterValue(deviceNameParam);
	} else {
	    super.attributeChanged(arg0);
	}
    }

    @Override
    /*
     * When not in mock mode, create the device proxy with the device name
     *
     * @throws InitializationException
     */
    protected void doInitialize() throws InitializationException {

	if (logger.isTraceEnabled()) {
	    logger.trace(getInfo() + " doInitialize() - entry");
	}

	if (!isMockMode() && createDeviceProxy) {
	    try {
		deviceProxy = ProxyFactory.getInstance().createDeviceProxy(deviceName);
		deviceProxy.ping();
	    } catch (final DevFailed e) {
		throw new DevFailedInitializationException(e, this);
	    }catch(final Exception e){
	    	throw new InitializationException(PasserelleException.Severity.FATAL, "Exception during doInit " + e.getMessage(), this, null);
	    }
	}
	super.doInitialize();

	if (logger.isTraceEnabled()) {
	    logger.trace(getInfo() + " doInitialize() - exit");
	}
    }

    /**
     *
     * @return The parameter for device name
     */
    protected final Parameter getDeviceNameParam() {
	return deviceNameParam;
    }

    /**
     *
     * @return The device name
     */
    protected final String getDeviceName() {
	return deviceName;
    }

    /**
     *
     * @return The device proxy (initialized in
     *         {@link ATangoDeviceActor#doInitialize()}
     * @throws PasserelleException
     */
    protected final DeviceProxy getDeviceProxy() throws PasserelleException {
	if (deviceProxy == null) {
	    throw new InitializationException("field not initialized", deviceName, null);
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
	final ATangoDeviceActor copy = (ATangoDeviceActor) super.clone(workspace);
	copy.deviceProxy = null;
	return copy;
    }
}
