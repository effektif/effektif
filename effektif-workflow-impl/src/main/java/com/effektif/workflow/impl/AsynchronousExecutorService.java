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
package com.effektif.workflow.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;


/**
 * @author Tom Baeyens
 */
public class AsynchronousExecutorService implements ExecutorService, Brewable {
  
  private static final Logger log = WorkflowEngineImpl.log;
  
  // TODO apply these tips: http://java.dzone.com/articles/executorservice-10-tips-and

  public Executor executor;
  public BlockingQueue<Runnable> queue;
  public long shutdownTimeout = 30;                    // TODO make configurable
  public TimeUnit shutdownTimeUnit = TimeUnit.SECONDS; // TODO make configurable

  public AsynchronousExecutorService() {
  }

  @Override
  public void brew(Brewery brewery) {
    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(4, new ThreadPoolExecutor.CallerRunsPolicy());
    this.queue = scheduledThreadPoolExecutor.getQueue();
    this.executor = scheduledThreadPoolExecutor;
  }

  @Override
  public void execute(Runnable command) {
    if (log.isDebugEnabled()) log.debug("Command executes asynchronous: "+command);
    executor.execute(command);
  }

  public int getQueueDepth() {
    return queue.size();
  }

  @Override
  public void startup() {
  }

  @Override
  public void shutdown() {
    if (executor instanceof ScheduledExecutorService) {
      if (!((ScheduledExecutorService)executor).isShutdown()) {
        try {
          ScheduledExecutorService scheduledExecutorService = (ScheduledExecutorService) executor;
          if (log.isDebugEnabled())
            log.debug("shutting down executor "+executor);
          scheduledExecutorService.shutdown();
          scheduledExecutorService.awaitTermination(shutdownTimeout, shutdownTimeUnit);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
