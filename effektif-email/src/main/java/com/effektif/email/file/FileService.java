/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */
package com.effektif.email.file;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import com.effektif.workflow.api.deprecated.model.FileId;


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

  /** retrieves the information for files by a group of id.  */
  List<File> getFilesByIds(Collection<FileId> fileIds);

  /** retrieves the file content by {@link File#getStreamId()}. */
  InputStream getFileStream(String fileStreamId);

}
