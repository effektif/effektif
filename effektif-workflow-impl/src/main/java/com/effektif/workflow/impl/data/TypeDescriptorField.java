package com.effektif.workflow.impl.data;

import com.effektif.workflow.api.types.DataType;

/**
 * Describes a {@link DataType} field for serialisation.
 */
public class TypeDescriptorField {

    private String key;
    private DataType type;

    public TypeDescriptorField(String key, DataType type) {
      this.key = key;
      this.type = type;
    }
}
