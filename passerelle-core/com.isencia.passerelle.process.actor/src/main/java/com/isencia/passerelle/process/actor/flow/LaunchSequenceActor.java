package com.isencia.passerelle.process.actor.flow;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.LoggerFactory;

import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.FlowUtils;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.edm.engine.api.DiagnosisEntityFactory;
import com.isencia.passerelle.edm.engine.api.service.ServicesRegistry;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.NamedValue;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.project.repository.api.RepositoryService;

public class LaunchSequenceActor extends Actor {
	public static final String PROJECT_CODE = "Project code";
	public static final String SEQUENCE_CODE = "Sequence code";
	public static final String PARAMETERS = "Parameters";

	public Port input; // NOSONAR
	public Port output; // NOSONAR
	public StringParameter projectCodeParameter; // NOSONAR
	public StringParameter sequenceCodeParameter; // NOSONAR
	public StringParameter parameterParameter; // NOSONAR

	public LaunchSequenceActor(CompositeEntity container, String name) throws IllegalActionException,
			NameDuplicationException {
		super(container, name);

		input = PortFactory.getInstance().createInputPort(this, null);
		output = PortFactory.getInstance().createOutputPort(this);
		projectCodeParameter = new StringParameter(this, PROJECT_CODE);
		sequenceCodeParameter = new StringParameter(this, SEQUENCE_CODE);
		parameterParameter = new StringParameter(this, PARAMETERS);

	}

	@Override
	protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response)
			throws ProcessingException {
		ManagedMessage message = request.getMessage(input);
		if (message != null) {
			try {
				Context flowContext = null;
				if (message.getBodyContent() instanceof Context) {
					flowContext = (Context) message.getBodyContent();
				} else {
					throw new ProcessingException("No context present in msg", message, null);
				}
				RepositoryService repoSvc = com.isencia.passerelle.process.actor.util.ServicesRegistry.getInstance()
						.getRepositoryService();
				String sequenceCode = sequenceCodeParameter.getExpression();
				String parameters = parameterParameter.getExpression();
				Flow flow = repoSvc.getFlow(sequenceCode);
				// com.isencia.passerelle.diagnosis.Parameter parameter =
				// flowContext
				// .getRequest().getDistinctParameterForName("revisionId");
				// flowContext.getRequest().getParameters().remove(parameter);
				// ServicesRegistry
				// .getInstance()
				// .getDiagnosisEntityFactory()
				// .createSystemParameter("revisionId",
				// getRevisionId(flow, flowContext.getRequest()),
				// flowContext.getRequest());
				if (flow != null) {
					try {
						if (parameters != null && !parameters.trim().equals("")) {
							addRequestAttributes(parameters, flowContext.getRequest(), ServicesRegistry.getInstance()
									.getDiagnosisEntityFactory());
						}
						new Parameter(flow, "context", new ObjectToken(flowContext));
					} catch (IllegalActionException e) {
					} catch (NameDuplicationException e) {
					}

					// Parameter parameter = new Parameter(flow, "jobID");
					// parameter.setExpression(requestId);
					String flowName = FlowUtils.generateUniqueFlowName(flow.getName());
					flow.setName(flowName);
					flow.propagateValues();
					flow.propagateValue();
					FlowManager flowManager = new FlowManager();
					if (flow != null && flowManager != null) {
						flowManager.executeBlockingLocally(flow, null);
					} else {
						getLogger().error("FlowManager and Flow were not set");
					}

				}

			} catch (MessageException ex) {
			} catch (Exception ex) {
			}
		} else {
			// should not happen, but one never knows, e.g. when a requestFinish
			// msg arrived or so...
			LoggerFactory.getLogger(getClass()).warn(
					"Actor " + this.getFullName() + " received empty message in process()");
		}
		sendOutputMsg(output, message);
	}

	private String getRevisionId(Flow flow, Request request) {
		Attribute attribute = flow.getAttribute("revisionID");

		if (attribute instanceof StringParameter) {
			StringParameter par = (StringParameter) attribute;
			return par.getExpression();
		}
		NamedValue distinctParameterForName = request.getAttribute("revisionID");
		if (distinctParameterForName == null) {
			return null;
		}
		return distinctParameterForName.getValueAsString();
	}

	protected void addRequestAttributes(String parameters, Request req, DiagnosisEntityFactory diagnosisEntityFactory) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
					parameters.getBytes())));
			try {
				String paramDef = null;
				while ((paramDef = reader.readLine()) != null) {
					String[] paramKeyValue = paramDef.split("=");
					if (paramKeyValue.length == 2) {
						diagnosisEntityFactory.createAttribute(paramKeyValue[0], paramKeyValue[1], req);
					}
				}
			} finally {
				if (reader != null)
					reader.close();
			}

		} catch (IOException e) {
		}

	}

}
