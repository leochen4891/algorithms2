import java.util.Hashtable;
import java.util.Map;

public class WordNet {
    private Digraph digraph;

    private Map<String, Bag<Integer>> nouns;
    private Map<Integer, String> indexes;
    
    private int[] unions;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {

        indexes = new Hashtable<Integer, String>();
        nouns = new Hashtable<String, Bag<Integer>>();

        // read synsets
        In in = new In(synsets);
        int lineCount = 0;
        String line;
        String[] strs;
        int index;
        while (in.hasNextLine()) {
            line = in.readLine();
            lineCount++;

            // a line looks like:
            // 71046,smoke fume,a cloud of fine particles suspended in a gas
            strs = line.split(",");
            index = Integer.parseInt(strs[0]);

            // indexes
            indexes.put(index, strs[1]);

            // nouns
            if (strs[1].contains(" ")) {
                for (String word : strs[1].split(" ")) {
                    Bag<Integer> bag = nouns.get(word);
                    if (null == bag)
                        bag = new Bag<Integer>();
                    bag.add(index);
                    nouns.put(word, bag);
                }
            } else {
                Bag<Integer> bag = nouns.get(strs[1]);
                if (null == bag)
                    bag = new Bag<Integer>();
                bag.add(index);
                nouns.put(strs[1], bag);
            }
        }

        digraph = new Digraph(lineCount);
        unions = new int[lineCount];
        for (int i = 0; i < lineCount; i++) {
            unions[i] = i;
        }

        // read hypernyms
        in = new In(hypernyms);
        while (in.hasNextLine()) {
            line = in.readLine();
            if (line.length() <= 0)
                continue;
            // looks like this:
            // 34,47569,48084
            strs = line.split(",");
            index = Integer.parseInt(strs[0]);
            for (int i = 1; i < strs.length; i++) {
                int target = Integer.parseInt(strs[i]);
                digraph.addEdge(index, target);
                
                // unions
                int flag = unions[target];
                for (int j = 0; j < lineCount; j++) {
                    if (unions[j] == flag) {
                        unions[j] = unions[index];
                    }
                }
                unions[target] = unions[index];
            }
        }
        
        DirectedCycle cycle = new DirectedCycle(digraph);
        if (cycle.hasCycle())
            throw new IllegalArgumentException();
        
        int union = unions[0];
        for (int i = 0; i < lineCount; i++) {
            if (unions[i] != union)
                throw new IllegalArgumentException();
        }
        
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return nouns.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        return nouns.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException();
        }
        Bag<Integer> indexA = nouns.get(nounA);
        Bag<Integer> indexB = nouns.get(nounB);

        SAP sap = new SAP(digraph);
        int dist = sap.length(indexA, indexB);
        return dist;
    }

    // a synset (second field of synsets.txt) that is the common ancestor of
    // nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException();
        }
        Bag<Integer> indexA = nouns.get(nounA);
        Bag<Integer> indexB = nouns.get(nounB);

        SAP sap = new SAP(digraph);
        int ancestor = sap.ancestor(indexA, indexB);
        String str = null;
        if (ancestor >= 0)
            str = indexes.get(ancestor);
        return str;
    }

    // for unit testing of this class
    public static void main(String[] args) {
        WordNet net = new WordNet("synsets.txt", "hypernyms.txt");
        //WordNet net = new WordNet("synsets_wordnet.txt", "hypernyms_wordnet.txt");
        for (String str : net.nouns()) {
            StdOut.println(str);
        }
        StdOut.println(net.digraph.toString());
    }
}
