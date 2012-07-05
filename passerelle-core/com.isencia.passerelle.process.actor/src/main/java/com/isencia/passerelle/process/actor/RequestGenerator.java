package com.isencia.passerelle.process.actor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.edm.engine.api.DiagnosisEntityFactory;
import com.isencia.passerelle.edm.engine.api.DiagnosisEntityManager;
import com.isencia.passerelle.edm.engine.api.service.ServicesRegistry;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Request;

/**
 * Actor to generate a simple Request that mainly can be used as trigger for
 * basic diagnostic sequences.
 * 
 * @author delerw
 * 
 */
public class RequestGenerator extends Actor {

	private static final long serialVersionUID = 1L;

	public Port trigger; // NOSONAR
	public Port output; // NOSONAR
	public StringParameter requestTypeParameter; // NOSONAR
	public StringParameter requestParamsParameter; // NOSONAR

	public RequestGenerator(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

		requestTypeParameter = new StringParameter(this, "Request type");
		requestParamsParameter = new StringParameter(this, "Request parameters");
		new TextStyle(requestParamsParameter, "paramsTextArea");

		// trigger = PortFactory.getInstance().createInputPort(this, "trigger",
		// null);
		output = PortFactory.getInstance().createOutputPort(this);

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"-20\" y=\"-20\" width=\"40\" "
				+ "height=\"40\" style=\"fill:orange;stroke:orange\"/>\n"
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
				+ "style=\"stroke-width:1.0;stroke:grey\"/>\n"
				+ "<circle cx=\"0\" cy=\"0\" r=\"10\""
				+ "style=\"fill:white;stroke-width:2.0\"/>\n"
				+ "<line x1=\"-15\" y1=\"0\" x2=\"0\" y2=\"0\" "
				+ "style=\"stroke-width:2.0\"/>\n"
				+ "<line x1=\"-3\" y1=\"-3\" x2=\"0\" y2=\"0\" "
				+ "style=\"stroke-width:2.0\"/>\n"
				+ "<line x1=\"-3\" y1=\"3\" x2=\"0\" y2=\"0\" "
				+ "style=\"stroke-width:2.0\"/>\n" + "</svg>\n");
	}

	@Override
	protected void process(ActorContext ctxt, ProcessRequest request,
			ProcessResponse response) throws ProcessingException {
		try {
			DiagnosisEntityFactory diagnosisEntityFactory = ServicesRegistry
					.getInstance().getDiagnosisEntityFactory();
			DiagnosisEntityManager diagnosisEntityManager = ServicesRegistry
					.getInstance().getDiagnosisEntityManager();

			Context processingContext = createContext(diagnosisEntityManager,
					diagnosisEntityFactory);

			ManagedMessage message = createMessage(processingContext,
					ManagedMessage.objectContentType);
			response.addOutputMessage(output, message);
		} catch (MessageException e) {
			throw new ProcessingException("Error creating output msg", this, e);
		} catch (Exception t) {
			throw new ProcessingException("Error generating request", this, t);
		} finally {
			// should terminate after one request generation
			requestFinish();
		}
	}

	protected Context createContext(
			DiagnosisEntityManager diagnosisEntityManager,
			DiagnosisEntityFactory diagnosisEntityFactory) {
		String owner = null;
		String jobID = null;
		String name = null;
		String revisionID = null;
		String reference = null;
		String locale = null;
		NamedObj flow = this.toplevel();

		List parameters = flow.attributeList(Parameter.class);
		Map<String, String> parMap = new HashMap<String, String>();
		for (Iterator iter = parameters.iterator(); iter.hasNext();) {
			Parameter p = (Parameter) iter.next();
			if ("userID".equals(p.getName())) {
				owner = p.getExpression();
			} else if ("jobID".equals(p.getName())) {
				jobID = p.getExpression();
			} else if ("flowname".equals(p.getName())) {
				name = p.getExpression();
			} else if ("revisionID".equals(p.getName())) {
				revisionID = p.getExpression();
			} else if ("REFERENCE".equals(p.getName())) {
				reference = p.getExpression();
			} else if ("locale".equals(p.getName())) {
				locale = p.getExpression();
			} else {
				parMap.put(p.getName(), p.getExpression());
			}
		}

		if (reference == null || reference.trim().equals("")) {
			reference = diagnosisEntityManager.generateRequestReference();
		}

		Request req = diagnosisEntityFactory.createRequest(flow.getName(),
				owner, jobID, name, reference);

		diagnosisEntityFactory.createSystemParameter("revisionId", revisionID,
				req);
		diagnosisEntityFactory.createSystemParameter("locale", locale, req);

		for (String key : parMap.keySet()) {
			diagnosisEntityFactory.createApplicationParameter(key, parMap
					.get(key), req);
		}
		diagnosisEntityFactory.createContext(req);
		req = diagnosisEntityManager.persistCorrelatedRequest(req);
		try {
			ptolemy.data.expr.Parameter parameter = new ptolemy.data.expr.Parameter(
					toplevel(), "requestId");
			parameter.setExpression(req.getId().toString());
		} catch (Exception e) {

		}
		return req.getProcessingContext();

	}

}
