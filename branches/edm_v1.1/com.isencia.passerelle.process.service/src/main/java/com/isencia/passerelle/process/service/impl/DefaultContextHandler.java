/**
 * 
 */
package com.isencia.passerelle.process.service.impl;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.scheduler.ResourceToken;
import com.isencia.passerelle.process.scheduler.TaskHandler;
import com.isencia.passerelle.process.service.AbstractAsyncService;
import com.isencia.passerelle.process.service.ManagedContext;
import com.isencia.passerelle.process.service.proxy.ContextManagerProxy;

/**
 * @author puidir
 *
 */
public class DefaultContextHandler implements TaskHandler {

	private AbstractAsyncService service;
	
	public DefaultContextHandler(AbstractAsyncService service) {
		this.service = service;
	}
	
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.scheduler.TaskHandler#handleTask(com.isencia.passerelle.diagnosis.Context, com.isencia.passerelle.process.scheduler.ResourceToken)
	 */
	public void handle(Context entity, ResourceToken resourceToken) {
		entity = ContextManagerProxy.notifyStarted(entity);
		service.handleNow(new ManagedContext(entity, resourceToken));
	}

}
