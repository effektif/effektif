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
package com.effektif.script;

import java.io.IOException;
import java.io.Writer;


/**
 * @author Tom Baeyens
 */
public class Console {

  Writer writer;

  public Console(Writer writer) {
    this.writer = writer;
  }

  public void log(String message) {
    try {
      writer.append(message);
      writer.append("\n");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}