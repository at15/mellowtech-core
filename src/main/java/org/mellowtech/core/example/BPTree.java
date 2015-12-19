package org.mellowtech.core.example;

import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.DiscBasedMap;
import org.mellowtech.core.collections.mappings.IntegerMapping;
import org.mellowtech.core.collections.mappings.StringMapping;
import org.mellowtech.core.collections.tree.BTree;
import org.mellowtech.core.collections.tree.BTreeBuilder;

/**
 * Created by at15 on 15-12-20.
 *
 * play with the BPTree
 */
public class BPTree {
    public static void main(String[] args) throws Exception {
        BTree bt = new BTreeBuilder().valuesInMemory(true)
//        BTree bt = new BTreeBuilder()
                .build(new CBString(), new CBString(),"test-bp");
        bt.put(new CBString("jack"),new CBString("is a good man"));
        bt.put(new CBString("arrow"),new CBString("is a good man2"));
        bt.put(new CBString("sunny"),new CBString("is a good man3"));
        bt.save(); // Forgot to call save ...  e ..
        System.out.println(bt.get(new CBString("jack")));
        System.out.println(bt.get(new CBString("arrow")));
        System.out.println(bt.get(new CBString("sunny")));

//        DiscBasedMap<String, Integer> dbMap = new DiscBasedMap<>(new StringMapping(), new IntegerMapping(),
//                "test-dm", false, false);
    }
}
