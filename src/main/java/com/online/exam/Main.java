package com.online.exam;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.*;

public class Main {
    static class Edge {
        String start;
        String end;
        int weight;

        Edge(String start, String end, int weight) {
            this.start = start;
            this.end = end;
            this.weight = weight;
        }
    }

    static class Graph {
        List<String> vertices = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        void addVertex(String vertex) {
            vertices.add(vertex);
        }

        void addEdge(String start, String end, int weight) {
            edges.add(new Edge(start, end, weight));
        }
    }

    public static JSONObject primAlgorithm(Graph graph) {
        long startTime = System.currentTimeMillis();
        Map<String, List<Edge>> adjList = new HashMap<>();
        for (Edge edge : graph.edges) {
            adjList.computeIfAbsent(edge.start, k -> new ArrayList<>()).add(edge);
            adjList.computeIfAbsent(edge.end, k -> new ArrayList<>()).add(new Edge(edge.end, edge.start, edge.weight));
        }
        Set<String> inMST = new HashSet<>();
        PriorityQueue<Edge> minHeap = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));
        List<Edge> mstEdges = new ArrayList<>();
        int totalCost = 0;
        int operations = 0;
        String startVertex = graph.vertices.get(0);
        inMST.add(startVertex);
        minHeap.addAll(adjList.get(startVertex));
        while (!minHeap.isEmpty()) {
            Edge edge = minHeap.poll();
            operations++;
            if (!inMST.contains(edge.end)) {
                inMST.add(edge.end);
                mstEdges.add(edge);
                totalCost += edge.weight;
                minHeap.addAll(adjList.get(edge.end));
            }
        }
        long endTime = System.currentTimeMillis();
        JSONObject result = new JSONObject();
        result.put("total_cost", totalCost);
        result.put("num_vertices", graph.vertices.size());
        result.put("num_edges", graph.edges.size());
        result.put("operations", operations);
        result.put("execution_time_ms", endTime - startTime);
        JSONArray mstJsonArray = new JSONArray();
        for (Edge edge : mstEdges) {
            JSONObject edgeObj = new JSONObject();
            edgeObj.put("start", edge.start);
            edgeObj.put("end", edge.end);
            edgeObj.put("weight", edge.weight);
            mstJsonArray.put(edgeObj);
        }
        result.put("mst", mstJsonArray);
        return result;
    }

    public static void main(String[] args) {
        String[] inputFiles = {
                "src/main/resources/input_test1.json",
                "src/main/resources/input_test2.json",
                "src/main/resources/input_test3.json",
                "src/main/resources/input_test4.json",
                "src/main/resources/input_test5.json"
        };
        File resultsDir = new File("results");
        if (!resultsDir.exists()) resultsDir.mkdir();
        for (String inputFile : inputFiles) {
            try {
                Graph graph = loadGraph(inputFile);
                JSONObject result = primAlgorithm(graph);
                String outputFileName = "results/" + inputFile.substring(inputFile.lastIndexOf("/") + 1).replace("input", "output");
                saveResults(result, outputFileName);
                System.out.println("Processed " + inputFile + " -> " + outputFileName);
                System.out.printf(
                        "MST cost=%d, V=%d, E=%d, ops=%d, time=%dms%n",
                        result.getInt("total_cost"),
                        result.getInt("num_vertices"),
                        result.getInt("num_edges"),
                        result.getInt("operations"),
                        result.getLong("execution_time_ms")
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Graph loadGraph(String filePath) throws IOException {
        FileReader reader = new FileReader(filePath);
        StringBuilder sb = new StringBuilder();
        int i;
        while ((i = reader.read()) != -1) {
            sb.append((char) i);
        }
        reader.close();
        JSONObject jsonObject = new JSONObject(sb.toString());
        Graph graph = new Graph();
        JSONArray vertices = jsonObject.getJSONArray("vertices");
        for (int j = 0; j < vertices.length(); j++) {
            graph.addVertex(vertices.getString(j));
        }
        JSONArray edges = jsonObject.getJSONArray("edges");
        for (int j = 0; j < edges.length(); j++) {
            JSONObject edge = edges.getJSONObject(j);
            graph.addEdge(edge.getString("start"), edge.getString("end"), edge.getInt("weight"));
        }
        return graph;
    }

    public static void saveResults(JSONObject result, String outputFile) throws IOException {
        try (FileWriter file = new FileWriter(outputFile)) {
            file.write(result.toString(4));
        }
    }
}