package com.isencia.passerelle.runtime.ws.rest;

import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="codes")
@XmlAccessorType(XmlAccessType.FIELD)
public class CodeList {
  
  @XmlElement(name="code")
  private List<String> codes;
  
  public CodeList() {
  }

  public CodeList(String... codes) {
    this.codes = Arrays.asList(codes);
  }

  public List<String> getCodes() {
    return codes;
  }
}
