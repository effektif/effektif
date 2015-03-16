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
import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.acl.Authentication;
import com.effektif.workflow.api.acl.Authentications;
import com.effektif.workflow.api.model.FileId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.impl.exceptions.BadRequestException;
import com.effektif.workflow.impl.exceptions.NotFoundException;
import com.effektif.workflow.impl.file.File;
import com.effektif.workflow.impl.file.FileService;
import com.effektif.workflow.impl.util.Streams;
import com.effektif.workflow.impl.util.Time;


/**
 * @author Tom Baeyens
 */
public class MemoryFileService implements FileService {
  
  Map<FileId,File> files = new HashMap<>();
  Map<String,byte[]> fileStreams = new HashMap<>();
  long nextFileId = 1;
  long nextFileStreamId = 1;
  
  @Override
  public File createFile(File file, InputStream fileStream) {
    initializeNewFile(file);
    
    String fileStreamId = file.getId().getInternal();
    byte[] stream = Streams.read(fileStream);
    fileStreams.put(fileStreamId, stream);
    file.setStreamId(fileStreamId);
    
    files.put(file.getId(), file);
    
    return file;
  }

  protected void initializeNewFile(File file) {
    Authentication authentication = Authentications.current();
    String organizationId = authentication!=null ? authentication.getOrganizationId() : null;
    if (organizationId!=null) {
      file.setOrganizationId(organizationId);
    }
    String userId = authentication!=null ? authentication.getUserId() : null;
    if (userId!=null) {
      file.creatorId(new UserId(userId));
    }

    String fileIdString = Long.toString(nextFileId++);
    FileId fileId = new FileId(fileIdString);
    file.setId(fileId);

    file.createTime(Time.now());
  }

  @Override
  public File createFile(File file) {
    if (file.getStreamId()==null) {
      throw new BadRequestException("file requires a fileStreamId");
    }

    initializeNewFile(file);
    
    files.put(file.getId(), file);

    return file;
  }

  @Override
  public File getFileById(FileId fileId) {
    return files.get(fileId);
  }

  @Override
  public InputStream getFileStream(String fileStreamId) {
    byte[] bytes = fileStreams.get(fileStreamId);
    if (bytes==null) {
      throw new NotFoundException("File '"+fileStreamId+" doesn't exist");
    }
    return new ByteArrayInputStream(bytes);
  }
}
