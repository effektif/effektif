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
package com.effektif.server;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tom Baeyens
 */
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {
  
  private static final Logger log = LoggerFactory.getLogger(DefaultExceptionMapper.class);
  
  @Override 
  public Response toResponse(Throwable ex) {
      StringWriter errorStackTrace = new StringWriter();
      ex.printStackTrace(new PrintWriter(errorStackTrace));
      log.error("Exception in Effektif server: "+ex.getMessage(), ex);
      return Response.status(400)
              .entity(errorStackTrace.toString())
              .type(MediaType.TEXT_PLAIN)
              .build();    
  }

}