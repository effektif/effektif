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
package com.effektif.workflow.api.activities;

import com.effektif.workflow.api.deprecated.activities.ServiceTask;
import com.effektif.workflow.api.serialization.bpmn.BpmnElement;
import com.effektif.workflow.api.serialization.bpmn.BpmnTypeAttribute;
import com.effektif.workflow.api.serialization.json.TypeName;


/** 
 * invokes a java method.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Java-Service-Task">Java Service Task</a>
 * @author Tom Baeyens
 */
@TypeName("javaServiceTask")
@BpmnElement("serviceTask")
@BpmnTypeAttribute(attribute="type", value="java")
public class JavaServiceTask extends ServiceTask {

}
