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
package com.effektif.workflow.impl.bpmn;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWritable;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.workflow.AbstractWorkflow;
import com.effektif.workflow.impl.bpmn.xml.XmlReader;
import com.effektif.workflow.impl.bpmn.xml.XmlWriter;
import com.effektif.workflow.impl.json.DefaultJsonStreamMapper;
import com.effektif.workflow.impl.json.JsonStreamMapper;

/**
 * A facade for API object BPMN serialisation and deserialisation,
 * using the {@link BpmnWriter} and {@link BpmnReader} APIs.
 *
 * @author Tom Baeyens
 */
public class BpmnMapper {

  protected BpmnMappings bpmnMappings;
  protected JsonStreamMapper jsonStreamMapper;
  
  public static BpmnMapper createBpmnMapperForTest() {
    return new BpmnMapper(new DefaultJsonStreamMapper());
  }

  public BpmnMapper(JsonStreamMapper jsonStreamMapper) {
    this.bpmnMappings = new BpmnMappings(jsonStreamMapper.getMappings());
    this.jsonStreamMapper = jsonStreamMapper;
  }

  public AbstractWorkflow readFromString(String bpmnString) {
    return readFromReader(new StringReader(bpmnString));
  }

  public AbstractWorkflow readFromString(String bpmnString, BpmnReaderImpl bpmnReader) {
    return readFromReader(new StringReader(bpmnString), bpmnReader);
  }

  public AbstractWorkflow readFromReader(Reader reader) {
    return readFromReader(reader, createBpmnReaderImpl());
  }

  public AbstractWorkflow readFromReader(Reader reader, BpmnReaderImpl bpmnReader) {
    XmlElement xmlRoot = XmlReader.parseXml(reader);
    return bpmnReader.readDefinitions(xmlRoot);
  }

  protected BpmnReaderImpl createBpmnReaderImpl() {
    return new BpmnReaderImpl(bpmnMappings, jsonStreamMapper);
  }

  public void writeToStream(AbstractWorkflow workflow, OutputStream out) {
    XmlElement bpmnDefinitions = new BpmnWriterImpl(bpmnMappings)
      .writeDefinitions(workflow);
    
    XmlWriter xmlWriter = new XmlWriter(out, "UTF-8");
    xmlWriter.writeDocument(bpmnDefinitions);
    xmlWriter.flush();
  }

  public String writeToString(AbstractWorkflow workflow) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    writeToStream(workflow, stream);
    return stream.toString();
  }

  /**
   * Work in progress for testing conditions.
   */
  public <T extends Condition> T readCondition(String xml, Class<T> conditionClass) {
    XmlElement xmlRoot = XmlReader.parseXml(new StringReader(xml));
    if (xmlRoot != null && xmlRoot.elements != null) {
      try {
        BpmnReaderImpl reader = createBpmnReaderImpl();
        reader.currentXml = xmlRoot;
        return (T) reader.readCondition();
      } catch (Exception e) {
        throw new RuntimeException("Could not read condition: " + e.getMessage(), e);
      }
    }
    else {
      return null;
    }
  }

  /**
   * Work in progress for testing conditions.
   */
  public String writeToString(BpmnWritable model) {
    BpmnWriterImpl writer = new BpmnWriterImpl(bpmnMappings);
    writer.startElementBpmn("conditionsTest");
    model.writeBpmn(writer);

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    XmlWriter xmlWriter = new XmlWriter(stream, "UTF-8");
    xmlWriter.writeDocument(writer.xml);
    xmlWriter.flush();
    return stream.toString();
  }
}
