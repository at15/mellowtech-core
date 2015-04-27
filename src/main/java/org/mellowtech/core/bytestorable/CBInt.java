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
package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;

/**
 * Wraps an int value as a ByteStorable
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class CBInt implements BComparable<Integer,CBInt> {
  
  private final int value;

  public CBInt() {value = 0;}

  public CBInt(int value) {this.value = value;}
  
  public CBInt(Integer value) {this.value = value;}
  
  @Override
  public CBInt create(Integer value) {return new CBInt(value);}
  
  @Override
  public Integer get() {return Integer.valueOf(value);}
  
  public int value(){return value;}

  @Override
  public boolean isFixed(){
    return true;
  }

  @Override
  public int byteSize() {
    return 4;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 4;
  }

  @Override
  public void to(ByteBuffer bb) {
    bb.putInt(value);
  }

  @Override
  public CBInt from(ByteBuffer bb) {
    return new CBInt(bb.getInt());
  }

  @Override
  public int compareTo(CBInt other) {
    return Integer.compare(value, other.value);
  }

  @Override
  public boolean equals(Object other) {
    if(other instanceof CBInt)
      return value == ((CBInt)other).value;
    return false;
  }
  
  @Override
  public int hashCode(){return value;}
  
  @Override
  public String toString(){return ""+value;}

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    return bb1.getInt(offset1) - bb2.getInt(offset2);
  }

}
