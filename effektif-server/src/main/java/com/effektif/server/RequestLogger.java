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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.internal.util.collection.StringIgnoreCaseKeyComparator;
import org.glassfish.jersey.message.MessageUtils;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Tom Baeyens
 */
public class RequestLogger implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

  public static final org.slf4j.Logger log = LoggerFactory.getLogger("HTTP");
  
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String REQUEST_PREFIX = ">>> ";
  private static final String RESPONSE_PREFIX = "<<< ";
  private static final String ENTITY_LOGGER_PROPERTY = LoggingFilter.class.getName() + ".entityLogger";
  private static final Comparator<Map.Entry<String, List<String>>> COMPARATOR = new Comparator<Map.Entry<String, List<String>>>() {
    @Override
    public int compare(final Map.Entry<String, List<String>> o1, final Map.Entry<String, List<String>> o2) {
      return StringIgnoreCaseKeyComparator.SINGLETON.compare(o1.getKey(), o2.getKey());
    }
  };
  private static final int DEFAULT_MAX_ENTITY_SIZE = 100 * 1024; // 100 KB

  protected boolean logEntity = true;
  protected boolean logEntityJsonPretty = true;
  protected boolean logHeaders = false;
  private final AtomicLong _id = new AtomicLong(0);
  private final int maxEntitySize = DEFAULT_MAX_ENTITY_SIZE;

  
  @Override
  public void filter(final ContainerRequestContext context) throws IOException {
    final long id = this._id.incrementAndGet();
    final StringBuilder logMsg = new StringBuilder();

    logMsg
      .append("\n\n")
      .append(REQUEST_PREFIX)
      .append(" ")
      .append(context.getMethod())
      .append(" /")
      .append(context.getUriInfo().getPath())
      .append("\n");

    if (logHeaders) {
      logHeaders(logMsg, id, REQUEST_PREFIX, context.getHeaders());
    }

    if (logEntity && context.hasEntity()) {
      context.setEntityStream(logInboundEntity(logMsg, context.getEntityStream(), MessageUtils.getCharset(context.getMediaType())));
    }

    if (log.isDebugEnabled())
      log.debug(logMsg.toString());
  }

  @Override
  public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
    final long id = this._id.incrementAndGet();
    final StringBuilder logMsg = new StringBuilder();

    logMsg
      .append("\n<<< ")
      .append(responseContext.getStatus())
      .append("\n");

    if (logHeaders) {
      logHeaders(logMsg, id, RESPONSE_PREFIX, responseContext.getStringHeaders());
    }

    if (logEntity && responseContext.hasEntity()) {
      final OutputStream stream = new LoggingStream(logMsg, responseContext.getEntityStream());
      responseContext.setEntityStream(stream);
      requestContext.setProperty(ENTITY_LOGGER_PROPERTY, stream);
      // not calling log(b) here - it will be called by the interceptor
    } else {
      if (log.isDebugEnabled())
        log.debug(logMsg.toString());
    }
  }

  private InputStream logInboundEntity(final StringBuilder b, InputStream stream, final Charset charset) throws IOException {
    if (!stream.markSupported()) {
      stream = new BufferedInputStream(stream);
    }
    stream.mark(maxEntitySize + 1);
    final byte[] entity = new byte[maxEntitySize + 1];
    final int entitySize = stream.read(entity);
    b.append(REQUEST_PREFIX);
    String entityString = new String(entity, 0, Math.min(entitySize, maxEntitySize), charset);
    if ( logEntityJsonPretty && (entitySize <= maxEntitySize) ){
      entityString = getJsonPrettyString(entityString);
    } 
    b.append(entityString);
    if (entitySize > maxEntitySize) {
      b.append("...more...");
    }
    stream.reset();
    return stream;
  }

  protected String getJsonPrettyString(String entityString) {
    try {
      @SuppressWarnings("unchecked")
      Map<String,Object> jsonMap = (Map<String,Object>) OBJECT_MAPPER.readValue(entityString, Map.class);
      return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void aroundWriteTo(final WriterInterceptorContext writerInterceptorContext) throws IOException, WebApplicationException {
    final LoggingStream stream = (LoggingStream) writerInterceptorContext.getProperty(ENTITY_LOGGER_PROPERTY);
    writerInterceptorContext.proceed();
    if (stream != null) {
      if (log.isDebugEnabled())
        log.debug(stream.getStringBuilder(MessageUtils.getCharset(writerInterceptorContext.getMediaType())).toString());
    }
  }

  private void logHeaders(final StringBuilder logMsg, final long id, final String prefix, final MultivaluedMap<String, String> headers) {
    for (final Map.Entry<String, List<String>> headerEntry : getSortedHeaders(headers.entrySet())) {
      final List< ? > val = headerEntry.getValue();
      final String header = headerEntry.getKey();
      if (val.size() == 1) {
        logMsg.append(prefix).append(header).append(": ").append(val.get(0)).append("\n");
      } else {
        final StringBuilder sb = new StringBuilder();
        boolean add = false;
        for (final Object s : val) {
          if (add) {
            sb.append(',');
          }
          add = true;
          sb.append(s);
        }
        logMsg.append(prefix).append(header).append(": ").append(sb.toString()).append("\n");
      }
    }
  }

  private Set<Map.Entry<String, List<String>>> getSortedHeaders(final Set<Map.Entry<String, List<String>>> headers) {
    final TreeSet<Map.Entry<String, List<String>>> sortedHeaders = new TreeSet<Map.Entry<String, List<String>>>(COMPARATOR);
    sortedHeaders.addAll(headers);
    return sortedHeaders;
  }

  private class LoggingStream extends OutputStream {
    private final StringBuilder logMsg;
    private final OutputStream inner;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    LoggingStream(final StringBuilder logMsg, final OutputStream inner) {
      this.logMsg = logMsg;
      this.inner = inner;
    }

    StringBuilder getStringBuilder(Charset charset) {
      // write entity to the builder
      final byte[] entity = baos.toByteArray();

      String entityString = new String(entity, 0, Math.min(entity.length, maxEntitySize), charset);
      if ( logEntityJsonPretty && (entity.length <= maxEntitySize) ){
        entityString = getJsonPrettyString(entityString);
      } 
      logMsg.append(entityString);
      if (entity.length > maxEntitySize) {
        logMsg.append("...more...");
      }

      return logMsg;
    }

    @Override
    public void write(final int i) throws IOException {
      if (baos.size() <= maxEntitySize) {
        baos.write(i);
      }
      inner.write(i);
    }
  }
}
