package com.online.exam;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.HashSet;
import java.io.FileReader;
import java.util.Comparator;
public class PrimDemo {

    static class Edge {
        String start, end;
        double weight;

        Edge(String start, String end, double weight) {
            this.start = start;
            this.end = end;
            this.weight = weight;
        }
    }

    static class Graph {
        List<Edge> edges;
        Map<String, Integer> vertices;

        Graph(String filePath) throws IOException {
            this.edges = new ArrayList<>();
            this.vertices = new HashMap<>();
            loadGraph(filePath);
        }

        void loadGraph(String filePath) throws IOException {
            JSONObject jsonObject = loadJson(filePath);
            JSONArray edgesArray = jsonObject.getJSONArray("edges");
            int index = 0;
            for (int i = 0; i < edgesArray.length(); i++) {
                JSONObject edgeObj = edgesArray.getJSONObject(i);
                String start = edgeObj.getString("start");
                String end = edgeObj.getString("end");
                double weight = edgeObj.getDouble("weight");

                if (!vertices.containsKey(start)) vertices.put(start, index++);
                if (!vertices.containsKey(end)) vertices.put(end, index++);

                edges.add(new Edge(start, end, weight));
            }
        }

        JSONObject loadJson(String filePath) throws IOException {
            FileReader reader = new FileReader(filePath);
            StringBuilder sb = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                sb.append((char) i);
            }
            return new JSONObject(sb.toString());
        }

        List<Edge> getEdges() {
            return edges;
        }

        Set<String> getVertices() {
            return vertices.keySet();
        }
    }

    static class PrimMST {
        private Map<String, Double> distTo;
        private Map<String, Edge> edgeTo;
        private Set<String> marked;
        private PriorityQueue<String> pq;

        PrimMST(Graph graph) {
            distTo = new HashMap<>();
            edgeTo = new HashMap<>();
            marked = new HashSet<>();
            pq = new PriorityQueue<>(Comparator.comparing(distTo::get));

            for (String vertex : graph.getVertices()) {
                distTo.put(vertex, Double.POSITIVE_INFINITY);
            }
        }

        public void run(Graph graph) {
            String startVertex = graph.getVertices().iterator().next();
            distTo.put(startVertex, 0.0);
            pq.add(startVertex);

            while (!pq.isEmpty()) {
                String v = pq.poll();
                marked.add(v);

                for (Edge e : graph.getEdges()) {
                    String w = e.start.equals(v) ? e.end : (e.end.equals(v) ? e.start : null);
                    if (w == null || marked.contains(w)) continue;

                    if (distTo.get(w) > e.weight) {
                        distTo.put(w, e.weight);
                        edgeTo.put(w, e);
                        pq.add(w);
                    }
                }
            }
        }

        public List<Edge> getEdges() {
            List<Edge> mst = new ArrayList<>();
            for (String v : distTo.keySet()) {
                Edge e = edgeTo.get(v);
                if (e != null) {
                    mst.add(e);
                }
            }
            return mst;
        }

        public double getWeight() {
            double weight = 0.0;
            for (Edge e : getEdges()) {
                weight += e.weight;
            }
            return weight;
        }
    }

    public static void writeDotGraph(Graph G, String outPath) throws IOException {
        try (PrintWriter pw = new PrintWriter(outPath)) {
            pw.println("graph G {");
            for (String vertex : G.getVertices()) {
                pw.printf("  %s;%n", vertex);
            }
            for (Edge e : G.getEdges()) {
                pw.printf("  %s -- %s [label=\"%.2f\"];%n", e.start, e.end, e.weight);
            }
            pw.println("}");
        }
    }

    public static void main(String[] args) throws IOException {
        String[] inputFiles = {
                "src/main/resources/input_test1.json",
                "src/main/resources/input_test2.json",
                "src/main/resources/input_test3.json",
                "src/main/resources/input_test4.json",
                "src/main/resources/input_test5.json"
        };

        java.io.File resultsDir = new java.io.File("results");
        if (!resultsDir.exists()) resultsDir.mkdir();

        for (int i = 0; i < inputFiles.length; i++) {
            System.out.println("=== Test #" + (i + 1) + " ===");
            Graph graph = new Graph(inputFiles[i]);
            PrimMST mst = new PrimMST(graph);
            mst.run(graph);

            System.out.println("Edges in MST:");
            for (Edge e : mst.getEdges()) {
                System.out.println(e.start + " - " + e.end + ": " + e.weight);
            }
            System.out.printf("Weight of MST: %.5f\n", mst.getWeight());

            String dotFilePath = "results/graph" + (i + 1) + ".dot";
            writeDotGraph(graph, dotFilePath);
            System.out.println("DOT file saved to: " + dotFilePath);
        }
    }
}