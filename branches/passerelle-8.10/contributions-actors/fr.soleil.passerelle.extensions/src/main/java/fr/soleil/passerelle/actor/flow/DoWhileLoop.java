package fr.soleil.passerelle.actor.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListener;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.actor.flow.ComparatorHelper.ComparisonNature;
import fr.soleil.passerelle.actor.flow.ComparatorHelper.ComparisonType;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class DoWhileLoop extends Transformer {

    public static final String RIGTH_VALUE_PARAM_NAME = "Rigth Value";
    public static final String COMPARISON_PARAM_NAME = "comparison";

    public static final String OUTPUT_PORT_NAME = "finished";
    public static final String CONTINUE_PORT_NAME = "continue";

    private final static Logger logger = LoggerFactory.getLogger(DoWhileLoop.class);

    public Parameter comparisonParam;

    private ComparisonNature comparison;
    String comparisonName;
    public Parameter valueParam;

    private String rightValue = "0";

    public Port leftValuePort;
    private PortHandler leftHandler = null;
    public Port continuing;

    boolean firstLoop;

    private String leftValueS = "";
    boolean valueReceived = false;

    public DoWhileLoop(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {

        super(container, name);

        comparisonParam = new StringParameter(this, COMPARISON_PARAM_NAME);
        comparisonParam.setExpression("==");

        valueParam = new StringParameter(this, RIGTH_VALUE_PARAM_NAME);
        valueParam.setExpression(rightValue);

        input.setName("start");
        leftValuePort = PortFactory.getInstance().createInputPort(this, "left value", String.class);
        input.setMultiport(false);

        output.setName(OUTPUT_PORT_NAME);
        continuing = PortFactory.getInstance().createOutputPort(this, CONTINUE_PORT_NAME);

        final StringAttribute leftValuePortCardinal = new StringAttribute(leftValuePort,
                "_cardinal");
        leftValuePortCardinal.setExpression("SOUTH");

        final StringAttribute continuingCardinal = new StringAttribute(continuing, "_cardinal");
        continuingCardinal.setExpression("SOUTH");

        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-25\" y=\"-25\" width=\"50\" "
                + "height=\"50\" style=\"fill:pink;stroke:pink\"/>\n"
                + "<line x1=\"-24\" y1=\"-24\" x2=\"24\" y2=\"-24\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"-24\" y1=\"-24\" x2=\"-24\" y2=\"24\" "
                + "style=\"stroke-width:1.0;stroke:white\"/>\n"
                + "<line x1=\"25\" y1=\"-24\" x2=\"25\" y2=\"25\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"-24\" y1=\"25\" x2=\"25\" y2=\"25\" "
                + "style=\"stroke-width:1.0;stroke:black\"/>\n"
                + "<line x1=\"24\" y1=\"-23\" x2=\"24\" y2=\"24\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
                + "<line x1=\"-23\" y1=\"24\" x2=\"24\" y2=\"24\" "
                + "style=\"stroke-width:1.0;stroke:grey\"/>\n" +

                "<circle cx=\"0\" cy=\"0\" r=\"10\"" + "style=\"fill:white;stroke-width:2.0\"/>\n" +

                "<line x1=\"10\" y1=\"0\" x2=\"7\" y2=\"-3\" " + "style=\"stroke-width:2.0\"/>\n"
                + "<line x1=\"10\" y1=\"0\" x2=\"13\" y2=\"-3\" "
                + "style=\"stroke-width:2.0\"/>\n" +

                "</svg>\n");
    }

    @Override
    protected void doInitialize() throws InitializationException {

        valueReceived = false;
        leftValueS = "";
        // If something connected to the set port, install a handler
        if (leftValuePort.getWidth() > 0) {
            leftHandler = new PortHandler(leftValuePort, new PortListener() {
                @Override
                public void tokenReceived() {
                    logger.debug("leftHandler.tokenReceived() ");
                    final Token token = leftHandler.getToken();
                    if (token != null && token != Token.NIL) {
                        try {
                            final ManagedMessage message = MessageHelper.getMessageFromToken(token);
                            leftValueS = (String) message.getBodyContent();
                            valueReceived = true;
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        leftValueS = null;
                    }

                }

                @Override
                public void noMoreTokens() {
                    requestFinish();
                }
            });
            if (leftHandler != null) {
                leftHandler.start();
            }
        }
        firstLoop = true;
        super.doInitialize();

    }

    @Override
    protected void doFire(final ManagedMessage message) throws ProcessingException {

        boolean continu = true;

        ExecutionTracerService.trace(this, "Start while");
        sendOutputMsg(continuing, PasserelleUtil.createTriggerMessage());

        while (continu && !isFinishRequested()) {
            while (!valueReceived && !isFinishRequested()) {
                // System.out.println("waiting ...");
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                }
            }
            if (valueReceived) {
                valueReceived = false;
                if (isFinishRequested()) {
                    logger.debug("break");
                    break;
                }

                double rightValueD = 0;
                double leftValueD = 0;
                final ComparisonType compType = ComparatorHelper.getComparisonType(leftValueS,
                        rightValue);
                // 0=string, 1= boolean, 2= double
                switch (compType) {
                case DOUBLE:
                    leftValueD = Double.parseDouble(leftValueS);
                    rightValueD = Double.parseDouble(rightValue);
                    break;
                case BOOLEAN:
                    if (Boolean.parseBoolean(leftValueS)) {
                        leftValueD = 1;
                    } else {
                        leftValueD = 0;
                    }
                    if (Boolean.parseBoolean(rightValue)) {
                        rightValueD = 1;
                    } else {
                        rightValueD = 0;
                    }
                    break;
                }

                if (compType == ComparisonType.STRING) {
                    continu = ComparatorHelper.compareString(leftValueS, rightValue, comparison);
                    ExecutionTracerService.trace(this, "comparison " + leftValueS + " "
                            + comparisonName + " " + rightValue + " is " + continu);
                } else {
                    continu = ComparatorHelper.compareDouble(leftValueD, rightValueD, comparison);
                    ExecutionTracerService.trace(this, "comparison " + leftValueD + " "
                            + comparisonName + rightValueD + " is " + continu);
                }
            } else {
                continu = false;
            }
            if (continu) {
                sendOutputMsg(continuing, PasserelleUtil.createTriggerMessage());
            }
        } // End while
        if (isFinishRequested()) {
            ExecutionTracerService.trace(this, "Finish requested");
        } else {
            ExecutionTracerService.trace(this, "End while");
            sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
        }
    }

    @Override
    public void attributeChanged(final Attribute attribute) throws IllegalActionException {

        if (attribute == comparisonParam) {
            comparisonName = comparisonParam.getExpression().trim();
            if (comparisonName.equals(">")) {
                comparison = ComparisonNature.GT;
            } else if (comparisonName.equals(">=")) {
                comparison = ComparisonNature.GE;
            } else if (comparisonName.equals("<")) {
                comparison = ComparisonNature.LT;
            } else if (comparisonName.equals("<=")) {
                comparison = ComparisonNature.LE;
            } else if (comparisonName.equals("==")) {
                comparison = ComparisonNature.EQ;
            } else if (comparisonName.equals("!=")) {
                comparison = ComparisonNature.NE;
            } else {
                throw new IllegalActionException(this, "Unrecognized comparison: " + comparisonName);
            }
        } else if (attribute == valueParam) {
            rightValue = ((StringToken) valueParam.getToken()).stringValue();
        } else {

            super.attributeChanged(attribute);
        }

    }

    @Override
    protected String getExtendedInfo() {
        return this.getName();
    }

}
