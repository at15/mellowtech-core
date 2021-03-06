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

import com.google.common.base.Objects;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.*;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;

/**
 * Same as SplitBlockFile but with the difference that
 * the second portion of the file is also memory mapped.
 * The file will increase in regular intervals
 * @author Martin Svensson
 */
public class MemSplitBlockFile implements SplitRecordFile {

  class BlockIterator implements Iterator<Record>{


    Record next = null;
    Integer nextRec;
    int record;
    boolean stop = false;
    int currIndex;
    BitSet bits;
    int bSize;

    public BlockIterator(Integer from){
      record = from != null ? from : 0;
      getNext();

    }

    private void getNext(){
      if(record < 0) return;
      record = bitSet.nextSetBit(record);
      if(record > -1){
        try{
          next = new Record(record, get(record));
          record++;
          return;
        }
        catch(IOException e){record = -1;}
      }
    }


    @Override
    public boolean hasNext() {
      return record > 0;
    }

    @Override
    public Record next() {
      Record toRet = next;
      getNext();
      return toRet;
    }

    @Override
    public void remove() {
    }
  }
  class MappedBlockIterator implements Iterator<Record>{


    Record next = null;
    Integer nextRec;
    int record;
    boolean stop = false;
    int currIndex;
    BitSet bits;
    int bSize;

    public MappedBlockIterator(Integer from){
      record = from != null ? from : 0;
      getNext();

    }

    private void getNext(){
      if(record < 0) return;
      record = mappedBitSet.nextSetBit(record);
      if(record > -1){
        try{
          next = new Record(record, getRegion(record));
          record++;
          return;
        }
        catch(IOException e){record = -1;}
      }
    }


    @Override
    public boolean hasNext() {
      return record > 0;
    }

    @Override
    public Record next() {
      Record toRet = next;
      getNext();
      return toRet;
    }

    @Override
    public void remove() {
    }
  }
  public static final String FILE_EXT = ".mlf";

  public static final int MIN_BLOCK_SIZE = 256;


  public static final String MAGIC = "SABF";
  //This will change to not be statically set
  private long blocksToMap;


  private String fileName;
  private FileChannel fc;

  private int blockSize;
  private int mappedBlockSize;

  private int maxBlocks;
  private int mappedMaxBlocks;

  private int bitSize;
  private int mappedBitSize;

  private BitSet bitSet;
  private BitSet mappedBitSet;
  private MappedByteBuffer bitBuffer;
  private MappedByteBuffer mappedBitBuffer;


  private MappedByteBuffer mappedBlocks;

  private List<MappedByteBuffer> blocks;
  private final int headerSize = 24;


  private long startMappedBlocks, startBlocks;

  private int reserve;

  public MemSplitBlockFile(String fileName) throws IOException{
    openFile(fileName);
  }

  public MemSplitBlockFile(String fileName, int blockSize, int maxBlocks,
                           int reserve, int mappedMaxBlocks, int mappedBlockSize) throws IOException{

    try{
    if(openFile(fileName)){
      return;
      }
    }
    catch(IOException e){
      CoreLog.L().log(Level.FINER, "Could Not Open Old File", e);
    }

    if(reserve < 0) reserve = 0;
    this.reserve = reserve;
    this.maxBlocks = maxBlocks;
    this.blockSize = blockSize < MIN_BLOCK_SIZE ? MIN_BLOCK_SIZE : blockSize;
    this.blocksToMap = this.calcBlocksToMap();
    this.mappedBlockSize = mappedBlockSize;
    this.mappedMaxBlocks = mappedMaxBlocks;
    this.fileName = fileName;
    File f = new File(fileName);
    RandomAccessFile raf = new RandomAccessFile(f, "rw");
    fc = raf.getChannel();

    init();

    //long blocksSize = fc.size() - startBlocks;
    raf.setLength(startBlocks+(blocksToMap*blockSize));
    mapBlocks(startBlocks, raf.length());
    bitSet = new BitSet();
    mappedBitSet = new BitSet();
  }

  @Override
  public void clear() throws IOException {
    bitSet.clear();
    mappedBitSet.clear();
    //shrink:
    fc.truncate(startBlocks + (blockSize * blocksToMap));

  }


  @Override
  public void close() throws IOException {
    save();
    fc.close();
  }


  @Override
  public Map<Integer, Integer> compact() throws IOException {
    return null;
  }

  @Override
  public boolean contains(int record) {
    return bitSet.get(record);
  }

  @Override
  public boolean containsRegion(int record){
    return mappedBitSet.get(record);
  }

  @Override
  public boolean delete(int record) throws IOException {
   if(bitSet.get(record)){
      bitSet.flip(record);
      saveBitSet(bitSet, bitBuffer);
      shrinkBlocks();
      return true;
    }
    return false;
  }

  @Override
  public boolean deleteRegion(int record) throws IOException {
    if(mappedBitSet.get(record)){
      mappedBitSet.flip(record);
      saveBitSet(mappedBitSet, mappedBitBuffer);
      return true;
    }
    return false;
  }
  
  private void expandBlocks(int toIndex) throws IOException{
    long pos = getBufferPos(toIndex);
    for(int i = blocks.size(); i <= pos; i++){
      long filePos = startBlocks + (pos * blockSize * blocksToMap);
      blocks.add(fc.map(FileChannel.MapMode.READ_WRITE, filePos, blockSize * blocksToMap));
    }
  }

  private MappedByteBuffer findBuffer(int record){
    return blocks.get(getBufferPos(record));
  }
  
  @Override
  public byte[] get(int record) throws IOException{
    byte[] bytes = new byte[blockSize];
    return get(record, bytes) == true ? bytes : null;
  }

  @Override
  public boolean get(int record, byte[] buffer) throws IOException{
    if(bitSet.get(record)){
      ByteBuffer bb = findBuffer(record);
      record = truncate(record);
      bb.position(record * blockSize);
      if(buffer.length > blockSize){
        bb.get(buffer, 0, blockSize);
      }
      else
        bb.get(buffer);
      return true;
    }
    return false;
  }
  
  private BitSet getBitSet(ByteBuffer bb){
    int longs = bb.getInt();
    LongBuffer lb = bb.asLongBuffer();
    lb.limit(longs);
    return BitSet.valueOf(lb);
  }

  @Override
  public int getBlockSize() {
    return blockSize;
  }

  @Override
  public int getBlockSizeRegion(){
    return mappedBlockSize;
  }
  
  private int getBufferPos(int record){
    return record / (int) blocksToMap;
  }

  @Override
  public int getFirstRecord() {
    return bitSet.nextSetBit(0);
  }

  @Override
  public int getFirstRecordRegion(){
    return mappedBitSet.nextSetBit(0);
  }

  @Override
  public int getFreeBlocks() {
    return maxBlocks - size();
  }

  @Override
  public int getFreeBlocksRegion() {
    return mappedMaxBlocks - sizeRegion();
  }

  public int getLastMappedRecord(){
    return mappedBitSet.length() - 1;
  }

  public int getLastRecord(){
    return bitSet.length() - 1;
  }

  @Override
  public byte[] getRegion(int record) throws IOException{
    byte[] bytes = new byte[mappedBlockSize];
    return getRegion(record, bytes) == true ? bytes : null;
  }

  @Override
  public boolean getRegion(int record, byte[] buffer) throws IOException{
    if(mappedBitSet.get(record)){
      mappedBlocks.position(record * mappedBlockSize);
      if(buffer.length > mappedBlockSize){
        mappedBlocks.get(buffer, 0, mappedBlockSize);
      }
      else
        mappedBlocks.get(buffer);
      return true;
    }
    return false;
  }

  @Override
  public byte[] getReserve() throws IOException {
    if(reserve < 1) return null;
    ByteBuffer bb = ByteBuffer.allocate(reserve);
    fc.read(bb, 20 + bitSize);
    return bb.array();
  }

  private void init() throws IOException{
    //number of bytes for bitSets
    bitSize = maxBlocks/8;
    mappedBitSize = mappedMaxBlocks/8;

    //mapped blocks start just after the reserved space
    startMappedBlocks = headerSize + bitSize + 4 + mappedBitSize + 4 + reserve;

    //normal blocks start just after mapped blocks
    startBlocks = startMappedBlocks + (mappedMaxBlocks * mappedBlockSize);

    //buffers for bitsets:
    mappedBitBuffer = fc.map(FileChannel.MapMode.READ_WRITE, headerSize, mappedBitSize + 4);
    bitBuffer = fc.map(FileChannel.MapMode.READ_WRITE,
            headerSize + mappedBitSize + 4,
            bitSize + 4);

    //map mapped region
    mappedBlocks = fc.map(FileChannel.MapMode.READ_WRITE, startMappedBlocks,
            mappedMaxBlocks * mappedBlockSize);

  }

  @Override
  public int insert(byte[] bytes) throws IOException{
    return insert(bytes, 0, bytes != null ? bytes.length : -1);
  }

  @Override
  public int insert(byte[] bytes, int offset, int length) throws IOException {
    int index = bitSet.nextClearBit(0);
    if(index >= maxBlocks)
      throw new IOException("no blocks left");

    if(getBufferPos(index) >= blocks.size()){
      expandBlocks(index);
    }

    if(bytes != null && length > 0){
      ByteBuffer bb = findBuffer(index);
      int record = truncate(index);
      bb.position(record * blockSize);
      bb.put(bytes, offset, length > blockSize ? blockSize : length);
    }
    bitSet.set(index, true);
    saveBitSet(bitSet, bitBuffer);
    return index;
  }

  @Override
  public void insert(int record, byte[] bytes) throws IOException {
    if(record >= maxBlocks)
      throw new IOException("record out of bounce");
    if(getBufferPos(record) >= blocks.size())
      expandBlocks(record);

    bitSet.set(record, true);
    saveBitSet(bitSet, bitBuffer);
    update(record, bytes);
  }

  @Override
  public int insertRegion(byte[] bytes) throws IOException{
    return insertRegion(bytes, 0, bytes != null ? bytes.length : 0);
  }

  @Override
  public int insertRegion(byte[] bytes, int offset, int length) throws IOException {
    int index = mappedBitSet.nextClearBit(0);
    if(index >= mappedMaxBlocks)
      throw new IOException("no blocks left in mapped region");
    if(bytes != null && length > 0){
      mappedBlocks.position(index * mappedBlockSize);
      int l = length > mappedBlockSize ? mappedBlockSize : length;
      mappedBlocks.put(bytes, offset, l);
    }
    mappedBitSet.set(index, true);
    saveBitSet(mappedBitSet, mappedBitBuffer);
    return index;
  }

  @Override
  public void insertRegion(int record, byte[] bytes) throws IOException {
    if(record > mappedMaxBlocks)
      throw new IOException("record out of bounce");

    mappedBitSet.set(record, true);
    saveBitSet(mappedBitSet, mappedBitBuffer);
    updateRegion(record, bytes);
  }

  @Override
  public Iterator<Record> iterator() {
    return new BlockIterator(null);
  }

  @Override
  public Iterator<Record> iterator(int record) {
    return new BlockIterator(record > -1 ? record : null);
  }

  @Override
  public Iterator<Record> iteratorRegion() {
    return new MappedBlockIterator(null);
  }

  @Override
  public Iterator <Record> iteratorRegion(int record){
    return new MappedBlockIterator(record > -1 ? record : null);
  }

  private void mapBlocks(long start, long end) throws IOException{
    int region = blockSize * (int) blocksToMap;
    long current = start;
    while(current < end){
      MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_WRITE, current, region);
      current += region;
      blocks.add(bb);
    }
  }

  @Override
  public MappedByteBuffer mapReserve() throws IOException {
    return fc.map(MapMode.READ_WRITE, 20+bitSize, reserve);
  }
  
  /**
   * Calculates a good number of blocks to map
   * @param blockSize
   * @return
   */
  protected int calcBlocksToMap(){
    int region4 = 1024*1024*4;
    int region8 = region4 * 2;
    int region16 = region8 * 2;
    int region32 = region16 * 2;
    int region64 = region32 * 2; //64 MB
    
    
    long maxRecordSize = (long) blockSize * (long) maxBlocks;
    if(maxRecordSize < region64){
      return maxBlocks;
    }
    else if(maxRecordSize/region64 > 10){
      return (int) Math.ceil(region64/(double)blockSize);
    }
    else if(maxRecordSize/region32 > 10){
      return (int) Math.ceil(region32/(double)blockSize);
    }
    else if(maxRecordSize/region16 > 10){
      return (int) Math.ceil(region16/(double)blockSize);
    }
    else if(maxRecordSize/region8 > 10){
      return (int) Math.ceil(region8/(double)blockSize);
    }
    else{
      return (int) Math.ceil(region4/(double)blockSize);
    }
  }


  protected boolean openFile(String fileName) throws IOException{
    blocks = new ArrayList<>();
    File f = new File(fileName);
    if(!f.exists())
      return false;

    @SuppressWarnings("resource")
    RandomAccessFile raf = new RandomAccessFile(f, "rw");
    fc = raf.getChannel();

    //Magic Marker, BlockSize, MaxBlocks, Reserved, BitSetSize

    ByteBuffer bb = ByteBuffer.allocate(headerSize);
    fc.read(bb, 0);
    bb.flip();
    if(bb.limit() < 24)
      throw new IOException("not a block file");

    //magic marker
    byte[] b = new byte[4];
    bb.get(b);
    String marker = new String(b);

    if(!marker.equals(MAGIC))
      throw new IOException("not a block file");

    //ordinary blocks:
    blockSize = bb.getInt();
    maxBlocks = bb.getInt();
    
    blocksToMap = this.calcBlocksToMap();

    //mapped blocks:
    mappedBlockSize = bb.getInt();
    mappedMaxBlocks = bb.getInt();

    //size of reserved space:
    reserve = bb.getInt();
    this.fileName = fileName;

    init();

    //map blocks
    //long blocksSize = fc.size() - startBlocks;
    mapBlocks(startBlocks, fc.size());
    //blocks = fc.map(FileChannel.MapMode.READ_WRITE, startBlocks, blocksSize);


    mappedBitSet = getBitSet(mappedBitBuffer);
    bitSet = getBitSet(bitBuffer);
    return true;
  }

  @Override
  public boolean save() throws IOException{
    ByteBuffer bb = ByteBuffer.allocate(headerSize);
    bb.put(MAGIC.getBytes());
    bb.putInt(blockSize);
    bb.putInt(maxBlocks);
    bb.putInt(mappedBlockSize);
    bb.putInt(mappedMaxBlocks);
    bb.putInt(reserve);
    bb.flip();
    fc.write(bb, 0);
    saveBitSet(mappedBitSet, mappedBitBuffer);
    mappedBitBuffer.force();
    saveBitSet(bitSet, bitBuffer);
    bitBuffer.force();
    mappedBlocks.force();
    //This is probably not needed:
    for(MappedByteBuffer mpp : blocks){
      mpp.force();
    }
    fc.force(true);
    return true;
  }

  protected void saveBitSet(BitSet toSave, ByteBuffer bb) throws IOException{
    bb.clear();
    long[] bits = toSave.toLongArray();
    bb.putInt(bits.length);
    bb.asLongBuffer().put(toSave.toLongArray());
  }

  @Override
  public void setReserve(byte[] bytes) throws IOException {
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    if(bytes.length > reserve) bb.limit(reserve);
    fc.write(bb, 20 + bitSize);
  }

  private void shrinkBlocks() throws IOException{
    int highSet = getLastRecord();
    int blockNo = getBufferPos(highSet);
    boolean removed = false;
    for(int i = blocks.size() - 1; i > blockNo; i--){
      blocks.remove(i);
      removed = true;
    }
    if(removed){
      long region = blockSize * blocksToMap;
      fc.truncate(startBlocks + (blockNo+1 * region));
    }
  }

  @Override
  public int size() {
    return bitSet.cardinality();
  }

  @Override
  public int sizeRegion() {
    return mappedBitSet.cardinality();
  }

  public String toString(){
    return Objects.toStringHelper(this).
            add("maxBlocks", maxBlocks).
            add("fileName", fileName).
            add("startMappedBlocks", startMappedBlocks).
            add("startBlocks", startBlocks).
            add("bitSize", bitSize).
            add("mappedBitSize", mappedBitSize).
            add("blockSize", blockSize).
            add("maxBlocks", maxBlocks).
            add("mappedMaxBlocks", mappedMaxBlocks).
            add("reserve", reserve).
            add("mappedBitSet", mappedBitSet.toString()).
            add("bitSet", bitSet.toString()).toString();
  }


  private int truncate(int record){
    return record - (getBufferPos(record) * (int)blocksToMap);
  }

  @Override
  public boolean update(int record, byte[] bytes) throws IOException {
    return update(record, bytes, 0, bytes.length);
  }

  @Override
  public boolean update(int record, byte[] bytes, int offset, int length) throws IOException {
    if(!bitSet.get(record)) return false;
    ByteBuffer bb = findBuffer(record);
    record = truncate(record);
    bb.position(record * blockSize);
    bb.put(bytes, offset, length > blockSize ? blockSize : length);
    return true;
  }

  @Override
  public boolean updateRegion(int record, byte[] bytes) throws IOException{
    if(!mappedBitSet.get(record)) return false;
    mappedBlocks.position(record * mappedBlockSize);
    if(bytes.length > mappedBlockSize)
      mappedBlocks.put(bytes, 0, mappedBlockSize);
    else
      mappedBlocks.put(bytes);
    return true;
  }

  @Override
  public boolean updateRegion(int record, byte[] bytes, int offset, int length) throws IOException {
    if(!mappedBitSet.get(record)) return false;
    mappedBlocks.position(record * mappedBlockSize);
    int l = length > mappedBlockSize ? mappedBlockSize : length;
    mappedBlocks.put(bytes, offset, l);
    return true;
  }



}
