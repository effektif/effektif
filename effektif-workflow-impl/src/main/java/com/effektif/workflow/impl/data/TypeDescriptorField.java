package com.effektif.workflow.impl.data;

import com.effektif.workflow.api.types.DataType;

/**
 * Describes a {@link DataType} field for serialisation.
 */
public class TypeDescriptorField {

    private String name;
    private DataType type;

    public TypeDescriptorField(String name, DataType type) {
      this.name = name;
      this.type = type;
    }
}
