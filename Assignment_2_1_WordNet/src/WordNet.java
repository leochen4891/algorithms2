import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public class WordNet {
    private List<Bag<Integer>> adj;
    
    private Map<String, Integer> nouns;
    private Map<Integer, String> indexes;
    
    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        
        adj = new ArrayList<Bag<Integer>>();
        indexes = new Hashtable<Integer, String>();
        nouns = new Hashtable<String, Integer>();
        
        // read synsets
        In in = new In(synsets);
        String line;
        String[] strs;
        Integer index;
        while (in.hasNextLine()) {
            line = in.readLine();
            // a line looks like:
            // 71046,smoke fume,a cloud of fine particles suspended in a gas   
            strs = line.split(",");
            index = Integer.parseInt(strs[0]);
            
            // adj
            adj.add(new Bag<Integer>());
            
            // indexes
            indexes.put(index, strs[1]);
            
            // nouns
            if (strs[1].contains(" ")) {
                for (String word:strs[1].split(" ")) {
                    nouns.put(word, index);
                }
            } else {
                nouns.put(strs[1], index);
            }
        }
        
        // read hypernyms
        in = new In(hypernyms);
        while(in.hasNextLine()) {
            line = in.readLine();
            // looks like this:
            // 34,47569,48084
            strs = line.split(",");
            index = Integer.parseInt(strs[0]);
            for (int i = 1; i < strs.length;i++) {
                adj.get(index).add(Integer.parseInt(strs[i]));
            }
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
        
        int indexA = nouns.get(nounA);
        int indexB = nouns.get(nounB);
        
        return -1;
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException();
        }
        return null;
    }

    // for unit testing of this class
    public static void main(String[] args) {
        WordNet net = new WordNet("synsets.txt", "hypernyms.txt");
        for (String str: net.nouns()) {
            StdOut.println(str);
        }
        
        String test = "1860s";
        StdOut.println("contains " + test + "? " + (net.isNoun(test)?"yes":"no"));
    }
}
