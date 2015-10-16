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
package com.effektif.mongo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.joda.time.LocalDateTime;

import com.mongodb.gridfs.GridFSDBFile;

public class PrettyPrinter {

  private static final String SPACES = "                                                    ";

  public static String toJsonPrettyPrint(Object jsonObject) {
    StringBuffer jsonText = new StringBuffer();
    jsonObjectToTextFormatted(jsonObject, 0, jsonText);
    return jsonText.toString();
  }

  public static void jsonObjectToTextFormatted(Map<String,Object> jsonObject, int indent, StringBuffer jsonText) {
    jsonText.append("{ ");
    appendNewLine(indent+2, jsonText);
    Set<String> keys = new TreeSet<String>(jsonObject.keySet());
    boolean isFirst = true;
    for (String key : keys) {
      if (isFirst) {
        isFirst = false;
      } else {
        jsonText.append(", "); 
        appendNewLine(indent+2, jsonText);
      }
      jsonText.append("\""); 
      jsonText.append(key);
      jsonText.append("\" : ");
      jsonObjectToTextFormatted(jsonObject.get(key), indent+2, jsonText);
    }
    appendNewLine(indent, jsonText);
    jsonText.append("}");
  }

  public static void jsonObjectToTextFormatted(List<Object> jsonList, int indent, StringBuffer jsonText) {
    jsonText.append("[ ");
    appendNewLine(indent+2, jsonText);
    boolean isFirst = true;
    for (Object element : jsonList) {
      if (isFirst) {
        isFirst = false;
      } else {
        jsonText.append(", "); 
        appendNewLine(indent+2, jsonText);
      }
      jsonObjectToTextFormatted(element, indent+2, jsonText);
    }
    appendNewLine(indent, jsonText);
    jsonText.append("]");
  }

  private static void appendNewLine(int indent, StringBuffer jsonText) {
    jsonText.append("\n");
    jsonText.append(SPACES.substring(0, indent));
  }

  public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  private static final SimpleDateFormat DATE_FORMAT = initializeDateFormat();
  private static final SimpleDateFormat initializeDateFormat() {
    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
    dateFormat.setCalendar(new GregorianCalendar(new SimpleTimeZone(0, "GMT")));
    return dateFormat;
  }

  @SuppressWarnings({ "unchecked" })
  public static void jsonObjectToTextFormatted(Object jsonObject, int indent, StringBuffer jsonText) {
    if (jsonObject==null) {
      jsonText.append("null");
    } else if (jsonObject instanceof Set) {
      jsonObjectToTextFormatted(new ArrayList((Set) jsonObject), indent, jsonText);
    } else if (jsonObject instanceof Map) {
      jsonObjectToTextFormatted((Map<String,Object>) jsonObject, indent, jsonText);
    } else if (jsonObject instanceof List) {
      jsonObjectToTextFormatted((List<Object>) jsonObject, indent, jsonText);
    } else if (jsonObject instanceof String) {
      jsonText.append("\""); 
      jsonText.append(jsonObject);
      jsonText.append("\"");
    } else if (jsonObject instanceof ObjectId) {
      jsonText.append("{ \"$oid\" : \""+jsonObject.toString()+"\" }");
    } else if (jsonObject instanceof Date) {
      jsonText.append("{ \"$date\" : \""+ DATE_FORMAT.format(jsonObject)+"\" }");
    } else if (jsonObject instanceof LocalDateTime) {
      jsonObjectToTextFormatted(((LocalDateTime) jsonObject).toDate(), indent, jsonText);
    } else if (jsonObject instanceof Number || jsonObject instanceof Boolean) {
      jsonText.append(jsonObject.toString());
    } else if (jsonObject.getClass().isArray()) {
      jsonObjectToTextFormatted(Arrays.asList((Object[]) jsonObject), indent, jsonText);
    } else if (jsonObject instanceof Pattern) {
      jsonText.append("/"+jsonObject.toString()+"/");
    } else if (jsonObject instanceof GridFSDBFile) {
      jsonText.append("/"+((GridFSDBFile)jsonObject).getFilename()+"/");
    } else {
      throw new RuntimeException("couldn't pretty print "+jsonObject.getClass().getName());
    }
  }

}
