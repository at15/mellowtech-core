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
 * Wraps a char value as a ByteStorable
 * 
 * @author Martin Svensson
 * @version 1.0
 */
public class CBChar extends ByteComparable <Character>{

  /**
   * Value of this CBInt
   *
   */
  private char value;

  /**
   * Default constructor is needed to create a new inte from a byte buffer.
   *
   */
  public CBChar() {
  }

  /**
   * Initialize with a  value
   *
   * @param value
   *          the value
   */
  public CBChar(char value) {
    this.value = value;
  }

  // ***********GET/SET**************
  @Override
  public void set(Character value){
    if(value == null) throw new ByteStorableException("null value not allowed");
    this.value = (char) value;
  }

  @Override
  public Character get() {
    return value;
  }

  @Override
  public boolean isFixed(){
    return true;
  }


  @Override
  public int hashCode() {
    return (int) value;
  }

  // ***********OVERWRITTEN BYTESTORABLE****************
  @Override
  public int byteSize() {
    return 2;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 2;
  }

  /*
   * public int byteSize(byte[] b, int offset){ return 4; }
   */
  @Override
  public void toBytes(ByteBuffer bb) {
    bb.putChar(value);
  }

  /*
   * public int toBytes(byte b[], int offset){ CBInt.writeInt(b, offset, value);
   * return 4; }
   */
  @Override
  public ByteStorable <Character> fromBytes(ByteBuffer bb) {
    return fromBytes(bb, doNew);
  }

  @Override
  public ByteStorable <Character> fromBytes(ByteBuffer bb, boolean doNew) {
    if (doNew)
      return new CBChar(bb.getChar());
    value = bb.getChar();
    return this;
  }

  @Override
  public int compareTo(ByteStorable<Character> other) {
    return this.value - other.get();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof CBChar)
      return this.value == ((CBChar) obj).value;
    return false;
  }

  @Override
  public String toString() {
    return "" + value;
  }

  @Override
  public int byteCompare(int offset1, ByteBuffer bb1, int offset2,
      ByteBuffer bb2) {
    return bb1.getChar(offset1) - bb2.getChar(offset2);
  }

}
