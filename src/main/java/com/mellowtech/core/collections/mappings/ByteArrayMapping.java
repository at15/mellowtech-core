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
import com.mellowtech.core.bytestorable.CBByteArray;

/**
 * @author Martin Svensson
 *
 */
public class ByteArrayMapping implements BCMapping<byte[]> {

  //private CBString template;

  public ByteArrayMapping(){
  }

  @Override
  public byte[] fromByteComparable(ByteComparable bc) {
    return ((CBByteArray) bc).getArray();
  }

  @Override
  public ByteComparable toByteComparable(byte[] byteArray) {
    return new CBByteArray(byteArray);
  }

  @Override
  public byte[] fromByteStorable(ByteStorable bs) {
    return ((CBByteArray) bs).get();
  }

  @Override
  public int byteSize(byte[] value) {
    return new CBByteArray(value).byteSize();
  }

  @Override
  public ByteStorable toByteStorable(byte[] value) {
    return new CBByteArray(value);
  }

  @Override
  public ByteStorable getTemplate() {
    return new CBByteArray();
  }

  @Override
  public byte[] getOrigTemplate() {
    return new byte[0];
  }

}