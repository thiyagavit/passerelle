package com.isencia.passerelle.project.repository.impl.filesystem.activator;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.isencia.passerelle.project.repository.api.RepositoryService;
import com.isencia.passerelle.project.repository.impl.filesystem.FileSystemBasedRepositoryService;

public class Activator implements BundleActivator {

	private RepositoryService repoSvc;
	private ServiceRegistration repoSvcReg;

	public void start(BundleContext context) throws Exception {
		repoSvc = new FileSystemBasedRepositoryService();
		repoSvcReg = context.registerService(RepositoryService.class.getName(),
				repoSvc, null);
	}

	public void stop(BundleContext context) throws Exception {
		repoSvcReg.unregister();

		repoSvc = null;
	}
}
