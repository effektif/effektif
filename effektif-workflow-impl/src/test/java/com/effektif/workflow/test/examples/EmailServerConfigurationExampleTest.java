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
package com.effektif.workflow.test.examples;

import org.junit.Test;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.impl.email.EmailServiceImpl;
import com.effektif.workflow.impl.memory.MemoryConfiguration;


/**
 * @author Tom Baeyens
 */
public class EmailServerConfigurationExampleTest {

  @Test
  public void testEmailServerConfiguration() {
    Configuration configuration = new MemoryConfiguration();
    configuration.get(EmailServiceImpl.class)
      // by default, localhost and port 25 are configured
      .host("smtp.gmail.com") // overwrite the default server
      .ssl() // also sets the port to the default ssl port 465 
      .tls() // also sets the port to the default tls port 587
      .connectionTimeoutSeconds(34523523l)
      .authenticate("youraccount@gmail.com", "***");
  }
}
