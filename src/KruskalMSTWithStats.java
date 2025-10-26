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
        int unionCount = 0, findCount = 0;

        UF(int n) {
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
                parent[p] = parent[parent[p]];
                p = parent[p];
                findCount++;
            }
            return p;
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
        }

        boolean connected(int p, int q) {
            return find(p) == find(q);
        }
    }

    List<Edge> mst;
    double totalWeight;
    int edgeComparisons;
    long executionTimeMs;

    KruskalMSTWithStats() {
        mst = new ArrayList<>();
    }

    public void runKruskal(List<Edge> edges, int vertices) {
        long start = System.nanoTime();
        Collections.sort(edges);
        UF uf = new UF(vertices);
        edgeComparisons = 0;
        totalWeight = 0;
        mst.clear();

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

    public static List<Edge> loadGraphFromJson(String filename, Map<String, Integer> vertexMap) throws IOException {
        List<Edge> edges = new ArrayList<>();
        JsonObject root = JsonParser.parseReader(new FileReader(filename)).getAsJsonObject();
        JsonArray nodes = root.getAsJsonArray("nodes");
        int index = 0;
        for (JsonElement node : nodes) vertexMap.put(node.getAsString(), index++);
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

    public void saveResultsToJson(String filename, Map<Integer, String> reverseVertexMap, int vertices, int edgesCount) throws IOException {
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
        String[] inputFiles = {
                "src/input_test1.json",
                "src/input_test2.json",
                "src/input_test3.json",
                "src/input_test4.json",
                "src/input_test5.json"
        };

        java.io.File resultsDir = new java.io.File("results");
        if (!resultsDir.exists()) resultsDir.mkdir();

        for (int i = 0; i < inputFiles.length; i++) {
            System.out.println("=== Test #" + (i + 1) + " ===");
            Map<String, Integer> vertexMap = new HashMap<>();
            List<Edge> edges = loadGraphFromJson(inputFiles[i], vertexMap);
            KruskalMSTWithStats kruskal = new KruskalMSTWithStats();
            kruskal.runKruskal(edges, vertexMap.size());

            Map<Integer, String> reverseMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : vertexMap.entrySet()) {
                reverseMap.put(entry.getValue(), entry.getKey());
            }

            String outputFile = "results/output_test" + (i + 1) + ".json";
            kruskal.saveResultsToJson(outputFile, reverseMap, vertexMap.size(), edges.size());
            System.out.println("Saved: " + outputFile);
        }
    }
}




