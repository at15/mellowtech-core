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

package com.mellowtech.core.io;

import com.mellowtech.core.bytestorable.CBString;
import com.mellowtech.core.util.Platform;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;

/**
 * Date: 2013-03-11
 * Time: 14:43
 *
 * @author Martin Svensson
 */
public class MappedBlockFileTest {

  @Test
  public void test() throws Exception{
    String tmpFile = Platform.getTempDir()+"/blockFileTest"+BlockFile.FILE_EXT;
    File f = new File(tmpFile);
    if(f.exists()) f.delete();

    MappedBlockFile rf = new MappedBlockFile(tmpFile, 1024, 1024, 40, 100);
    byte[] test = "This is My Test Record".getBytes();
    CBString reserved = new CBString("this is my reserved");
    rf.setReserve(reserved.toBytes().array());
    rf.insert(test);
    rf.insert(test);
    rf.insert(test);
    rf.insertMapped(test);
    rf.insertMapped(test);
    rf.delete(101);
    Iterator<Record> iter = rf.iterator();
    while(iter.hasNext()){
      Record next = iter.next();
      String str = new String(next.data, 0, test.length);
      Assert.assertEquals("This is My Test Record", str);
    }

    Assert.assertTrue(rf.contains(0));

    rf.close();
    rf = new MappedBlockFile(tmpFile);

    iter = rf.iterator();
    while(iter.hasNext()){
      Record next = iter.next();
      String str = new String(next.data, 0, test.length);
      Assert.assertEquals("This is My Test Record", str);
    }
    reserved.set("");
    reserved.fromBytes(rf.getReserve(), 0, false);
    Assert.assertEquals("this is my reserved", reserved.get());
    Assert.assertEquals(4, rf.size());
    Assert.assertTrue(rf.contains(100));
    Assert.assertTrue(rf.contains(102));
    Assert.assertTrue(rf.contains(0));
    Assert.assertTrue(rf.contains(1));
    Assert.assertFalse(rf.contains(101));
  }
}
