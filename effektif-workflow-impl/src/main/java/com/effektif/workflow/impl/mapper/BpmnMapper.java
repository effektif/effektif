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
package com.effektif.workflow.impl.mapper;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;

import com.effektif.workflow.api.mapper.XmlElement;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.bpmn.xml.XmlReader;
import com.effektif.workflow.impl.bpmn.xml.XmlWriter;




/**
 * @author Tom Baeyens
 */
public class BpmnMapper extends AbstractMapper {
  
//  protected DataTypeService dataTypeService;
//  
//  public DataTypeService getDataTypeService() {
//    return dataTypeService;
//  }
//  
//  public void setDataTypeService(DataTypeService dataTypeService) {
//    this.dataTypeService = dataTypeService;
//  }

  public Workflow readFromString(String bpmnString) {
    return readFromReader(new StringReader(bpmnString));
  }

  public Workflow readFromReader(java.io.Reader reader) {
    XmlElement xmlRoot = XmlReader.parseXml(reader);
    return new BpmnReaderImpl(mappings).readDefinitions(xmlRoot);
  }

  public void writeToStream(Workflow workflow, OutputStream out) {
    XmlElement bpmnDefinitions = new BpmnWriterImpl(mappings).writeDefinitions(workflow);
    XmlWriter xmlWriter = new XmlWriter(out, "UTF-8");
    xmlWriter.writeDocument(bpmnDefinitions);
    xmlWriter.flush();
  }

  public String writeToString(Workflow workflow) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    writeToStream(workflow, stream);
    return stream.toString();
  }
}
