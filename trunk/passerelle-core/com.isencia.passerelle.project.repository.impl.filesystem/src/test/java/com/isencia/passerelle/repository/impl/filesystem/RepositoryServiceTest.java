/**
 * 
 */
package com.isencia.passerelle.repository.impl.filesystem;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.isencia.passerelle.project.repository.api.Project;
import com.isencia.passerelle.project.repository.api.RepositoryService;
import com.isencia.passerelle.project.repository.impl.filesystem.FileSystemBasedRepositoryService;

/**
 * @author delerw
 *
 */
public class RepositoryServiceTest implements CommandProvider {

	
	@Before
	public void setUp() throws Exception {
		// TODO should probably install some folders/files in a temp folder,
		// that we use then for the actual tests, i.o. as now
		// depend on pre-existing folders on the HD of the test machine
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAllProjects() {
		RepositoryService repoSvc = new FileSystemBasedRepositoryService(new File("C:/temp/passerelle-repository"));
		String[] allProjectCodes = repoSvc.getAllProjectCodes();
		Assert.assertTrue("Wrong number of Projects found", allProjectCodes.length>1);
		List<String> asList = Arrays.asList(allProjectCodes);
		Assert.assertTrue("Project1 missing", asList.contains("Project1"));
	}

	@Test
	public void testGetProject() {
		RepositoryService repoSvc = new FileSystemBasedRepositoryService(new File("C:/temp/passerelle-repository"));
		Project proj = repoSvc.getProject("Project1");
		Assert.assertNotNull("Project 1 not found", proj);
		Assert.assertEquals("Project with wrong code", "Project1", proj.getCode());
		Assert.assertEquals("Project has wrong nr of knowledgebases", 2, proj.getAllKnowledgeBaseCodes().length);
		Assert.assertEquals("Project has wrong nr of sequences", 1, proj.getAllSequences().length);
	}

	public void _testRepoSvc(CommandInterpreter ci) {
    	Result result = JUnitCore.runClasses(this.getClass());
    	if( result==null) {
        	System.out.println("\nNo Results from tests");
        	return;
    	}
    	System.out.println("\n"+ result.getRunCount() + " tests executed");
    	if( !result.wasSuccessful() ) {
        	System.out.println(result.getFailureCount() + " tests have failures");
    		List<Failure> failures = result.getFailures();
    		for (Failure failure : failures) {
    			System.out.println(failure.getDescription());
    			System.out.println(failure.getMessage());
   				System.out.println(failure.getTrace());
			}
    	} else {
        	System.out.println("\nAll tests passed successfull !");
    	}
	}

	public String getHelp() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\n--- File-based Passerelle Repository test ---\n");
        buffer.append("\ttestRepoSvc - test Repo API \n");
        return buffer.toString();
	}
}
