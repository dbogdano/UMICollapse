package algo;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.BitSet;

import data.ParallelDataStructure;
import util.ReadFreq;
import util.Read;

public class ParallelConnectedComponents implements Algorithm{
    @Override
    public List<Read> apply(Map<BitSet, ReadFreq> reads, ParallelDataStructure data, int umiLength, int k, int threadCount){
        Map<BitSet, Integer> m = new HashMap<>();
        BitSet[] idxToUMI = new BitSet[reads.size()];

        int idx = 0;

        for(Map.Entry<BitSet, ReadFreq> e : reads.entrySet()){
            m.put(e.getKey(), e.getValue().freq);
            idxToUMI[idx++] = e.getKey();
        }

        data.init(m, umiLength, k);

        List<List<BitSet>> adjIdx = new ArrayList<>();

        for(int i = 0; i < m.size(); i++)
            adjIdx.add(null);

        ForkJoinPool pool = new ForkJoinPool(threadCount); // custom pool for custom thread count

        pool.submit(() -> IntStream.range(0, m.size()).parallel()
                    .forEach(i -> adjIdx.set(i, data.near(idxToUMI[i], k, Integer.MAX_VALUE)))).get();

        Map<BitSet, List<BitSet>> adj = new HashMap<>();

        for(int i = 0; i < adjIdx.size(); i++)
            adj.put(idxToUMI[i], adjIdx.get(i));

        List<Read> res = new ArrayList<>();
        Set<BitSet> visited = new HashSet<>();

        for(BitSet umi : m.keySet()){
            if(!visited.contains(umi))
                res.add(visitAndRemove(umi, reads, adj, visited, k).read);
        }

        return res;
    }

    private ReadFreq visitAndRemove(BitSet u, Map<BitSet, ReadFreq> reads, Map<BitSet, List<BitSet>> adj, Set<BitSet> visited, int k){
        if(visited.contains(u))
            return null;

        ReadFreq max = reads.get(u);
        List<BitSet> c = adj.get(u);
        visited.add(u);

        for(BitSet v : c){
            if(u.equals(v))
                continue;

            ReadFreq r = visitAndRemove(v, reads, adj, visited, k);

            if(r != null && r.freq > max.freq)
                max = r;
        }

        return max;
    }
}