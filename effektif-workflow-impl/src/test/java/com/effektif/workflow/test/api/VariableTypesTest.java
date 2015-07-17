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

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import com.effektif.workflow.api.json.GenericType;
import com.effektif.workflow.api.model.Money;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.model.VariableValues;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.types.DateType;
import com.effektif.workflow.api.types.EmailAddressType;
import com.effektif.workflow.api.types.JavaBeanType;
import com.effektif.workflow.api.types.LinkType;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.MoneyType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.util.Lists;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class VariableTypesTest extends WorkflowTest {

  @Test
  public void testDateType() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("v", new DateType());
    
    deploy(workflow);

    LocalDateTime value = new LocalDateTime();
    
    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", value));

    assertEquals(value, workflowInstance.getVariableValue("v"));
  }

  @Test
  public void testDateTypeSetVariables() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("v", new DateType());
    
    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId()));
    WorkflowInstanceId workflowInstanceId = workflowInstance.getId();

    LocalDateTime value = new LocalDateTime();

    VariableValues variableValues = new VariableValues();
    variableValues.value("v", value);

    workflowEngine.setVariableValues(workflowInstanceId, variableValues);

    VariableValues retrieved = workflowEngine.getVariableValues(workflowInstanceId);
    
    assertEqualsVariableValue("v", variableValues, retrieved);
  }

  @Test
  public void testListOfDatesType() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("v", new ListType(new DateType()));
    
    deploy(workflow);

    long time1 = new Date().getTime();
    long time2 = time1+1;

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", Lists.of(new LocalDateTime(time1), new LocalDateTime(time2))));

    List<LocalDateTime> variableValue = (List<LocalDateTime>) workflowInstance.getVariableValue("v");
    assertEquals(new LocalDateTime(time1), variableValue.get(0));
    assertEquals(new LocalDateTime(time2), variableValue.get(1));
  }

  @Test
  public void testEmailAddressType() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("v", new EmailAddressType());

    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance().workflowId(workflow.getId())
      .data("v", "info@effektif.com"));

    Object value = workflowInstance.getVariableValue("v");
    assertEquals("info@effektif.com", value);
  }

  @Test
  public void testListOfEmailAddressesType() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("v", new ListType(new EmailAddressType()));

    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance().workflowId(workflow.getId())
      .data("v", Lists.of("info@effektif.com", "sales@effektif.com")));

    List<String> value = (List<String>) workflowInstance.getVariableValue("v");
    assertEquals("info@effektif.com", value.get(0));
    assertEquals("sales@effektif.com", value.get(1));

    value = (List<String>)workflowInstance.getVariableValue("v");
    assertEquals("info@effektif.com", value.get(0));
    assertEquals("sales@effektif.com", value.get(1));
  }

  @Test
  public void testNumberType() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("v", new NumberType());

    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", 5));

    assertEquals(new Long(5), workflowInstance.getVariableValue("v", Long.class));
  }

  @Test
  public void testListOfNumbersType() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("v", new ListType(new NumberType()));

    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", Lists.of(5, 6)));

    GenericType genericType = new GenericType(List.class, Long.class);
    List<Long> listOfNumbers = workflowInstance.getVariableValue("v", genericType);
    assertEquals(new Long(5), listOfNumbers.get(0));
    assertEquals(new Long(6), listOfNumbers.get(1));
  }

  @Test
  public void testLinkType() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("v", new LinkType());
    
    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(
      new TriggerInstance().workflowId(workflow.getId())
        .data("v", "http://www.effektif.com/"));
    
    Object link = workflowInstance.getVariableValue("v");
    assertEquals("http://www.effektif.com/", link);
  }

  @Test
  public void testListOfLinkType() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("v", new ListType(new LinkType()));
    
    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(
      new TriggerInstance().workflowId(workflow.getId())
        .data("v", Lists.of(
                "http://effektif.com/",
                "http://signavio.com/")));
    
    List<String> links = (List<String>) workflowInstance.getVariableValue("v");
    assertEquals("http://effektif.com/", links.get(0));
    assertEquals("http://signavio.com/", links.get(1));
  }

  @Test
  public void testMoneyType() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("v", new MoneyType());
    
    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", new Money().amount(5d).currency("USD")));
    
    Money money = workflowInstance.getVariableValue("v", Money.class);
    assertEquals(new Double(5d), money.getAmount());
    assertEquals("USD", money.getCurrency());
  }

  @Test
  public void testListOfMoneyType() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("v", new ListType(new MoneyType()));
    
    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", Lists.of(
              new Money().amount(5d).currency("USD"),
              new Money().amount(6d).currency("EUR"))));
    
    List<Money> moneys = workflowInstance.getVariableValue("v");
    assertEquals(new Double(5d), moneys.get(0).getAmount());
    assertEquals("USD", moneys.get(0).getCurrency());
    assertEquals(new Double(6d), moneys.get(1).getAmount());
    assertEquals("EUR", moneys.get(1).getCurrency());
  }

  public static class MyBean {
    String name;
    List<String> values;
  }
  
  @Test
  public void testJavaBeanType() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("v", new JavaBeanType().javaClass(MyBean.class));
    
    deploy(workflow);
    
    MyBean myBean = new MyBean();

    WorkflowInstance workflowInstance = start(createTriggerInstance(workflow)
      .data("v", myBean));
    
    MyBean retrievedBean = workflowInstance.getVariableValue("v");
    assertNotNull(retrievedBean);
  }

  protected void assertEqualsVariableValue(String variableId, VariableValues expected, VariableValues actual) {
    assertEquals(expected.getValue(variableId), actual.getValue(variableId));
  }
}
