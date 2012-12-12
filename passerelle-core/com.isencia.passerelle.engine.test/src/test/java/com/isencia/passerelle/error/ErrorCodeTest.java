package com.isencia.passerelle.error;

import com.isencia.passerelle.core.ErrorCategory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.ErrorCode.Severity;
import junit.framework.TestCase;

public class ErrorCodeTest extends TestCase {

  @Override
  protected void tearDown() throws Exception {
    TestErrorCode.clear();
  }
  
  public void testErrorCodeConstruction() {
    TestErrorCode TESTERRCODE = new TestErrorCode("TESTERRCODE", "1234", ErrorCategory.PASS_FUNCTIONAL, Severity.ERROR, "nothing special");
    assertEquals(TESTERRCODE, ErrorCode.valueOf("TESTERRCODE"));
  }
  
  public void testErrorCodeAutoTopic() {
    TestErrorCode TESTERRCODE = new TestErrorCode("TESTERRCODE", "1234", ErrorCategory.PASS_FUNCTIONAL, Severity.ERROR, "nothing special");
    assertEquals("FUNC/ERROR", TESTERRCODE.getTopic());
    
    System.out.println(TESTERRCODE);
  }
  
  public void testErrorCodeSpecificTopic() {
    TestErrorCode TESTERRCODE = new TestErrorCode("TESTERRCODE", "1234", "myTopic", ErrorCategory.PASS_FUNCTIONAL, Severity.ERROR, "nothing special");
    assertEquals("myTopic", TESTERRCODE.getTopic());
  }
  

  public void testErrorCodeInvalidCodeTooLong() {
    try {
      new TestErrorCode("TESTERRCODE", "12345", ErrorCategory.PASS_FUNCTIONAL, Severity.ERROR, "nothing special");
      fail("Invalid error code 12345 must be refused");
    } catch (AssertionError e) {
    }
  }
  
  public void testErrorCodeInvalidCodeTooShort() {
    try {
      new TestErrorCode("TESTERRCODE", "123", ErrorCategory.PASS_FUNCTIONAL, Severity.ERROR, "nothing special");
      fail("Invalid error code 123 must be refused");
    } catch (AssertionError e) {
    }
  }
  
  public void testErrorCodeInvalidCodeAlphaNumeric() {
    try {
      new TestErrorCode("TESTERRCODE", "12C4", ErrorCategory.PASS_FUNCTIONAL, Severity.ERROR, "nothing special");
      fail("Invalid error code 12C4 must be refused");
    } catch (AssertionError e) {
    }
  }
}
