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
package com.effektif.workflow.test.configuration;

import com.effektif.workflow.impl.configuration.Brewery;
import org.hamcrest.core.StringContains;
import org.junit.Test;

import static org.junit.Assert.*;


public class BreweryBasicsTest {

  @Test
  public void testRetrieveAnIngredient() {
    Brewery brewery = new Brewery();

    // Put h2o as an ingredient named water into the brewery 
    Object h2o = "h2o";
    brewery.ingredient(h2o, "water");

    // Get water from the brewery
    // No initialization required
    assertSame(h2o, brewery.get("water"));
    
    // Getting the water again produces the exact same object
    assertSame(h2o, brewery.get("water"));
  }

  @Test
  public void testLookupByClassname() {
    Brewery brewery = new Brewery();

    Object h2o = "h2o";
    brewery.ingredient(h2o);
    assertSame(h2o, brewery.get(String.class));
  }

  @Test
  public void testRequiredUnavailable() {
    Brewery brewery = new Brewery();

    try {
      brewery.get("unavailable");
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertThat(e.getMessage(), new StringContains("Unknown component name: 'unavailable'"));
    }
  }

  @Test
  public void testOptionalUnavailable() {
    Brewery brewery = new Brewery();
    assertNull(brewery.getOpt("unavailable"));
  }
}
