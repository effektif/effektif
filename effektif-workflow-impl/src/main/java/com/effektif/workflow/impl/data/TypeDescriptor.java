package com.effektif.workflow.impl.data;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.types.DataType;

/**
 * Describes a {@link DataType} for serialisation.
 */
public class TypeDescriptor {

  private String id;

  /** Indicates that (complex) values of this type may have their fields modified within a script. */
  private Boolean mutable;

  /** Indicates that values of this time are a single value. */
  private Boolean primitive;

  private List<TypeDescriptorField> configuration;
  private List<TypeDescriptorField> fields;

  public TypeDescriptor(String id) {
    this.id = id;
  }

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
