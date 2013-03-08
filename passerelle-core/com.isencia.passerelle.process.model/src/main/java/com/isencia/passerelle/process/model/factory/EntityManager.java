/**
 * 
 */
package com.isencia.passerelle.process.model.factory;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Status;
import com.isencia.passerelle.process.model.Task;

/**
 * @author "puidir"
 * 
 */
public interface EntityManager {

	/**
	 * Persist a Case that is still transient
	 * 
	 * @param caze
	 * @return
	 */
	Case persistCase(Case caze);

	/**
	 * Persist a request that is still transient.
	 * 
	 * @param request
	 * @return
	 */
	Request persistRequest(Request request);

	/**
	 * Persist a context that is still transient. Remark that this may not often
	 * be needed, as a context is persisted in cascade when its request is
	 * persisted.
	 * 
	 * @param context
	 * @return
	 */
	Context persistContext(Context context);

	ResultBlock mergeResultBlock(ResultBlock block);

	/**
	 * Persist updates in an existing context entity or in its contained
	 * properties
	 * 
	 * @param context
	 * @return
	 */
	Context mergeContext(Context context);

	/**
	 * Merge two or more branched contexts
	 * 
	 * @param context
	 * @param branches
	 * @return
	 */
	Context mergeWithBranchedContexts(Context context, Collection<Context> branches);

	/**
	 * Retrieve the case with the given id
	 * 
	 * @param id
	 *            Id of the case to find
	 * @return Found case, null if not found
	 */
	Case getCase(Long id);

	/**
	 * Get the latest persisted version of a Request
	 * 
	 * @param request
	 *            Request to find
	 * @return Latest version of the request
	 */
	Request getRequest(Request request);

	/**
	 * Get a request by Id
	 * 
	 * @param requestId
	 * @return
	 */
	Request getRequest(Long requestId);

	/**
	 * Get a task by Id
	 * 
	 * @param taskId
	 * @return
	 */
	Task getTask(Long taskId);

	Task getTask(Long taskId, boolean bypassCache);

	/**
	 * Get the latest persisted version of a Context
	 * 
	 * @param context
	 *            Context to find
	 * @return Latest version of the Context
	 */
	Context getContext(Context context);

	Context getContext(Context context, boolean bypassCache);

	/**
	 * Retrieve the first Request with the given correlation identifier
	 * 
	 * @param correlationId
	 * @return
	 */
	Request getRequest(String correlationId);

	/**
	 * Get all the requests that are in a certain state.
	 * 
	 * @param status one of the enum values
	 * @return List of requests
	 */
	List<Request> getRequestsByContextStatus(Status status);

	/**
	 * Generate a Request with the same correlation identifier as the given
	 * Request
	 * 
	 * @param request
	 * @return
	 */
	Request persistCorrelatedRequest(Request request);

	/**
	 * @param requestedReferenceId
	 * @return
	 */
	List<Task> getTasksForCase(Long caseId, Long excludedRequestId);

	/**
	 * @param caseId
	 * @param requestId
	 *            to exclude
	 * @return
	 */
	List<Request> getRequestsForCase(Long caseId, Long excludedRequestId);

	/**
	 * @param request
	 * @return
	 */
	List<Request> getRequestsForCase(Request requestToExclude);

	/**
	 * @param requestedReferenceId
	 * @return
	 */
	List<Request> getRequestsForCase(Request requestToExclude, Collection<String> requestTypes);

	/**
	 * @param requestedReferenceId
	 * @return
	 */
	List<Task> getTasksForContext(Context context);

	/**
	 * @param requestId
	 * @return
	 */
	List<ErrorItem> getErrorsForRequest(Long requestId);

	/**
	 * Helper method to ensure a refresh of the given entity
	 * 
	 * @param entity
	 * @return
	 */
	<T extends Serializable> T refresh(T entity);

	/**
	 * @param requestedReferenceId
	 * @return
	 */
	List<ResultBlock> getResultBlocks(Long caseId, Long requestId, Long taskId, Collection<Long> resultBlockIds,
			Collection<String> taskTypes, Collection<String> resultBlockTypes);
}
