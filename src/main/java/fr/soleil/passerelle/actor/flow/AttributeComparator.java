/*	Synchrotron Soleil
 *
 *   File          :  AttributeComp.java
 *
 *   Project       :  passerelle-soleil
 *
 *   Description   :
 *
 *   Author        :  ABEILLE
 *
 *   Original      :  11 mai 2005
 *
 *   Revision:  					Author:
 *   Date: 							State:
 *
 *   Log: AttributeComp.java,v
 *
 */
/*
 * Created on 11 mai 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
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

import fr.soleil.passerelle.actor.flow.ComparatorHelper.ComparisonNature;
import fr.soleil.passerelle.actor.flow.ComparatorHelper.ComparisonType;
import fr.soleil.passerelle.util.ExceptionUtil;

/**
 * @author ABEILLE
 *
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@SuppressWarnings("serial")
public class AttributeComparator extends Actor {

	private final static Logger logger = LoggerFactory.getLogger(AttributeComparator.class);
	/** The left input port */
	public Port left;

	/** The right input port */
	public Port right;

	/** The output ports */
	public Port trueOutput;
	public Port falseOutput;

	public Parameter comparisonParam;
	private ComparisonNature comparison;
	private String comparisonName;
	public Parameter toleranceParam;
	private double tolerance;

	private PortHandler leftHandler = null;
	private PortHandler rightHandler = null;
	private boolean rightReceived = false;
	private boolean leftReceived = false;

	// TangoAttribute rightProxy = null;
	// TangoAttribute leftProxy = null;

	String rightConst = null;
	String leftConst = null;

	boolean tokenIsNull = false;

	/**
	 * @param container
	 * @param name
	 * @throws ptolemy.kernel.util.IllegalActionException
	 * @throws ptolemy.kernel.util.NameDuplicationException
	 */
	public AttributeComparator(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		// Parameters
		comparisonParam = new StringParameter(this, "comparison");
		comparisonParam.setExpression(">");

		toleranceParam = new StringParameter(this, "tolerance");
		toleranceParam.setExpression("0.0");

		// Ports
		left = PortFactory.getInstance().createInputPort(this,
				"left (AttributeProxy or Const)", String.class);
		left.setMultiport(false);
		right = PortFactory.getInstance().createInputPort(this,
				"right (AttributeProxy or Const)", String.class);
		right.setMultiport(false);
		trueOutput = PortFactory.getInstance().createOutputPort(this,
				"trueOutput (Trigger)");
		falseOutput = PortFactory.getInstance().createOutputPort(this,
				"falseOutput (Trigger)");

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"-30\" y=\"-15\" " + "width=\"50\" height=\"40\" "
				+ "style=\"fill:white\"/>\n"
				+ "<polyline points=\"-30,5, -15,5\" "
				+ "style=\"stroke:grey\"/>\n"
				+ "<polyline points=\"-30,15, 8,15, 8,5\" "
				+ "style=\"stroke:grey\"/>\n" + "</svg>\n");

	}

	@Override
	public void doInitialize() throws InitializationException {

		tokenIsNull = false;
		leftReceived = false;
		rightReceived = false;

		if (logger.isTraceEnabled())
			logger.trace(getName() + " doInitialize() - entry");

		// If something connected to the set port, install a handler
		if (left.getWidth() > 0) {
			// System.out.println("left.getWidth() > 0");
			leftHandler = new PortHandler(left, new PortListener() {
				public void tokenReceived() {
					// System.out.println("leftHandler.tokenReceived() ");
					Token token = leftHandler.getToken();
					if (token != null && token != Token.NIL) {
						try {
							ManagedMessage message = MessageHelper
									.getMessageFromToken(token);
							leftConst = (String) message.getBodyContent();
						} catch (MessageException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (PasserelleException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						leftReceived = true;
						// System.out.println("left received");

					}
					performNotify();
				}

				public void noMoreTokens() {
					do {
						performWait(1000);
					} while (leftReceived);
					// System.out.println("no more tokens left");
					leftReceived = true;
					tokenIsNull = true;
					performNotify();
				}
			});
			if (leftHandler != null) {
				leftHandler.start();
			}
		}
		if (right.getWidth() > 0) {
			// System.out.println("right.getWidth() > 0");
			rightHandler = new PortHandler(right, new PortListener() {
				public void tokenReceived() {
					// System.out.println("rightHandler.tokenReceived() ");
					Token token = rightHandler.getToken();
					/*
					 * System.out.println("rightHandler.tokenReceived: " +
					 * token);
					 */
					if (token != null) {
						try {
							ManagedMessage message = MessageHelper
									.getMessageFromToken(token);
							rightConst = (String) message.getBodyContent();
						} catch (MessageException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (PasserelleException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						rightReceived = true;
					}
					performNotify();
				}

				public void noMoreTokens() {
					do {
						performWait(1000);
					} while (rightReceived);
					tokenIsNull = true;
					rightReceived = true;
					performNotify();
				}
			});
			if (rightHandler != null) {
				rightHandler.start();
			}

		}
		if (logger.isTraceEnabled())
			logger.trace(getName() + " doInitialize() - exit");
	}

	private synchronized void performNotify() {
		notify();
	}

	@Override
	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {

		if (attribute == toleranceParam) {
			tolerance = Double.valueOf(
					((StringToken) toleranceParam.getToken()).stringValue())
					.doubleValue();
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
	protected void doFire() throws ProcessingException {
		if (logger.isTraceEnabled())
			logger.trace(getName() + " doFire() - entry");
		while (!leftReceived || !rightReceived) {
			performWait(1000);
		}
		leftReceived = false;
		rightReceived = false;
		if (!tokenIsNull) {
			double rightValue = 0;
			double leftValue = 0;
			ComparisonType compType = ComparatorHelper.getComparisonType(
					leftConst, rightConst);

			boolean result = false;
			if (compType == ComparisonType.STRING) {
				result = ComparatorHelper.compareString(leftConst, rightConst,
						comparison);
				ExecutionTracerService.trace(this, "comparison " + leftConst
						+ " " + comparisonName + " " + rightConst + " is "
						+ result);
			} else {
				// boolean or double comparison
				double[] values = ComparatorHelper.getDoubleValues(leftConst,
						rightConst);
				leftValue = values[0];
				rightValue = values[1];
				result = ComparatorHelper.compareDouble(leftValue, rightValue,
						comparison, tolerance);
				ExecutionTracerService.trace(this, "comparison " + leftValue
						+ " " + comparisonName + " " + rightValue + " (+-"
						+ tolerance + ") is " + result);
			}
			try {
				if (result) {
					if (logger.isTraceEnabled())
						logger.trace(getName()
								+ " doFire() - comparison is true");
					sendOutputMsg(trueOutput, MessageFactory.getInstance()
							.createTriggerMessage());
				} else {
					if (logger.isTraceEnabled())
						logger.trace(getName()
								+ " doFire() - comparison is false");
					sendOutputMsg(falseOutput, MessageFactory.getInstance()
							.createTriggerMessage());
				}
			} catch (TerminateProcessException e) {
				requestFinish();
			} catch (NoRoomException e) {
				ExceptionUtil.throwProcessingException("send output message failed", this,e);
			}
			rightConst = null;
			leftConst = null;
		} else {
			requestFinish();

		}
		if (logger.isTraceEnabled())
			logger.trace(getName() + " doFire() - exit");
	}

	private synchronized void performWait(int time) {
		try {
			if (time == -1)
				wait();
			else
				wait(time);
		} catch (InterruptedException e) {
		}

	}

}
