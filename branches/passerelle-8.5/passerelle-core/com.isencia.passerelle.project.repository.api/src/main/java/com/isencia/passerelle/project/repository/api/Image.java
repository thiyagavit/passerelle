package com.isencia.passerelle.project.repository.api;

public class Image {
  public Image(byte[] data, String extennsion) {
    super();
    this.data = data;
    this.extennsion = extennsion;
  }

  private byte[] data;
  private String extennsion;

  public byte[] getData() {
    return data;
  }

  public String getExtennsion() {
    return extennsion;
  }

}
