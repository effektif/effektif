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
package com.effektif.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.impl.mapper.JsonMapper;


/**
 * @author Tom Baeyens
 */
@Provider
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class EffektifJsonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
  
  private static final Logger log = LoggerFactory.getLogger(EffektifJsonProvider.class);

  JsonMapper jsonMapper;
  
  public EffektifJsonProvider(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  @Override
  public boolean isReadable(Class< ? > type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    log.info("isReadable("+type.getName()+", "+genericType.toString()+", "+mediaType);
    return true;
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
          InputStream entityStream) throws IOException, WebApplicationException {
    log.info("readFrom("+type.getName()+", "+genericType.toString()+", "+mediaType);
    return jsonMapper.readFromReader(new InputStreamReader(entityStream), type);
  }

  @Override
  public boolean isWriteable(Class< ? > type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    log.info("isWriteable("+type.getName()+", "+genericType.toString()+", "+mediaType);
    return true;
  }

  @Override
  public long getSize(Object t, Class< ? > type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return 0;
  }

  @Override
  public void writeTo(Object t, Class< ? > type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
          OutputStream entityStream) throws IOException, WebApplicationException {
    log.info("writeTo("+t+", "+genericType.toString()+", "+mediaType);
    jsonMapper.writeToStreamPretty(t, new OutputStreamWriter(entityStream));
  }
}
