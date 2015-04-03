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

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.mapper.BpmnMappings;
import com.effektif.workflow.impl.bpmn.Bpmn;


/**
 * @author Tom Baeyens
 */
public class BpmnMappingsImpl implements BpmnMappings {
  
  Map<String,BpmnMapping> bpmnMappings = new HashMap<>();
  
  @Override
  public void mapToBpmn(String fieldName, String bpmnName) {
    bpmnMappings.put(fieldName, new BpmnMapping(bpmnName, Bpmn.BPMN_URI));
  }

  @Override
  public void mapToEffektif(String fieldName, String effektifName) {
    bpmnMappings.put(fieldName, new BpmnMapping(effektifName, Bpmn.EFFEKTIF_URI));
  }

}
