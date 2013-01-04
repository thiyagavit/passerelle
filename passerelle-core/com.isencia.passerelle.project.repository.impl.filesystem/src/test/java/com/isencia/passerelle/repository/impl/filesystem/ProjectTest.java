/**
 * 
 */
package com.isencia.passerelle.repository.impl.filesystem;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.drools.KnowledgeBase;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import be.isencia.passerelle.model.Flow;

import com.isencia.passerelle.project.repository.api.Project;
import com.isencia.passerelle.project.repository.impl.filesystem.FileSystemBasedProject;

/**
 * @author delerw
 *
 */
public class ProjectTest implements CommandProvider {

	
	@Before
	public void setUp() throws Exception {
		// TODO should probably install some folders/files in a temp folder,
		// that we use then for the actual tests, i.o. as now
		// depend on pre-existing folders on the HD of the test machine
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.isencia.passerelle.project.repository.impl.filesystem.FileSystemBasedProject#getAllKnowledgeBaseCodes()}.
	 */
	@Test
	public void testGetAllKnowledgeBaseCodes() {
		Project project = new FileSystemBasedProject(new File("C:/temp/passerelle-repository"),"Project1");
		
		String[] allKnowledgeBaseCodes = project.getAllKnowledgeBaseCodes();
		Assert.assertEquals("Wrong number of KBs found", 2, allKnowledgeBaseCodes.length);
		
		List<String> asList = Arrays.asList(allKnowledgeBaseCodes);
		Assert.assertTrue("KB1 missing", asList.contains("KB1"));
		Assert.assertTrue("KB2 missing", asList.contains("KB2"));
	}

	/**
	 * Test method for {@link com.isencia.passerelle.project.repository.impl.filesystem.FileSystemBasedProject#getAllSequences()}.
	 */
//	@Test
//	public void testGetAllSequences() {
//		Project project = new FileSystemBasedProject(new File("C:/temp/passerelle-repository"),"Project1");
//		
//		Flow[] allSequences = project.getAllSequences();
//		Assert.assertEquals("Wrong number of flows found", 1, allSequences.length);
//		
//		Assert.assertEquals("HelloWorld missing", "helloworld",allSequences[0].getName());
//	}

	/**
	 * Test method for {@link com.isencia.passerelle.project.repository.impl.filesystem.FileSystemBasedProject#getKnowledgeBase(java.lang.String)}.
	 */
	@Test
	public void testGetKnowledgeBase1() {
		Project project = new FileSystemBasedProject(new File("C:/temp/passerelle-repository"),"Project1");
		KnowledgeBase kb = project.getKnowledgeBase("KB1");
		
		Assert.assertNotNull("KB1 must not be null", kb);
	}

	/**
	 * Test method for {@link com.isencia.passerelle.project.repository.impl.filesystem.FileSystemBasedProject#getKnowledgeBase(java.lang.String)}.
	 */
//	@Test
//	public void testGetKnowledgeBase2() {
//		Project project = new FileSystemBasedProject(new File("C:/temp/passerelle-repository"),"Project1");
//		KnowledgeBase kb = project.getKnowledgeBase("KB2");
//		
//		Assert.assertNotNull("KB2 must not be null", kb);
//	}

	/**
	 * Test method for {@link com.isencia.passerelle.project.repository.impl.filesystem.FileSystemBasedProject#getSequence(java.lang.String)}.
	 */
//	@Test
//	public void testGetSequence() {
//		Project project = new FileSystemBasedProject(new File("C:/temp/passerelle-repository"),"Project1");
//		
//		Flow flow = project.getSequence("HelloWorld.moml");
//		
//		Assert.assertNotNull("HelloWorld sequence must not be null", flow);
//		Assert.assertEquals("helloworld", flow.getName());
//	}
	
	public void _testProject(CommandInterpreter ci) {
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
        buffer.append("\ttestProject - test Project in Repo API \n");
        return buffer.toString();
	}

}
