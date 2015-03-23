package fr.soleil.passerelle.actor.ptolemy;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtAssignmentNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeConstant;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.esrf.Tango.DevFailed;
import fr.soleil.passerelle.actor.DynamicPortsActor;
import fr.soleil.passerelle.ptolemy.PtolemyType;
import fr.soleil.passerelle.ptolemy.data.exp.SoleilFunctions;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * @author ABEILLE
 * 
 *         TODO Use automatic conversion
 */
@SuppressWarnings("serial")
public class Expression extends DynamicPortsActor {

    private final static Logger logger = LoggerFactory.getLogger(Expression.class);

    /** The output port. */
    public Port output;

    public Parameter expression;
    // public Parameter resultNameParam;
    // private int inputNumber = 1;
    public Parameter inputTypesParam;
    private PtolemyType[] inputTypes;
    // private int _iterationCount = 1;
    private ASTPtRootNode _parseTree = null;
    private ParseTreeEvaluator _parseTreeEvaluator = null;
    private VariableScope _scope = null;
    private Map<String, Token> _tokenMap;
    private static Map<String, Token> memorizedResults = Collections.synchronizedMap(new HashMap<String, Token>());

    private boolean assignement;

    private String targetName;

    private static final String portName = "x";

    /**
     * @param container
     * @param name
     * @throws ptolemy.kernel.util.IllegalActionException
     * @throws ptolemy.kernel.util.NameDuplicationException
     */
    public Expression(final CompositeEntity container, final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        super.setIdxOffsetPort(1);
        super.setInputPortPrefix(portName);

        output = PortFactory.getInstance().createOutputPort(this, "result");
        output.setMultiport(false);
        expression = new StringParameter(this, "expression");
        expression.setExpression("");

        inputTypesParam = new StringParameter(this, "input types");
        final String defaultV = PtolemyType.DOUBLE.toString();
        inputTypesParam.setExpression(defaultV);

        // resultNameParam = new StringParameter(this, "result name");
        // resultNameParam.setExpression("");
        SoleilFunctions.init();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.isencia.passerelle.actor.Actor#doInitialize()
     */
    @Override
    protected void doInitialize() throws InitializationException {

        super.doInitialize();
        Locale.setDefault(Locale.US);

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doInitialize() - entry");
        }

        assignement = false;

        // _iterationCount = 1;
        _tokenMap = new HashMap<String, Token>();

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doInitialize() - exit");
        }
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == expression) {
            _parseTree = null;

            if (nrInputPorts > 0) {
                // +==============================================================
                // check if port names are written in lower case. If we throw an
                // exception.Port name is xi (i as integer > 1).
                //
                // We have to differentiate several cases:
                // case1: Port Name is in the "middle" of expression. ie
                // expression
                // = 2 *x1 *3
                //
                // case2: Port Name is the first or the last statement in
                // expression. ie expression = X1 *3 or expression = 2*8 *X1
                //
                // case3: Port Name is contains in another name of function or
                // constant... ie: expression = 2* X1 + blablaX1() *3. "X1" of
                // "blablaX1" musn't set to lower case !
                //
                // We proceed in 2 steps:
                // step one: we add "\n" at the beginning and end of expression
                // to prevent case 3
                //
                // step two we detect "X" thanks to regex and throws exception.

                final String exp = "\n" + expression.getExpression() + "\n";
                final Pattern p = Pattern.compile("[\\+*\\-/ (),\\n]" + portName.toUpperCase() + "[1-" + nrInputPorts
                        + "]+[\\+*\\-/ (),\\n]");
                final Matcher m = p.matcher(exp);

                if (m.find()) {
                    throw new IllegalActionException("Port Name with capital detect near column " + (m.start() + 1));
                }

            }
        } else if (attribute == inputTypesParam) {
            final String[] types = ((StringToken) inputTypesParam.getToken()).stringValue().split(",");
            inputTypes = new PtolemyType[types.length];
            for (int i = 0; i < types.length; i++) {
                inputTypes[i] = PtolemyType.valueOf(types[i]);
            }
            // } else if (attribute == resultNameParam) {
            // resultName = PasserelleUtil.getParameterValue(resultNameParam);
        } else {

            super.attributeChanged(attribute);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.isencia.passerelle.actor.Actor#getExtendedInfo()
     */

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return expression.getExpression();
    }

    private Token extractPtolemyToken(final ManagedMessage msg, final int portNr) throws ProcessingException {

        Token t = null;
        String input = null;

        try {
            // put input in ptolemy type
            final Object inputValue = msg.getBodyContent();
            if (inputValue instanceof TangoAttribute) {
                // input is a Tango Attribute
                // System.err.println(this.getName() + " Tango Attribute");
                final TangoAttribute attr = (TangoAttribute) inputValue;
                if (attr.isScalar()) {
                    input = attr.extract(String.class);
                } else if (attr.isSpectrum()) {
                    input = attr.extractToString(",", "");
                } else {// image
                    input = attr.extractToString(",", ";");
                }
            } else if (inputValue.getClass().isArray()) {
                // try to convert array to a string
                final String[] values = new String[Array.getLength(inputValue)];
                for (int i = 0; i < values.length; i++) {
                    values[i] = Array.get(inputValue, i).toString();
                }
                input = Arrays.toString(values);
            } else {
                input = inputValue.toString();
            }

            final int typeIndex = portNr - 1;
            if (inputTypes.length <= typeIndex) {
                // Take the fisrt by default ! Good practice ?
                t = inputTypes[0].getTokenForString(input);
            } else {
                // TAke care : port number start to 1 and inputTypes array start to 0
                t = inputTypes[typeIndex].getTokenForString(input);
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            ExceptionUtil.throwProcessingExceptionWithLog(this, "Invalid configuration (see the input types number)",
                    input, e);
        } catch (final IllegalActionException e) {
            ExceptionUtil.throwProcessingExceptionWithLog(this, "input message is not correct", input, e);
        } catch (final MessageException e) {
            ExceptionUtil.throwProcessingExceptionWithLog(this, "cannot get input value", input, e);
        } catch (final DevFailed e) {
            ExceptionUtil.throwProcessingException(this, e);
        }

        return t;
    }

    private class VariableScope extends ModelScope {

        /**
         * Look up and return the attribute with the specified name in the scope. Return null if
         * such an attribute does not exist.
         * 
         * @return The attribute with the specified name in the scope.
         */
        @Override
        public Token get(final String name) throws IllegalActionException {
            if (name.equals("time")) {
                return new DoubleToken(getDirector().getModelTime().getDoubleValue());
            } /*
              * else if (name.equals("iteration")) { return new
              * IntToken(_iterationCount); }
              */

            if (memorizedResults.containsKey(name)) {
                // System.out.println("get variable " + name + " "
                // + memorizedResults.get(name));
                return memorizedResults.get(name);
            }

            final Token token = _tokenMap.get(name);
            if (token != null) {
                return token;
            }

            final Variable result = getScopedVariable(null, Expression.this, name);
            if (result != null) {
                return result.getToken();
            }

            return null;
        }

        /**
         * Look up and return the type of the attribute with the specified name in the scope. Return
         * null if such an attribute does not exist.
         * 
         * @return The attribute with the specified name in the scope.
         */
        @Override
        public Type getType(final String name) throws IllegalActionException {
            if (name.equals("time")) {
                return BaseType.DOUBLE;
            } else if (name.equals("iteration")) {
                return BaseType.INT;
            }

            // Check the port names.
            final TypedIOPort port = (TypedIOPort) getPort(name);
            if (port != null) {
                return port.getType();
            }

            final Variable result = getScopedVariable(null, Expression.this, name);
            if (result != null) {
                return (Type) result.getTypeTerm().getValue();
            }
            return null;
        }

        /**
         * Look up and return the type term for the specified name in the scope. Return null if the
         * name is not defined in this scope, or is a constant type.
         * 
         * @return The InequalityTerm associated with the given name in the scope.
         * @exception IllegalActionException If a value in the scope exists with the given name, but
         *                cannot be evaluated.
         */
        @Override
        public ptolemy.graph.InequalityTerm getTypeTerm(final String name) throws IllegalActionException {
            if (name.equals("time")) {
                return new TypeConstant(BaseType.DOUBLE);
            } else if (name.equals("iteration")) {
                return new TypeConstant(BaseType.INT);
            }

            // Check the port names.
            final TypedIOPort port = (TypedIOPort) getPort(name);
            if (port != null) {
                return port.getTypeTerm();
            }

            final Variable result = getScopedVariable(null, Expression.this, name);
            if (result != null) {
                return result.getTypeTerm();
            }
            return null;
        }

        /**
         * Return the list of identifiers within the scope.
         * 
         * @return The list of identifiers within the scope.
         */
        @Override
        @SuppressWarnings("unchecked")
        public Set identifierSet() {
            return getAllScopedVariableNames(null, Expression.this);
        }
    }

    @Override
    protected void process(final ActorContext arg0, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doFire() - entry");
        }
        String log = " where ";
        // get all inputs' messages
        final Iterator<MessageInputContext> allInputContexts = request.getAllInputContexts();
        while (allInputContexts.hasNext()) {
            final MessageInputContext messageInputContext = allInputContexts.next();
            if (!messageInputContext.isProcessed()) {
                final ManagedMessage msg = messageInputContext.getMsg();
                final int portIndex = getPortIndex(messageInputContext);
                final Token expressionToken = extractPtolemyToken(msg, portIndex);

                if (expressionToken != null) {
                    if (!(expressionToken instanceof MatrixToken)) {
                        log += messageInputContext.getPortName() + "=" + expressionToken.toString() + " ";
                    } else {
                        log += messageInputContext.getPortName() + "= matrix ";
                    }
                    // System.out.println(getFullName() + " " +
                    // messageInputContext.getPortName()
                    // + " = " + expressionToken.toString() + " ");
                    _tokenMap.put(messageInputContext.getPortName(), expressionToken);
                }
            } else {
                System.err.println(getFullName() + " " + messageInputContext.getPortName() + " not received ");
                // return;
                // we can be here when stop is called and not all inputs has be
                // received
                if (isFinishRequested()) {
                    System.err.println(getFullName() + " " + messageInputContext.getPortName() + " finished ");
                    return;
                } else {
                    System.err.println(getFullName() + " " + messageInputContext.getPortName() + " has no data ");
                    // throw new ProcessingException("Input port " +
                    // messageInputContext.getPortName()
                    // + " has no data.", this, null);
                }
            }
        }

        Token result = null;
        try {
            // Note: this code parallels code in the OutputTypeFunction
            // class
            // below.
            // if (_parseTree == null) {
            // Note that the parser is NOT retained, since in most
            // cases the expression doesn't change, and the parser
            // requires a large amount of memory.
            final PtParser parser = new PtParser();
            // _parseTree = parser.generateParseTree(expression
            // .getExpression());

            _parseTree = parser.generateSimpleAssignmentParseTree(expression.getExpression());
            // Figure out if we got an assignment... if so, then get the
            // identifier name and only evaluated the expression part.
            if (_parseTree instanceof ASTPtAssignmentNode) {
                final ASTPtAssignmentNode assignmentNode = (ASTPtAssignmentNode) _parseTree;
                targetName = assignmentNode.getIdentifier();
                assignement = true;
                _parseTree = assignmentNode.getExpressionTree();
            }
            // }

            if (_parseTreeEvaluator == null) {
                _parseTreeEvaluator = new ParseTreeEvaluator();
            }
            if (_scope == null) {
                _scope = new VariableScope();
            }
            // System.out.println(_parseTreeEvaluator.traceParseTreeEvaluation(
            // _parseTree, _scope));
            result = _parseTreeEvaluator.evaluateParseTree(_parseTree, _scope);

            synchronized (memorizedResults) {
                // synchronized as said in javadoc of
                // Collections.synchronizedMap
                for (final Map.Entry<String, Token> set : memorizedResults.entrySet()) {
                    if (expression.getExpression().contains(set.getKey())) {
                        log += set.getKey() + "=" + set.getValue().toString() + " ";
                    }
                }
            }
            ExecutionTracerService.trace(this, "calculating " + expression.getExpression() + log);

        } catch (final IllegalActionException ex) {
            // ex.printStackTrace();
            // Chain exceptions to get the actor that threw the exception.
            ExceptionUtil.throwProcessingExceptionWithLog(this, "Expression invalid : " + ex.getMessage(), this, ex);
        }
        if (result == null) {
            ExceptionUtil.throwProcessingExceptionWithLog(this,
                    "Expression yields a null result: " + expression.getExpression(), this);
        }
        if (assignement) {
            ExecutionTracerService.trace(this, "result memorized in " + targetName);
            memorizedResults.put(targetName, result);
        }

        final String resultAsString;
        if (result instanceof StringToken) {
            resultAsString = ((StringToken) result).stringValue();
            response.addOutputMessage(output, PasserelleUtil.createContentMessage(this, resultAsString));
        } else {// Token can be an instance of DoubleToken or what ever... (depends of input type)
            resultAsString = result.toString();
            response.addOutputMessage(output, PasserelleUtil.createContentMessage(this, result));
        }

        ExecutionTracerService.trace(this, "result is " + resultAsString);

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doFire() - exit");
        }

    }

    @Override
    protected DynamicPortType getPortConfiguration() {
        return DynamicPortType.ONLY_INPUTS;
    }

    /*
     * * Return PULL mode for all input ports, so the fire loop blocks till all
     * ports have received an input msg.
     */
    @Override
    protected PortMode getPortModeForNewInputPort(final String portName) {
        return PortMode.PULL;
    }
}