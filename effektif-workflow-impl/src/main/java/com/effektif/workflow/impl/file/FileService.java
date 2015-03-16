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

import java.io.InputStream;

import com.effektif.workflow.api.model.FileId;


/** manages files and file streams.
 * 
 * A 'File' has all the information about a file.  It also acts 
 * as a flyweight around the file streams.  The purpose of splitting 
 * files from their stream is to support multiple files could potentially 
 * point to the same file stream.
 * 
 * @author Tom Baeyens
 */
public interface FileService {

  /** inserts the file stream and the file in one convenient method. */
  File createFile(File file, InputStream fileStream);

  /** used for creating a new file with an existing fileStreamId.
   * The fileStreamId property has to point to an existing file.  */
  File createFile(File file);

  /** retrieves the file information by id.  */
  File getFileById(FileId fileId);

  /** retrieves the file content by {@link File#getStreamId()}. */
  InputStream getFileStream(String fileStreamId);
}
