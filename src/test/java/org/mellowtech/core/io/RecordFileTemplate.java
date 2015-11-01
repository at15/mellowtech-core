package org.mellowtech.core.io;

import org.junit.*;
import org.mellowtech.core.TestUtils;
import org.mellowtech.core.bytestorable.*;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by msvens on 24/10/15.
 */
public abstract class RecordFileTemplate {

  public RecordFile rf;
  public abstract String fname();
  public static String dir = "rftests";
  public static int maxBlocks = 10;
  public static int blockSize = 1024;
  public static int reserve = 1024;

  public static String testString = "01234567ABCDEFGH";
  public static byte[] testBlock;

  static {
    StringBuilder stringBuilder = new StringBuilder();
    for(int i = 0; i < 64; i++){
      stringBuilder.append(testString);
    }
    testBlock = stringBuilder.toString().getBytes();
  }

  public abstract RecordFile init(int blockSize, int reserve, int maxBlocks, String fname) throws Exception;

  public abstract RecordFile reopen(String fname) throws Exception;

  @BeforeClass
  public static void createDir(){
    TestUtils.deleteTempDir(dir);
    TestUtils.createTempDir(dir);
  }

  @AfterClass
  public static void rmDir(){
    TestUtils.createTempDir(dir);
  }

  @Before
  public void setup() throws Exception{
    rf = init(blockSize,reserve,maxBlocks, TestUtils.getAbsolutDir(dir+"/"+fname()));
  }

  @After
  public void after() throws Exception{
    rf.close();
    TestUtils.getTempFile(dir,fname()).delete();
  }

  /**COMMON TEST CASES**/
  @Test
  public void blockSize(){
    Assert.assertEquals(blockSize, rf.getBlockSize());
  }

  @Test
  public void reserveSize() throws IOException{
    Assert.assertEquals(reserve, rf.getReserve().length);
  }

  /****TESTS WITH ZERO ELEMENTS*********/
  @Test
  public void zeroSize(){
    Assert.assertEquals(0, rf.size());
  }

  @Test
  public void zeroFree(){
    Assert.assertEquals(maxBlocks, rf.getFreeBlocks());
  }

  @Test
  public void zeroIterator(){
    Assert.assertFalse(rf.iterator().hasNext());
  }

  @Test
  public void zeroContains() throws Exception{
    Assert.assertFalse(rf.contains(0));
  }

  @Test
  public void zeroFirstRecord() throws Exception {
    Assert.assertEquals(-1, rf.getFirstRecord());
  }

  @Test
  public void zeroGet() throws Exception {
    Assert.assertNull(rf.get(0));
  }

  @Test
  public void zeroGetBoolean() throws Exception {
    byte b[] = new byte[blockSize];
    Assert.assertFalse(rf.get(0, b));
  }

  @Test
  public void zeroReopen() throws Exception {
    rf.close();
    rf = reopen(TestUtils.getAbsolutDir(dir+"/"+fname()));
    Assert.assertNull(rf.get(0));
  }

  @Test
  public void zeroUpdate() throws Exception {
    byte b[] = new byte[blockSize];
    Assert.assertFalse(rf.update(0,b));
  }

  @Test
  public void zeroDelete() throws Exception {
    Assert.assertFalse(rf.delete(0));
    Assert.assertEquals(0, rf.size());
  }

  /****TESTS WITH 1 ELEMENT*************/

  @Test
  public void oneSize() throws Exception{
    rf.insert(testBlock);
    Assert.assertEquals(1, rf.size());
  }

  @Test
  public void oneFree() throws Exception{
    rf.insert(testBlock);
    Assert.assertEquals(maxBlocks - 1, rf.getFreeBlocks());
  }

  @Test
  public void oneIterator() throws Exception{
    rf.insert(testBlock);
    Assert.assertTrue(rf.iterator().hasNext());
  }

  @Test
  public void oneContains() throws Exception{
    rf.insert(testBlock);
    Assert.assertTrue(rf.contains(0));
  }

  @Test
  public void oneFirstRecord() throws Exception {
    rf.insert(testBlock);
    Assert.assertEquals(0, rf.getFirstRecord());
  }

  @Test
  public void oneGet() throws Exception {
    rf.insert(testBlock);
    Assert.assertEquals(new String(testBlock), new String(rf.get(0)));
  }

  @Test
  public void oneGetBoolean() throws Exception {
    rf.insert(testBlock);
    byte b[] = new byte[blockSize];
    Assert.assertTrue(rf.get(0, b));
    Assert.assertEquals(new String(testBlock), new String(b));
  }

  @Test
  public void oneReopen() throws Exception {
    rf.insert(testBlock);
    rf.close();
    rf = reopen(TestUtils.getAbsolutDir(dir+"/"+fname()));
    Assert.assertEquals(new String(testBlock), new String(rf.get(0)));
  }

  @Test
  public void oneUpdate() throws Exception {
    rf.insert(testBlock);
    byte b[] = new byte[blockSize];
    b[0] = 1;
    Assert.assertTrue(rf.update(0,b));
  }

  @Test
  public void oneDelete() throws Exception {
    rf.insert(testBlock);
    Assert.assertTrue(rf.delete(0));
    Assert.assertEquals(0, rf.size());
  }

  /**Test with last record added***/
  @Test
  public void lastSize() throws Exception{
    rf.insert(maxBlocks-1, testBlock);
    Assert.assertEquals(1, rf.size());
  }

  @Test
  public void lastFree() throws Exception{
    rf.insert(maxBlocks-1, testBlock);
    Assert.assertEquals(maxBlocks - 1, rf.getFreeBlocks());
  }

  @Test
  public void lastIterator() throws Exception{
    rf.insert(maxBlocks-1, testBlock);
    Assert.assertTrue(rf.iterator().hasNext());
  }

  @Test
  public void lastContains() throws Exception{
    rf.insert(maxBlocks-1, testBlock);
    Assert.assertTrue(rf.contains(maxBlocks-1));
  }

  @Test
  public void lastFirstRecord() throws Exception {
    rf.insert(maxBlocks-1, testBlock);
    Assert.assertEquals(maxBlocks-1, rf.getFirstRecord());
  }

  @Test
  public void lastGet() throws Exception {
    rf.insert(maxBlocks-1, testBlock);
    Assert.assertEquals(new String(testBlock), new String(rf.get(maxBlocks-1)));
  }

  @Test
  public void lastGetBoolean() throws Exception {
    rf.insert(maxBlocks-1,testBlock);
    byte b[] = new byte[blockSize];
    Assert.assertTrue(rf.get(maxBlocks-1, b));
    Assert.assertEquals(new String(testBlock), new String(b));
  }

  @Test
  public void lastReopen() throws Exception {
    rf.insert(maxBlocks-1, testBlock);
    rf.close();
    rf = reopen(TestUtils.getAbsolutDir(dir+"/"+fname()));
    Assert.assertEquals(new String(testBlock), new String(rf.get(maxBlocks-1)));
  }

  @Test
  public void lastUpdate() throws Exception {
    rf.insert(maxBlocks-1,testBlock);
    byte b[] = new byte[blockSize];
    b[0] = 1;
    Assert.assertTrue(rf.update(maxBlocks-1,b));
  }

  @Test
  public void lastDelete() throws Exception {
    rf.insert(maxBlocks-1,testBlock);
    Assert.assertTrue(rf.delete(maxBlocks-1));
    Assert.assertEquals(0, rf.size());
  }

  private void fillFile() throws Exception {
    for(int i = 0; i < maxBlocks; i++)
      rf.insert(testBlock);
  }
  /**Test with all records added***/
  @Test
  public void allSize() throws Exception{
    fillFile();
    Assert.assertEquals(maxBlocks, rf.size());
  }

  @Test
  public void allFree() throws Exception{
    fillFile();
    Assert.assertEquals(0, rf.getFreeBlocks());
  }

  @Test
  public void allIterator() throws Exception{
    fillFile();
    int i = 0;
    Iterator<Record> iter = rf.iterator();
    while(iter.hasNext()){
      i++;
      iter.next();
    }
    Assert.assertEquals(maxBlocks, i);
  }

  @Test
  public void allContains() throws Exception{
    fillFile();
    for(int i = 0; i < maxBlocks; i++){
      Assert.assertTrue(rf.contains(i));
    }
  }

  @Test
  public void allFirstRecord() throws Exception {
    fillFile();
    Assert.assertEquals(0, rf.getFirstRecord());
  }

  @Test
  public void allGet() throws Exception {
    fillFile();
    for(int i = 0; i < maxBlocks; i++) {
      Assert.assertEquals(new String(testBlock), new String(rf.get(i)));
    }
  }

  @Test
  public void allGetBoolean() throws Exception {
    fillFile();
    for(int i = 0; i < maxBlocks; i++){
      byte b[] = new byte[blockSize];
      Assert.assertTrue(rf.get(i, b));
      Assert.assertEquals(new String(testBlock), new String(b));
    }
  }

  @Test
  public void allReopen() throws Exception {
    fillFile();
    rf.close();
    rf = reopen(TestUtils.getAbsolutDir(dir+"/"+fname()));
    for(int i = 0; i < maxBlocks; i++) {
      Assert.assertEquals(new String(testBlock), new String(rf.get(i)));
    }
  }


  @Test
  public void allUpdate() throws Exception {
    fillFile();
    for(int i = 0; i < maxBlocks; i++) {
      byte b[] = new byte[blockSize];
      b[0] = 1;
      Assert.assertTrue(rf.update(i, b));
    }
  }

  @Test
  public void allDelete() throws Exception {
    fillFile();
    for(int i = 0; i < maxBlocks; i++) {
      Assert.assertTrue(rf.delete(i));
      Assert.assertEquals(maxBlocks - 1 - i, rf.size());
    }
  }

  /****Test Error paths**********************/

  @Test
  public void insertLargeBlock() throws Exception {
    byte b[] = new byte[blockSize+1];
    rf.insert(b);
    Assert.assertEquals(1, rf.size());
  }

  @Test
  public void insertNull() throws Exception {
    rf.insert(null);
    Assert.assertEquals(1, rf.size());
  }

  @Test(expected = Exception.class)
  public void insertInFull() throws Exception {
    fillFile();
    rf.insert(testBlock);
  }

  @Test(expected = Exception.class)
  public void insertOutOfRange() throws Exception {
    rf.insert(maxBlocks, testBlock);
  }


}
