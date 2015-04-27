/*
 * Copyright 2014 Effektif GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.effektif.workflow.impl.deprecated.file;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.deprecated.model.FileId;
import com.effektif.workflow.api.deprecated.model.UserId;
import com.effektif.workflow.api.workflow.Extensible;


/**
 * @author Tom Baeyens
 */
public class File extends Extensible {
  
  public static final Set<String> INVALID_PROPERTY_KEYS = new HashSet<>(Arrays.asList(
          "id", "organizationId", "creatorId", "createTime", "fileName", "contentType",
          "sizeInBytes"));

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

  @Override
  protected void checkPropertyKey(String key) {
    super.checkPropertyKey(key, INVALID_PROPERTY_KEYS);
  }
}
