/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */
package com.effektif.email.file;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.deprecated.model.FileId;
import com.effektif.workflow.api.deprecated.model.UserId;
import com.effektif.workflow.api.workflow.Extensible;


/**
 * @author Tom Baeyens
 */
public class File extends Extensible {
  
  protected FileId id;
  protected String organizationId;
  protected UserId creatorId;
  protected LocalDateTime createTime;
  protected String fileName;
  protected String contentType;
  protected Long sizeInBytes;
  protected String streamId;
  protected Map<String,String> headers;

  public File() {
  }
  
  /** shallow copy */
  public File(File other) {
    this.id = other.id;
    this.organizationId = other.organizationId;
    this.creatorId = other.creatorId;
    this.createTime = other.createTime;
    this.fileName = other.fileName;
    this.contentType = other.contentType;
    this.sizeInBytes = other.sizeInBytes;
  }
  
  public FileId getId() {
    return this.id;
  }
  public void setId(FileId id) {
    this.id = id;
  }
  public File id(FileId id) {
    this.id = id;
    return this;
  }
  public File id(String id) {
    this.id = new FileId(id);
    return this;
  }

  public String getOrganizationId() {
    return this.organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  
  public UserId getCreatorId() {
    return this.creatorId;
  }
  public void setCreatorId(UserId creatorId) {
    this.creatorId = creatorId;
  }
  public File creatorId(UserId creatorId) {
    this.creatorId = creatorId;
    return this;
  }
  public File ownerId(String creatorId) {
    this.creatorId = new UserId(creatorId);
    return this;
  }
  
  public LocalDateTime getCreateTime() {
    return this.createTime;
  }
  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }
  public File createTime(LocalDateTime createTime) {
    this.createTime = createTime;
    return this;
  }

  public String getFileName() {
    return this.fileName;
  }
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  public File fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  public String getContentType() {
    return this.contentType;
  }
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
  public File contentType(String contentType) {
    this.contentType = contentType;
    return this;
  }
  
  public Long getSizeInBytes() {
    return this.sizeInBytes;
  }
  public void setSizeInBytes(Long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }
  public File sizeInBytes(Long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
    return this;
  }

  public String getStreamId() {
    return this.streamId;
  }
  public void setStreamId(String streamId) {
    this.streamId = streamId;
  }
  public File streamId(String streamId) {
    this.streamId = streamId;
    return this;
  }

  public Map<String,String> getHeaders() {
    return this.headers;
  }
  public void setHeaders(Map<String,String> headers) {
    this.headers = headers;
  }
  public File header(String headerName, String headerValue) {
    if (this.headers==null) {
      this.headers = new HashMap<>();
    };
    headers.put(headerName, headerValue);
    return this;
  }
}
