package com.effektif.workflow.impl.bpmn;

import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;

/**
 * Value object for the details of a file loaded by {@link ClasspathResourceResolver}.
 */
public class LoadInput implements LSInput {

  private Reader characterStream;
  private InputStream byteStream;
  private String stringData;
  private String systemId;
  private String publicId;
  private String baseURI;
  private String encoding;
  private boolean certifiedText;

  @Override
  public Reader getCharacterStream() {
    return characterStream;
  }
  @Override
  public void setCharacterStream(Reader characterStream) {
    this.characterStream = characterStream;
  }
  @Override
  public InputStream getByteStream() {
    return byteStream;
  }
  @Override
  public void setByteStream(InputStream byteStream) {
    this.byteStream = byteStream;
  }
  @Override
  public String getStringData() {
    return stringData;
  }
  @Override
  public void setStringData(String stringData) {
    this.stringData = stringData;
  }
  @Override
  public String getSystemId() {
    return systemId;
  }
  @Override
  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }
  @Override
  public String getPublicId() {
    return publicId;
  }
  @Override
  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }
  @Override
  public String getBaseURI() {
    return baseURI;
  }
  @Override
  public void setBaseURI(String baseURI) {
    this.baseURI = baseURI;
  }
  @Override
  public String getEncoding() {
    return encoding;
  }
  @Override
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
  @Override
  public boolean getCertifiedText() {
    return certifiedText;
  }
  @Override
  public void setCertifiedText(boolean certifiedText) {
    this.certifiedText = certifiedText;
  }
}
