/* Copyright (c) 2014, Effektif GmbH.
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
 * limitations under the License. */
package com.effektif.workflow.impl.file;

import com.effektif.workflow.api.model.Attachment;
import com.effektif.workflow.api.model.FileId;
import com.effektif.workflow.api.model.UserId;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractFile implements File, Attachment {

  protected FileId id;
  protected String organizationId;
  protected UserId ownerId;
  protected String fileName;
  protected String contentType;
  protected Long sizeInBytes;

  public FileId getId() {
    return this.id;
  }
  public void setId(FileId id) {
    this.id = id;
  }
  public AbstractFile id(FileId id) {
    this.id = id;
    return this;
  }

  public String getOrganizationId() {
    return this.organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  public AbstractFile organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  public UserId getOwnerId() {
    return this.ownerId;
  }
  public void setOwnerId(UserId ownerId) {
    this.ownerId = ownerId;
  }
  public AbstractFile ownerId(UserId ownerId) {
    this.ownerId = ownerId;
    return this;
  }
  public AbstractFile ownerId(String ownerId) {
    this.ownerId = new UserId(ownerId);
    return this;
  }

  public String getFileName() {
    return this.fileName;
  }
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  public AbstractFile fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  public String getContentType() {
    return this.contentType;
  }
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
  public AbstractFile contentType(String contentType) {
    this.contentType = contentType;
    return this;
  }
  
  public Long getSizeInBytes() {
    return this.sizeInBytes;
  }
  public void setSizeInBytes(Long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }
  public AbstractFile sizeInBytes(Long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
    return this;
  }
}
