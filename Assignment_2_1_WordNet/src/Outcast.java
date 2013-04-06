public class Outcast {
    private WordNet mWordNet;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        mWordNet = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        int size = nouns.length;
        if (size < 1)
            return null;
        if (size == 1)
            return nouns[0];

        int[] dists = new int[size];
        for (int i = 0; i < size; i++) {
            dists[i] = 0;
            for (int j = 0; j < size; j++) {
                int dist = mWordNet.distance(nouns[i], nouns[j]);
                // StdOut.println(nouns[i] + " and " + nouns[j] + " ---> " +
                // dist);

                dists[i] += dist;
            }
        }

        int max = 0;
        for (int i = 0; i < size; i++) {
            if (dists[i] > dists[max])
                max = i;
        }

        return nouns[max];
    }

    // for unit testing of this class (such as the one below)
    public static void main(String[] args) {
        WordNet net = new WordNet("synsets_wordnet.txt", "hypernyms_wordnet.txt");
        Outcast outcast = new Outcast(net);

        String[] strs1 = "horse zebra cat bear table".split(" ");
        String[] strs2 = "water soda bed orange_juice milk apple_juice tea coffee"
                .split(" ");

        String ret = outcast.outcast(strs1);
        StdOut.println("1:" + ret);

        ret = outcast.outcast(strs2);
        StdOut.println("2:" + ret);
    }
}
