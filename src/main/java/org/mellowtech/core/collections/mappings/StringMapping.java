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
import org.mellowtech.core.bytestorable.CBString;

/**
 * @author Martin Svensson
 *
 */
public class StringMapping implements BCMapping<String> {
  
  //private CBString template;
  
  public StringMapping(){
  }



  @Override
  public String fromByteComparable(ByteComparable <String> bc) {
    return bc.get();
  }

  @Override
  public ByteComparable <String> toByteComparable(String key) {
    return new CBString(key);
  }

  @Override
  public String fromByteStorable(ByteStorable <String> bs) {
    return bs.get();
  }

  @Override
  public int byteSize(String value) {
    return new CBString(value).byteSize();
  }

  @Override
  public ByteStorable <String> toByteStorable(String value) {
    return new CBString(value);
  }

  @Override
  public ByteComparable <String> getTemplate() {
    return new CBString();
  }

  @Override
  public String getOrigTemplate() {
    return "";
  }

}
