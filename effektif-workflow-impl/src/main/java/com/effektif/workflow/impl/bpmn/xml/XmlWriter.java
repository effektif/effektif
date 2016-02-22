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
package com.effektif.workflow.impl.bpmn.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;

import com.effektif.workflow.api.bpmn.XmlElement;


/** serializes {@link XmlElement our own jsonnable xml dom structure} 
 * to XML text. */
// TODO consider refactoring from stringwriter to sax streaming or nio if that makes sense.
public class XmlWriter {

  public OutputStream out;
  public Writer writer;
  public String encoding;
  
  public XmlWriter(OutputStream out) {
    this(out, "UTF-8");
  }
  
  public XmlWriter(OutputStream out, String encoding) {
    this.out = out;
    this.encoding = encoding;
    this.writer = new OutputStreamWriter(out, Charset.forName(encoding));
  }
  
  public void flush() {
    try {
      writer.flush();
      out.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeDocument(XmlElement xmlElement) {
    try {
      writer.write("<?xml version=\"1.0\" encoding=\""+encoding+"\"?>\n");
      write(xmlElement);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void write(XmlElement xmlElement) {
    write(xmlElement, 0);
  }
  
  protected void write(XmlElement xmlElement, int indentation) {
    // TODO refactor this to the JAX writer if necessary.
    try {
      writeIndentation(writer, indentation);
      writer.write('<');
      writer.write(xmlElement.name);
      if (xmlElement.attributes!=null) {
        for (String attributeName: xmlElement.attributes.keySet()) {
          writer.write(' ');
          writer.write(attributeName);
          writer.write("=\"");
          writer.write(xmlElement.attributes.get(attributeName));
          writer.write("\"");
        }
      }
      if (xmlElement.namespaces!=null) {
        for (URI uri: xmlElement.namespaces.getNames()) {
          writer.write(' ');
          if (xmlElement.namespaces.isDefault(uri)) {
            writer.write("xmlns");
          } else {
            writer.write("xmlns:");
            String prefix = xmlElement.namespaces.getPrefix(uri);
            writer.write(prefix);
          }
          writer.write("=\"");
          writer.write(uri.toString());
          writer.write("\"");
        }
      }
      
      if (!xmlElement.hasContent()) {
        writer.write('>');
        if (xmlElement.elements!=null) {
          for (XmlElement element: xmlElement.elements) {
            writer.write("\n");
            write(element, indentation+1);
          }
          writer.write("\n");
          writeIndentation(writer, indentation);
        }
        if (xmlElement.text!=null) {
          writer.write(xmlElement.text);
        }
        writer.write("</");
        writer.write(xmlElement.name);
        writer.write(">");
        
      } else {
        writer.write("/>");
      }
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void writeIndentation(Writer writer, int indentation) throws Exception {
    for (int i=0; i<indentation; i++) {
      writer.write("  ");
    }
  }

  public static String toString(XmlElement xmlElement) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      XmlWriter xmlWriter = new XmlWriter(out);
      xmlWriter.writeDocument(xmlElement);
      xmlWriter.flush();
      return out.toString(xmlWriter.encoding);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
