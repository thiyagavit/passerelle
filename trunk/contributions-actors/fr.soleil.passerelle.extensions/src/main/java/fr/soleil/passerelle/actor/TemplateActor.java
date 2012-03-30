package fr.soleil.passerelle.actor;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

public class TemplateActor extends Actor {
	public static final String TEMPLATE = "template";
	public Port input; // NOSONAR
	public Port output; // NOSONAR
	public StringParameter templateParameter; // NOSONAR

	public TemplateActor(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

		input = PortFactory.getInstance().createInputPort(this, null);
		output = PortFactory.getInstance().createOutputPort(this);
		templateParameter = new StringParameter(this, TEMPLATE);
		new Attribute(templateParameter, "TextArea");
	}

	@Override
	protected void process(ActorContext ctxt, ProcessRequest request,
			ProcessResponse response) throws ProcessingException {
		try {
			ManagedMessage message = request.getMessage(input);
			if (message != null) {
				Object bodyContent = message.getBodyContent();
				if (bodyContent instanceof HashMap) {
					HashMap map = (HashMap) bodyContent;
					String template = templateParameter.getExpression();
					if (template == null || template.trim().isEmpty()) {
						throw new ProcessingException("Not template defined",
								message, null);
					}
					Velocity.init();
					VelocityContext context = new VelocityContext();
					context.put("context", bodyContent);
					StringWriter writer = new StringWriter();
					Velocity.evaluate(context, writer, "velocity", template);
					ManagedMessage newMessage = createMessage(
							writer.toString(), "text/plain");
					response.addOutputMessage(output, newMessage);
				}
			}
		} catch (Exception e) {
			throw new ProcessingException(
					"Error executing knowledge-based analysis", this, e);
		}

	}

}
