package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;


import java.net.URL;

import javax.management.MBeanServerConnection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.jmx.RemoteManagerAgent;
import com.isencia.passerelle.workbench.model.launch.ModelRunner;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

/**
 * This action is run on the workbench and starts the workflow.
 * 
 * The workflow notifies with a jmx service whose port is defined by
 * the system property "com.isencia.jmx.service.port" before the action
 * is run. It is variable as the workbench will attempt to find a free port
 * for it to connect to the worklow.
 * 
 * @author gerring
 *
 */
@SuppressWarnings("restriction")
public class RunAction extends ExecutionAction implements IEditorActionDelegate {

	private static final Logger logger = LoggerFactory.getLogger(RunAction.class);
	
	public RunAction() {
		super();
		setId(getClass().getName());
		setText("Run the workflow from start to end until finished.");
		setImageDescriptor(Activator.getImageDescriptor("/icons/run_workflow.gif"));
	}

	@Override
	public void run() {
		run(this);
	}
	
	@Override
	public void run(IAction action) {
	    
		try {
			final IFile config = getModelRunner();
			EclipseUtils.getPage().saveAllEditors(true);
			
			final IEditorPart editor = EclipseUtils.getPage().getActiveEditor();
			if (editor!=null)  EclipseUtils.getPage().activate(editor);
			
			// Make sure that the current editor is the selected resource.
			final IResource sel = getSelectedResource();
			if (sel instanceof IFile)  EclipseUtils.openEditor((IFile)sel);
			
            if (System.getProperty("eclipse.debug.session")!=null) {
				final Job job = new Job("Run workflow debug mode") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						final ModelRunner runner = new ModelRunner();
						try {
							runner.runModel(sel.getLocation().toOSString(), false);
						} catch (Throwable e) {
							logger.error("Error during workflow execution", e);
						}
						return Status.OK_STATUS;
					}
				};
				job.setUser(false);
				job.setPriority(Job.LONG);
				job.schedule();
				
				refreshToolbars();
				
			} else { // Normally the case in real application
				// TODO Find way of sending port, addSystemProperty not working
				WorkflowLaunchConfiguration configuration = new WorkflowLaunchConfiguration(config);
				configuration.addSystemProperty("com.isencia.jmx.service.port");
				DebugUITools.launch(configuration, ILaunchManager.RUN_MODE);
				createModelListener();
			}
			
			
		} catch (Exception e) {
			logger.error("Cannot read configuration", e);
			refreshToolbars();
		}

	}
	
	private void createModelListener() {
		
		// Polling is horrible...
		final Job job = new Job("Running workflow") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					logger.debug("Running run buttons update task");
					int waited = 0;
					// BODGE Sleep while starting and then refresh when service up.
					while(DebugUITools.getCurrentProcess()==null) {
						Thread.sleep(100);
						waited+=100;
						if (waited>=10000) break;
					}
					logger.debug("process "+DebugUITools.getCurrentProcess());
					logger.debug("process "+DebugUITools.getCurrentProcess().getLabel());


				} catch (Exception ignored) {
					// try to wait, if not then die.
				}
				try {
					logger.debug("Adding polling");
					MBeanServerConnection client = RemoteManagerAgent.getServerConnection(10000);
					while(client.isRegistered(RemoteManagerAgent.REMOTE_MANAGER)) {
						refreshToolbars();
						Thread.sleep(1000);
						monitor.worked(1);
					}
					refreshToolbars();
				} catch (Exception e) {
					logger.error("Cannot add polling to be notified when run finished.", e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		
		// This makes the job show in the status but does not 
		// pop up a dialog.
		job.setUser(false);
		job.setPriority(Job.SHORT);
		job.schedule(500);

	}
	
	public boolean isEnabled() {
		if (System.getProperty("eclipse.debug.session")!=null) {
			return ModelRunner.getRunningInstance()==null;
		}
		try { 
			final MBeanServerConnection server = RemoteManagerAgent.getServerConnection(100);
			return server.getObjectInstance(RemoteManagerAgent.REMOTE_MANAGER)==null;
		} catch (Throwable e) {
			return true;
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}
	
	public IFile getModelRunner() throws Exception {
		
	    // if the bundle is not ready then there is no image
        Bundle bundle = Activator.getDefault().getBundle();

        // look for the image (this will check both the plugin and fragment folders
        URL fullPathString = null;
        if ("true".equals(System.getProperty("edna","true"))){	
        	fullPathString = BundleUtility.find(bundle, "ModelRunnerEdna.txt");
        } else{
        	fullPathString = BundleUtility.find(bundle, "ModelRunner.txt");
        	
        }
        final IResource sel= getSelectedResource();
        final IFile   file = sel.getProject().getFile("WorkflowConfiguration.launch");
        if (file.exists()) return file;
        file.create(fullPathString.openStream(), true, null);
        file.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
        return file;
 	}

	private IResource getSelectedResource() {
		final ISelection sel = EclipseUtils.getPage().getSelection();
		if (sel!=null && sel instanceof IStructuredSelection) {
			final IStructuredSelection str = (IStructuredSelection)sel;
			final Object res = str.getFirstElement();
			if (res instanceof IFile) {
				final IFile file = (IFile)res;
				if (file.getName().toLowerCase().endsWith(".moml")) return file;
			}
		} 
		final IFile file =  EclipseUtils.getIFile(EclipseUtils.getPage().getActiveEditor().getEditorInput());
		if (file.getName().toLowerCase().endsWith(".moml")) return file;
		return null;
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub

	}

}
