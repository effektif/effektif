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
package com.effektif.workflow.api.model;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.json.GenericType;


/**
 * @author Tom Baeyens
 */
public class ValueConverter {

  static Map<Class,Map<Class,Converter>> converters = new LinkedHashMap<>();
  
  public interface Converter<SOURCE extends Object,TARGET> {
    TARGET convert(SOURCE o);
  }
  
  static {
    addConverter(Number.class, new Converter<Number,Integer>() {
      public Integer convert(Number number) {
        return number.intValue();
      }
    }, Integer.class, int.class);
    addConverter(Number.class, new Converter<Number,Long>() {
      public Long convert(Number number) {
        return number.longValue();
      }
    }, Long.class, long.class);
    addConverter(Number.class, new Converter<Number,Double>() {
      public Double convert(Number number) {
        return number.doubleValue();
      }
    }, Double.class, double.class);
    addConverter(Number.class, new Converter<Number,Float>() {
      public Float convert(Number number) {
        return number.floatValue();
      }
    }, Float.class, float.class);
    addConverter(Number.class, new Converter<Number,Short>() {
      public Short convert(Number number) {
        return number.shortValue();
      }
    }, Short.class, short.class);
    addConverter(Number.class, new Converter<Number,Byte>() {
      public Byte convert(Number number) {
        return number.byteValue();
      }
    }, Byte.class, byte.class);
  }
  
  public static <S,T> void addConverter(Class<S> sourceClass, Converter<S,T> converter, Class... targetClasses) {
    Map<Class,Converter> sourceConverters = converters.get(sourceClass);
    if (sourceConverters==null) {
      sourceConverters = new HashMap<>();
      converters.put(sourceClass, sourceConverters);
    }
    for (Class clazz: targetClasses) {
      sourceConverters.put(clazz, converter);
    }
  }
  
  public static <S,T> T shoehorn(S foot, Class<T> shoe) {
    if (foot==null) {
      return null;
    }
    if (foot.getClass()==shoe) {
      return (T) foot;
    }
    Converter<S,T> converter = (Converter<S, T>) findConverter(foot.getClass(), shoe);
    return converter.convert(foot);
  }

  public static <S,T> T shoehorn(S foot, Type shoe) {
    if (shoe instanceof Class) {
      return shoehorn(foot, (Class<T>)shoe);
    }
    if (shoe instanceof GenericType) {
      GenericType genericType = (GenericType) shoe;
      if (genericType.getRawClass().isAssignableFrom(ArrayList.class)
          && foot instanceof Collection) {
        return (T) shoehorn((Collection)foot, (Class)genericType.getTypeArgs()[0]);
      }
    }
    throw new RuntimeException("don't know how to shoehorn "+foot+" into "+shoe);
  }

  public static <S,T> List<T> shoehorn(Collection<S> feet, Class<T> shoe) {
    if (feet==null) {
      return null;
    }
    ArrayList<T> shoehornedFeet = new ArrayList<>();
    for (S foot: feet) {
      shoehornedFeet.add(shoehorn(foot, shoe));
    }
    return shoehornedFeet;
  }

  public static <S,T> Converter<S,T> findConverter(Class<S> sourceClass, Class<T> targetClass) {
    for (Class<S> candidateSourceClass: converters.keySet()) {
      if (candidateSourceClass==sourceClass) {
        Converter<S,T> converter = converters.get(candidateSourceClass).get(targetClass);
        if (converter!=null) {
          return converter;
        }
      }
    }
    Class superclass = sourceClass.getSuperclass();
    if (superclass!=null) {
      return findConverter(superclass, targetClass);
    }
    return null;
  }
}
