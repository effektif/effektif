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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.workflow.ExpressionImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;

/**
 * @author Tom Baeyens
 */
public class TextTemplate {

  private static final Pattern BINDING_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");

  public List<TemplateElement> elements = new ArrayList<>();
  public Hints hints;
  
  public TextTemplate(String templateText, Hint[] hints, WorkflowParser parser) {
    if (templateText!=null) {
      int expressionIndex = 0;
      int startScan = 0;
      Matcher matcher = BINDING_PATTERN.matcher(templateText);
      while (matcher.find()) {
        String expression = matcher.group(1);
        int start = matcher.start();
        if (startScan<start) {
          elements.add(new StringTemplateElement(templateText.substring(startScan, start)));
        }
        elements.add(new ExpressionTemplateElement(expression, expressionIndex, parser));
        expressionIndex++;
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
  
  public interface TemplateElement {
    void append(StringBuilder out, ScopeInstanceImpl scopeInstance, Hints hints);
  }
  
  public class StringTemplateElement implements TemplateElement {
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
  
  public class ExpressionTemplateElement implements TemplateElement {
    ExpressionImpl expression;
    public ExpressionTemplateElement(String expression, int expressionIndex, WorkflowParser parser) {
      this.expression = new ExpressionImpl();
      parser.pushContext("expression", expression, this.expression, expressionIndex);
      this.expression.parse(expression, parser);
      parser.popContext();
    }
    public String toString() {
      return expression!=null ? "{{"+expression+"}}" : "{{}}";
    }
    @Override
    public void append(StringBuilder out, ScopeInstanceImpl scopeInstance, Hints hints) {
      TypedValueImpl typedFieldValue = scopeInstance.getTypedValue(expression);
      if (typedFieldValue != null && typedFieldValue.value != null) {
        String text = typedFieldValue.type.convertInternalToText(typedFieldValue.value, hints);
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
