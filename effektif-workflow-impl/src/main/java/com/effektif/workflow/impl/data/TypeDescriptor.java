package com.effektif.workflow.impl.data;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.types.DataType;

/**
 * Describes a {@link DataType} for serialisation.
 */
public class TypeDescriptor {

  private Boolean mutable;
  private Boolean primitive;

  private List<TypeDescriptorField> configuration;
  private List<TypeDescriptorField> fields;

  public TypeDescriptor mutable() {
    mutable = Boolean.TRUE;
    return this;
  }

  public TypeDescriptor primitive() {
    primitive = Boolean.TRUE;
    return this;
  }

  public TypeDescriptor configuration(String name, DataType type) {
    if (configuration == null) {
      configuration = new ArrayList<>();
    }
    configuration.add(new TypeDescriptorField(name, type));
    return this;
  }

  public TypeDescriptor field(String name, DataType type) {
    if (fields == null) {
      fields = new ArrayList<>();
    }
    fields.add(new TypeDescriptorField(name, type));
    return this;
  }
}
