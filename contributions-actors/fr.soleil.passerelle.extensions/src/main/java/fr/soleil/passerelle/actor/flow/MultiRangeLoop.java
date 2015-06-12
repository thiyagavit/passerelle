package fr.soleil.passerelle.actor.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.MiscellaneousUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class MultiRangeLoop extends Actor {

	private final static Logger logger = LoggerFactory.getLogger(MultiRangeLoop.class);
	// input ports
	public Port triggerPort;
	public Port handledPort;

	// output ports
	public Port outputPort;
	public Port endRangePort;
	public Port endLoopPort;

	public Parameter fromParam;
	double[] froms = { 0 };
	public Parameter toParam;
	double[] tos = { 0 };
	public Parameter deltaParam;
	double[] deltas = { 0 };

	public Parameter outputRangesOneByOneParam;
	boolean outputRangesOneByOne = false;

	MultiRange ranges;
	Range currentRange;

	private boolean triggerPortExhausted = false;
	private boolean handledPortExhausted = false;

	public MultiRangeLoop(CompositeEntity arg0, String arg1)
			throws IllegalActionException, NameDuplicationException {
		super(arg0, arg1);

		ranges = new MultiRange();
		triggerPort = PortFactory.getInstance().createInputPort(this,
				"trigger (start loop)", null);
		triggerPort.setMultiport(false);
		handledPort = PortFactory.getInstance().createInputPort(this,
				"handled", null);
		handledPort.setMultiport(false);
		endLoopPort = PortFactory.getInstance().createOutputPort(this,
				"end loop trigger");
		endRangePort = PortFactory.getInstance().createOutputPort(this,
				"end range trigger");
		outputPort = PortFactory.getInstance().createOutputPort(this,
				"output value");

		fromParam = new StringParameter(this, "Start Value");
		fromParam.setExpression("0,2");

		toParam = new StringParameter(this, "End Value");
		toParam.setExpression("3,5");

		deltaParam = new StringParameter(this, "Step Width");
		deltaParam.setExpression("1,6");

		outputRangesOneByOneParam = new Parameter(this,
				"Output ranges one by one", new BooleanToken(false));
		outputRangesOneByOneParam.setTypeEquals(BaseType.BOOLEAN);

		StringAttribute outputPortCardinal = new StringAttribute(outputPort,
				"_cardinal");
		outputPortCardinal.setExpression("SOUTH");

		StringAttribute handledPortCardinal = new StringAttribute(handledPort,
				"_cardinal");
		handledPortCardinal.setExpression("SOUTH");

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"-25\" y=\"-25\" width=\"50\" "
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

				"<circle cx=\"0\" cy=\"0\" r=\"10\""
				+ "style=\"fill:white;stroke-width:2.0\"/>\n" +

				"<line x1=\"10\" y1=\"0\" x2=\"7\" y2=\"-3\" "
				+ "style=\"stroke-width:2.0\"/>\n"
				+ "<line x1=\"10\" y1=\"0\" x2=\"13\" y2=\"-3\" "
				+ "style=\"stroke-width:2.0\"/>\n" +

				"</svg>\n");
	}

	@Override
	protected void doInitialize() throws InitializationException {
		triggerPortExhausted = false;
		handledPortExhausted = !(handledPort.getWidth() > 0);
		ranges.clear();
		// assume that all tables have the same size
		for (int i = 0; i < froms.length; i++) {
			Range range = new Range();
			logger.debug(String.valueOf(froms[i]));
			range.setFrom(froms[i]);
			range.setTo(tos[i]);
			range.setDelta(deltas[i]);
			range.setName("range" + (i + 1));
			ranges.add(range);
		}
		ranges.initialize();
		logger.debug("config = " + ranges);
		super.doInitialize();
	}

	@Override
	protected void doFire() throws ProcessingException {
		ManagedMessage inputMsg = null;
		if (!triggerPortExhausted) {
			try {
				inputMsg = MessageHelper.getMessage(triggerPort);
				if (inputMsg == null) {
					triggerPortExhausted = true;
				}
			} catch (PasserelleException e) {
			    ExceptionUtil.throwProcessingException("Error reading from port", triggerPort, e);
			}
		}
		if (inputMsg != null) {
			if (outputRangesOneByOne) {
				currentRange = ranges.getCurrentRange();
				if (currentRange.hasNext())
					sendOneRangeLoopData();
				// and now do the loop, each time after receiving a loop
				// iteration
				// handled notification.
				// Loop step+1 times to send a message on the output hasFinished
				// at the end of the last loop
				boolean endRange = false;
				while (!handledPortExhausted && !endRange) {
					ManagedMessage handledMsg = null;
					try {
						handledMsg = MessageHelper.getMessage(handledPort);
					} catch (PasserelleException e) {
					    ExceptionUtil.throwProcessingException("Error reading from port", handledPort, e);
					}
					if (handledMsg == null) {
						handledPortExhausted = true;
					} else {
						if (currentRange.hasNext()) {
							sendOneRangeLoopData();
						} else {
							ExecutionTracerService.trace(this, "All "
									+ currentRange.getName() + " done");
							sendOutputMsg(endRangePort, PasserelleUtil
									.createTriggerMessage());
							endRange = true;
							if (ranges.hasNext()) {
								currentRange = ranges.next();
							} else {
								ExecutionTracerService.trace(this,
										"All loops done");
								sendOutputMsg(endLoopPort, PasserelleUtil
										.createTriggerMessage());
							}

						}
					}
				}
			} else {
				ranges.initialize();
				sendAllRangesLoopData();
				// and now do the loop, each time after receiving a loop
				// iteration
				// handled notification.
				// Loop step+1 times to send a message on the output hasFinished
				// at the end of the last loop
				boolean end = false;
				while (!handledPortExhausted && !end) {
					ManagedMessage handledMsg = null;
					try {
						handledMsg = MessageHelper.getMessage(handledPort);
					} catch (PasserelleException e) {
					    ExceptionUtil.throwProcessingException("Error reading from port", handledPort, e);
					}
					if (handledMsg == null) {
						handledPortExhausted = true;
					} else {
						if (ranges.hasNextValue()) {
							sendAllRangesLoopData();
						} else {
							ExecutionTracerService
									.trace(this, "All loops done");
							sendOutputMsg(endLoopPort, PasserelleUtil
									.createTriggerMessage());
							end = true;
						}
					}
				}
			}
		}
		if (triggerPortExhausted) {
			requestFinish();
		}
	}

	private void sendOneRangeLoopData() throws ProcessingException,
			IllegalArgumentException {
		double currentValue = currentRange.next();
		ExecutionTracerService.trace(this, currentRange.getName()
				+ "- outputting value " + currentValue);
		sendOutputMsg(outputPort, PasserelleUtil.createContentMessage(this,
				currentValue));
	}

	private void sendAllRangesLoopData() throws ProcessingException,
			IllegalArgumentException {
		double currentValue = ranges.nextValue();
		ExecutionTracerService.trace(this, ranges.getCurrentRange().getName()
				+ "- outputting value " + currentValue);
		sendOutputMsg(outputPort, PasserelleUtil.createContentMessage(this,
				currentValue));
	}

	@Override
	public void attributeChanged(Attribute arg0) throws IllegalActionException {
		if (arg0 == fromParam) {
			String[] table = ((StringToken) fromParam.getToken()).stringValue()
					.split(",");
			froms = MiscellaneousUtil.convertStringsTodoubles(table);
		} else if (arg0 == toParam) {
			String[] table = ((StringToken) toParam.getToken()).stringValue()
					.split(",");
			tos = MiscellaneousUtil.convertStringsTodoubles(table);
		} else if (arg0 == deltaParam) {
			String[] table = ((StringToken) deltaParam.getToken())
					.stringValue().split(",");
			deltas = MiscellaneousUtil.convertStringsTodoubles(table);
		} else if (arg0 == outputRangesOneByOneParam) {
			outputRangesOneByOne = Boolean.valueOf(outputRangesOneByOneParam
					.getExpression());
		} else
			super.attributeChanged(arg0);
	}

	@Override
	public Object clone(final Workspace workspace)
			throws CloneNotSupportedException {
		final MultiRangeLoop copy = (MultiRangeLoop) super.clone(workspace);
		copy.ranges = new MultiRange();
		return copy;
	}

}
