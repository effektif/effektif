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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.effektif.workflow.api.workflow.ParseIssue.IssueType;


/**
 * @author Tom Baeyens
 */
public class ParseIssues {

  protected List<ParseIssue> issues;
  
  /** throws a RuntimeException if there were errors deploying the process */
  public ParseIssues checkNoErrors() {
    checkNoIssues(false);
    return this;
  }

  /** throws a RuntimeException if there were errors or warnings while deploying the process */
  public ParseIssues checkNoErrorsAndNoWarnings() {
    checkNoIssues(true);
    return this;
  }

  protected void checkNoIssues(boolean throwIfWarning) {
    if (issues!=null) {
      for (ParseIssue issue: issues) {
        if (issue.type==ParseIssue.IssueType.error || throwIfWarning) {
          throw new RuntimeException(getIssueReport());
        }
      }
    }
  }
  
  public String getIssueReport() {
    return getIssueReport(null);
  }

  public String getIssueReport(Locale l) {
    if (hasIssues()) {
      StringBuilder issueReport = new StringBuilder();
      issueReport.append("Issues: \n");
      for (ParseIssue issue: issues) {
        issueReport.append(issue.toString());
        issueReport.append("\n");
      }
      return issueReport.toString();
    }
    return null;
  }

  public boolean hasIssues() {
    return issues!=null && !issues.isEmpty();
  }
  
  public List<ParseIssue> getIssues() {
    return issues;
  }

  public boolean hasErrors() {
    if (hasIssues()) {
      for (ParseIssue issue: issues) {
        if (ParseIssue.IssueType.error==issue.type) {
          return true;
        }
      }
    }
    return false;
  }
  
  public void addIssue(IssueType issueType, String path, Long line, Long column, String message, Object... messageArgs) {
    if (issues==null) {
      issues = new ArrayList<>();
    }
    issues.add(new ParseIssue(issueType, path, line, column, message, messageArgs));
  }

  public boolean isEmpty() {
    return issues==null || issues.isEmpty();
  }
}
