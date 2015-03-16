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
package com.effektif.mongo;

import java.io.InputStream;
import java.util.Map;

import com.effektif.workflow.api.model.FileId;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.file.File;
import com.effektif.workflow.impl.file.FileService;
import com.effektif.workflow.impl.util.Exceptions;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;


/**
 * @author Tom Baeyens
 */
public class MongoFileService implements FileService, Brewable {

  public GridFS gridFs;
  
  @Override
  public void brew(Brewery brewery) {
    this.gridFs = brewery.get(GridFS.class);
  }
  
  @Override
  public File createFile(File file, InputStream fileStream) {
    Exceptions.checkNotNullParameter(file, "file");
    Exceptions.checkNotNullParameter(fileStream, "file.inputStream");
    
    GridFSInputFile gridFsFile = gridFs.createFile(fileStream);
    gridFsFile.setContentType(file.getContentType());
    gridFsFile.setFilename(file.getFileName());
    if (file.getProperties()!=null) {
      for (Map.Entry<String, Object> property: file.getProperties().entrySet()) {
        gridFsFile.put(property.getKey(), property.getValue());
      }
    }
    gridFsFile.save();
    
    file.setId(new FileId(gridFsFile.getId().toString()));

    return file;
  }

  @Override
  public InputStream getFileStream(String fileStreamId) {
    return null;
  }
  
  @Override
  public File getFileById(FileId fileId) {
    return null;
  }

  @Override
  public File createFile(File file) {
    return null;
  }
}
