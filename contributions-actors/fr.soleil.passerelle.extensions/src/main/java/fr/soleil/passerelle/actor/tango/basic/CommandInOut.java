/*
 * Synchrotron Soleil
 * 
 * File : CommandInOut.java
 * 
 * Project : passerelle-soleil
 * 
 * Description :
 * 
 * Author : ABEILLE
 * 
 * Original : 2 juin 2005
 * 
 * Revision: Author:
 * Date: State:
 * 
 * Log: CommandInOut.java,v
 */
/*
 * Created on 2 juin 2005
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.tango.basic;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.NoRoomException;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.doc.generator.ParameterName;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoCommand;

/**
 * Execute a command on a Tango device.
 * 
 * @author ABEILLE
 */
@SuppressWarnings("serial")
public class CommandInOut extends Actor {

    private static final String ARGOUT_IS_A_TANGO_DEVVAR__X_STRINGARRAY = "Argout is a Tango_DEVVAR_<X>STRINGARRAY";
    private static final String ARGIN_IS_A_TANGO_DEVVAR__X_STRINGARRAY = "Argin is a Tango_DEVVAR_<X>STRINGARRAY";
    private static final String COMMAND_NAME = "Command Name";
    private static final String DEVICE_NAME = "Device Name";
    private final static Logger logger = LoggerFactory.getLogger(CommandInOut.class);
    /**
     * If there is, the input argument of the command.
     */
    public Port argin1;// = null;
    /**
     * Used only for complex input argurments like Tango_DEVVAR_LONGSTRINGARRAY or Tango_DEVVAR_DOUBLESTRINGARRAY ( a
     * mix
     * of a table of string and a table of double or long) Will be visible only is parameter
     * "Argin is a Tango_DEVVAR_<X>STRINGARRAY" is visible
     */
    public Port argin2;// = null;
    // private boolean argin1Received = false;
    private PortHandler argin1Handler = null;
    private PortHandler argin2Handler = null;

    /**
     * If there is, the output argument of the command.
     */
    public Port argout1;// = null;
    /**
     * Used only for complex output argurments like Tango_DEVVAR_LONGSTRINGARRAY or Tango_DEVVAR_DOUBLESTRINGARRAY ( a
     * mix
     * of a table of string and a table of double or long) Will be visible only is parameter
     * "Argout is a Tango_DEVVAR_<X>STRINGARRAY" is visible
     */
    public Port argout2;// = null;

    /**
     * The device on which to execute the commmand
     */
    @ParameterName(name = DEVICE_NAME)
    public Parameter deviceNameParam;
    private String deviceName;

    /**
     * The command name
     */
    @ParameterName(name = COMMAND_NAME)
    public Parameter commandNameParam;
    private String commandName;

    /**
     * to usecomplex input argurments like Tango_DEVVAR_LONGSTRINGARRAY or Tango_DEVVAR_DOUBLESTRINGARRAY ( a mix of a
     * table of string and a table of double or long)
     */
    @ParameterName(name = ARGIN_IS_A_TANGO_DEVVAR__X_STRINGARRAY)
    public Parameter useXStringArrayInParam;
    private boolean useXStringArrayIn;

    /**
     * to usecomplex output argurments like Tango_DEVVAR_LONGSTRINGARRAY or Tango_DEVVAR_DOUBLESTRINGARRAY ( a mix of a
     * table of string and a table of double or long)
     */
    @ParameterName(name = ARGOUT_IS_A_TANGO_DEVVAR__X_STRINGARRAY)
    public Parameter useXStringArrayOutParam;
    private boolean useXStringArrayOut;

    private TangoCommand comHelp;
    private boolean tokenIsNull1 = false;
    private boolean tokenIsNull2 = false;

    private ManagedMessage messageArgin1;
    private ManagedMessage messageArgin2;

    /**
     * @param arg0
     * @param arg1
     * @throws ptolemy.kernel.util.NameDuplicationException
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    public CommandInOut(final CompositeEntity arg0, final String arg1) throws NameDuplicationException,
            IllegalActionException {

        super(arg0, arg1);

        tokenIsNull1 = false;
        tokenIsNull2 = false;

        argin1 = PortFactory.getInstance().createInputPort(this, "argin1", String.class);
        argout1 = PortFactory.getInstance().createOutputPort(this, "argout1");

        deviceNameParam = new StringParameter(this, DEVICE_NAME);
        deviceNameParam.setExpression(deviceName);
        registerConfigurableParameter(deviceNameParam);

        commandNameParam = new StringParameter(this, COMMAND_NAME);
        commandNameParam.setExpression(commandName);
        registerConfigurableParameter(commandNameParam);

        useXStringArrayInParam = new Parameter(this, ARGIN_IS_A_TANGO_DEVVAR__X_STRINGARRAY, new BooleanToken(false));
        useXStringArrayInParam.setTypeEquals(BaseType.BOOLEAN);
        useXStringArrayInParam.setVisibility(Settable.EXPERT);

        useXStringArrayOutParam = new Parameter(this, ARGOUT_IS_A_TANGO_DEVVAR__X_STRINGARRAY, new BooleanToken(false));
        useXStringArrayOutParam.setTypeEquals(BaseType.BOOLEAN);
        useXStringArrayOutParam.setVisibility(Settable.EXPERT);

        final URL url = this.getClass().getResource("/fr/soleil/tango/tango.jpg");
        _attachText("_iconDescription", "<svg>\n"
                + " <image x=\"-20\" y=\"-20\" width =\"40\" height=\"40\" xlink:href=\"" + url + "\"/>\n"
                + "<line x1=\"-20\" y1=\"20\" x2=\"-20\" y2=\"-20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-20\" y1=\"-20\" x2=\"20\" y2=\"-20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-19\" y1=\"19\" x2=\"-19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-19\" y1=\"-19\" x2=\"18\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-20\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"19\" y1=\"-19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-19\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "</svg>\n");

    }

    @Override
    public void attributeChanged(final Attribute arg0) throws IllegalActionException {
        if (arg0 == deviceNameParam) {
            deviceName = ((StringToken) deviceNameParam.getToken()).stringValue();
        } else if (arg0 == commandNameParam) {
            commandName = ((StringToken) commandNameParam.getToken()).stringValue();
        } else if (arg0 == useXStringArrayInParam) {
            useXStringArrayIn = new Boolean(useXStringArrayInParam.getExpression().trim());

            if (useXStringArrayIn) {
                if (argin2 == null) {
                    // System.out.println("input ports empty");
                    try {
                        final Port extraInputPort = (Port) getPort("argin2 (String)");
                        if (extraInputPort == null) {
                            argin2 = PortFactory.getInstance().createInputPort(this, "argin2 (String)", String.class);
                        } else {
                            argin2 = extraInputPort;
                        }
                    } catch (final NameDuplicationException e) {
                        e.printStackTrace();
                        throw new IllegalActionException(this, e, "Error adding argin2");
                    }
                } else {
                    // System.out.println("argin2 already added");
                }
            } else {
                if (argin2 != null) {
                    // System.out.println("argin2 removed");
                    try {
                        argin2.setContainer(null);
                        argin2 = null;
                    } catch (final NameDuplicationException e) {
                        throw new IllegalActionException(this, e, "Error removing argin2");
                    }
                }
            }
        } else if (arg0 == useXStringArrayOutParam) {
            useXStringArrayOut = new Boolean(useXStringArrayOutParam.getExpression().trim());
            if (useXStringArrayOut) {
                if (argout2 == null) {
                    try {
                        final Port extraOutputPort = (Port) getPort("argout2 (String)");
                        if (extraOutputPort == null) {
                            argout2 = PortFactory.getInstance().createOutputPort(this, "argout2 (String)");
                        } else {
                            argout2 = extraOutputPort;
                        }
                    } catch (final NameDuplicationException e) {
                        throw new IllegalActionException(this, e, "Error adding argout2");
                    }
                }
            } else {
                if (argout2 != null) {
                    try {
                        argout2.setContainer(null);
                        argout2 = null;
                    } catch (final NameDuplicationException e) {
                        throw new IllegalActionException(this, e, "Error removing argout2");
                    }
                }
            }
        } else {
            super.attributeChanged(arg0);
        }
    }

    @Override
    protected void doInitialize() throws InitializationException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doInitialize() - entry");
        }

        tokenIsNull1 = false;
        tokenIsNull2 = false;
        if (argin1.getWidth() <= 0) {
            ExceptionUtil.throwInitializationException(ErrorCode.FATAL, "Input port " + argin1.getName()
                    + " has no data.", this);
        }

        if (useXStringArrayIn && argin2.getWidth() <= 0) {
            ExceptionUtil.throwInitializationException(ErrorCode.FATAL, "Input port " + argin2.getName()
                    + " has no data.", this);
        }

        if (!isMockMode()) {
            try {
                comHelp = new TangoCommand(deviceName, commandName);
            } catch (final DevFailed e) {
                ExceptionUtil.throwInitializationException(this, e);
            } catch (final Exception e) {
                ExceptionUtil.throwInitializationException(ErrorCode.FATAL,
                        "Exception during TangoCommand " + e.getMessage(), this);
            }

        }
        // If something connected to the argin1 port, install a handler
        if (argin1.getWidth() > 0) {
            argin1Handler = new PortHandler(argin1);
            if (argin1Handler != null) {
                argin1Handler.start();
            }
        }

        // use a devXString array so wait for data on argin2
        if (argin2 != null) {
            if (argin2.getWidth() > 0) {
                if (logger.isTraceEnabled()) {
                    logger.trace(getName() + " doInitialize() -  new PortHandler(argin2)");
                }
                argin2Handler = new PortHandler(argin2);
                if (argin2Handler != null) {
                    argin2Handler.start();
                }
            }
        }
        super.doInitialize();
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doInitialize() - exit");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.isencia.passerelle.actor.Actor#doFire()
     */
    @Override
    protected void doFire() throws ProcessingException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doFire() - entry");
        }
        try {
            if (!tokenIsNull1 && !tokenIsNull2) {
                // System.out.println("in !tokenIsNull1 && !tokenIsNull2");
                if (isMockMode()) {
                    ExecutionTracerService.trace(this, "MOCK - executing " + commandName + " on device: " + deviceName);
                    sendOutputMsg(argout1, PasserelleUtil.createTriggerMessage());
                    if (argout2 != null) {
                        sendOutputMsg(argout2, PasserelleUtil.createTriggerMessage());
                    }
                } else {
                    ExecutionTracerService.trace(this, "executing " + commandName + " on device: " + deviceName);
                    if (comHelp.isArginScalar()) {
                        // scalar
                        logger.debug("scalar");
                        comHelp.execute((String) PasserelleUtil.getInputValue(messageArgin1));
                    } else if (comHelp.isArginSpectrum()) {
                        // simple spectrum
                        Object[] argin = ((String) PasserelleUtil.getInputValue(messageArgin1)).split(",");
                        comHelp.execute(argin);
                    } else if (comHelp.isArginMixFormat()) {
                        // mix spectrum
                        comHelp.insertMixArgin(((String) PasserelleUtil.getInputValue(messageArgin1)).split(","),
                                ((String) PasserelleUtil.getInputValue(messageArgin2)).split(","));
                        comHelp.execute();
                    } else {// else void -> do not insert
                        comHelp.execute();
                    }
                    if (!isFinishRequested()) {
                        // -----------------extract
                        // argout---------------------------
                        if (comHelp.isArgoutScalar() || comHelp.isArgoutSpectrum()) {
                            // scalar
                            final String val = comHelp.extractToString(",");
                            sendOutputMsg(argout1, PasserelleUtil.createContentMessage(this, val));
                        } else if (comHelp.isArgoutMixFormat()) {
                            // mix spectrum
                            final String[] numVal = comHelp.getNumMixArrayArgout();
                            final String[] stringVal = comHelp.getStringMixArrayArgout();
                            sendOutputMsg(argout1, PasserelleUtil.createContentMessage(this, numVal));
                            sendOutputMsg(argout2, PasserelleUtil.createContentMessage(this, stringVal));
                        } else {
                            // void argout, output trigger
                            sendOutputMsg(argout1, PasserelleUtil.createTriggerMessage());
                        }
                    }
                }
                tokenIsNull1 = false;
                tokenIsNull2 = false;
                // System.out.println("out !tokenIsNull1 && !tokenIsNull2");
            } else {
                // System.out.println("request finished");
                if (logger.isTraceEnabled()) {
                    logger.trace(getName() + " doFire() - request finish");
                }
                requestFinish();
            }
        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        } catch (final NumberFormatException e) {
            ExceptionUtil.throwProcessingException("NumberFormatException", deviceName, e);
        } catch (final NoRoomException e) {
            ExceptionUtil.throwProcessingException("NoRoomException", deviceName, e);
        } catch (final ProcessingException e) {
            throw e;
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doFire() - exit");
        }
    }

    @Override
    protected String getExtendedInfo() {
        return this.getName();
    }

    @Override
    protected boolean doPreFire() throws ProcessingException {
        // -----------------get data from input ports----------------------
        final Token token = argin1Handler.getToken();
        try {
            if (token != null && token != Token.NIL) {
                if (!isMockMode()) {
                    if (!comHelp.isArginVoid()) {
                        messageArgin1 = MessageHelper.getMessageFromToken(token);
                    }
                }
            } else {
                tokenIsNull1 = true;
                // System.out.println("tokenIsNull1");
            }
            if (useXStringArrayIn) {
                if (!isMockMode()) {
                    // the argin is not an devvarXstring
                    if (!comHelp.isArginMixFormat()) {
                        ExceptionUtil.throwProcessingException(
                                "The parameter argin Tango_DEVVAR_<X>STRINGARRAY must be enable", deviceName);
                    }
                }
                final Token token2 = argin2Handler.getToken();
                if (token2 != null && token != Token.NIL) {
                    messageArgin2 = MessageHelper.getMessageFromToken(token2);
                } else {
                    tokenIsNull2 = true;
                    // System.out.println("tokenIsNull2");
                }
            } else {
                if (!isMockMode()) {
                    // the argin is a devvarXstring array
                    if (comHelp.isArginMixFormat()) {
                        ExceptionUtil.throwProcessingException(
                                "The parameter argin Tango_DEVVAR_<X>STRINGARRAY must be disable", deviceName);
                    }
                }
            }
        } catch (final PasserelleException e) {
            ExceptionUtil.throwProcessingException("cannot get input", deviceName, e);
        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        }
        return super.doPreFire();
    }
}
