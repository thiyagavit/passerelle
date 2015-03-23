package fr.soleil.passerelle.actor.flow;

import ptolemy.actor.NoRoomException;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.actor.flow.ComparatorHelper.ComparisonNature;
import fr.soleil.passerelle.actor.flow.ComparatorHelper.ComparisonType;
import fr.soleil.passerelle.util.ExceptionUtil;

@SuppressWarnings("serial")
public class ComparatorWithConstant extends Transformer {

	public Parameter comparisonParam;

	private ComparatorHelper.ComparisonNature comparison;
	private String comparisonName;
	public Parameter rightValueParam;

	private String rightValue;

	public Parameter toleranceParam;

	private double tolerance;

	public Port falseOutput;

	public ComparatorWithConstant(final CompositeEntity container,
			final String name) throws NameDuplicationException,
			IllegalActionException {
		super(container, name);

		input.setName("left value");
		input.setExpectedMessageContentType(String.class);
		output.setName("true trigger");
		falseOutput = PortFactory.getInstance().createOutputPort(this,
				"false trigger");

		// Parameters
		comparisonParam = new StringParameter(this, "comparison");
		comparisonParam.setExpression(">");

		rightValueParam = new StringParameter(this, "right value");
		rightValueParam.setExpression("0.0");

		toleranceParam = new StringParameter(this, "tolerance");
		toleranceParam.setExpression("0.0");

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"-30\" y=\"-15\" " + "width=\"50\" height=\"30\" "
				+ "style=\"fill:white\"/>\n"
				+ "<polyline points=\"-30,10, -15,10, -15,3\" "
				+ "style=\"stroke:grey\"/>\n" + "</svg>\n");
	}

	@Override
	public void attributeChanged(final Attribute attribute)
			throws IllegalActionException {

		if (attribute == toleranceParam) {
			tolerance = Double.valueOf(
					((StringToken) toleranceParam.getToken()).stringValue())
					.doubleValue();
		} else if (attribute == rightValueParam) {
			rightValue = ((StringToken) rightValueParam.getToken())
					.stringValue();
		} else if (attribute == comparisonParam) {
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
				throw new IllegalActionException(this,
						"Unrecognized comparison: " + comparisonName);
			}
		} else {
			super.attributeChanged(attribute);
		}

	}

	@Override
	protected void doFire(final ManagedMessage message)
			throws ProcessingException {
		String leftValue = null;
		double rightDouble = 0;
		double leftDouble = 0;
		try {
			leftValue = (String) message.getBodyContent();
		} catch (final MessageException e) {
		    ExceptionUtil.throwProcessingException("get input data failed", this,e);
		}
		final ComparisonType compType = ComparatorHelper.getComparisonType(
				leftValue, rightValue);

		boolean result = false;
		if (compType == ComparisonType.STRING) {
			result = ComparatorHelper.compareString(leftValue, rightValue,
					comparison);
			ExecutionTracerService.trace(this, "comparison " + leftValue + " "
					+ comparisonName + " " + rightValue + " is " + result);
		} else {
			// boolean or double comparison
			final double[] values = ComparatorHelper.getDoubleValues(leftValue,
					rightValue);
			leftDouble = values[0];
			rightDouble = values[1];
			result = ComparatorHelper.compareDouble(leftDouble, rightDouble,
					comparison, tolerance);
			ExecutionTracerService.trace(this, "comparison " + leftDouble + " "
					+ comparisonName + rightDouble + " (+-" + tolerance
					+ ") is " + result);
		}
		try {
			if (result) {
				sendOutputMsg(output, MessageFactory.getInstance()
						.createTriggerMessage());
			} else {
				sendOutputMsg(falseOutput, MessageFactory.getInstance()
						.createTriggerMessage());
			}
			// } catch (TerminateProcessException e) {
			// requestFinish();
		} catch (final NoRoomException e) {
		        ExceptionUtil.throwProcessingException("send output message failed", this,e);
		}

	}

}
