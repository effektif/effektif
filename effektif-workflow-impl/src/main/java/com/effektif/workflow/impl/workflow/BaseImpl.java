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
package com.effektif.workflow.impl.workflow;

import com.effektif.workflow.api.workflow.Base;
import com.effektif.workflow.impl.WorkflowEngineImpl;


/**
 * @author Tom Baeyens
 */
public class BaseImpl {

  public String id;
  public ScopeImpl parent;
  public WorkflowEngineImpl workflowEngine;
  public WorkflowImpl workflow;

  public void parse(Base apiBase, WorkflowParser parser, ScopeImpl parent) {
    this.id = apiBase.getId();
    this.workflowEngine = parser.workflowEngine;
    if (parent!=null) {
      this.parent = parent;
      this.workflow = parent.workflow;
    }
  }
}
