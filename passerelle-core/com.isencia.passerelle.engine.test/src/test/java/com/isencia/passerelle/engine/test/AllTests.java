package com.isencia.passerelle.engine.test;

import com.isencia.librarybuilder.LibraryBuilderTrial;
import com.isencia.passerelle.actor.ActorApiTest;
import com.isencia.passerelle.validation.ModelValidationServiceTest;
import com.isencia.passerelle.validation.VersionSpecificationTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(ActorApiTest.class);
    suite.addTestSuite(LibraryBuilderTrial.class);
    suite.addTestSuite(ModelValidationServiceTest.class);
    suite.addTestSuite(VersionSpecificationTest.class);
    //$JUnit-END$
    return suite;
  }

}
