package fr.soleil.passerelle.actor.tango.control;

import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListener;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.passerelle.actor.ActorV3;
import fr.soleil.passerelle.tango.util.WaitStateTask;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.tango.clientapi.factory.ProxyFactory;

@SuppressWarnings("serial")
public class Diffractometer extends ActorV3 {
    private static final String[] HKL_COORDINATE = { "HCoordinate", "KCoordinate", "LCoordinate" };
    public Parameter deviceNameParam;
    private String deviceName = "";

    public final Port hCoordinatePort;
    public final Port kCoordinatePort;
    public final Port lCoordinatePort;

    private PortHandler hCoordinateHandler = null;
    private PortHandler kCoordinateHandler = null;
    private PortHandler lCoordinateHandler = null;
    private boolean hCoordinateReceived = false;
    private boolean kCoordinateReceived = false;
    private boolean lCoordinateReceived = false;

    Double hCoordinateConst = null;
    Double kCoordinateConst = null;
    Double lCoordinateConst = null;

    boolean tokenIsNull = false;
    private final Port output;

    private DeviceProxy dev;
    private WaitStateTask waitTask;

    public Diffractometer(final CompositeEntity container, final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        deviceNameParam = new StringParameter(this, "Device Name");
        deviceNameParam.setExpression(deviceName);

        // Ports
        hCoordinatePort = PortFactory.getInstance().createInputPort(this, "HCoordinate", Double.class);
        hCoordinatePort.setMultiport(false);
        kCoordinatePort = PortFactory.getInstance().createInputPort(this, "KCoordinate", Double.class);
        kCoordinatePort.setMultiport(false);
        lCoordinatePort = PortFactory.getInstance().createInputPort(this, "LCoordinate", Double.class);
        lCoordinatePort.setMultiport(false);

        output = PortFactory.getInstance().createOutputPort(this, "output (Trigger)");
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == deviceNameParam) {
            deviceName = ((StringToken) deviceNameParam.getToken()).stringValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    protected void doInitialize() throws InitializationException {
        if (!isMockMode()) {
            try {
                dev = ProxyFactory.getInstance().createDeviceProxy(deviceName);
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            }
        }
        tokenIsNull = false;
        hCoordinateReceived = false;
        kCoordinateReceived = false;
        lCoordinateReceived = false;

        // If something connected to the set port, install a handler
        if (hCoordinatePort.getWidth() > 0) {
            // System.out.println("hCoordinatePort.getWidth() > 0");
            hCoordinateHandler = new PortHandler(hCoordinatePort, new PortListener() {
                public void tokenReceived() {
                    // System.out.println("hCoordinateHandler.tokenReceived() ");
                    final Token token = hCoordinateHandler.getToken();
                    if (token != null && token != Token.NIL) {
                        try {
                            final ManagedMessage message = MessageHelper.getMessageFromToken(token);
                            hCoordinateConst = (Double) message.getBodyContent();
                        } catch (final MessageException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (final PasserelleException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        hCoordinateReceived = true;
                        // System.out.println("hCoordinate received");

                    }
                    performNotify();

                }

                public void noMoreTokens() {
                    do {
                        performWait(1000);
                    } while (hCoordinateReceived);
                    // System.out.println("no more tokens hCoordinate");
                    hCoordinateReceived = true;
                    tokenIsNull = true;
                    performNotify();
                }
            });
            if (hCoordinateHandler != null) {
                hCoordinateHandler.start();
            }
        } // end "hCoordinatePort.getWidth() > 0"

        if (kCoordinatePort.getWidth() > 0) {
            // System.out.println("kCoordinatePort.getWidth() > 0");
            kCoordinateHandler = new PortHandler(kCoordinatePort, new PortListener() {
                public void tokenReceived() {
                    // System.out.println("kCoordinateHandler.tokenReceived() ");
                    final Token token = kCoordinateHandler.getToken();
                    if (token != null && token != Token.NIL) {
                        try {
                            final ManagedMessage message = MessageHelper.getMessageFromToken(token);
                            kCoordinateConst = (Double) message.getBodyContent();
                        } catch (final MessageException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (final PasserelleException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        kCoordinateReceived = true;
                        // System.out.println("kCoordinatePort received");

                    }
                    performNotify();

                }

                public void noMoreTokens() {
                    do {
                        performWait(1000);
                    } while (hCoordinateReceived);
                    // System.out.println("no more tokens kCoordinatePort");
                    kCoordinateReceived = true;
                    tokenIsNull = true;
                    performNotify();
                }
            });
            if (kCoordinateHandler != null) {
                kCoordinateHandler.start();
            }
        }

        if (lCoordinatePort.getWidth() > 0) {
            // System.out.println("lCoordinatePort.getWidth() > 0");
            lCoordinateHandler = new PortHandler(lCoordinatePort, new PortListener() {
                public void tokenReceived() {
                    // System.out.println("lCoordinateHandler.tokenReceived() ");
                    final Token token = lCoordinateHandler.getToken();
                    if (token != null && token != Token.NIL) {
                        try {
                            final ManagedMessage message = MessageHelper.getMessageFromToken(token);
                            lCoordinateConst = (Double) message.getBodyContent();
                        } catch (final MessageException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (final PasserelleException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        lCoordinateReceived = true;
                        // System.out.println("lCoordinatePort received");

                    }
                    performNotify();

                }

                public void noMoreTokens() {
                    do {
                        performWait(1000);
                    } while (hCoordinateReceived);
                    // System.out.println("no more tokens lCoordinatePort");
                    lCoordinateReceived = true;
                    tokenIsNull = true;
                    performNotify();
                }
            });
            if (lCoordinateHandler != null) {
                lCoordinateHandler.start();
            }
        }

    }

    private synchronized void performNotify() {
        notify();
    }

    private synchronized void performWait(final int time) {
        try {
            if (time == -1) {
                wait();
            } else {
                wait(time);
            }
        } catch (final InterruptedException e) {
        }

    }

    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        while (!hCoordinateReceived || !kCoordinateReceived || !lCoordinateReceived) {
            performWait(1000);
        }
        hCoordinateReceived = false;
        kCoordinateReceived = false;
        lCoordinateReceived = false;

        if (!tokenIsNull) {
            try {
                if (isMockMode()) {
                    ExecutionTracerService.trace(this, "MOCK - start moving H/K/L on " + deviceName);
                    ExecutionTracerService.trace(this, "MOCK - end of moving H/K/L on " + deviceName);
                } else {
                    DeviceAttribute[] da = new DeviceAttribute[3];
                    da = dev.read_attribute(HKL_COORDINATE);
                    da[0].insert(hCoordinateConst);
                    da[1].insert(kCoordinateConst);
                    da[2].insert(lCoordinateConst);
                    dev.write_attribute(da);
                    ExecutionTracerService.trace(this, "start moving H/K/L on " + deviceName);
                    waitTask = new WaitStateTask(deviceName, DevState.MOVING, 1000, false);
                    waitTask.run();
                    if (waitTask.hasFailed()) {
                        throw waitTask.getDevFailed();
                    }
                    ExecutionTracerService.trace(this, "end of moving H/K/L on " + deviceName);
                }
                sendOutputMsg(output, MessageFactory.getInstance().createTriggerMessage());
            } catch (final DevFailed e) {
                ExceptionUtil.throwProcessingException("Initialization problem", this);
            }
            hCoordinateConst = null;
            kCoordinateConst = null;
            lCoordinateConst = null;
        } else {
            requestFinish();
        }
    }

    @Override
    protected void doStop() {
        if (waitTask != null) {
            waitTask.cancel();
        }
        super.doStop();
    }

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}
