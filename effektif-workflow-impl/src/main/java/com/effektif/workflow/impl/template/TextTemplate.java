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
package com.effektif.workflow.impl.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.effektif.workflow.api.model.TypedValue;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;

/**
 * @author Tom Baeyens
 */
public class TextTemplate {

//  public static void main(String[] args) {
//    System.out.println(new TextTemplate("aaa {{"+new ObjectId()+".firstName}} bbb"));
//    System.out.println(new TextTemplate("{{"+new ObjectId()+".firstName}} bbb"));
//    System.out.println(new TextTemplate("aaa {{"+new ObjectId()+".firstName}}"));
//    System.out.println(new TextTemplate("{{"+new ObjectId()+".firstName}}"));
//    System.out.println(new TextTemplate("{{"+new ObjectId()+".firstName}} aaa {{"+new ObjectId()+".lastName}}"));
//    System.out.println(new TextTemplate("{{"+new ObjectId()+".firstName}} aaa {{"+new ObjectId()+".lastName}} bbb {{"+new ObjectId()+".fullName}}"));
//    System.out.println(new TextTemplate("{{"+new ObjectId()+".firstName}}{{"+new ObjectId()+".lastName}}{{"+new ObjectId()+".fullName}}"));
//    System.out.println(new TextTemplate("{{"+new ObjectId()+"}}"));
//    System.out.println(new TextTemplate("{{"+new ObjectId()+".firstName.lastName}}"));
//  }

  private static final Pattern BINDING_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");

  Hints hints;
  List<TemplateElement> elements = new ArrayList<>();
  
  public static TextTemplate parse(String templateText, Hint... hints) {
    if (templateText==null) {
      return null;
    }
    return new TextTemplate(templateText, hints);
  }
  
  public TextTemplate(String templateText, Hint... hints) {
    if (templateText!=null) {
      int startScan = 0;
      Matcher matcher = BINDING_PATTERN.matcher(templateText);
      while (matcher.find()) {
        String bindingString = matcher.group(1);
        int start = matcher.start();
        if (startScan<start) {
          elements.add(new StringTemplateElement(templateText.substring(startScan, start)));
        }
        elements.add(new BindingTemplateElement(bindingString));
        startScan = matcher.end();
      }
      if (startScan<templateText.length()) {
        elements.add(new StringTemplateElement(templateText.substring(startScan, templateText.length())));
      }
    }
    this.hints = new Hints();
    if (hints!=null) {
      for (Hint hint: hints) {
        this.hints.add(hint);
      }
    }
  }
  
  interface TemplateElement {
    void append(StringBuilder out, ScopeInstanceImpl scopeInstance, Hints hints);
  }
  
  class StringTemplateElement implements TemplateElement {
    String text;
    public StringTemplateElement(String text) {
      this.text = text;
    }
    public String toString() {
      return text;
    }
    @Override
    public void append(StringBuilder out, ScopeInstanceImpl scopeInstance, Hints hints) {
      out.append(text);
    }
  }
  
  class BindingTemplateElement implements TemplateElement {
    String variableId;
    List<String> fields;
    public BindingTemplateElement(String dynamicText) {
      StringTokenizer stringTokenizer = new StringTokenizer(dynamicText, ".");
      Binding binding = null;
      while (stringTokenizer.hasMoreTokens()) {
        String token = stringTokenizer.nextToken();
        if (binding==null) {
          variableId = token;
        } else {
          if (fields==null) {
            fields = new ArrayList<>();
          }
          fields.add(token);
        }
      }
    }
    public String toString() {
      StringBuilder text = new StringBuilder();
      text.append("{");
      text.append(variableId);
      if (fields!=null) {
        for (String field: fields) {
          text.append(".");
          text.append(field);
        }
      }
      text.append("}");
      return text.toString();
    }
    @Override
    public void append(StringBuilder out, ScopeInstanceImpl scopeInstance, Hints hints) {
      TypedValueImpl typedFieldValue = BindingImpl.getTypedFieldValue(scopeInstance, variableId, fields);
      String text = typedFieldValue.type.convertInternalToText(typedFieldValue.value, hints);
      if (text!=null) {
        out.append(text);
      }
    }
  }
  
  public String toString() {
    return elements.toString();
  }

  public String resolve(ScopeInstanceImpl scopeInstance) {
    StringBuilder out = new StringBuilder();
    for (TemplateElement element: elements) {
      element.append(out, scopeInstance, hints);
    }
    return out.toString();
  }
}
