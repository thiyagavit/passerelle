/**
 * Created on 12 juin 2005
 * 
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package fr.soleil.ptolemy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
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
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.actor.DynamicPortsActor;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * @author root
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class Expression extends DynamicPortsActor {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(Expression.class);

    /** The output port. */
    public Port output;

    /**
     * The expression that is evaluated to produce the output.
     */
    public Parameter expression;

    private int _iterationCount = 1;

    private ASTPtRootNode _parseTree = null;

    private ParseTreeEvaluator _parseTreeEvaluator = null;

    private VariableScope _scope = null;

    private Map<String, Token> _tokenMap;

    // private final List<Port> inputsPorts;
    // private final List<PortHandler> inputsHandlers;

    // public Parameter inputNumberParam;

    // private int inputNumber = 0;
    // private boolean finishRequested = false;
    public static final String INPUTPORTPREFIX = "x";

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
        super.setInputPortPrefix("x");
        super.setExpectedContentType(String.class);
        output = PortFactory.getInstance().createOutputPort(this, "result");

        expression = new StringParameter(this, "expression");
        expression.setExpression("");

        // inputNumberParam = new StringParameter(this, "number of inputs");
        // inputNumberParam.setExpression(Integer.toString(inputNumber));
        // registerConfigurableParameter(inputNumberParam);

        // inputsPorts = new ArrayList<Port>(10);
        // inputsHandlers = new ArrayList<PortHandler>(10);
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {
        if (attribute == expression) {
            _parseTree = null;
        } else {
            super.attributeChanged(attribute);
        }
    } /*
       * (non-Javadoc)
       *
       * @see com.isencia.passerelle.actor.Actor#getExtendedInfo()
       */

    @Override
    protected String getExtendedInfo() {
        // TODO Auto-generated method stub
        return expression.getExpression();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.isencia.passerelle.actor.Actor#doFire()
     */
    @Override
    protected void process(final ActorContext ctxt, final ProcessRequest request, final ProcessResponse response)
            throws ProcessingException {

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " process() - entry");
        }
        String log = "";

        // get all inputs' messages
        final Iterator<MessageInputContext> allInputContexts = request.getAllInputContexts();
        while (allInputContexts.hasNext()) {
            final MessageInputContext messageInputContext = allInputContexts.next();
            if (!messageInputContext.isProcessed()) {
                final ManagedMessage msg = messageInputContext.getMsg();
                // System.out.println("getting msg for "
                // + messageInputContext.getPortName());
                String input = null;
                try {
                    // if message is a tango attribute, conversion has
                    // been done to a String[]
                    if (msg.getBodyContent().getClass().isArray()) {
                        // System.out.println("is array");
                        final String[] s = (String[]) msg.getBodyContent();
                        input = "{";
                        for (int j = 0; j < s.length - 1; j++) {
                            input += s[j] + ",";
                        }
                        input += s[s.length - 1];
                        input += "}";
                    } else {
                        input = (String) msg.getBodyContent();
                        logger.debug("is not an array " + input);
                    }
                    log += messageInputContext.getPortName() + "=" + input + " ";
                } catch (final MessageException e) {
                    ExceptionUtil.throwProcessingException("cannot get input value", msg, e);
                }
                // Put input in ptolemy type
                Token t = null;
                try {
                    if (input.startsWith("{") && input.endsWith("}")) {
                        t = new ArrayToken(input);
                    } else {
                        // try{
                        // t = new BooleanToken(input);
                        // }catch(IllegalActionException e){
                        try {
                            t = new IntToken(input);
                        } catch (final IllegalActionException e1) {
                            try {
                                t = new DoubleToken(input);
                            } catch (final IllegalActionException e2) {
                                try {
                                    t = new FunctionToken(input);
                                } catch (final IllegalActionException e3) {
                                    t = new StringToken(input);
                                }
                            }
                        }
                        // }
                    }
                } catch (final IllegalActionException e) {
                    ExceptionUtil.throwProcessingException("input message is not correct", input, e);
                }
                _tokenMap.put(messageInputContext.getPortName(), t);
            } else {
                ExceptionUtil.throwProcessingException(ErrorCode.FATAL,
                        "Input port " + messageInputContext.getPortName() + " has no data.", this);
            }
        }

        ExecutionTracerService.trace(this, "calculating " + expression.getExpression() + " where " + log);
        Token result = null;
        try {
            // Note: this code parallels code in the OutputTypeFunction
            // class
            // below.
            if (_parseTree == null) {
                // Note that the parser is NOT retained, since in most
                // cases the expression doesn't change, and the parser
                // requires a large amount of memory.
                final PtParser parser = new PtParser();
                _parseTree = parser.generateParseTree(expression.getExpression());
            }
            if (_parseTreeEvaluator == null) {
                _parseTreeEvaluator = new ParseTreeEvaluator();
            }
            if (_scope == null) {
                _scope = new VariableScope();
            }
            System.out.println(_parseTreeEvaluator.traceParseTreeEvaluation(_parseTree, _scope));
            result = _parseTreeEvaluator.evaluateParseTree(_parseTree, _scope);

        } catch (final IllegalActionException ex) {
            // ex.printStackTrace();
            // Chain exceptions to get the actor that threw the exception.
            ExceptionUtil.throwProcessingException("Expression invalid.", this, ex);

        }
        if (result == null) {
            ExceptionUtil.throwProcessingException("Expression yields a null result: " + expression.getExpression(),
                    this);
        }

        final Type t = result.getType();
        // System.out.println("type " + t);
        // System.out.println("type " + t.getTokenClass());
        if (t.getTokenClass() == ArrayToken.class) {
            final Token[] dT = ((ArrayToken) result).arrayValue();

            // TODO: check all types of Token
            final Double[] out = new Double[dT.length];
            String s = "{";
            for (int i = 0; i < dT.length; i++) {
                out[i] = ((DoubleToken) dT[i]).doubleValue();
                s = s + out[i];
                if (i < dT.length - 1) {
                    s = s + ",";
                } else {
                    s = s + "}";
                }
                // System.out.println(out[i]);
            }
            ExecutionTracerService.trace(this, "result is " + s);
            response.addOutputMessage(output, PasserelleUtil.createContentMessage(this, s));
            // sendOutputMsg(output, PasserelleUtil.createContentMessage(this,
            // s));
        } else if (t.getTokenClass() == FunctionToken.class) {
            final String out = ((FunctionToken) result).toString();
            ExecutionTracerService.trace(this, "result is " + out);
            response.addOutputMessage(output, PasserelleUtil.createContentMessage(this, out));
            // sendOutputMsg(output, PasserelleUtil
            // .createContentMessage(this, out));
        } else if (t.getTokenClass() == StringToken.class) {
            final String out = ((StringToken) result).stringValue();
            ExecutionTracerService.trace(this, "result is " + out);
            response.addOutputMessage(output, PasserelleUtil.createContentMessage(this, out));
            // sendOutputMsg(output, PasserelleUtil
            // .createContentMessage(this, out));
        } else if (t.getTokenClass() == DoubleToken.class) {
            final Double out = ((DoubleToken) result).doubleValue();
            ExecutionTracerService.trace(this, "result is " + out);
            response.addOutputMessage(output, PasserelleUtil.createContentMessage(this, out));
            // sendOutputMsg(output, PasserelleUtil
            // .createContentMessage(this, out));
        } else if (t.getTokenClass() == LongToken.class) {
            final Long out = ((LongToken) result).longValue();
            ExecutionTracerService.trace(this, "result is " + out);
            response.addOutputMessage(output, PasserelleUtil.createContentMessage(this, out));
            // sendOutputMsg(output, PasserelleUtil
            // .createContentMessage(this, out));
        } else if (t.getTokenClass() == IntToken.class) {
            final Integer out = ((IntToken) result).intValue();
            ExecutionTracerService.trace(this, "result is " + out);
            response.addOutputMessage(output, PasserelleUtil.createContentMessage(this, out));
            // sendOutputMsg(output, PasserelleUtil
            // .createContentMessage(this, out));
        } else if (t.getTokenClass() == BooleanToken.class) {
            final Boolean out = ((BooleanToken) result).booleanValue();
            /**
             * Double out = 0.0; if(b) out = 1.0;
             */
            ExecutionTracerService.trace(this, "result is " + out);
            response.addOutputMessage(output, PasserelleUtil.createContentMessage(this, out));
            // sendOutputMsg(output, PasserelleUtil
            // .createContentMessage(this, out));
        }
        // System.out.println(getInfo() + " doFire() - exit");
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doFire() - exit");
        }

    }

    // Add a constraint to the type output port of this object.
    /*
     * private void _setOutputTypeConstraint() { output.setTypeAtLeast(new
     * OutputTypeFunction()); }
     */

    private class VariableScope extends ModelScope {

        /**
         * Look up and return the attribute with the specified name in the
         * scope. Return null if such an attribute does not exist.
         * 
         * @return The attribute with the specified name in the scope.
         */
        public Token get(final String name) throws IllegalActionException {
            if (name.equals("time")) {
                return new DoubleToken(getDirector().getModelTime().getDoubleValue());
            } else if (name.equals("iteration")) {
                return new IntToken(_iterationCount);
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
         * Look up and return the type of the attribute with the specified name
         * in the scope. Return null if such an attribute does not exist.
         * 
         * @return The attribute with the specified name in the scope.
         */
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
         * Look up and return the type term for the specified name in the scope.
         * Return null if the name is not defined in this scope, or is a
         * constant type.
         * 
         * @return The InequalityTerm associated with the given name in the
         *         scope.
         * @exception IllegalActionException
         *                If a value in the scope exists with the given name,
         *                but cannot be evaluated.
         */
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
        public Set<?> identifierSet() {
            return getAllScopedVariableNames(null, Expression.this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.isencia.passerelle.actor.Actor#doInitialize()
     */
    @Override
    protected void doInitialize() throws InitializationException {

        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doInitialize() - entry");
        }

        _iterationCount = 1;
        _tokenMap = new HashMap<String, Token>();
        for (final Port port : getInputPorts()) {
            if (port.getWidth() == 0) {
                ExceptionUtil.throwInitializationException(port.getName() + " is not connected", port);
            }
        }

        super.doInitialize();
        if (logger.isTraceEnabled()) {
            logger.trace(getName() + " doInitialize() - exit");
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