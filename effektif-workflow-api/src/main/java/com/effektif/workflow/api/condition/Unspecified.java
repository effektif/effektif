package com.effektif.workflow.api.condition;

import com.effektif.workflow.api.bpmn.BpmnElement;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.workflow.Binding;

import java.util.List;
import java.util.Map;

/**
 * This condition is used if the user hasn't selected a specific condition,
 * but the UI wants to store a certain configuration.
 *
 * @author Christian Wiggert
 */
@TypeName("unspecified")
@BpmnElement("unspecified")
public class Unspecified extends Condition {

  protected Condition condition;
  protected List<Condition> conditions;
  protected Binding<?> left;
  protected Binding<?> right;
  protected Map<String,Object> properties;

  @Override
  public boolean isEmpty() {
    boolean hasNoCondition = condition == null;
    boolean hasNoConditions = conditions == null || conditions.isEmpty();
    boolean hasNoLeft = left == null;
    boolean hasNoRight = right == null;
    return hasNoCondition && hasNoConditions && hasNoLeft && hasNoRight;
  }

  @Override
  public void readBpmn(BpmnReader reader) {
    left = reader.readBinding("left", String.class);
    right = reader.readBinding("right", String.class);
    List<Condition> tempConditions = reader.readConditions();
    if (tempConditions != null && !tempConditions.isEmpty()) {
      // we do not really know, if it was a nested single condition or a nested list of conditions
      // when this parent condition was exported, so we use the following assumption
      if (tempConditions.size() == 1) {
        condition = tempConditions.get(0);
      } else {
        conditions = tempConditions;
      }
    }
  }

  @Override
  public void writeBpmn(BpmnWriter writer) {
    if (!isEmpty()) {
      writer.startElementEffektif(getClass());
      writer.writeBinding("left", getLeft());
      writer.writeBinding("right", getRight());
      if (condition != null) {
        condition.writeBpmn(writer);
      }
      if (conditions != null) {
        for (Condition cond : conditions) {
          cond.writeBpmn(writer);
        }
      }
      writer.endElement();
    }
  }

  public Condition getCondition() {
    return condition;
  }

  public void setCondition(Condition condition) {
    this.condition = condition;
  }

  public Unspecified condition(Condition condition) {
    this.condition = condition;
    return this;
  }

  public List<Condition> getConditions() {
    return conditions;
  }

  public void setConditions(List<Condition> conditions) {
    this.conditions = conditions;
  }

  public Binding<?> getLeft() {
    return left;
  }

  public void setLeft(Binding<?> left) {
    this.left = left;
  }

  public Unspecified left(Binding<?> left) {
    this.left = left;
    return this;
  }

  public Binding<?> getRight() {
    return right;
  }

  public void setRight(Binding<?> right) {
    this.right = right;
  }

  public Unspecified right(Binding<?> right) {
    this.right = right;
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }
}
