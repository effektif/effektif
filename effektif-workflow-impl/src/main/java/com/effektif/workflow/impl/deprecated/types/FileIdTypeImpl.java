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
package com.effektif.workflow.impl.deprecated.types;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.deprecated.model.FileId;
import com.effektif.workflow.api.deprecated.types.FileIdType;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.deprecated.file.File;
import com.effektif.workflow.impl.deprecated.file.FileAttachment;
import com.effektif.workflow.impl.deprecated.file.FileService;



/**
 * @author Tom Baeyens
 */
public class FileIdTypeImpl extends AbstractDataType<FileIdType> {
  
  protected FileService fileService;
  protected AttachmentTypeImpl attachmentTypeImpl;
  protected FileTypeImpl fileTypeImpl;
  
  public FileIdTypeImpl() {
    super(FileIdType.INSTANCE, FileId.class);
  }
  
  @Override
  public void setConfiguration(Configuration configuration) {
    super.setConfiguration(configuration);
    this.attachmentTypeImpl = getSingletonDataType(AttachmentTypeImpl.class);
    this.fileTypeImpl = getSingletonDataType(FileTypeImpl.class);
  }
  
  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    return jsonValue!=null ? new FileId((String)jsonValue) : null;
  }

  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    return internalValue!=null ? ((FileId)internalValue).getInternal() : null;
  }
  
  @Override
  public TypedValueImpl dereference(Object value, String fieldName) {
    FileId fileId = (FileId) value;
    FileService fileService = configuration.get(FileService.class);
    File file = fileId!=null ? fileService.getFileById(fileId) : null;
    if ("*".equals(fieldName)) {
      return new TypedValueImpl(fileTypeImpl, file);
    } else if ("attachment".equals(fieldName)) {
      FileAttachment fileAttachment = FileAttachment.createFileAttachment(file, fileService);
      return new TypedValueImpl(attachmentTypeImpl, fileAttachment);
    }
    return fileTypeImpl.dereference(file, fieldName);
  }
}
