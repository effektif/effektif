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
package com.effektif.workflow.impl.data.types;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.JavaBeanType;
import com.effektif.workflow.impl.file.File;
import com.effektif.workflow.impl.file.FileService;


/**
 * @author Tom Baeyens
 */
public class FileTypeImpl extends JavaBeanTypeImpl<JavaBeanType> {
  
  FileService fileService;

  public FileTypeImpl() {
  }
  public void initialize(Configuration configuration) {
    initialize(new JavaBeanType(File.class), File.class, configuration);
    this.fileService = configuration.get(FileService.class);
  }

  public FileTypeImpl(Configuration configuration) {
    initialize(configuration);
  }

  public FileTypeImpl(JavaBeanType typeApi, Configuration configuration) {
    super(typeApi, configuration);
  }
}
