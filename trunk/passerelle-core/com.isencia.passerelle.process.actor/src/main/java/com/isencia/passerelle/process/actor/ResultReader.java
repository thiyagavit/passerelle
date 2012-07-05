/**
 * 
 */
package com.isencia.passerelle.process.actor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.edm.actor.common.SynchronousTaskBasedActor;
import com.isencia.passerelle.edm.engine.api.DiagnosisEntityManager;
import com.isencia.passerelle.process.actor.activator.Activator;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Request;

// REMARK puidir: In the move from Dare to EDM, I ignored everything except REFID ... will only put in what we actually use

/**
 * An actor that reads results of previous task based processing, based on a
 * combination of the following parameters, with indication of their possible
 * source :
 * <ul>
 * <li>REFID : should be in the incoming request, as the request's refId</li>
 * <li>REQUESTID : should be in the incoming request, as a parameter</li>
 * <li>TASKID : should be in the incoming request, as a parameter</li>
 * <li>MOSTRECENT : should be in the incoming request, as a parameter
 * (optional), or if absent : obtained from actor cfg parameter</li>
 * <li>TASKTYPES : should be in the incoming request, as a parameter (optional),
 * or if absent : obtained from actor cfg parameter</li>
 * <li>DATATYPES : should be in the incoming request, as a parameter (optional),
 * or if absent : obtained from actor cfg parameter</li>
 * </ul>
 * At least one of REFID, REQUESTID or TASKID is required. <br/>
 * <br/>
 * MOSTRECENT is a boolean (although in text format). When true, it means that
 * for duplicate resultblocks (i.e. multiple instances of the same datatype),
 * only the most recent one is returned. <br/>
 * TASKTYPES is a comma-separated list. When present it limits the results to
 * ones originating from tasks with a type in this list. <br/>
 * DATATYPES is a comma-separated list. When present it limits the results to
 * blocks with a type in this list.
 * 
 * <p>
 * Besides the MOSTRECENT,TASKTYPES & DATATYPES, the actor also has cfg
 * parameters for selecting one/both of RAWDATA or ANALYSISDATA. RAWDATA returns
 * data obtained from backend data collections. ANALYSISDATA returns data
 * obtained from rules-based analysis.
 * </p>
 * 
 * @author delerw
 * @author puidir
 * 
 */
public class ResultReader extends SynchronousTaskBasedActor {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultReader.class);

	private static final String PREVIOUS_RESULTS_BLOCK_TYPE = "PREVIOUS_RESULTS";

	// public Parameter mostRecentOnlyParam;
	public StringParameter taskTypesParam;
	public StringParameter dataTypesParam;

	// public Parameter rawDataParam;
	// public Parameter analysisDataParam;

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public ResultReader(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);

		// rawDataParam = new Parameter(this, "Return backend data", new
		// BooleanToken(true));
		// new CheckBoxStyle(rawDataParam, "cb1");
		//
		// analysisDataParam = new Parameter(this, "Return analysis results",
		// new BooleanToken(true));
		// new CheckBoxStyle(analysisDataParam, "cb2");
		//
		// mostRecentOnlyParam = new Parameter(this, "Most recent only", new
		// BooleanToken(true));
		// new CheckBoxStyle(mostRecentOnlyParam, "cb3");
		//
		taskTypesParam = new StringParameter(this, "Selected task types");
		dataTypesParam = new StringParameter(this, "Selected data types");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.edm.actor.common.AbstractTaskBasedActor#
	 * initActivatorOutsideOSGi()
	 */
	@Override
	protected void initActivatorOutsideOSGi() {
		Activator.initOutsideOSGi();

	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.isencia.passerelle.edm.actor.common.SynchronousTaskBasedActor#handle
	 * (com.isencia.passerelle.diagnosis.Context,
	 * com.isencia.passerelle.diagnosis.Context, java.util.Map)
	 */
	@Override
	protected Context handle(Context taskContext, Context flowContext, Map<String, String> actorAttributes)
			throws ProcessingException {

		Request flowRequest = flowContext.getRequest();

		String requestedReferenceId = null;
		// Long requestedRequestId = null;
		// Long requestedTaskId = null;
		// Boolean requestedMostRecent = null;
		// String[] requestedDataTypes = null;
		// String[] requestedTaskTypes = null;

		if (flowRequest.getReference() != null) {
			requestedReferenceId = flowRequest.getReference();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Reference ID requested : " + requestedReferenceId);
			}
		}

		// String requestId =
		// flowContext.getValue(StandardParameterNames.REQUEST_ID);
		// if (requestId != null) {
		// requestedRequestId = Long.valueOf(requestId);
		// if (LOGGER.isDebugEnabled()) {
		// LOGGER.debug("Request ID requested : " + requestedRequestId);
		// }
		// }
		//
		// String taskId = flowContext.getValue(StandardParameterNames.TASK_ID);
		// if (taskId != null) {
		// requestedTaskId = Long.valueOf(taskId);
		// if (LOGGER.isDebugEnabled()) {
		// LOGGER.debug("Task ID requested : " + requestedTaskId);
		// }
		// }
		//
		// String mostRecent =
		// flowContext.getValue(StandardParameterNames.MOST_RECENT);
		// if (mostRecent != null) {
		// requestedMostRecent = Boolean.valueOf(mostRecent);
		// } else {
		// try {
		// requestedMostRecent = ((BooleanToken)
		// mostRecentOnlyParam.getToken()).booleanValue();
		// } catch (IllegalActionException e) {
		// LOGGER.error(ErrorCode.SYSTEM_ERROR +
		// " - error reading actor cfg parameter", e);
		// }
		// }
		//
		// if (LOGGER.isDebugEnabled()) {
		// LOGGER.debug("Only Most Recent requested : " + requestedMostRecent);
		// }
		//
		// String dataTypes =
		// flowContext.getValue(StandardParameterNames.DATA_TYPES);
		// if (dataTypes == null) {
		// dataTypes = this.dataTypesParam.getExpression();
		// }
		//
		// if (dataTypes != null) {
		// requestedDataTypes = parseArrayAttribute(dataTypes);
		// if (LOGGER.isDebugEnabled()) {
		// LOGGER.debug("Data Types requested : " + requestedDataTypes);
		// }
		// }
		//
		// String taskTypes =
		// flowContext.getValue(StandardParameterNames.TASK_TYPES);
		// if (taskTypes == null) {
		// taskTypes = this.taskTypesParam.getExpression();
		// }
		//
		// if (taskTypes != null) {
		// requestedTaskTypes = parseArrayAttribute(taskTypes);
		// if (LOGGER.isDebugEnabled()) {
		// LOGGER.debug("Task Types requested : " + requestedTaskTypes);
		// }
		// }

//		IResultReaderService resultReaderService = com.isencia.passerelle.diagnosis.actor.util.ServicesRegistry
//				.getInstance().getResultReaderService();
		DiagnosisEntityManager diagnosisEntityManager = com.isencia.passerelle.edm.engine.api.service.ServicesRegistry
				.getInstance().getDiagnosisEntityManager();
		try {
			List<Request> requests = diagnosisEntityManager.getRequestsFromRequestRefId(requestedReferenceId,
					flowContext.getRequest().getId());
			for (Request request : requests) {
				flowContext.addTransientContext(request.getProcessingContext());
			}
		} catch (Exception e) {

		}
		/*
		 * foundTasks = resultReaderService.getResults( requestedReferenceId,
		 * requestedRequestId, requestedTaskId, requestedMostRecent,
		 * requestedDataTypes, requestedTaskTypes);
		 */

		// Only get the latest result for each resultitem

		// Deepest opportunity to sort creation date is the resultblock
		// List<ResultBlock> foundResultBlocks = new ArrayList<ResultBlock>();
		// for (Task foundTask : foundTasks) {
		// foundResultBlocks.addAll(foundTask.getResultBlocks());
		// }
		// Collections.sort(foundResultBlocks, new Comparator<ResultBlock>() {
		//
		// public int compare(ResultBlock lhs, ResultBlock rhs) {
		// return new CompareToBuilder().append(lhs.getCreationTS(),
		// rhs.getCreationTS()).toComparison();
		// }
		//
		// });

		String dataTypes = dataTypesParam.getExpression();
		List supportedDataTypes = null;
		if (dataTypes != null && !dataTypes.trim().equals("")) {
			supportedDataTypes = Arrays.asList(StringUtils.split(dataTypes, ","));
		}

		// Map<String, ResultItem> exclusiveItems = new HashMap<String,
		// ResultItem>();
		// for (ResultBlock foundResultBlock : foundResultBlocks) {
		// for (ResultItem foundResultItem : foundResultBlock.getAllItems()) {
		// if (supportedDataTypes == null ||
		// supportedDataTypes.contains(foundResultItem.getResultBlock().getType()))
		// {
		// exclusiveItems.put(foundResultItem.getName(),foundResultItem);
		// }
		// }
		// }
		//
		// DiagnosisEntityFactory factory = ServicesRegistry.getInstance()
		// .getDiagnosisEntityFactory();
		//
		// // Add found result blocks to the existing task
		// // In dare, the resultblocks are simply stored in the result, but
		// we'll
		// // create a new resultblock
		// ResultBlock resultDataBlock = factory.createResultBlock(
		// PREVIOUS_RESULTS_BLOCK_TYPE, (Task) taskContext.getRequest());
		// for (ResultItem foundResultItem : exclusiveItems.values()) {
		// factory.createResultItem(foundResultItem.getName(),
		// foundResultItem.getValue(), foundResultItem.getUnit(),
		// resultDataBlock);
		// }
		//
		// Result results = new Result(ResultType.SYNCH);
		// results.addResultBlock(resultDataBlock);
		// ContextManagerProxy.notifyEvent(entity, eventType)
		return (Context) ContextManagerProxy.notifyFinished(taskContext);
	}

	public static String[] parseArrayAttribute(String attribute) {
		String[] split = attribute.split(",");
		List<String> list = new ArrayList<String>();

		for (String name : split) {
			if (StringUtils.isNotBlank(name)) {
				list.add(name.trim());
			}
		}
		return (String[]) list.toArray(new String[list.size()]);
	}
}
