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
package com.effektif.workflow.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.Configuration;


/**
 * @author Tom Baeyens
 */
public class TestSuiteHelper {
  
  private static final Logger log = LoggerFactory.getLogger(TestSuiteHelper.class);
  
  public static final Class<?>[] API_TEST_CLASSES = scanTestClasses();
  
  public static void run(Configuration configuration) {
    run(configuration, null, null);
  }

  public static void run(Configuration configuration, Class<?> clazz, String methodName) {
    try {
      Request request = null;
      if (clazz!=null && methodName!=null) {
        request = Request.method(clazz, methodName);
      } else {
        Suite suite = new Suite(new JUnit4Builder(), API_TEST_CLASSES);
        request = Request.runner(suite);
      }
      
      Configuration originalConfiguration = WorkflowTest.cachedConfiguration;
      WorkflowTest.cachedConfiguration = configuration;

      JUnitCore junitCore = new JUnitCore();
      Result result = junitCore.run(request);

      WorkflowTest.cachedConfiguration = originalConfiguration;
      
      checkResult(result);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public static void checkResult(Result result) {
    if (!result.wasSuccessful()) {
      StringWriter msgWriter = new StringWriter();
      PrintWriter out = new PrintWriter(msgWriter);
      int index=1;
      for (Failure failure: result.getFailures()) {
        out.println(index+". "+failure.getDescription().getTestClass().getSimpleName()+".class, \""+failure.getDescription().getMethodName()+"\"");
        out.println(failure.getDescription());
        if (failure.getException()!=null) {
          out.println("EXCEPTION ");
          failure.getException().printStackTrace(out);
        }
        out.println("-------------------------------------------------------- ");
        index++;
      }
      out.flush();
      String msg  = result.getFailureCount()+" failures:\n"+msgWriter.toString();
      log.error(msg);
      Assert.fail(msg);
    }
  }
  
  public static Class<?>[] scanTestClasses() {
    Reflections reflections = new Reflections("com.effektif.workflow.test.api");
    Set<Class<? extends WorkflowTest>> resources = reflections.getSubTypesOf(WorkflowTest.class);
    Class<?>[] testClasses = new Class<?>[resources.size()];
    Iterator<Class< ? extends WorkflowTest>> iterator = resources.iterator();
    int i=0;
    while (iterator.hasNext()) {
      testClasses[i] = iterator.next();
      i++;
    }
    return testClasses;
  }
}
