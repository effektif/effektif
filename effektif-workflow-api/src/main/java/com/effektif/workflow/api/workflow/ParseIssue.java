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
package com.effektif.workflow.api.workflow;

import java.util.Locale;


/**
 * @author Tom Baeyens
 */
public class ParseIssue {
  
  IssueType type;
  String message; 
  Object[] messageArgs; // msg and arguments are split so that msg can be translated first.
  String path;
  Long line;
  Long column;

  public ParseIssue() {
  }

  public ParseIssue(IssueType type, String message, Object[] messageArgs) {
    this.type = type;
    this.message = message;
    this.messageArgs = messageArgs;
  }
  
  public ParseIssue(IssueType type, String path, Long line, Long column, String message, Object[] messageArgs) {
    this.type = type;
    this.message = message;
    this.messageArgs = messageArgs;
    this.path = path;
    this.line = line;
    this.column = column;
  }

  public String toString() {
    return toString(null);
  }

  public String toString(Locale l) {
    return (IssueType.error==type ? "|ERROR  | " : "|warning| ")
           +String.format(l, message, messageArgs)
           +" | "+path
           +(line!=null ? " line("+line+")" : "")
           +(column!=null ? " column("+column+")" : "");
  }

  public enum IssueType {
    error,
    warning
  }
  
  public IssueType getType() {
    return type;
  }

  public ParseIssue setType(IssueType type) {
    this.type = type;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public ParseIssue setMessage(String message) {
    this.message = message;
    return this;
  }

  public Object[] getMessageArgs() {
    return messageArgs;
  }

  public ParseIssue setMessageArgs(Object[] messageArgs) {
    this.messageArgs = messageArgs;
    return this;
  }

  public String getPath() {
    return path;
  }

  public Long getLineNumber() {
    return line;
  }

  public Long getColumnNumber() {
    return column;
  }
}