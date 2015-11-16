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

package org.mellowtech.core.io;

import java.nio.file.Paths;

/**
 * Date: 2013-03-11
 * Time: 14:43
 *
 * @author Martin Svensson
 */
public class CachedRecordFileTest extends RecordFileTemplate {

  @Override
  public String fname() {return "cachedRecordFileTest.blf";}

  @Override
  public long blocksOffset() {
    return ((CachedRecordFile)rf).blocksOffset();
  }

  @Override
  public RecordFile reopen(String fname) throws Exception {
    return new CachedRecordFile(Paths.get(fname), maxBlocks);
  }
  @Override
  public RecordFile init(int blockSize, int reserve, int maxBlocks, String fname) throws Exception {
    return new CachedRecordFile(Paths.get(fname), blockSize, maxBlocks, reserve, maxBlocks);
  }


}