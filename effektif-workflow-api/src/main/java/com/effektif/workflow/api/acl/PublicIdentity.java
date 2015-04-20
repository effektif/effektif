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
package com.effektif.workflow.api.acl;

import com.effektif.workflow.api.mapper.BpmnReader;
import com.effektif.workflow.api.mapper.BpmnWriter;
import com.effektif.workflow.api.mapper.TypeName;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/** TODO check if this can be removed
 * 
 * refers to anyone, inside or outside the organization. 
 * Used to allow everyone to start a workflow that has access control. 
 * 
 * @author Tom Baeyens
 */
@TypeName("public")
@Deprecated 
public class PublicIdentity extends AccessIdentity {

  @Override
  public void readBpmn(BpmnReader r) {
    throw new NotImplementedException();
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    throw new NotImplementedException();
  }
}
