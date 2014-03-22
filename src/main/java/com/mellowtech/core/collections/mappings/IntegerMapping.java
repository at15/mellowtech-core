/*
 * Copyright (c) 2013 mellowtech.org.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 */
package com.mellowtech.core.collections.mappings;

import com.mellowtech.core.bytestorable.ByteComparable;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.bytestorable.CBInt;
import com.mellowtech.core.bytestorable.CBString;

/**
 * @author Martin Svensson
 *
 */
public class IntegerMapping implements BCMapping <Integer> {
  
  //private CBString template;
  
  public IntegerMapping(){
  }

  @Override
  public Integer fromByteComparable(ByteComparable bc) {
    return ((CBInt) bc).get();
  }

  @Override
  public ByteComparable toByteComparable(Integer key) {
    return new CBInt(key);
  }

  @Override
  public Integer fromByteStorable(ByteStorable bs) {
    return ((CBInt) bs).get();
  }

  @Override
  public int byteSize(Integer value) {
    return 4;
  }

  @Override
  public ByteStorable toByteStorable(Integer value) {
    return new CBInt(value);
  }

  @Override
  public ByteStorable getTemplate() {
    return new CBInt();
  }

  @Override
  public Integer getOrigTemplate() {
    return 0;
  }

}