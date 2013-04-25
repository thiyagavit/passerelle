package fr.soleil.passerelle.actor.flow;

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
import com.isencia.passerelle.util.ExecutionTracerService;
import fr.soleil.passerelle.actor.flow.ComparatorHelper.ComparisonNature;
import fr.soleil.passerelle.actor.flow.ComparatorHelper.ComparisonType;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class WhileLoop extends Transformer {

	public Parameter comparisonParam;

	private ComparisonNature comparison;

	public Parameter valueParam;
	String comparisonName;
	private String rightValue = "0";

	public Port continuing;

	public WhileLoop(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {

		super(container, name);

		comparisonParam = new StringParameter(this, "comparison");
		comparisonParam.setExpression("==");

		valueParam = new StringParameter(this, "Rigth Value");
		valueParam.setExpression(rightValue);

		input.setExpectedMessageContentType(String.class);
		input.setName("left value");
		output.setName("finished");
		continuing = PortFactory.getInstance().createOutputPort(this,
				"continue");

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"-20\" y=\"-20\" width=\"40\" "
				+ "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
				+ "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" "
				+ "style=\"stroke-width:1.0;stroke:white\"/>\n"
				+ "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" "
				+ "style=\"stroke-width:1.0;stroke:white\"/>\n"
				+ "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" "
				+ "style=\"stroke-width:1.0;stroke:black\"/>\n"
				+ "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
				+ "style=\"stroke-width:1.0;stroke:black\"/>\n"
				+ "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" "
				+ "style=\"stroke-width:1.0;stroke:grey\"/>\n"
				+ "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" "
				+ "style=\"stroke-width:1.0;stroke:grey\"/>\n" +

				"<circle cx=\"0\" cy=\"0\" r=\"10\""
				+ "style=\"fill:white;stroke-width:2.0\"/>\n" +

				"<line x1=\"10\" y1=\"0\" x2=\"7\" y2=\"-3\" "
				+ "style=\"stroke-width:2.0\"/>\n"
				+ "<line x1=\"10\" y1=\"0\" x2=\"13\" y2=\"-3\" "
				+ "style=\"stroke-width:2.0\"/>\n" +

				"</svg>\n");

	}

	@Override
	protected void doFire(ManagedMessage message) throws ProcessingException {

		String leftValueS = null;
		ExecutionTracerService.trace(this, "doFire in ");
		if (!isFinishRequested()) {
			ExecutionTracerService.trace(this, "waiting for value");
			leftValueS = (String) PasserelleUtil.getInputValue(message);
		}

		boolean continu = false;
		double rightValueD = 0;
		double leftValueD = 0;
		ComparisonType compType = ComparatorHelper.getComparisonType(
				leftValueS, rightValue);
		switch (compType) {
		case DOUBLE:
			leftValueD = Double.parseDouble(leftValueS);
			rightValueD = Double.parseDouble(rightValue);
			break;
		case BOOLEAN:
			if (Boolean.parseBoolean(leftValueS))
				leftValueD = 1;
			else
				leftValueD = 0;
			if (Boolean.parseBoolean(rightValue))
				rightValueD = 1;
			else
				rightValueD = 0;
			break;
		}
		if (compType == ComparisonType.STRING) {
			continu = ComparatorHelper.compareString(leftValueS, rightValue,
					comparison);
			ExecutionTracerService.trace(this, "comparison " + leftValueS + " "
					+ comparisonName + " " + rightValue + " is " + continu);
		} else {
			continu = ComparatorHelper.compareDouble(leftValueD, rightValueD,
					comparison);
			ExecutionTracerService.trace(this, "comparison " + leftValueD + " "
					+ comparisonName + rightValueD + " is " + continu);
		}
		if (continu) {
			sendOutputMsg(continuing, PasserelleUtil.createTriggerMessage());
		} else {
			ExecutionTracerService.trace(this, "End loop");
			sendOutputMsg(output, PasserelleUtil.createTriggerMessage());
			// requestFinish();
		}
		ExecutionTracerService.trace(this, "doFire out");
	}

	@Override
	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {

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
				throw new IllegalActionException(this,
						"Unrecognized comparison: " + comparisonName);
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
