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

import junit.framework.Assert;
import org.junit.Test;

/**
 * Date: 2013-04-17
 * Time: 17:59
 *
 * @author Martin Svensson
 */
public class CBAutoTest {

  @Test
  public void test(){
    AutoContainer i1 = new AutoContainer(1, "one");
    AutoContainer i2 = (AutoContainer) i1.deepCopy();
    Assert.assertTrue(i1.getF1().equals(i2.getF1()));
    Assert.assertTrue(i2.getF2().equals(i2.getF2()));

    //check null
    i1 = new AutoContainer();
    i2 = (AutoContainer) i1.deepCopy();
    Assert.assertNull(i2.getF1());
    Assert.assertNull(i2.getF2());
  }

  @Test
  public void testPrimitives(){
    AutoContainer2 ac2 = new AutoContainer2((byte)1, (short)10, 100, 1000, (float) 1.0,
            10.0, 'a', true);
    AutoContainer2 ac3;
    ac3 = (AutoContainer2) ac2.deepCopy();
    Assert.assertEquals(ac2.toString(), ac3.toString());

  }
}
