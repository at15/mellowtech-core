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
package com.mellowtech.core.disc.blockfile;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.bytestorable.CBString;
import com.mellowtech.core.util.Platform;

import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Stores ByteStorable objects in a BlockFile, where each Block is of fixed
 * size. This object creates two separate files: the BlockFile itself and also a
 * file that contains a list of non-full Blocks. The list of free Blocks is kept
 * in memory at all times, and only written when close() is called.
 * 
 * @author rickard.coster@asimus.se
 * @version 1.0
 */
public class ByteStorableBlockFile {

  protected BlockFileWithId blockFile;
  protected MemoryFit memoryFit;
  protected String fileName;
  protected IOException exception = null;
  protected ByteStorable template = null;
  protected int blockSize;

  /**
   * Creates a new <code>ByteStorableBlockFile</code> instance where each
   * block is 64KB.
   * 
   * @param fileName
   *          the file name prefix of the two files: the BlockFile and a file
   *          that contains a list of non-full Blocks.
   * @param template
   *          the object template for Block values
   * @param numBlocksToCache
   *          the size of the cache
   * @exception IOException
   *              if an error occurs
   */
  public ByteStorableBlockFile(String fileName, ByteStorable template,
      int numBlocksToCache) throws IOException {
    this(fileName, 1024 * 64, template, numBlocksToCache);
  }

  /**
   * Creates a new <code>ByteStorableBlockFile</code> instance.
   * 
   * @param fileName
   *          the file name prefix of the two files: the BlockFile and a file
   *          that contains a list of non-full Blocks.
   * @param blockSize
   *          the block size
   * @param template
   *          the object template for Block values
   * @param numBlocksToCache
   *          the size of the cache
   * @exception IOException
   *              if an error occurs
   */
  public ByteStorableBlockFile(String fileName, int blockSize,
      ByteStorable template, int numBlocksToCache) throws IOException {

    this.template = template;
    this.fileName = fileName;
    this.blockSize = blockSize;

    // open or create BlockFile
    blockFile = new BlockFileWithId(fileName + ".blk", blockSize, numBlocksToCache,
        template);

    // check if memory fit file exists
    File file = new File(fileName + ".mef");

    if (!file.exists()) {
      // In case memory fit file does not exist, then the blockFile
      // must be empty. If it is not, it is an error (e.g the file
      // was removed).
      // RC:TODO re-calculate memoryFit for each Block instead of
      // signalling an error.
      if (blockFile.size() > 0) {
        throw new IOException("BlockFile was not empty, when it "
            + "should have been ");
      }
      memoryFit = new MemoryFit();

      // create first Block in BlockFile.
      BlockWithId block = new BlockWithId(blockSize, template);
      blockFile.write(0, block);

      // store number of free bytes for Block 0 in memory fit.
      int bytes = block.freeBytes();
      memoryFit.set(0, bytes);

      // create empty memory fit file
      FileChannel fc = new RandomAccessFile(fileName + ".mef", "rw")
          .getChannel();
      fc.close();
    }
    else {
      // memory fit file exists, read from it.
      FileChannel fc = new RandomAccessFile(fileName + ".mef", "r")
          .getChannel();

      memoryFit = new MemoryFit();
      ByteBuffer bb = ByteBuffer.allocate((int) fc.size());

      // read from memory fit file
      fc.read(bb);
      bb.flip();
      memoryFit = (MemoryFit) memoryFit.fromBytes(bb);
      fc.close();
    }
  }


  public void deleteFile() throws IOException{
    File f= new File(fileName + ".mef");
    f.delete();
    blockFile.deleteFile();
  }

  /**
   * Flushes the files, and writes the block info data.
   * 
   * @exception IOException
   *              if an error occurs
   */
  public void flush() throws IOException {
    blockFile.flush();
    FileChannel fc = new RandomAccessFile(fileName + ".mef", "rw").getChannel();
    fc.write((ByteBuffer) memoryFit.toBytes().flip());
    fc.close();
  }

  /**
   * Flushes all Blocks and closes the Block file, and writes the block info
   * data.
   * 
   * @exception IOException
   *              if an error occurs
   */
  public void close() throws IOException {
    blockFile.close();
    FileChannel fc = new RandomAccessFile(fileName + ".mef", "rw").getChannel();
    fc.write((ByteBuffer) memoryFit.toBytes().flip());
    fc.close();
  }

  /**
   * Writes (stores) id and value and id the BlockFile, returns blockno.
   * 
   * @param id
   *          the value id
   * @param value
   *          the value
   * @return the blockno where id and value was inserted, or -1 if value is too
   *         large to fit even in an empty Block.
   * @exception IOException
   *              if an error occurs
   */
  public synchronized int write(int id, ByteStorable value) throws IOException {
    int byteSize = value.byteSize();
    int blockno = memoryFit.firstFit(byteSize);
    BlockWithId block = null;
    if (blockno < 0) {
      // create new Block
      block = new BlockWithId(blockSize, template);
      if (!block.insert(id, value)) {
        CoreLog.L().info("value to large to fit");
        return -1; // value did not fit in an empty Block.
      }
      blockno = blockFile.highestBlockno() + 1;

      // Add to memory fit here
      memoryFit.set(blockno, block.freeBytes());
    }
    else {
      // fetch block
      block = blockFile.read(blockno);
      if (!block.insert(id, value)) {
        CoreLog.L().warning("did not fit in old block - should never happen");
        return -1;
      }
      // Update memory fit
      memoryFit.set(blockno, block.freeBytes());
    }
    // write Block.
    blockFile.write(blockno, block);
    return blockno;
  }

  /**
   * Updates value, possibly returns a new blockno.
   * 
   * @param id
   *          the id
   * @param blockno
   *          the blockno where id and value is stored
   * @param value
   *          the new value
   * @return the blockno where id and value now is stored, possibly a new
   *         blockno
   * @exception IOException
   *              if an error occurs
   */
  public synchronized int update(int blockno, int id, ByteStorable value)
      throws IOException {

    BlockWithId block = blockFile.read(blockno);

    if (block == null)
      throw new IOException("Blockno " + blockno + " is invalid."
          + "Highest Blockno in file is " + blockFile.highestBlockno());

    // update block.
    if (block.update(id, value)) {
      // update did fit in same block. write changed block
      blockFile.write(blockno, block);
      // Update memory fit
      memoryFit.set(blockno, block.freeBytes());
      return blockno;
    }
    else {
      // remove from block.
      block.remove(id);
      // write updated old block.
      blockFile.write(blockno, block);
      // Update memory fit
      memoryFit.set(blockno, block.freeBytes());
      // write as new value, write() returns a new blockno.
      return write(id, value);
    }
  }

  /**
   * Delete value.
   * 
   * @param id
   *          the id
   * @param blockno
   *          the blockno where id and value is stored
   * @exception IOException
   *              if an error occurs
   */
  public synchronized ByteStorable delete(int blockno, int id)
      throws IOException {

    BlockWithId block = blockFile.read(blockno);

    if (block == null)
      throw new IOException("Blockno " + blockno + " is invalid."
          + "Highest Blockno in file is " + blockFile.highestBlockno());

    // delete object from block.
    ByteStorable deletedObject = block.remove(id);

    if (deletedObject != null) {
      // Object was found, write changed block
      blockFile.write(blockno, block);
      // Update memory fit
      memoryFit.set(blockno, block.freeBytes());
    }
    return deletedObject;
  }

  /**
   * Reads a value from the BlockFile, returns null if id and value could not be
   * found in the specified block
   * 
   * @param id
   *          the value id
   * @param blockno
   *          the blockno where to find the value and id
   * @return the value associated with id
   * @exception IOException
   *              if an error occurs
   */
  public synchronized ByteStorable read(int blockno, int id) throws IOException {
    BlockWithId block = blockFile.read(blockno);
    ByteStorable value = block.get(id);
    return value;
  }
}
