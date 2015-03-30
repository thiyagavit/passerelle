/*	Synchrotron Soleil
 *
 *   File          :  BooleanSelection.java
 *
 *   Project       :  soleil
 *
 *   Description   :
 *
 *   Author        :  ABEILLE
 *
 *   Original      :  4 janv. 07
 *
 *   Revision:  					Author:
 *   Date: 							State:
 *
 *   Log: BooleanSelection.java,v
 *
 */
/*
 * Created on 4 janv. 07
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.flow;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import fr.soleil.passerelle.actor.TransformerV3;
import fr.soleil.passerelle.util.PasserelleUtil;

@SuppressWarnings("serial")
public class BooleanSelection extends TransformerV3 {

	public Parameter outputTrueParam;
	private boolean outputTrue = false;

	Port falseOutput;

	public BooleanSelection(final CompositeEntity arg0, final String arg1) throws NameDuplicationException, IllegalActionException {
		super(arg0, arg1);
		outputTrueParam = new Parameter(this, "output true", new BooleanToken(outputTrue));
		outputTrueParam.setTypeEquals(BaseType.BOOLEAN);

		output.setName("true output");

		falseOutput = PortFactory.getInstance().createOutputPort(this, "false output");
		/*
		 * _attachText("_iconDescription",
		 * "<svg>\n<rect x=\"-20\" y=\"-20\" width=\"40\" height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
		 * +
		 * "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" style=\"stroke-width:1.0;stroke:white\"/>\n"
		 * +
		 * "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" style=\"stroke-width:1.0;stroke:white\"/>\n"
		 * +
		 * "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n"
		 * +
		 * "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n"
		 * +
		 * "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n"
		 * +
		 * "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n"
		 * +
		 * "<circle cx=\"0\" cy=\"0\" r=\"10\"style=\"fill:white;stroke-width:2.0\"/>\n"
		 * +
		 * "<line x1=\"-15\" y1=\"0\" x2=\"15\" y2=\"0\" style=\"stroke-width:2.0\"/>\n"
		 * +
		 * "<line x1=\"12\" y1=\"-3\" x2=\"15\" y2=\"0\" style=\"stroke-width:2.0\"/>\n"
		 * +
		 * "<line x1=\"12\" y1=\"3\" x2=\"15\" y2=\"0\" style=\"stroke-width:2.0\"/>\n"
		 * + "</svg>\n");
		 */
		_attachText("_iconDescription", "<svg>\n<rect x=\"-20\" y=\"-20\" width=\"40\" height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
				+ "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" style=\"stroke-width:1.0;stroke:white\"/>\n"
				+ "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" style=\"stroke-width:1.0;stroke:white\"/>\n"
				+ "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n"
				+ "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n"
				+ "<rect x=\"-13\" y=\"-8\" width=\"27\" height=\"15\"style=\"fill:white;stroke-width:1.0;stroke:black\"/>\n" + "</svg>\n");
	}

	@Override
	public void attributeChanged(final Attribute arg0) throws IllegalActionException {
		if (arg0 == outputTrueParam) {
			// outputTrue = new
			// Boolean(outputTrueParam.getExpression().trim()).booleanValue();
			outputTrue = PasserelleUtil.getParameterBooleanValue(outputTrueParam);
		} else {
			super.attributeChanged(arg0);
		}
	}

	@Override
	protected void process(final ActorContext arg0, final ProcessRequest request, final ProcessResponse reponse) throws ProcessingException {
		if (outputTrue) {
			reponse.addOutputMessage(0, output, PasserelleUtil.createContentMessage(this, "true"));
		} else {
			reponse.addOutputMessage(1, falseOutput, PasserelleUtil.createContentMessage(this, "false"));
		}
	}
}
