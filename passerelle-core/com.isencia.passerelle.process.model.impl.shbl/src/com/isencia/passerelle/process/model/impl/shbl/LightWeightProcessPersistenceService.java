package com.isencia.passerelle.process.model.impl.shbl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityTransaction;
import javax.persistence.FetchType;
import javax.persistence.criteria.Root;

import org.eclipse.persistence.sessions.server.ServerSession;

import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.impl.CaseImpl;
import com.isencia.passerelle.process.model.impl.ContextImpl;
import com.isencia.passerelle.process.model.impl.RequestImpl;
import com.isencia.passerelle.process.model.impl.ResultBlockImpl;
import com.isencia.passerelle.process.model.impl.TaskImpl;
import com.isencia.passerelle.process.model.impl.util.ProcessUtils;
import com.isencia.passerelle.process.service.ProcessPersistenceService;
import com.isencia.sherpa.persistence.commons.EntityManagerPool;
import com.isencia.sherpa.persistence.context.PersistenceRequestContext;
import com.isencia.sherpa.persistence.jpa.LightWeightEntityManager;
import com.isencia.sherpa.persistence.jpa.SherpaEntityCache;
import com.isencia.sherpa.persistence.jpa.query.SherpaCriteriaBuilder;
import com.isencia.sherpa.persistence.jpa.query.SherpaCriteriaQuery;
import com.isencia.sherpa.persistence.jpa.query.SherpaQuery;

public class LightWeightProcessPersistenceService implements ProcessPersistenceService {	
	public void close() {
		LightWeightEntityManager em = getEntityManager();
		EntityTransaction transaction = em.getTransaction();
		if (transaction.isActive())
			if (transaction.getRollbackOnly())
				transaction.rollback();
			else
				transaction.commit();

		EntityManagerPool.closeEntityManager("passerelle");
	}
	
	@Override
	public Case getCase(Long id) {
		LightWeightEntityManager em = getEntityManager();
		return(em.find(CaseImpl.class,id));
	}
	
	protected LightWeightEntityManager getEntityManager() {
		return((LightWeightEntityManager) EntityManagerPool.getEntityManager("passerelle"));
	}
	
	@Override
	public Request getRequest(Case caze, Long id) {
		// return the request from the case if it is found
		if (caze != null) {
			for (Request request : caze.getRequests()) {
				if (request.getId().equals(id))
					return(request);
			}
		}
		
		LightWeightEntityManager em = getEntityManager();
		Request request = em.find(RequestImpl.class,id);

		// update the references to/from the case
		if (caze != null) {
			if (request instanceof RequestImpl && caze instanceof CaseImpl)
				((RequestImpl)request).setCase((CaseImpl)caze);
			caze.getRequests().add(request);
		}
		
		return(request);
	}

	@Override
	public Task getTask(Request request, Long id) {
		// return the task from the request if it is found
		if (request != null) {
			for (Task task : request.getProcessingContext().getTasks()) {
				if (task.getId().equals(id))
					return(task);
			}
		}

		LightWeightEntityManager em = getEntityManager();
		Task task = em.find(TaskImpl.class,id);

		// update the references to/from the request context
		if (request != null) {
			if (task instanceof TaskImpl && request.getProcessingContext() instanceof ContextImpl)
				((TaskImpl)task).setParentContext((ContextImpl)request.getProcessingContext());
			request.getProcessingContext().addTask(task);
		}
		
		return(task);
	}
	
	@Override
	public Task getTaskWithResults(Request request, Long id) {
		// get the task from the request if it is found, 
		Task task = null;
		if (request != null) {
			for (Task t : request.getProcessingContext().getTasks()) {
				if (t.getId().equals(id)) {
					task = t;
					if (ProcessUtils.isInitialized(task.getResultBlocks()))
						return(task);
					break;
				}
			}
		}

		LightWeightEntityManager em = getEntityManager();
		if (task != null) {
			// load the resultBlocks for the task we found
			SherpaCriteriaBuilder cb = em.getCriteriaBuilder();
			SherpaCriteriaQuery<ResultBlockImpl> criteriaQuery = cb.createQuery(ResultBlockImpl.class);
			Root<ResultBlockImpl> root = criteriaQuery.from(ResultBlockImpl.class);
			criteriaQuery.where(cb.equal(cb.path(root,ResultBlockImpl._TASK),task.getId()));
			SherpaQuery<ResultBlockImpl> query = em.createQuery(criteriaQuery);

			// use an object cache that already contains the task, so that reference can be reused when building the resultBlock instances
			SherpaEntityCache cache = new SherpaEntityCache();
			cache.put(task);
			List<ResultBlockImpl> resultBlocks = query.getResultList(cache);
			
			// update the collection of resultBlocks in the task
			for (ResultBlockImpl resultBlock : resultBlocks)
				((TaskImpl)task).addResultBlock(resultBlock);
		} else {
			// load the task and resultBlocks in one call
			
			Map<String,FetchType> fetchTypes = new HashMap<String,FetchType>(1);
			fetchTypes.put(TaskImpl._RESULT_BLOCKS,FetchType.EAGER);
			task = em.load(TaskImpl.class,id,fetchTypes);

			// update the references to/from the request context
			if (request != null) {
				if (task instanceof TaskImpl && request.getProcessingContext() instanceof ContextImpl)
					((TaskImpl)task).setParentContext((ContextImpl)request.getProcessingContext());
				request.getProcessingContext().addTask(task);
			}
		}
		
		return(task);
	}

	public void open(boolean transactional) {
		// make sure to login to the database the first time an entityManager is
		// created
		// (login is done lazily to allow initialization after the entities have
		// been woven and before the persistenceUnit is deployed)
		ServerSession session = EntityManagerPool.getFactory("passerelle").getServerSession();
		if (!session.isLoggedIn()) {
			// PERF: Avoid synchronization.
			synchronized (session) {
				// DCL ok as isLoggedIn is volatile boolean, set after login is
				// complete.
				if (!session.isLoggedIn()) {
					PersistenceRequestContext.setPersistenceUnitName(session.getName());
					session.login();
					PersistenceRequestContext.setPersistenceUnitName(null);
					PersistenceRequestContext.unset();
				}
			}
		}

		LightWeightEntityManager em = getEntityManager();

		if (transactional) {
			EntityTransaction transaction = em.getTransaction();
			if (!transaction.isActive())
				transaction.begin();
		}
	}

	@Override
	public void persistAttributes(Request request) {
		LightWeightEntityManager em = getEntityManager();
		try {
			em.update(request, RequestImpl._ATTRIBUTES);
		} catch (Exception e) {
			EntityTransaction transaction = em.getTransaction();
			if (transaction.isActive())
				transaction.setRollbackOnly();
		}
	}

	@Override
	public void persistCase(Case caze) {
		LightWeightEntityManager em = getEntityManager();
		try {
			em.persist(caze);
		} catch (Exception e) {
			EntityTransaction transaction = em.getTransaction();
			if (transaction.isActive())
				transaction.setRollbackOnly();
		}
	}

	@Override
	public void persistRequest(Request request) {
		LightWeightEntityManager em = getEntityManager();
		em.persist(request);
		try {
			em.update(request, RequestImpl._ATTRIBUTES);
		} catch (Exception e) {
			EntityTransaction transaction = em.getTransaction();
			if (transaction.isActive())
				transaction.setRollbackOnly();
		}
	}

	@Override
	public void persistResultBlocks(ResultBlock... resultBlocks) {
		LightWeightEntityManager em = getEntityManager();
	    try {
	      for (ResultBlock resultBlock : resultBlocks) {
	        em.persist(resultBlock);
	      }
		} catch (Exception e) {
			EntityTransaction transaction = em.getTransaction();
			if (transaction.isActive())
				transaction.setRollbackOnly();
		}
	}

	@Override
	public void persistTask(Task task) {
		persistRequest(task);
	}
	
	@Override
	public void updateResultBlock(ResultBlock resultBlock) {
		LightWeightEntityManager em = getEntityManager();
		try {
			em.update(resultBlock);
		} catch (Exception e) {
			EntityTransaction transaction = em.getTransaction();
			if (transaction.isActive())
				transaction.setRollbackOnly();
		}
	}
	
	@Override
	public void updateStatus(Request request) {
		Context context = request.getProcessingContext();
		if (context == null || context.isForkedContext() || context.isMinimized())
			return;

		LightWeightEntityManager em = getEntityManager();
		try {
			em.update(context,ContextImpl._STATUS);
		} catch (Exception e) {
			EntityTransaction transaction = em.getTransaction();
			if (transaction.isActive())
				transaction.setRollbackOnly();
		}
	}
}
