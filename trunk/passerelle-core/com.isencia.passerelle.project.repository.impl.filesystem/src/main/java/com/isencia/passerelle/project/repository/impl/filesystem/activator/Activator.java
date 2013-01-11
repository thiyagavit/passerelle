package com.isencia.passerelle.project.repository.impl.filesystem.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.isencia.passerelle.project.repository.api.RepositoryService;
import com.isencia.passerelle.project.repository.impl.filesystem.FileSystemBasedRepositoryService;

public class Activator implements BundleActivator {

	private RepositoryService repoSvc;
	private ServiceRegistration repoSvcReg;

	public void start(BundleContext context) throws Exception {
	  String rootFolderPath = System.getProperty("com.isencia.passerelle.project.root", "C:/temp/passerelle-repository");
	  String submodelPath = System.getProperty("com.isencia.passerelle.submodel.root", "C:/temp/submodel-repository");
		repoSvc = new FileSystemBasedRepositoryService(rootFolderPath,submodelPath);
		repoSvcReg = context.registerService(RepositoryService.class.getName(),
				repoSvc, null);
	}

	public void stop(BundleContext context) throws Exception {
		repoSvcReg.unregister();

		repoSvc = null;
	}
}
