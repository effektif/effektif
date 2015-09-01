package com.effektif.workflow.test.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.effektif.workflow.api.model.Money;
import com.effektif.workflow.test.WorkflowTest;

/**
 * @author Tom Baeyens
 */
public class MoneyTest extends WorkflowTest {

  @Test
  public void testMoneyToString() {
    assertEquals("5.35 EUR", new Money()
      .amount(5.35)  
      .currency("EUR")
      .toString());

    assertEquals("-5.35 EUR", new Money()
      .amount(-5.35d)  
      .currency("EUR")
      .toString());

    assertEquals("1234567.89 EUR", new Money()
      .amount(1234567.893333)  
      .currency("EUR")
      .toString());
  }
}
