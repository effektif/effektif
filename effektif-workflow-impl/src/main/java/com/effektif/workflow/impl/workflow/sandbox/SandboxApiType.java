package com.effektif.workflow.impl.workflow.sandbox;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author mavo
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SandboxApiType {
  Class<? extends Sandboxable> value();
}
