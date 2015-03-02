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

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.model.FileId;
import com.effektif.workflow.impl.file.File;
import com.effektif.workflow.impl.file.FileService;


/**
 * @author Tom Baeyens
 */
public class MemoryFileService implements FileService {
  
  Map<FileId,MemoryFile> files = new HashMap<>();

  public MemoryFileService addFile(String fileId, byte[] content, String fileName, String contentType) {
    addFile(new MemoryFile()
      .id(new FileId(fileId))
      .content(content)
      .fileName(fileName)
      .contentType(contentType));
    return this;
  }
  
  public MemoryFileService addFile(MemoryFile memoryFile) {
    files.put(memoryFile.getId(), memoryFile);
    return this;
  }
  
  @Override
  public File getFile(FileId fileId) {
    return files.get(fileId);
  }
}
