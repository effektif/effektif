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
package com.effektif.workflow.impl.memory;

import com.effektif.workflow.impl.DefaultConfiguration;


/**
 * A {@link java.util.Map}-based {@link com.effektif.workflow.api.Configuration}
 * that stores workflows and workflow instances in memory without serialisation or de-serialisation.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Workflow-engine-types#in-memory-workflow-engine">In-memory workflow engine</a>
 * @author Tom Baeyens
 */
public class MemoryConfiguration extends DefaultConfiguration {

  public MemoryConfiguration() {
    brewery.ingredient(new MemoryWorkflowStore());
    brewery.ingredient(new MemoryWorkflowInstanceStore());
    brewery.ingredient(new MemoryJobStore());
  }
}
