/* Copyright (c) 2014, Effektif GmbH.
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
 * limitations under the License. */
package com.effektif.mongo;

import java.util.Collection;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;


public class Query {
  
  public Integer skip;
  public Integer limit;
  public BasicDBObject query = new BasicDBObject();
  public BasicDBObject orderBy = null;
  
  public Query _id(ObjectId id) {
    query.append("_id", id);
    return this;
  }

  public Query _ids(Collection<ObjectId> ids) {
    in("_id", ids);
    return this;
  }

  public Query _id(String idString) {
    query.append("_id", new ObjectId(idString));
    return this;
  }
  
  public Query page(Number skip, Number limit) {
    this.skip = skip!=null ? skip.intValue() : null;
    this.limit = limit!=null ? limit.intValue() : null;
    return this;
  }
  
  public Query in(String fieldName, Collection<?> values) {
    query.put(fieldName, new BasicDBObject("$in", values));
    return this;
  }

  
  public Query equal(String fieldName, Object dbValue) {
    query.put(fieldName, dbValue);
    return this;
  }

  public Query notEqual(String fieldName, Object dbValue) {
    query.put(fieldName, new BasicDBObject("$ne", dbValue));
    return this;
  }

  public Query equalOpt(String fieldName, Object dbValue) {
    if (dbValue!=null) {
      query.put(fieldName, dbValue);
    }
    return this;
  }

  public Query gt(String fieldName, Object dbValue) {
    query.put(fieldName, new BasicDBObject("$gt", dbValue));
    return this;
  }

  public Query gte(String fieldName, Object dbValue) {
    query.put(fieldName, new BasicDBObject("$gte", dbValue));
    return this;
  }

  public Query lt(String fieldName, Object dbValue) {
    query.put(fieldName, new BasicDBObject("$lt", dbValue));
    return this;
  }

  public Query lte(String fieldName, Object dbValue) {
    query.put(fieldName, new BasicDBObject("$lte", dbValue));
    return this;
  }

  /**
   * Adds a list of clauses to the query as disjunction (logical OR).
   */
  public Query or(BasicDBObject... orClauses) {
    BasicDBList clauses = new BasicDBList();
    for (BasicDBObject orClause: orClauses) {
      if (orClause!=null) {
        clauses.add(orClause);
      }
    }
    return or(clauses);
  }
  
  public Query or(Query... orClauses) {
    BasicDBList clauses = new BasicDBList();
    for (Query orClause: orClauses) {
      if (orClause!=null) {
        clauses.add(orClause.get());
      }
    }
    return or(clauses);
  }

  /**
   * Adds a list of clauses to the query as disjunction (logical OR).
   */
  public Query or(BasicDBList clauses) {
    if (clauses==null || clauses.size()==0) {
      return this;
    }
    query.append("$or", clauses);
    return this;
  }

  /**
   * Adds a list of clauses to the query as disjunction (logical OR).
   */
  public Query or(List<? extends Query> clauses) {
    BasicDBList dbClauses = new BasicDBList();
    for (Query orClause: clauses) {
      if (orClause!=null) {
        dbClauses.add(orClause.get());
      }
    }
    return or(dbClauses);
  }

  /**
   * Adds a list of clauses to the query as conjunction (logical AND).
   */
  public Query and(BasicDBObject... andClauses) {
    BasicDBList clauses = new BasicDBList();
    for (BasicDBObject andClause: andClauses) {
      if (andClause!=null) {
        clauses.add(andClause);
      }
    }
    return and(clauses);
  }
  
  public Query and(Query... andClauses) {
    BasicDBList clauses = new BasicDBList();
    for (Query andClause: andClauses) {
      if (andClause!=null) {
        clauses.add(andClause.get());
      }
    }
    return and(clauses);
  }

  public Query and(List<? extends Query> andClauses) {
    BasicDBList clauses = new BasicDBList();
    for (Query andClause: andClauses) {
      if (andClause!=null) {
        clauses.add(andClause.get());
      }
    }
    return and(clauses);
  }

  /**
   * Adds a list of clauses to the query as conjunction (logical AND).
   */
  public Query and(BasicDBList clauses) {
    if (clauses==null || clauses.size()==0) {
      return this;
    }
    query.append("$and", clauses);
    return this;
  }
  
  public Query doesNotExist(String field) {
    query.append(field, new BasicDBObject("$exists", false));
    return this;
  }
  
  public Query exists(String field) {
    query.append(field, new BasicDBObject("$exists", true));
    return this;
  }
  
  public Query orderAsc(String field) {
    if (orderBy==null) {
      orderBy = new BasicDBObject();
    }
    orderBy.append(field, 1);
    return this;
  }
  
  public Query orderDesc(String field) {
    if (orderBy==null) {
      orderBy = new BasicDBObject();
    }
    orderBy.append(field, -1);
    return this;
  }
  
  public BasicDBObject get() {
    return query;
  }
  
  public Integer getSkip() {
    return skip;
  }
  
  public Integer getLimit() {
    return limit;
  }

  public void applyCursorConfigs(DBCursor dbCursor) {
    if (skip!=null) {
      dbCursor.skip(skip);
    }
    if (limit!=null) {
      dbCursor.limit(limit);
    }
    if (orderBy!=null) {
      dbCursor.sort(orderBy);
    }
  }
}
