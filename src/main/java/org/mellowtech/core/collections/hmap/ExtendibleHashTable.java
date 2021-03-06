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
package org.mellowtech.core.collections.hmap;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.ByteStorable;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.disc.SpanningBlockFile;
import org.mellowtech.core.io.Record;
import org.mellowtech.core.io.RecordFile;
import org.mellowtech.core.io.RecordFileBuilder;
import org.mellowtech.core.util.DataTypeUtils;



public class ExtendibleHashTable <K extends ByteStorable <?>, V extends ByteStorable <?>> {
  public static final int VERSION = 10;

	private static final String dirExt = ".directory";
	private static final String bucketExt = ".buckets";
	private int dirDepth;
	private int bucketSize;
	private int maxKeyValueSize;
	//private SpanningBlockFile bucketFile;
	private RecordFile bucketFile;
	private int[] directory;
	private KeyValue <K, V> dbKey;
	private Bucket <K, V> bucketTemplate;
	private String fName;
	private K keyType;
	private V valueType;
	private int lastCreatedBucket;
  public int size;

	private Object fLocker = new Object();

	public ExtendibleHashTable(String fName) throws Exception {
		this.fName = fName;
		bucketTemplate = new Bucket <> ();
		openFiles();
	}

	public ExtendibleHashTable(String fName, K keyType,
			V valueType, int bucketSize, int maxKeyValueSize)
			throws Exception {
		this.fName = fName;
		
		this.keyType = keyType;
		this.valueType = valueType;
		this.bucketSize = bucketSize;
		this.maxKeyValueSize = maxKeyValueSize;
		dbKey = new KeyValue <> (keyType, valueType);
		bucketTemplate = new Bucket <> (dbKey);
		dirDepth = 0;
		directory = new int[1];
		createHashFiles();
		Bucket <K,V> bucket = createNewBucket();
		directory[0] = lastCreatedBucket;
		writeBucket(directory[0], bucket);
    this.size = 0;
	}

	public void saveHashTable() throws IOException {
		synchronized (fLocker) {
			this.saveFiles();
		}
	}

  public void deleteHashTable() throws IOException{
    synchronized (fLocker){
      deleteHashFiles();
    }
  }

	public V search(K key) throws IOException {
		synchronized (fLocker) {
			KeyValue <K, V> keyValue = new KeyValue <>(key, null);
			int rrn = find(keyValue);
			Bucket <K, V> bucket = readBucket(rrn);
			KeyValue <K,V> tmp = bucket.getKey(keyValue);
			if (tmp != null)
				return tmp.getValue();
		}
		return null;
	}

	public boolean containsKey(K key) throws IOException {
		synchronized (fLocker) {
			KeyValue <K,V> keyValue = new KeyValue <> (key, null);
			int rrn = find(keyValue);
			Bucket <K,V> bucket = readBucket(rrn);
      return bucket.getKey(keyValue) != null;
    }
	}

  public int getNumberOfElements(){
    return this.size;
  }

  public boolean isEmpty(){
    return this.size == 0 ? true : false;
  }

	public V insert(K key, V value, boolean update)
			throws IOException {
		synchronized (fLocker) {
			return _insert(key, value, update);
		}
	}

	private V _insert(K key, V value, boolean update)
			throws IOException {
		// first check the maxSize:
		if (key.byteSize() + value.byteSize() > maxKeyValueSize)
			throw new IOException("Size of key and value to large");

		KeyValue <K,V> keyValue = new KeyValue <> (key, value);
		int rrn = find(keyValue);
		Bucket <K,V> bucket;
		try {
			bucket = readBucket(rrn);
		} catch (IOException e) {
			throw e;
		}
		// for now dont update:
    KeyValue <K,V> prev = bucket.getKey(keyValue);
    if(prev != null){
      if(update){
        bucket.addKey(keyValue);
        writeBucket(rrn, bucket);
        return prev.getValue();
      }
      else{
        throw new IOException("key already exists");
      }
    }
    //key did not exist:
		if (bucket.size() < bucketSize) {
			bucket.addKey(keyValue);
			size++;
			writeBucket(rrn, bucket);
			return null;
		} else {
      CoreLog.L().finest("Splitting bucket: "+ rrn + this);
      splitBucket(bucket, keyValue);
      CoreLog.L().finest(this.toString());
			return _insert(key, value, update);
		}
	}

	public Iterator <KeyValue <K,V>> iterator() {
		return new EHTIterator();
	}

	public KeyValue <K,V> delete(K key) throws IOException {
		synchronized (fLocker) {
			KeyValue <K,V> toDelete = new KeyValue <> (key, null);
			int rrn = find(toDelete);
			Bucket <K,V> bucket = readBucket(rrn);
			KeyValue <K,V> deleted = bucket.removeKey(toDelete);
			if (deleted == null)
				return null;
      size--;
			writeBucket(rrn, bucket);
			combine(bucket, rrn);
			return deleted;
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		//int currAddr = -1;
		int totSize = 0;
		int totBuckets = 0;
		for (int i = 0; i < directory.length; i++) {
			// if (currAddr != directory[i]) {
			//currAddr = directory[i];
			sb.append("index: " + i + " ");
			sb.append("bucket number: " + directory[i] + " ");
			try {
				int size = readBucket(directory[i]).size();
				totSize += size;
				totBuckets++;
				sb.append("bucket size: " + size + " ");
				sb.append("bucket load: "
						+ ((double) size / (double) bucketSize));
				sb.append("\n");
			} catch (IOException e) {
				sb.append("error reading bucket");
			}
			// }
		}
		sb.append("\n\nTotalSize: " + totSize + " total buckets: " + totBuckets
				+ " ");
		sb.append("total load: "
				+ ((double) totSize / (double) (totBuckets * bucketSize)));
		return sb.toString();
	}

	/** ***************PRIVATE METHODS******************************** */
	private int makeAdress(K key, int depth) {
		int hashVal = key.hashCode();
		int ret = 0;
		int mask = 1;
		int lowbit;
		for (int i = 0; i < depth; i++) {
			ret = ret << 1;
			lowbit = hashVal & mask;
			ret = ret | lowbit;
			hashVal = hashVal >> 1;
		}
		return ret;
	}

	/** *********for deletion************************************* */
	private void combine(Bucket <K,V> bucket, int rrn) throws IOException {
		int bAdress = findBuddy(bucket);
		if (bAdress == -1)
			return;
		int brrn = directory[bAdress];
		Bucket <K,V> bBucket = readBucket(brrn);
		if (bBucket.size() + bucket.size() <= bucketSize) {
			for (Iterator <KeyValue <K,V>> it = bBucket.iterator(); it.hasNext();) {
				bucket.addKey(it.next());
			}
			directory[bAdress] = rrn;
			writeBucket(rrn, bucket);
			deleteBucket(brrn);
			if (collapseDir())
				combine(bucket, rrn);
		}
	}

	private int findBuddy(Bucket <K,V> bucket) {
		if (dirDepth == 0)
			return -1;
		if (bucket.get().depth < dirDepth)
			return -1;
		int sharedAdress = makeAdress(bucket.getKey(0).getKey(), bucket.get().depth);
		return sharedAdress ^ 1;
	}

	private boolean collapseDir() {
		if (dirDepth == 0)
			return false;
		int dirSize = (int) Math.pow(2, dirDepth);
		for (int i = 0; i < (dirSize - 1); i = i + 2) {
			if (directory[i] != directory[i + 1])
				return false;
		}
		int nDirSize = dirSize / 2;
		int nDir[] = new int[nDirSize];
		for (int i = 0; i < nDirSize; i++)
			nDir[i] = directory[i * 2];
		directory = nDir;
		dirDepth--;
		return true;
	}

	private void splitBucket(Bucket <K,V> bucket, KeyValue <K,V> tmp) throws IOException {
		int bucketAddr = find(bucket.getKey(0));

			CoreLog.L().finest(DataTypeUtils.printBits((short) find(bucket
					.getKey(0))) + " " + (bucket.getKey(0)).getKey());

		if (bucket.get().depth == dirDepth) {
			doubleDir();
		}
		Bucket <K,V> newBucket = createNewBucket();
		Range r = findRange(bucket);

		CoreLog.L().finest("Bucket Range: " + r.from + " " + r.to + " "
					+ bucketAddr + " " + find(tmp));

		insertBucket(lastCreatedBucket, r);
		bucket.get().depth = bucket.get().depth + 1;
		newBucket.get().depth = bucket.get().depth;
		redistribute(bucket, newBucket, bucketAddr);
		writeBucket(bucketAddr, bucket);
		writeBucket(lastCreatedBucket, newBucket);
	}

	private void insertBucket(int bucketAddr, Range range) {
		for (int i = range.from; i <= range.to; i++)
			directory[i] = bucketAddr;
	}

	private Range findRange(Bucket <K,V> bucket) {
		int shared = makeAdress(bucket.getKey(0).getKey(), bucket.get().depth);
		int toFill = dirDepth - (bucket.get().depth + 1);
		int newShared = shared << 1;
		newShared = newShared | 1;
		Range r = new Range();
		r.to = r.from = newShared;
		for (int i = 0; i < toFill; i++) {
			r.from = r.from << 1;
			r.to = r.to << 1;
			r.to = r.to | 1;
		}
		return r;
	}

	private void redistribute(Bucket <K,V> oldBucket, Bucket <K,V> newBucket, int oldAddr) {
		KeyValue <K,V> keyValue;
		for (Iterator <KeyValue <K,V>> it = oldBucket.iterator(); it.hasNext();) {
			keyValue = it.next();
			if (find(keyValue) != oldAddr) {
				it.remove();
				newBucket.addLast(keyValue);
			}
		}
	}

	private void doubleDir() {
		int currentSize = (int) Math.pow(2, dirDepth);
		int newSize = currentSize * 2;
		int[] tmp = new int[newSize];
		for (int i = 0; i < currentSize; i++) {
			tmp[i * 2] = directory[i];
			tmp[(i * 2) + 1] = directory[i];
		}
		directory = tmp;
		dirDepth++;
	}

	private int find(KeyValue <K,V> kv) {
		return directory[makeAdress(kv.getKey(), dirDepth)];
	}

	private Bucket <K,V> createNewBucket() throws IOException {
		Bucket <K,V> b = new Bucket <> ();
		byte bb[] = new byte[b.byteSize()];
		b.toBytes(bb, 0);
		lastCreatedBucket = bucketFile.insert(bb);
		return b;
	}

	private Bucket <K, V> readBucket(int rrn) throws IOException {
		return (Bucket <K,V>) bucketTemplate.fromBytes(bucketFile.get(rrn), 0);
	}

	private void writeBucket(int rrn, Bucket <K, V> bucket) throws IOException {
		int bSize = bucket.byteSize();
		byte[] b = new byte[bSize];
		bucket.toBytes(b, 0);
		bucketFile.update(rrn, b, 0, bSize);
	}

	private void deleteBucket(int rrn) throws IOException {
		bucketFile.delete(rrn);
	}

	// private initializers:
	private void createHashFiles() throws IOException {
	  bucketFile = new RecordFileBuilder().span(true).blockSize(512).maxBlocks(1024*2048).build(fName + bucketExt);
	}

  private void openFiles() throws Exception{
    //retrive header information
    FileInputStream fis = new FileInputStream(fName + dirExt);
    TableHeader <K,V> th = new TableHeader <K,V> ();
    th.fromBytes(fis);

    //make sure version is the same!
    if(VERSION != th.get().getTableVersion())
      throw new IOException("wrong table version");

    this.bucketSize = th.get().getBucketSize();
    this.maxKeyValueSize = th.get().getMaxKeyValueSize();
    this.dirDepth = th.get().getDirDepth();
    this.keyType = th.getKeyType();
    this.valueType = th.getValueType();
    this.directory = th.getDirectory();
    this.size = th.get().getNumElements();

    //open bucket file
    bucketFile = new RecordFileBuilder().span(true).build(fName + bucketExt);

    // finally create various templates:
    dbKey = new KeyValue <> (keyType, valueType);
    bucketTemplate = new Bucket <> ();
    bucketTemplate.setKeyValueTemplate(dbKey);
  }

  private void saveFiles() throws IOException {
    bucketFile.save();
    TableHeader <K,V> th = new TableHeader <> ();
    th.get().setBucketSize(this.bucketSize);
    th.get().setDirDepth(this.dirDepth);
    th.setDirectory(this.directory);
    th.get().setTableVersion(VERSION);
    th.setKeyType(this.keyType);
    th.setValueType(this.valueType);
    th.get().setMaxKeyValueSize(this.maxKeyValueSize);
    th.get().setNumElements(this.size);
    FileOutputStream fos = new FileOutputStream(fName + dirExt);
    th.toBytes(fos);
    fos.close();
  }

  private void deleteHashFiles() throws IOException{
    bucketFile.clear();
    File f = new File(fName + dirExt);
    f.delete();

  }

	/**
	 * ****************inner
	 * classes*********************************************
	 */
	class EHTIterator implements Iterator <KeyValue<K, V>> {
		Iterator <Record> fileIterator = bucketFile.iterator();
		
		Bucket <K,V> bucket = new Bucket <> ();
		Iterator <KeyValue <K,V>> bucketIterator;
		byte b[];

		public EHTIterator() {
			if (!fileIterator.hasNext())
				return;
			b = fileIterator.next().data;
			bucket = (Bucket <K,V>) bucketTemplate.fromBytes(b, 0);
			bucketIterator = bucket.iterator();
		}

		public boolean hasNext() {
			if (bucketIterator.hasNext())
				return true;
			while (fileIterator.hasNext()) {
				b = fileIterator.next().data;
				bucket = (Bucket <K,V>) bucketTemplate.fromBytes(b, 0);
				bucketIterator = bucket.iterator();
				if (bucketIterator.hasNext())
					return true;
			}
			return false;
		}

		public KeyValue <K,V> next() {
			return bucketIterator.next();
		}

		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}
}

class Range {
	int from, to;
}
