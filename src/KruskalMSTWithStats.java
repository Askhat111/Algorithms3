import com.google.gson.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class KruskalMSTWithStats {

    static class Edge implements Comparable<Edge> {
        int v, w;
        double weight;

        Edge(int v, int w, double weight) {
            this.v = v; this.w = w; this.weight = weight;
        }

        int either() { return v; }
        int other(int vertex) { return vertex == v ? w : v; }

        public int compareTo(Edge that) {
            return Double.compare(this.weight, that.weight);
        }

        public String toString() {
            return String.format("%d-%d %.2f", v, w, weight);
        }
    }

    static class UF {
        private int[] parent, size;
        int count;
        int unionCount = 0;
        int findCount = 0;

        UF(int n) {
            count = n;
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                size[i] = 1;
            }
        }

        int find(int p) {
            findCount++;
            while (p != parent[p]) {
                parent[p] = parent[parent[p]];  // path compression
                p = parent[p];
                findCount++;
            }
            return p;
        }

        boolean connected(int p, int q) {
            return find(p) == find(q);
        }

        void union(int p, int q) {
            unionCount++;
            int rootP = find(p);
            int rootQ = find(q);
            if (rootP == rootQ) return;
            if (size[rootP] < size[rootQ]) {
                parent[rootP] = rootQ;
                size[rootQ] += size[rootP];
            } else {
                parent[rootQ] = rootP;
                size[rootP] += size[rootQ];
            }
            count--;
        }
    }

    List<Edge> mst = new ArrayList<>();
    double totalWeight = 0;
    int edgeComparisons = 0;
    int vertices;
    int edgesCount;
    long executionTimeMs;

    public void runKruskal(List<Edge> edges, int vertices) {
        this.vertices = vertices;
        this.edgesCount = edges.size();

        long start = System.nanoTime();

        Collections.sort(edges);
        UF uf = new UF(vertices);

        for (Edge e : edges) {
            edgeComparisons++;
            int v = e.either();
            int w = e.other(v);

            if (!uf.connected(v, w)) {
                uf.union(v, w);
                mst.add(e);
                totalWeight += e.weight;
            }
            if (mst.size() == vertices - 1) break;
        }

        long end = System.nanoTime();
        executionTimeMs = (end - start) / 1_000_000;
    }

    public void printResults() {
        System.out.println("MST Edges:");
        for (Edge e : mst)
            System.out.println(e);

        System.out.printf("Total MST cost: %.2f\n", totalWeight);
        System.out.println("Vertices: " + vertices);
        System.out.println("Edges: " + edgesCount);
        System.out.println("Comparisons: " + edgeComparisons);
        System.out.println("Union operations: " + mst.size() - 1);
        System.out.println("Find operations (approx): " + (mst.size() * 2));  // rough estimate
        System.out.printf("Execution time: %d ms\n", executionTimeMs);
    }

    // Load input graph data from JSON file
    public static List<Edge> loadGraphFromJson(String filename, Map<String, Integer> vertexMap) throws IOException {
        List<Edge> edges = new ArrayList<>();
        JsonObject root = JsonParser.parseReader(new FileReader(filename)).getAsJsonObject();

        JsonArray nodes = root.getAsJsonArray("nodes");
        int index = 0;
        for (JsonElement node : nodes) {
            vertexMap.put(node.getAsString(), index++);
        }

        JsonArray edgeArray = root.getAsJsonArray("edges");
        for (JsonElement edgeElem : edgeArray) {
            JsonObject edgeObj = edgeElem.getAsJsonObject();
            int v = vertexMap.get(edgeObj.get("from").getAsString());
            int w = vertexMap.get(edgeObj.get("to").getAsString());
            double weight = edgeObj.get("weight").getAsDouble();
            edges.add(new Edge(v, w, weight));
        }
        return edges;
    }

    // Save output in JSON format
    public void saveResultsToJson(String filename, Map<Integer, String> reverseVertexMap) throws IOException {
        JsonObject output = new JsonObject();

        output.addProperty("vertices", vertices);
        output.addProperty("edges", edgesCount);
        output.addProperty("totalCost", totalWeight);
        output.addProperty("operationCount", edgeComparisons);
        output.addProperty("executionTimeMs", executionTimeMs);

        JsonArray mstEdgesJson = new JsonArray();
        for (Edge e : mst) {
            JsonObject edgeObj = new JsonObject();
            edgeObj.addProperty("from", reverseVertexMap.get(e.v));
            edgeObj.addProperty("to", reverseVertexMap.get(e.w));
            edgeObj.addProperty("weight", e.weight);
            mstEdgesJson.add(edgeObj);
        }
        output.add("mstEdges", mstEdgesJson);

        try (FileWriter writer = new FileWriter(filename)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(output, writer);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java KruskalMSTWithStats <input-json-file> <output-json-file>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        Map<String, Integer> vertexMap = new HashMap<>();
        List<Edge> edges = loadGraphFromJson(inputFile, vertexMap);

        KruskalMSTWithStats kruskal = new KruskalMSTWithStats();
        kruskal.runKruskal(edges, vertexMap.size());

        // Prepare reverse map for output
        Map<Integer, String> reverseMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : vertexMap.entrySet()) {
            reverseMap.put(entry.getValue(), entry.getKey());
        }

        kruskal.saveResultsToJson(outputFile, reverseMap);
        kruskal.printResults();
    }
}


