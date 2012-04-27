package com.isencia.passerelle.validation;

import junit.framework.TestCase;

public class VersionSpecificationTest extends TestCase {

  public void testParseAndToString() {
    String VERSION_SPEC = "1.2.3_hello-world";
    VersionSpecification version = VersionSpecification.parse(VERSION_SPEC);
    assertNotNull("Version should parse correctly and be not-null",version);
    assertEquals("Version should have original string specification", VERSION_SPEC, version.toString());
  }

  public void testParseAndMajor() {
    String VERSION_SPEC = "1.2.3_hello-world";
    VersionSpecification version = VersionSpecification.parse(VERSION_SPEC);
    assertEquals("Version major not correctly parsed", 1, version.getMajor());
  }

  public void testParseAndMinor() {
    String VERSION_SPEC = "1.2.3_hello-world";
    VersionSpecification version = VersionSpecification.parse(VERSION_SPEC);
    assertEquals("Version minor not correctly parsed", 2, version.getMinor());
  }

  public void testParseAndMicro() {
    String VERSION_SPEC = "1.2.3_hello-world";
    VersionSpecification version = VersionSpecification.parse(VERSION_SPEC);
    assertEquals("Version micro not correctly parsed", 3, version.getMicro());
  }

  public void testEquality() {
    VersionSpecification version1 = VersionSpecification.parse("1.2.3_hello-world");
    VersionSpecification version2 = VersionSpecification.parse("1.2.3_hello-world");
    
    assertEquals(version1, version2);
  }

  public void testNotEqual() {
    VersionSpecification version1 = VersionSpecification.parse("1.2.3_hello-world");
    VersionSpecification version2 = VersionSpecification.parse("2.2.3");
    
    assertFalse("1.2.3_hello-world should not be equal to 2.2.3", version1.equals(version2));
  }

  public void testCompareMajorDiff() {
    VersionSpecification version1 = VersionSpecification.parse("1.2.3_hello-world");
    VersionSpecification version2 = VersionSpecification.parse("2.2.3");
    
    assertTrue("1.2.3_hello-world must be smaller than 2.2.3", version1.compareTo(version2)<0);
  }

  public void testCompareMinorDiff() {
    VersionSpecification version1 = VersionSpecification.parse("1.2.3_hello-world");
    VersionSpecification version2 = VersionSpecification.parse("1.3.3");
    
    assertTrue("1.2.3_hello-world must be smaller than 1.3.3", version1.compareTo(version2)<0);
  }

  public void testCompareMicroDiff() {
    VersionSpecification version1 = VersionSpecification.parse("1.2.3_hello-world");
    VersionSpecification version2 = VersionSpecification.parse("1.2.4");
    
    assertTrue("1.2.3_hello-world must be smaller than 1.2.4", version1.compareTo(version2)<0);
  }

  public void testCompareQualifierDiff() {
    VersionSpecification version1 = VersionSpecification.parse("1.2.3_hello-world");
    VersionSpecification version2 = VersionSpecification.parse("1.2.3_hello-world2");
    
    assertTrue("1.2.3_hello-world must be smaller than 1.2.3_hello-world2", version1.compareTo(version2)<0);
  }

}
