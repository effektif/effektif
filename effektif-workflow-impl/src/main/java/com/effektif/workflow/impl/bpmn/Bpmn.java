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
package com.effektif.workflow.impl.bpmn;

/**
 * Constants for fixed string values used by both the {@link BpmnReaderImpl} and {@link BpmnWriterImpl}.
 */
public class Bpmn {

  public static final String BPMN_URI = "http://www.omg.org/spec/BPMN/20100524/MODEL";
  public static final String BPMN_DI_URI = "http://www.omg.org/spec/BPMN/20100524/DI";
  public static final String EFFEKTIF_URI = "effektif.com:1";

  public static final String OMG_DI_URI = "http://www.omg.org/spec/DD/20100524/DI";
  public static final String OMG_DC_URI = "http://www.omg.org/spec/DD/20100524/DC";

  public static final String KEY_BPMN = "bpmn";
  public static final String KEY_DEFINITIONS = "bpmnDefinitions";
}
