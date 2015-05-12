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
package com.effektif.workflow.impl.data.types;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.InvalidValueException;


/**
 * @author Tom Baeyens
 */
public class NumberTypeImpl extends AbstractDataType<NumberType> {

  public NumberTypeImpl() {
    this(NumberType.INSTANCE);
  }
  
  public NumberTypeImpl(NumberType numberType) {
    super(numberType);
  }
  
  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    // the next section keeps the java types predictable based on the values
    
    // keep big decimals and big integers as they are
    if ( (jsonValue instanceof BigDecimal)
         || (jsonValue instanceof BigInteger) ) {
      return jsonValue;
    } else if (jsonValue instanceof Number) {
      Number number = (Number) jsonValue;
      
      // convert all numbers with a decimal fraction to doubles
      double doubleValue = number.doubleValue();
      if ( Math.rint(doubleValue)!=doubleValue
           || Double.NEGATIVE_INFINITY==doubleValue
           || Double.POSITIVE_INFINITY==doubleValue ) {
        return doubleValue;
      }
      // we use long for all integer values
      return number.longValue();
    } else if (jsonValue instanceof String) { 
      try {
        return Double.parseDouble((String) jsonValue);
      } catch (NumberFormatException e) {
        throw new InvalidValueException("Invalid number string "+jsonValue);
      }
    }
    return null;
  }
}
