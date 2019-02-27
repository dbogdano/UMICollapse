package data;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

import fastq.Read;
import static util.Utils.charGet;
import static util.Utils.charSet;

public class Trie{
    private int umiLength;
    private Node root;

    public Trie(Set<BitSet> s, int umiLength){
        this.umiLength = umiLength;

        root = new Node();

        for(BitSet umi : s)
            insert(umi);
    }

    @Override
    public List<BitSet> removeNear(BitSet umi, int k){
        List<BitSet> res = new ArrayList<>();
        recursiveRemoveNear(umi, 0, root, k, new BitSet(), res);
        return res;
    }

    private void recursiveRemoveNear(BitSet umi, int idx, Node currNode, int k, BitSet currStr, List<BitSet> res){
        if(k < 0)
            return;

        if(idx >= umiLength){
            res.add((BitSet)currStr.clone());
            currNode.setExists(false);
            return;
        }

        boolean exists = false;

        for(int c : ENCODING_IDX.keySet()){
            if(!currNode.exists(ENCODING_IDX.get(c)))
                continue;

            if(charEquals(umi, idx, c))
                recursiveRemoveNear(umi, idx + 1, currNode.get(i), k, charSet(currStr, idx, c), res);
            else
                recursiveRemoveNear(umi, idx + 1, currNode.get(i), k - 1, charSet(currStr, idx, c), res);

            exists |= currNode.exists(i);
        }

        currNode.setExists(exists);
    }

    private void insert(BitSet umi){
        Node curr = root;

        for(int i = 0; i < umiLength; i++){
            int nextIdx = Read.ENCODING_IDX.get(charGet(umi, i));
            curr = curr.ensureCreated(nextIdx);
        }
    }

    private static class Node{
        private Node[] c;
        private boolean exists;

        Node(){
            this.c = null;
            this.exists = true;
        }

        Node ensureCreated(int idx){
            if(c = null)
                c = new Node[Read.ENCODING_MAP.size()];

            if(c[idx] == null)
                c[idx] = new Node();

            return c[idx];
        }

        boolean exists(int idx){
            return c[idx] != null && c[idx].exists;
        }

        void setExists(boolean exists){
            this.exists = exists;
        }

        Node get(int idx){
            return c[idx];
        }
    }
}