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

package org.mellowtech.core.collections;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created with IntelliJ IDEA.
 * User: Martin Svensson
 * Date: 2012-10-21
 * Time: 11:04
 * To change this template use File | Settings | File Templates.
 */
public interface DiscMap <K,V> extends Map<K,V> {

  /**
   * persist the disc map
   * @throws IOException
   */
  public void save() throws IOException;
  
  /**
   * close this disc map. After close has to reopen it again
   * @throws IOException
   */
  public void close() throws IOException;


  /**
   * Perform a compaction of this disc map
   * @throws IOException
   */
  public void compact() throws IOException, UnsupportedOperationException;

  /**
   * Delete this disc map on disc
   * @throws IOException
   */
  public void delete() throws IOException;

  /**
   * Iterate over the entries of this disc map...
   * @return
   */
  public Iterator <Entry <K,V>> iterator();

  /**
   * Iterate over the entries in this map starting from
   * position pos
   * @param key
   * @return
   */
  public Iterator <Entry <K,V>> iterator(K key) throws UnsupportedOperationException;


}
