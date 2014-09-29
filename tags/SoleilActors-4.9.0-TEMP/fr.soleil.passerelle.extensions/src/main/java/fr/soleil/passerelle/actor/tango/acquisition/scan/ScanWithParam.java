package fr.soleil.passerelle.actor.tango.acquisition.scan;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.isencia.passerelle.util.ExecutionTracerService;

import com.isencia.passerelle.doc.generator.ParameterName;

import fr.soleil.passerelle.actor.tango.acquisition.ScanOLD;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * Do scans using Salsa config files and allow to configure trajectories for one
 * actuator
 *
 * @author ABEILLE
 */
@SuppressWarnings("serial")
public class ScanWithParam extends ScanOLD {

	private final static Logger logger = LoggerFactory.getLogger(ScanWithParam.class);

	private static final String CONFIG = "Config Params";
	/**
	 * Some parameters of the scan for the trajectories.<br>
	 * <ul>
	 * The parameters must start with:
	 * <li><b>f</b> for From
	 * <li><b>t</b> for To
	 * <li><b>s</b> for Step Number
	 * <li><b>i</b> for Integration time
	 * </ul>
	 * In 2D Scan, the dimensions must be fill in with <b>x</b> for dimension X
	 * and <b>y</b> dimension Y.<br>
	 * <ul>
	 * <li>For 1DScan the parameters are: f1;t1;s1;i1...fi;ti;si;ii with i the
	 * range number.
	 * <li>For 2DScan the parameters are:
	 * fx1;tx1;sx1;ix1;...fxi;txi;sxi;ixi;fy1;ty1;sy1...fyi;tyi;syi with i the
	 * range number. Warning: there is integration time in dimension Y.
	 * </ul>
	 * <ul>
	 * Examples:
	 *<li>1D Scan: f1=0;t1=5;s1=10;i1=1 <br>
	 * <li>2D Scan: fx1=2;tx1=10;sx1=10;ix1=0.1;fy1=2;ty1=10;sy1=10
	 * </ul>
	 */
	@ParameterName(name = CONFIG)
	public Parameter argsParam;
	private String args;

	Map<Integer, ScanRangeX> rangesXList = new HashMap<Integer, ScanRangeX>();
	Map<Integer, ScanRangeY> rangesYList = new HashMap<Integer, ScanRangeY>();

	public ScanWithParam(final CompositeEntity container, final String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		argsParam = new StringParameter(this, CONFIG);
		argsParam.setExpression("f1=2;t1=10;s1=10;i1=0.1");
	}

	@Override
	public void doInitialize() throws InitializationException {
		if (!this.isMockMode()) {
			final StringTokenizer tokenizer = new StringTokenizer(args, ";");
			while (tokenizer.hasMoreElements()) {
				final String element = tokenizer.nextToken();
				logger.debug("****treating " + element);
				final String[] values = element.split("=");
				final String name = values[0];
				logger.debug("name " + name);

				boolean scan1D = true;
				if (element.contains("y")) {
					scan1D = false;
					logger.debug("2D ");
				}

				final String value = values[1];
				logger.debug("value " + value);
				if (scan1D) {
					// 1D
					final int rangeNumber = Integer.valueOf(name
							.substring(1, 2));
					logger.debug("rangeNumber " + rangeNumber);
					if (!rangesXList.containsKey(rangeNumber)) {
						rangesXList.put(rangeNumber, new ScanRangeX());
					}
					final ScanRangeX range = rangesXList.get(rangeNumber);
					if (element.startsWith("f")) {
						range.setFrom(Double.valueOf(value));
					} else if (element.startsWith("t")) {
						range.setTo(Double.valueOf(value));
					} else if (element.startsWith("s")) {
						range.setNumberOfSteps(Integer.valueOf(value));
					} else if (element.startsWith("i")) {
						range.setIntegrationTime(Double.valueOf(value));
					} else {
						if (value.equalsIgnoreCase("abs")) {
							range.setRelative(false);
						} else if (value.equalsIgnoreCase("rel")) {
							range.setRelative(true);
						}
					}
				} else {
					final int rangeNumber = Integer.valueOf(name
							.substring(2, 3));
					logger.debug("rangeNumber " + rangeNumber);
					// 2D
					if (!rangesYList.containsKey(rangeNumber)) {
						rangesYList.put(rangeNumber, new ScanRangeY());
					}
					final ScanRangeY range = rangesYList.get(rangeNumber);
					if (element.startsWith("f")) {
						range.setFrom(Double.valueOf(value));
					} else if (element.startsWith("t")) {
						range.setTo(Double.valueOf(value));
					} else if (element.startsWith("s")) {
						range.setNumberOfSteps(Integer.valueOf(value));
					} else if (element.startsWith("i")) {
						// impossible
					}
				}
			}
		}
		super.doInitialize();
		scanApi.changeTrajectory(rangesXList, rangesYList);
	}

	@Override
	protected void process(final ActorContext ctxt,
			final ProcessRequest request, final ProcessResponse response)
			throws ProcessingException {

		if (this.isMockMode()) {
			ExecutionTracerService.trace(this, "MOCK - Scan with: " + args);
		} else {
			ExecutionTracerService.trace(this, "Scan Scan with: " + args);
		}
		super.process(ctxt, request, response);

	}

	@Override
	public void attributeChanged(final Attribute attribute)
			throws IllegalActionException {
		if (attribute == argsParam) {
			args = PasserelleUtil.getParameterValue(argsParam);
		} else {
			super.attributeChanged(attribute);
		}
	}

}
