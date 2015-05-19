/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */
package com.effektif.email.file;

import com.effektif.email.Attachment;

import java.io.InputStream;


/**
 * @author Tom Baeyens
 */
public class FileAttachment implements Attachment {
  
  protected File file;
  protected InputStream inputStream;
  
  protected FileAttachment() {
  }

  protected FileAttachment(File file, InputStream inputStream) {
    super();
    this.file = file;
    this.inputStream = inputStream;
  }

  public static FileAttachment createFileAttachment(File file, FileService fileService) {
    String fileStreamId = file!=null ? file.getStreamId() : null;
    if (fileStreamId==null) {
      return null;
    }
    InputStream inputStream = fileService.getFileStream(file.getStreamId());
    if (inputStream==null) {
      return null;
    }
    return new FileAttachment(file, inputStream);
  }

  @Override
  public String getFileName() {
    return file.getFileName();
  }

  @Override
  public String getContentType() {
    return file.getContentType();
  }

  @Override
  public InputStream getInputStream() {
    return inputStream;
  }

}
