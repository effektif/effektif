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
package com.effektif.workflow.impl.memory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.effektif.workflow.api.model.FileId;
import com.effektif.workflow.impl.file.File;


/**
 * @author Tom Baeyens
 */
public class MemoryFile extends File {
  
  protected byte[] content;

  public byte[] getContent() {
    return this.content;
  }
  public void setContent(byte[] content) {
    this.content = content;
  }
  public MemoryFile content(byte[] content) {
    this.content = content;
    return this;
  }
  
  @Override
  public InputStream getInputStream() {
    return new ByteArrayInputStream(content);
  }
  
  @Override
  public MemoryFile id(FileId id) {
    super.id(id);
    return this;
  }
  @Override
  public MemoryFile fileName(String fileName) {
    super.fileName(fileName);
    return this;
  }
  @Override
  public MemoryFile contentType(String contentType) {
    super.contentType(contentType);
    return this;
  }
  @Override
  public MemoryFile sizeInBytes(Long sizeInBytes) {
    super.sizeInBytes(sizeInBytes);
    return this;
  }
}
