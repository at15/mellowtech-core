package org.mellowtech.core.example;

import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.DiscBasedMap;
import org.mellowtech.core.collections.KeyValue;
import org.mellowtech.core.collections.mappings.IntegerMapping;
import org.mellowtech.core.collections.mappings.StringMapping;
import org.mellowtech.core.collections.tree.BTree;
import org.mellowtech.core.collections.tree.BTreeBuilder;

import java.io.*;
import java.util.Iterator;

/**
 * Created by at15 on 15-12-20.
 * <p/>
 * play with the BPTree
 */
public class BPTree {
    public static class SortedFileIterator implements Iterator<KeyValue<CBInt, CBString>> {
        private BufferedReader br;
        private String cachedLine;
        private Boolean end;

        public SortedFileIterator(BufferedReader br) {
            this.br = br;
            this.end = false;
        }

        public boolean hasNext() {
            if (end) {
                return false;
            }
            try {
                cachedLine = br.readLine();
                if (cachedLine != null) {
                    return true;
                } else {
                    return false;
                }
            } catch (IOException ignore) {
                end = true;
                return false;
            }
        }

        public KeyValue<CBInt, CBString> next() {
            // TODO: how to handle no more next?
            String line;
            if (cachedLine != null) {
                line = cachedLine;
                cachedLine = null;
            } else {
                try {
                    line = br.readLine();
                    if (line == null) {
                        end = true;
                        return null;
                    }
                } catch (IOException ignore) {
                    end = true;
                    return null;
                }
            }
            // parse the line and return value
            String[] splits = line.split(",");
            return new KeyValue<>(new CBInt(Integer.valueOf(splits[0])),
                    new CBString(line));
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static void main(String[] args) throws Exception {
        BTree bt = new BTreeBuilder().valuesInMemory(true)
                .build(new CBInt(), new CBString(), "test-bp");

//        BufferedReader br = new BufferedReader(new FileReader("data/sub-data.txt"));
//        SortedFileIterator iterator = new SortedFileIterator(br);
//        bt.createIndex(iterator);
//        bt.save();
        System.out.println(bt.get(new CBInt(1)));
    }
}
