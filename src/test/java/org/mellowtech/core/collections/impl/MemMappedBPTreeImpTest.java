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
package org.mellowtech.core.collections.impl;

import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.BTree;
import org.mellowtech.core.collections.BTreeTemplate;


/**
 * @author Martin Svensson
 */
public class MemMappedBPTreeImpTest extends BTreeTemplate {

  @Override
  public String fName() {
    return "memmappedbtreeimp";
  }

  @Override
  public BTree<String, CBString, Integer, CBInt> init(String fileName, int valueBlockSize, int indexBlockSize,
                                               int maxValueBlocks, int maxIndexBlocks) throws Exception{
    return new MemMappedBPTreeImp<>(fileName, CBString.class, CBInt.class, indexBlockSize,
        valueBlockSize,false,maxValueBlocks,maxIndexBlocks);
  }
  @Override
  public BTree<String, CBString, Integer, CBInt> reopen(String fileName) throws Exception{
    return new MemMappedBPTreeImp<>(fileName, CBString.class, CBInt.class, false);
  }


}
