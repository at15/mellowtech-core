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
package org.mellowtech.core.bytestorable.io;

import java.nio.ByteBuffer;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.mellowtech.core.bytestorable.ByteComparable;
import org.mellowtech.core.bytestorable.ByteStorable;

/**
 * The SortedBlock keeps a sorted byte array of ByteStorables. The SortedBlock
 * divides the byte array into two sections, the first section is the pointers
 * to the actual keys stored in the array. All sorting, searching, rearranging is
 * done in the pointers section. Thus, keys does not have to be physically
 * sorted in the block, it is only the pointers that are sorted. The SortedBlock
 * do not have to be defragmented. Whenever keys are deleted the SortedBlock
 * automatically move that space to the unused space section. So using very long
 * keys with heavy insert/delete can reduce performance.<br>
 * <br>
 * The overhead for using the SortedBlock depends on the pointer size. It can
 * either be 4, 2, 1 byte extra for each key stored in the sorted block.
 * 
 * @author Martin Svensson
 */


public class SortedBlock <E extends ByteStorable> {

  private byte[] block;
  private byte[] tmpArr = new byte[128];
  private ByteBuffer buffer;
  private E keyType;
  private int high;
  private int bytesWritten;
  private short reservedSpace;
  // public int numComparisons;

  /**
   * Use for really big blocks, i.e, store up to 2000000000 keys
   */
  public static final byte PTR_BIG = 4;

  /**
   * Use for normal blocks, i.e, store up to 32000 keys
   */
  public static final byte PTR_NORMAL = 2;

  /**
   * Use for tiny blocks, i.e, store up to 256 keys
   */
  public static final byte PTR_TINY = 1;

  private byte ptrSize = PTR_NORMAL;
  private int headerSize = 1 + (ptrSize * 2);

  private int read(int position) {
    switch (ptrSize) {
    case PTR_BIG:
      return buffer.getInt(position);
    case PTR_NORMAL:
      return buffer.getShort(position);
    case PTR_TINY:
      return buffer.get(position);
    }
    return -1;
  }

  private void write(int position, int value) {
    switch (ptrSize) {
    case PTR_BIG:
      buffer.putInt(position, value);
      break;
    case PTR_NORMAL:
      buffer.putShort(position, (short) value);
      break;
    case PTR_TINY:
      buffer.put(position, (byte) value);
      break;
    }
  }

  private void setPhysicalPos(int index, int value) {
    write(getIndexPos(index), value);
  }

  private int getIndexPos(int index) {
    return reservedSpace + headerSize + (index * ptrSize);
  }

  private int getPhysicalPos(int index) {
    return read(getIndexPos(index));
  }

  private void writeBytesWritten(int bytesWritten) {
    write(1 + ptrSize + reservedSpace, bytesWritten);
  }

  private void writeNumElements(int numElements) {
    write(1 + reservedSpace, numElements);
  }

  private void writeIndexPos(int index, int value) {
    write(getIndexPos(index), value);
  }

  private int readBytesWritten() {
    return read(1 + ptrSize + reservedSpace);
  }

  private int readNumberOfElements() {
    return read(1 + reservedSpace);
  }

  private byte readPtrSize() {
    return buffer.get(reservedSpace);
  }

  private short readReservedSpace() {
    return buffer.getShort(0);
  }

  private void writeReservedSpaceLength() {
    buffer.putShort(0, (short) (reservedSpace - 2));
  }

  private void writePtrSize() {
    buffer.put(reservedSpace, ptrSize);
  }

  /**
   * Returns an iter.
   * 
   * @return an <code>Iterator</code> value
   */
  public Iterator <E> iterator() {
    return new SBIterator();
  }

  /**
   * Returns an iterator starting at the given key or the following one.
   * 
   * @param key
   *          a given key to start from
   * @return an <code>Iterator</code> value
   */
  public Iterator <E> iterator(E key) {
    return new SBIterator(key);
  }

  /**
   * The byte block has previously been initialized to be used as a sorted
   * block.
   * 
   * @param block
   *          the sortedblock bytes.
   * @param keyType
   *          the keyType always has to be provied.
   */
  public void setBlock(byte[] block, E keyType) {
    setBlock(block, keyType, false, (byte) -1, (short) -1);
  }

  /**
   * Set the block to work with.
   * 
   * @param block
   *          the physical block to store keys in
   * @param keyType
   *          The type of keys that this block handles
   * @param newBlock
   *          Indicate wheter this is a new block.
   * @param ptrSize
   *          The size of the pointers in this block. If the normal pointer size
   *          is used this block can store up to 32000 keys
   * @param reservedSpace
   *          X bytes will be reserved for caller to use freely (for instance
   *          for specific header information).
   */
  public void setBlock(byte[] block, E keyType, boolean newBlock,
      byte ptrSize, short reservedSpace) {
    this.keyType = keyType;
    this.block = block;
    this.buffer = ByteBuffer.wrap(block);

    if (newBlock) {
      this.reservedSpace = (short) (reservedSpace + 2); // the size has to be
      // stored:
      high = 0;
      bytesWritten = 0;
      this.ptrSize = ptrSize;
      writeNumElements(0);
      writeBytesWritten(0);
      this.ptrSize = ptrSize;
      writeReservedSpaceLength();
      writePtrSize();
    }
    else {
      this.reservedSpace = (short) (readReservedSpace() + 2);
      this.ptrSize = readPtrSize();
      high = readNumberOfElements();
      bytesWritten = readBytesWritten();
    }
    headerSize = (this.ptrSize * 2) + 1;
  }

  /**
   * Set the block to work with. This initializer use no reserved space.
   * 
   * @param block
   *          the physical block to store keys in
   * @param keyType
   *          The type of keys that this block handles
   * @param newBlock
   *          Indicate wheter this is a new block.
   * @param ptrSize
   *          The size of the pointers in this block. If the normal pointer size
   *          is used this block can store up to 32000 keys
   */
  public void setBlock(byte[] block, E keyType, boolean newBlock,
      byte ptrSize) {
    setBlock(block, keyType, newBlock, ptrSize, (short) 0);
  }

  /**
   * Return the current block. Be careful to manipulate a block directly (and
   * not via SortedBlock) since the sorted block stores pointers in each block.
   * 
   * @return an array of bytes containing the keys.
   */
  public byte[] getBlock() {
    return block;
  }

  /**
   * Return the current block. Be careful to manipulate a block directly (and
   * not via SortedBlock) since the sorted block stores pointers in each block.
   * 
   * @return the buffer containing the keys.
   */
  public ByteBuffer getByteBuffer() {
    return buffer;
  }

  /**
   * Return the type of keys this sorted block handles
   * 
   * @return The type of keys this block handles
   */
  public E getKeyType() {
    return keyType;
  }

  /**
   * This returs the start postion of the reserved space in this sorted block.
   * Be careful to read/write from the reserved space since the sorted block has
   * no control over this.
   * 
   * @return an <code>int</code> value
   */
  public int getReservedSpaceStart() {
    return 2;
  }

  /**
   * Returnts the number of bytes that has been reserved in this block.
   * 
   * @return an <code>int</code> value
   */
  public int getReservedSpace() {
    return reservedSpace - 2;
  }

  /**
   * Check if this block has room for an additional key.
   * 
   * @param key
   *          the key to check
   * @return True if the key can be stored in this block.
   */
  public boolean fitsKey(E key) {
    if (reservedSpace + headerSize + bytesWritten + ((high + 1) * ptrSize)
        + key.byteSize() > buffer.capacity())
      return false;
    return true;
  }

  /**
   * Number of keys in this block.
   * 
   * @return number of elements
   */
  public int getNumberOfElements() {
    return high;
  }

  /**
   * Number of bytes used to store the actual keys in this block (excluding
   * pointers).
   * 
   * @return number of bytes used
   */
  public int getDataBytes() {
    return bytesWritten;
  }

  /**
   * Number of databytes and pointer bytes written in this file.
   * 
   * @return number of bytes
   */
  public int getDataAndPointersBytes() {
    return bytesWritten + (high * ptrSize);
  }

  /**
   * The actual storage capacity of this sortedblock. Remember that Each key
   * stored will have an additional number of bytes for its pointer.
   * 
   * @return storage in bytes
   */
  public int storageCapacity() {
    return buffer.capacity() - headerSize - reservedSpace;
  }

  /**
   * Number of bytes actually written in this block. Including the reserved
   * space
   * 
   * @return number of bytes
   */
  public int getBytesWritten() {
    return reservedSpace + headerSize + bytesWritten + (high * ptrSize);
  }

  /**
   * The pointer size in this block, i.e 4 for integer size pointers
   * 
   * @return pointer size
   */
  public byte getPointerSize() {
    return ptrSize;
  }

  /**
   * Checks if this block can be merged with another block.
   * 
   * @param other
   *          the block to merge with.
   * @return true if the two blocks can be merged.
   */
  public boolean canMerge(SortedBlock <E> other) {
    int totDataBytes = other.getDataBytes() + getDataBytes();
    int totElements = other.getNumberOfElements() + getNumberOfElements();
    if (reservedSpace + headerSize + totDataBytes + (totElements * ptrSize) > buffer
        .capacity())
      return false;
    return true;
  }

  /**
   * Checks if this block can be merged with another block and an additional
   * key.
   * 
   * @param other
   *          the block to merge with.
   * @param additional
   *          a <code>ByteStorable</code> value
   * @return true if the two blocks can be merged.
   */
  public boolean canMerge(SortedBlock <E> other, E additional) {
    int totDataBytes = other.getDataBytes() + getDataBytes()
        + additional.byteSize();
    int totElements = other.getNumberOfElements() + getNumberOfElements() + 1;
    if (reservedSpace + headerSize + totDataBytes + ((totElements) * ptrSize) > buffer
        .capacity())
      return false;
    return true;
  }

  /**
   * Key at position index.
   * 
   * @param index
   *          positon of key
   * @return null if the block did not contain the key or the index was out of
   *         range
   */
  public E getKey(int index) {
    if (index >= high || index < 0)
      return null;

    buffer.position(getPhysicalPos(index));
    return (E) keyType.fromBytes(buffer);

  }

  /**
   * Binary search to find a key
   * 
   * @param key
   *          Key to search for
   * @return the key read from the current block
   */
  public E getKey(E key) {
    return getKey(binarySearch(key));
  }

  /**
   * The first key in this block.
   * 
   * @return null if the block is empty
   */
  public E getFirstKey() {
    return getKey(0);
  }

  /**
   * The last key in this block.
   * 
   * @return null if the block is empty
   */
  public E getLastKey() {
    return getKey(high - 1);
  }

  /**
   * Appends a key to this block. Note that this method does not guarantee that
   * the block stays sorted.
   * 
   * @param key
   *          Key to insert
   * @return true if the insert was successfull
   */
  public boolean insertKeyUnsorted(E key) {
    if (!fitsKey(key))
      return false;
    int pPos = buffer.capacity() - bytesWritten - key.byteSize();
    setPhysicalPos(high, pPos);
    high++;
    writeNumElements(high);
    bytesWritten += key.byteSize();
    writeBytesWritten(bytesWritten);
    buffer.position(pPos);
    key.toBytes(buffer);
    return true;
  }

  /**
   * This will update a previously stored key. Use with caution since it will
   * just overwrite the current data at the given index. Thus, the keySize
   * cannot have changed.
   * 
   * @param key
   *          a value of type 'ByteStorable'
   * @param index
   *          a value of type 'int'
   */
  public void updateKey(E key, int index) {
    buffer.position(getPhysicalPos(index));
    key.toBytes(buffer);
  }

  /**
   * Inserts a key in this block. Perfoms a binary search to find the correct
   * position.
   * 
   * @param key
   *          The key to insert.
   * @return the index if successfull or < 0 otherwise.
   * @see SortedBlock#insertKeyUnsorted
   */
  public int insertKey(E key) {
    // check if it can be inserted here:
    if (!fitsKey(key))
      return -1;
    int pos = binarySearch(key);
    if (pos >= 0)
      return -1;
    pos++;
    pos = Math.abs(pos);

    // calculate physical position:
    int pPos = buffer.capacity() - bytesWritten - key.byteSize();

    // shift all the elments to the right of pos to fit the pPos (i.e. a short)
    if (pos < high)
      byteBufferCopy(getIndexPos(pos), getIndexPos(pos + 1), (high - pos)
          * ptrSize);

    setPhysicalPos(pos, pPos);
    high++;
    writeNumElements(high);
    bytesWritten += key.byteSize();
    writeBytesWritten(bytesWritten);
    buffer.position(pPos);
    key.toBytes(buffer);
    return pos;
  }

  /**
   * Splits a block in two. The new block will contain the upper half of this
   * block's keys. The resved space is also copied
   * 
   * @return A SortedBlock with keys greater than this block's keys
   */
  public SortedBlock <E> splitBlock() {
    SortedBlock <E> sb = new SortedBlock <> ();
    byte[] newBlock = new byte[block.length];
    sb.setBlock(newBlock, keyType, true, ptrSize, (short) (reservedSpace - 2));
    int half = bytesWritten / 2;
    E lastKey;
    int numWritten = 0;

    // snacka med Rickard om den h�r l�sningen:
    while (numWritten < half) {
      lastKey = getLastKey();
      sb.insertKey(lastKey);
      deleteKey(lastKey);
      numWritten += lastKey.byteSize();
    }
    // finally copy the reserved space:
    System.arraycopy(block, getReservedSpaceStart(), sb.getBlock(),
        getReservedSpaceStart(), reservedSpace - 2);

    return sb;
  }

  /**
   * Merge this block with another block. The merge will override any
   * reservedspace in the other block. So if you want to save that do that prior
   * to calling this method.
   * 
   * @param sortedBlock
   *          The block to merge with this block.
   */
  public void mergeBlock(SortedBlock <E> sortedBlock) {
    // save the reserved space into sortedBlock:

    System.arraycopy(block, getReservedSpaceStart(), sortedBlock.getBlock(),
        getReservedSpaceStart(), reservedSpace - 2);
    mergeBlockSimple(sortedBlock);
    /*
    ByteStorable thisFirst = getFirstKey();
    ByteStorable otherFirst = sortedBlock.getFirstKey();
    if (thisFirst.compareTo(otherFirst) < 0) { // this is less
      mergeBlock(this, sortedBlock);
      this.setBlock(block, keyType, false, ptrSize);
    }
    else {
      mergeBlock(sortedBlock, this);
      this.setBlock(sortedBlock.getBlock(), keyType, false, ptrSize);
    }
    */
  }

  /**
   * If the keys in this block has been inserted unsorted use sort to sort the
   * contents. This method works as follows:<br>
   * it reads in every key into an CBytalbe[], and then either calls Arrays.sort
   * or sort.<br>
   * If you are reading in many keys at once it is a good idea to first store
   * them in an array than call sort, and then insert them using insertUnsorted
   * into this block.
   * 
   * @param mergeSort
   *          If true use the mergeSort in Arrays.sort, otherwise use quicksort
   * @see SortedBlock#sort
   * @see SortedBlock#insertKeyUnsorted
   */
  public void sort(boolean mergeSort) {
    ByteStorable toSort[] = new ByteStorable[high];
    for (int i = 0; i < high; i++) {
      toSort[i] = getKey(i);
    }
    if (mergeSort)
      java.util.Arrays.sort(toSort);
    else
      org.mellowtech.core.sort.Sorters.quickSort(toSort);
    this.setBlock(block, keyType, true, ptrSize);
    for (int i = 0; i < high; i++) {
      insertKeyUnsorted((E)toSort[i]);
    }
  }

  /**
   * Redistribute the keys in a number of blocks as evenly as possible.
   * 
   * @param blocks
   *          An array of sorted blocks that should be redistributed.
   */
  public static <E extends ByteStorable <?>> void redistribute(SortedBlock <E> [] blocks) {
    // first the total num bytes written and calculate the
    // optimal bytes in each block:
    int totalBytes = 0;
    int optimalBytes = 0;
    for (int i = 0; i < blocks.length; i++)
      totalBytes += blocks[i].getDataBytes();
    optimalBytes = calculateOptimum(totalBytes, blocks.length);

    // now loop from the end:
    int blockBytes;
    for (int i = blocks.length - 1; i > 0; i--) {
      blockBytes = blocks[i].getDataBytes();
      if (blockBytes > optimalBytes)
        putKeysPrevious(blocks, i, optimalBytes);
      else if (blockBytes < optimalBytes)
        getKeysPrevious(blocks, i, optimalBytes);
    }
  }

  /**
   * Remove a key at a given position
   * 
   * @param pos
   *          position
   * @return the deleted key
   */
  public E deleteKey(int pos) {
    if (pos < 0)
      return null;

    // read the physical position:
    int pPos = getPhysicalPos(pos);
    buffer.position(pPos);
    E toDelete = (E) keyType.fromBytes(buffer);
    int firstPos = buffer.capacity() - bytesWritten;
    int byteSize = toDelete.byteSize();
    if (pos < high - 1) { // we have to compact the array:
      byteBufferCopy(getIndexPos(pos + 1), getIndexPos(pos), (high - 1 - pos)
          * ptrSize);

    }
    // now compact the data:
    byteBufferCopy(firstPos, firstPos + byteSize, pPos - firstPos);

    // finally update all positions less than pos by adding toDelete.byteSize()
    // to
    // their byte positions (we have to loop through the entire array):
    high--;
    for (int i = 0; i < high; i++) {
      int oPos = getPhysicalPos(i);
      if (oPos < pPos)
        setPhysicalPos(i, oPos + byteSize);
    }
    writeNumElements(high);
    bytesWritten -= byteSize;
    writeBytesWritten(bytesWritten);
    return toDelete;
  }

  /**
   * Deletes a key from this block.
   * 
   * @param key
   *          The key to delete
   * @return The deleted key read from this block.
   */
  public E deleteKey(E key) {
    return deleteKey(binarySearch(key));
  }

  /**
   * Returns true if this block cotains the given key.
   * 
   * @param key
   *          the key to search for.
   * @return true if the key was found
   */
  public boolean containsKey(E key) {
    return (binarySearch(key) >= 0) ? true : false;
  }

  /**
   * Binary search. Works in the same fashion as Arrays.binarySearch.
   * 
   * @param key
   *          The key to search for
   * @return position
   * @see java.util.Arrays
   */
  public int binarySearch(E key) {
    int highSearch = high - 1;
    int low = 0, mid;
    E current;
    // compare bytewise if the
    if (key instanceof ByteComparable) 
      return binarySearchBC((ByteComparable <?>) key, low, highSearch);
    
    while (low <= highSearch) {
      mid = (low + highSearch) / 2;
      buffer.position(getPhysicalPos(mid));
      current = (E) keyType.fromBytes(buffer);
      int cmp = current.compareTo(key);
      if (cmp < 0)
        low = mid + 1;
      else if (cmp > 0)
        highSearch = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1);
  }
  
  public int binarySearchBC(ByteComparable <?> key, int low, int highSearch) {
    ByteBuffer bbKey = key.toBytes();
    ByteBuffer bbVal = ByteBuffer.wrap(block);
    while (low <= highSearch) {
      int mid = (low + highSearch) / 2;
      int midValOffset = getPhysicalPos(mid);
      int cmp = key.byteCompare(0, bbKey, midValOffset, bbVal);
      if (cmp < 0)
        low = mid + 1;
      else if (cmp > 0)
        highSearch = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1); // key not found.
  }
  
  
  public String toString() {

    StringBuffer sbuff = new StringBuffer();
    try{
    int high = readNumberOfElements();
    sbuff.append("number of items: " + high + "\n");
    sbuff.append("number of bytes written: " + readBytesWritten() + "\n");
    sbuff.append("load factor: "
        + (double) ((double) getBytesWritten() / (double) buffer.capacity()));
    sbuff.append("\nsort order:\n");

    for (int i = 0; i < high; i++) {
      int offset = getPhysicalPos(i);
      sbuff.append("offset: " + offset);
      buffer.position(offset);
      sbuff.append(" item: " + keyType.fromBytes(buffer) + "\n");
    }
    }
    catch(Exception e){/*e.printStackTrace();*/}
    return sbuff.toString();

  }

  private static int calculateOptimum(int total, int divider) {
    int optimal = total / divider;
    int remainder = total % divider;
    optimal = (remainder > divider / 2) ? optimal + 1 : optimal;
    return optimal;
  }

  private static <E extends ByteStorable <?>> void putKeysPrevious(SortedBlock <E> [] blocks, int index,
      int optimal) {
    int blockBytes;
    E current;
    int diff;
    int diffPut;
    while (true) {
      blockBytes = blocks[index].getDataBytes();
      diff = Math.abs(blockBytes - optimal);
      current = blocks[index].getFirstKey();
      diffPut = Math.abs(blockBytes - current.byteSize() - optimal);
      // the action of removing the current key will get this block
      // closer to the optimal
      if (diffPut < diff && blocks[index - 1].fitsKey(current)) {
        blocks[index].deleteKey(current);
        blocks[index - 1].insertKey(current);
      }
      else
        return;
    }
  }

  private static <E extends ByteStorable <?>> void getKeysPrevious(SortedBlock <E> [] blocks, int index,
      int optimal) {
    int blockBytes;
    E current;
    int diff;
    int diffPut;
    while (true) {
      blockBytes = blocks[index].getDataBytes();
      diff = Math.abs(blockBytes - optimal);
      current = blocks[index - 1].getLastKey();
      diffPut = Math.abs(blockBytes + current.byteSize() - optimal);
      // the action of removing the current key will get this block
      // closer to the optimal
      if (diffPut < diff && blocks[index].fitsKey(current)) {
        blocks[index].insertKey(current);
        blocks[index - 1].deleteKey(current);
      }
      else
        return;
    }
  }

  private void mergeBlockSimple(SortedBlock <E> other){
    for(int i = 0; i < other.getNumberOfElements(); i++){
      this.insertKey(other.getKey(i));
    }
  }

  private void mergeBlock(SortedBlock <E> smaller, SortedBlock <E> larger) {
    int highSmall = smaller.getNumberOfElements();
    int highLarge = larger.getNumberOfElements();
    // int sLastPtrPos = headerSize + (highSmall * ptrSize);
    int sBytesWritten = smaller.getDataBytes();
    int lBytesWritten = larger.getDataBytes();
    int physicalPos;

    // now insert the larger pointers into the smaller block and
    // update their physical pointer position for the bytes already written, i.e
    // the positon will be position - sBytesWritten
    for (int i = 0; i < highLarge; i++) {
      physicalPos = larger.getPhysicalPos(i);
      smaller.setPhysicalPos(highSmall + i, physicalPos - sBytesWritten);
    }

    // copy the data from larger starting at positon larger.length -
    // sBytesWritten):
    System.arraycopy(larger.getBlock(), larger.getBlock().length
        - lBytesWritten, smaller.getBlock(), smaller.getBlock().length
        - lBytesWritten - sBytesWritten, lBytesWritten);
    // finally update the bytesWritten and number of pointers:
    smaller.writeNumElements(highSmall + highLarge);
    smaller.writeBytesWritten(sBytesWritten + lBytesWritten);
  }

  private void byteBufferCopy(int srcPos, int destPos, int length) {
    if (tmpArr.length < length)
      tmpArr = new byte[length];
    buffer.position(srcPos);
    buffer.get(tmpArr, 0, length);
    buffer.position(destPos);
    buffer.put(tmpArr, 0, length);
  }

  /** *******************************INNER CLASSES************************* */
  class SBIterator implements Iterator <E> {
    int count = high;
    int cursor = 0;
    int lastRet = -1;

    public SBIterator(E start) {
      cursor = binarySearch(start);
      if (cursor >= 0)
        return;
      cursor++;
      cursor = Math.abs(cursor);
    }

    public SBIterator() {
    }

    public boolean hasNext() {
      return (cursor < count);
    }

    public E next() {
      check();
      if (cursor > count)
        throw new NoSuchElementException();
      E key = getKey(cursor);
      lastRet = cursor++;
      return key;
    }

    public void remove() {
      if (lastRet == -1)
        throw new IllegalStateException();
      check();
      deleteKey(lastRet);
      if (lastRet < cursor)
        cursor--;
      lastRet = -1;
      count = high;
    }

    private void check() {
      if (count != high)
        throw new ConcurrentModificationException();
    }
  }

  

}
