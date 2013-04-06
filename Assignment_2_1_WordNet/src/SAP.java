public class SAP {
    private Digraph digraph;

    private int[] distV;
    private int[] distW;
    private int possibleAncestor;
    private int possibleLength;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        digraph = G;
        distV = new int[digraph.V()];
        distW = new int[digraph.V()];
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        int ancestor = ancestor(v, w);
        if (ancestor >= 0)
            return distV[ancestor] + distW[ancestor];
        return -1;
    }

    // a common ancestor of v and w that participates in a shortest ancestral
    // path; -1 if no such path
    public int ancestor(int v, int w) {
        for (int i = 0; i < digraph.V(); i++) {
            distV[i] = -1;
            distW[i] = -1;
        }
        possibleAncestor = -1;
        possibleLength = -1;

        distV[v] = 0;
        distW[w] = 0;

        if (v == w)
            return v;

        Queue<Integer> qv = new Queue<Integer>();
        Queue<Integer> qw = new Queue<Integer>();

        qv.enqueue(v);
        qw.enqueue(w);

        return findAncestor(qv, qw, 0);
    }

    // length of shortest ancestral path between any vertex in v and any vertex
    // in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        int ancestor = ancestor(v, w);
        if (ancestor >= 0)
            return distV[ancestor] + distW[ancestor];
        return -1;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no
    // such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        for (int i = 0; i < digraph.V(); i++) {
            distV[i] = -1;
            distW[i] = -1;
        }
        possibleAncestor = -1;
        possibleLength = -1;

        for (int i : v) {
            for (int j : w) {
                if (i == j) {
                    distV[i] = 0;
                    distW[j] = 0;
                    return i;
                }
            }
        }

        Queue<Integer> qv = new Queue<Integer>();
        Queue<Integer> qw = new Queue<Integer>();

        for (int cur : v) {
            qv.enqueue(cur);
            distV[cur] = 0;
        }
        for (int cur : w) {
            qw.enqueue(cur);
            distW[cur] = 0;
        }

        return findAncestor(qv, qw, 0);
    }

    private int findAncestor(Queue<Integer> qv, Queue<Integer> qw, int depth) {
        /*
         * String strV = ""; for (int i : qv) { strV += " " + i; } String strW =
         * ""; for (int i : qw) { strW += " " + i; }
         * StdOut.println("findAncestor " + strV + " and " + strW +
         * " , possible = " + possibleAncestor); //
         */

        if (depth > digraph.V()) {
            if (possibleAncestor >= 0)
                return possibleAncestor;
            return -1;
        }

        int qvSize = qv.size();
        int qwSize = qw.size();
        int cur;
        int dist;
        int maxDistV = -1;
        int maxDistW = -1;
        // replace items in qv and qw with their adjacents, and update distance
        // array
        for (int i = 0; i < qvSize; i++) {
            cur = qv.dequeue();
            dist = distV[cur];
            maxDistV = Math.max(maxDistV, dist);
            for (int adj : digraph.adj(cur)) {
                qv.enqueue(adj);
                if (distV[adj] < 0)
                    distV[adj] = dist + 1;

                // check if adj is reachable to qw
                if (distW[adj] >= 0) {
                    int length = distV[adj] + distW[adj];
                    if (possibleLength < 0 || possibleLength >= length) {
                        possibleLength = length;
                        possibleAncestor = adj;
                    }
                }
            }
        }

        for (int i = 0; i < qwSize; i++) {
            cur = qw.dequeue();
            dist = distW[cur];
            maxDistW = Math.max(maxDistW, dist);
            for (int adj : digraph.adj(cur)) {
                qw.enqueue(adj);
                if (distW[adj] < 0)
                    distW[adj] = dist + 1;

                // check if adj is reachable to qw
                if (distV[adj] >= 0) {
                    int length = distV[adj] + distW[adj];
                    if (possibleLength < 0 || possibleLength > length) {
                        possibleLength = length;
                        possibleAncestor = adj;
                    }
                }
            }
        }

        if (possibleAncestor >= 0 && maxDistV >= possibleLength
                && maxDistW >= possibleLength) {
            return possibleAncestor;
        }

        if (qv.size() > 0 || qw.size() > 0) {
            return findAncestor(qv, qw, depth + 1);
        }

        return possibleAncestor;
    }

    // for unit testing of this class (such as the one below)
    public static void main(String[] args) {
        In in = new In("digraph3.txt");
        Digraph digraph = new Digraph(in);
        StdOut.println(digraph.toString());

        SAP sap = new SAP(digraph);

        for (int i = 0; i < digraph.V(); i++) {
            for (int j = 0; j < digraph.V(); j++) {
                StdOut.println("" + i + " and " + j + " ancstr ---> "
                        + sap.ancestor(i, j));
            }
        }

        /*
         * int i, j; i = 13; j = 9; StdOut.println("" + i + " and " + j +
         * " ancstr ---> " + sap.ancestor(i, j)); StdOut.println("" + i +
         * " and " + j + " length ---> " + sap.length(i, j));
         */

    }

}
