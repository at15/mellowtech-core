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
//            String[] splits = line.split(",");
            String[] splits = line.split("\\t");
            return new KeyValue<>(new CBInt(Integer.valueOf(splits[0])),
                    new CBString(line));
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    public static void local() throws Exception {
        BTree bt = new BTreeBuilder().valuesInMemory(true)
                .build(new CBInt(), new CBString(), "test-bp");

//        BufferedReader br = new BufferedReader(new FileReader("data/part-r-00000.txt"));
//        SortedFileIterator iterator = new SortedFileIterator(br);
//        bt.createIndex(iterator);
//        bt.save();
        System.out.println(bt.get(new CBInt(1)));
        System.out.println(bt.get(new CBInt(2)));
        System.out.println(bt.get(new CBInt(3)));
//        System.out.println(bt.getKey(0));
        // FIXME: isEmpty always return true ...
        System.out.println(bt.isEmpty()); // the tree is not saved, so it's true
        System.out.println(bt.toString());
    }

    public static void tmp() throws Exception {
        BTree bt = new BTreeBuilder()
                // NOTE: do NOT pass the .idx
                .build(new CBInt(), new CBString(), "/tmp/tree-index/L3VzZXIvYXQxNS90cmVlLWluZGV4L291dC1zb3J0L3BhcnQtci0wMDAwMA==");
//                .build(new CBInt(), new CBString(), "/tmp/tree-index/a.idx");
//        System.out.println(bt.get(new CBInt(100)));
//        System.out.println(bt.get(new CBInt(300)));
        System.out.println(bt.get(new CBInt(1)));
//        System.out.println(bt.getKey(0));

        System.out.println(bt.isEmpty()); // it's false now, when open the write file
//        System.out.println(bt.toString());
    }

    public static void main(String[] args) throws Exception {
        local();
        tmp();
    }
}
