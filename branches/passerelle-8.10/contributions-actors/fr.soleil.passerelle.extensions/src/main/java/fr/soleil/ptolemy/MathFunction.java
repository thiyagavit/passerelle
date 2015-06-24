/*
 * Created on 10 juin 2005
 * 
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package fr.soleil.ptolemy;

import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageHelper;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.tango.util.TangoToPasserelleUtil;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * @author root TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 *         Style - Code Templates
 */
@SuppressWarnings("serial")
public class MathFunction extends Actor {
    /**
     * The function to compute. This is a string-valued attribute that defaults to "exp".
     */
    public StringParameter function;

    /**
     * The port for the first operand. The port has type BaseType.DOUBLE
     */
    public Port firstOperand = null;

    /**
     * The port for the second operand, if it is needed. The port has type BaseType.DOUBLE
     */
    public Port secondOperand = null;

    /**
     * Output port The port has type BaseType.DOUBLE
     */
    public Port output = null;

    private PortHandler firstOperandHandler;

    private PortHandler secondOperandHandler;

    // Constants used for more efficient execution.
    private static final int _EXP = 0;

    private static final int _LOG = 1;

    private static final int _MODULO = 2;

    private static final int _SIGN = 3;

    private static final int _SQUARE = 4;

    private static final int _SQRT = 5;

    // An indicator for the function to compute.
    private int _function;

    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.NameDuplicationException
     * @throws ptolemy.kernel.util.IllegalActionException
     */
    public MathFunction(final CompositeEntity container, final String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        // Parameters
        function = new StringParameter(this, "function");
        function.setExpression("exp");
        function.addChoice("exp");
        function.addChoice("log");
        function.addChoice("modulo");
        function.addChoice("sign");
        function.addChoice("square");
        function.addChoice("sqrt");
        _function = _EXP;

        // Ports
        // secondOperand port is not allocated in the constructor
        // instead it will allocated dynamically during run-time

        firstOperand = PortFactory.getInstance().createInputPort(this, "input", Double.class);
        output = PortFactory.getInstance().createOutputPort(this, "output");

        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-30\" y=\"-15\" " + "width=\"60\" height=\"30\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        try {
            if (attribute == function) {
                final String functionName = function.stringValue();
                if (functionName.equals("exp")) {
                    _function = _EXP;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else if (functionName.equals("log")) {
                    _function = _LOG;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else if (functionName.equals("modulo")) {
                    _function = _MODULO;
                    _createSecondPort();
                } else if (functionName.equals("sign")) {
                    _function = _SIGN;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else if (functionName.equals("square")) {
                    _function = _SQUARE;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else if (functionName.equals("sqrt")) {
                    _function = _SQRT;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else {
                    throw new IllegalActionException(this, "Unrecognized math function: " + functionName);
                }
            } else {
                super.attributeChanged(attribute);
            }
        } catch (final NameDuplicationException nameDuplication) {
            throw new InternalErrorException(this, nameDuplication, "Unexpected name duplication");
        }
    }

    /*
     * (non-Javadoc)
     * @see com.isencia.passerelle.actor.Actor#doInitialize()
     */
    @Override
    protected void doInitialize() throws InitializationException {
        // TODO Auto-generated method stub
        super.doInitialize();
        firstOperandHandler = new PortHandler(firstOperand);
        if (firstOperand.getWidth() > 0) {
            firstOperandHandler.start();
        }
        if (secondOperand != null) {
            secondOperandHandler = new PortHandler(secondOperand);
            if (secondOperand.getWidth() > 0) {
                secondOperandHandler.start();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.isencia.passerelle.actor.Transformer#doFire(com.isencia.passerelle. message.ManagedMessage)
     */
    @Override
    protected void doFire() throws ProcessingException {

        double input1 = 0;
        final Token token = firstOperandHandler.getToken();
        if (token != null && token != Token.NIL) {
            input1 = getDoubleFromMessage(token);
            double input2 = 1.0;
            if (_function == _MODULO) {
                if (secondOperand != null) {
                    final Token tokenSec = secondOperandHandler.getToken();
                    if (tokenSec != null && token != Token.NIL) {
                        input2 = getDoubleFromMessage(tokenSec);
                    }
                }
            }
            final ManagedMessage resultMsg = createMessage();
            try {
                resultMsg.setBodyContent(new Double(_doFunction(input1, input2)), ManagedMessage.objectContentType);
                output.broadcast(new ObjectToken(resultMsg));
            } catch (final MessageException e) {
                ExceptionUtil.throwProcessingException("Cannot send result to output", output, e);
            } catch (final IllegalActionException e) {
                ExceptionUtil.throwProcessingException("Cannot send result to output", output, e);
            }
        } else {
            requestFinish();
        }

    }

    /**
     * @param token
     * @param cd
     * @return @throws MessageException
     * @throws NumberFormatException
     * @throws DevFailed
     * @throws ProcessingException
     * @throws
     */
    private double getDoubleFromMessage(final Token token) throws ProcessingException {
        double result = 0;
        ManagedMessage message;
        Object input = null;
        try {
            message = MessageHelper.getMessageFromToken(token);
            input = message.getBodyContent();
            if (input instanceof Number) {
                result = ((Number) input).doubleValue();
            } else if (input instanceof TangoAttribute) {
                result = Double.parseDouble(((TangoAttribute) input).readAsString("", ""));
            } else {
                result = Double.parseDouble(input.toString());
            }
        } catch (final NumberFormatException e) {
            ExceptionUtil.throwProcessingException("Input type not supported", input, e);
        } catch (final PasserelleException e) {
            ExceptionUtil.throwProcessingException("Error reading msg content", input, e);
        } catch (final DevFailed e) {
            // e.printStackTrace();
            ExceptionUtil.throwProcessingException(TangoToPasserelleUtil.getDevFailedString(e, this), input, e);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see com.isencia.passerelle.actor.Actor#getExtendedInfo()
     */
    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Create the second port needed by modulo function
     */
    private void _createSecondPort() throws NameDuplicationException, IllegalActionException {
        // Go looking for the port in case somebody else created the port
        // already. For example, this might
        // happen in shallow code generation.
        secondOperand = (Port) getPort("secondOperand");
        if (secondOperand == null) {
            secondOperand = PortFactory.getInstance().createInputPort(this, "secondOperand", Double.class);

        } else if (secondOperand.getContainer() == null) {
            secondOperand.setContainer(this);
        }
    }

    /**
     * Calculate the function on the given argument.
     * 
     * @param input1 The first input value.
     * @param input2 The second input value.
     * @return The result of applying the function.
     */
    private double _doFunction(final double input1, final double input2) {
        double result;
        switch (_function) {
            case _EXP:
                result = Math.exp(input1);
                break;
            case _LOG:
                result = Math.log(input1);
                break;
            case _MODULO:
                result = input1 % input2;
                break;
            case _SIGN:
                if (input1 > 0) {
                    result = 1.0;
                } else if (input1 < 0) {
                    result = -1.0;
                } else {
                    result = 0.0;
                }
                break;
            case _SQUARE:
                result = input1 * input1;
                break;
            case _SQRT:
                result = Math.sqrt(input1);
                break;
            default:
                throw new InternalErrorException("Invalid value for _function private variable. "
                        + "MathFunction actor (" + getFullName() + ")" + " on function type " + _function);
        }
        return result;
    }

}