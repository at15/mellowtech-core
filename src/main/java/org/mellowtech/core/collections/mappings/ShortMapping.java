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
package org.mellowtech.core.collections.mappings;


import org.mellowtech.core.bytestorable.ByteComparable;
import org.mellowtech.core.bytestorable.ByteStorable;
import org.mellowtech.core.bytestorable.CBShort;

/**
 * @author Martin Svensson
 *
 */
public class ShortMapping implements BCMapping <Short> {


  public ShortMapping(){
  }

  @Override
  public Short fromByteComparable(ByteComparable <Short> bc) {
    return bc.get();
  }

  @Override
  public ByteComparable <Short> toByteComparable(Short key) {
    return new CBShort(key);
  }

  @Override
  public Short fromByteStorable(ByteStorable <Short> bs) {
    return bs.get();
  }

  @Override
  public int byteSize(Short value) {
    return 2;
  }

  @Override
  public ByteStorable <Short> toByteStorable(Short value) {
    return new CBShort(value);
  }

  @Override
  public ByteComparable <Short> getTemplate() {
    return new CBShort();
  }

  @Override
  public Short getOrigTemplate() {
    return 0;
  }

}
