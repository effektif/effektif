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
package com.effektif.workflow.impl.activity.types;

import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.bpmn.ServiceTaskType;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


/**
 * A service task implemented in Java.
 *
 * BPMN XML: {@code <serviceTask id="sendMail" effektif:type="java">}
 *
 * @author Tom Baeyens
 */
public class JavaServiceTaskImpl extends AbstractActivityType<JavaServiceTask> {

  private static final String BPMN_ELEMENT_NAME = "serviceTask";

  public JavaServiceTaskImpl() {
    super(JavaServiceTask.class);
  }

  @Override
  public JavaServiceTask readBpmn(XmlElement xml, BpmnReader reader) {
    if (!reader.isLocalPart(xml, BPMN_ELEMENT_NAME) || !reader.hasServiceTaskType(xml, ServiceTaskType.JAVA)) {
      return null;
    }
    JavaServiceTask task = new JavaServiceTask();
    return task;
  }

  @Override
  public void writeBpmn(JavaServiceTask task, XmlElement xml, BpmnWriter writer) {
    writer.setBpmnName(xml, BPMN_ELEMENT_NAME);
    writer.writeBpmnAttribute(xml, "id", task.getId());
    writer.writeEffektifType(xml, ServiceTaskType.JAVA);
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
  }
}
