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
package com.effektif.workflow.test.api;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import com.effektif.workflow.api.model.EmailAddress;
import com.effektif.workflow.api.model.FileId;
import com.effektif.workflow.api.model.Link;
import com.effektif.workflow.api.model.Money;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.types.DateType;
import com.effektif.workflow.api.types.EmailAddressType;
import com.effektif.workflow.api.types.FileIdType;
import com.effektif.workflow.api.types.LinkType;
import com.effektif.workflow.api.types.MoneyType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.types.UserIdType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.file.File;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class VariableTypesTest extends WorkflowTest {

  @Test
  public void testDateType() {
    Workflow workflow = new Workflow()
      .variable("v", new DateType());
    
    deploy(workflow);

    long time = new Date().getTime();

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", new LocalDateTime(time)));

    assertEquals(new LocalDateTime(time), workflowInstance.getVariableValueDate("v"));

    WorkflowInstanceId workflowInstanceId = workflowInstance.getId();
    
    Map<String, Object> variableValues = new HashMap<>();
    variableValues.put("v", new LocalDateTime(time));
    workflowEngine.setVariableValues(workflowInstanceId, variableValues);
    assertEquals(variableValues, new HashMap<String,Object>(workflowEngine.getVariableValues(workflowInstanceId)));
  }

  @Test
  public void testEmailAddressType() {
    Workflow workflow = new Workflow()
      .variable("v", new EmailAddressType());

    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance().workflowId(workflow.getId())
      .data("v", new EmailAddress("info@effektif.com")));

    Object value = workflowInstance.getVariableValue("v");
    assertEquals(EmailAddress.class, value.getClass());
    EmailAddress address = (EmailAddress) value;
    assertEquals("info@effektif.com", address.getValue());
  }

  @Test
  public void testNumberType() {
    Workflow workflow = new Workflow()
      .variable("v", new NumberType());

    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", 5));

    assertEquals(new Long(5), workflowInstance.getVariableValueLong("v"));

    WorkflowInstanceId workflowInstanceId = workflowInstance.getId();

    Map<String, Object> variableValues = new HashMap<>();
    variableValues.put("v", 6l);
    workflowEngine.setVariableValues(workflowInstanceId, variableValues);
    assertEquals(variableValues, new HashMap<String,Object>(workflowEngine.getVariableValues(workflowInstanceId)));
  }

  @Test
  public void testUserIdType() {
    Workflow workflow = new Workflow()
      .variable("v", new UserIdType());
    
    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", new UserId(JOHN_ID)));
    
    assertEquals(UserId.class, workflowInstance.getVariableValue("v").getClass());

    WorkflowInstanceId workflowInstanceId = workflowInstance.getId();

    Map<String, Object> variableValues = new HashMap<>();
    variableValues.put("v", new UserId(MARY_ID));
    workflowEngine.setVariableValues(workflowInstanceId, variableValues);
    assertEquals(variableValues, new HashMap<String,Object>(workflowEngine.getVariableValues(workflowInstanceId)));
  }

  @Test
  public void testLinkType() {
    Workflow workflow = new Workflow()
      .variable("v", new LinkType());
    
    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(
      new TriggerInstance().workflowId(workflow.getId())
        .data("v", new Link().name("Effektif").url("http://www.effektif.com/")));
    
    Object value = workflowInstance.getVariableValue("v");
    assertEquals(Link.class, value.getClass());
    Link link = (Link) value;
    assertEquals("Effektif", link.getName());
    assertEquals("http://www.effektif.com/", link.getUrl());
  }

  @Test
  public void testMoneyType() {
    Workflow workflow = new Workflow()
      .variable("v", new MoneyType());
    
    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", new Money().amount(5d).currency("USD")));
    
    Object value = workflowInstance.getVariableValue("v");
    assertEquals(Money.class, value.getClass());
    Money money = (Money) value;
    assertEquals(new Double(5d), money.getAmount());
    assertEquals("USD", money.getCurrency());
  }

  @Test
  public void testFileIdType() {
    Workflow workflow = new Workflow()
      .variable("v", new FileIdType());
    
    deploy(workflow);
    
    File file = createTestFile("blabla", "joke.txt", "text/plain");

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", file.getId()));
    
    Object value = workflowInstance.getVariableValue("v");
    assertEquals(FileId.class, value.getClass());
    assertEquals(file.getId().getInternal(), ((FileId)value).getInternal());
  }
}
