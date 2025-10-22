package com.online.exam;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.io.File;

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

    public static Graph loadGraph(String filePath) throws IOException {
        FileReader reader = new FileReader(filePath);
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        JSONObject jsonObject = new JSONObject(sb.toString());
        Graph graph = new Graph();

        JSONArray vertices = jsonObject.getJSONArray("vertices");
        for (int i = 0; i < vertices.length(); i++) {
            graph.addVertex(vertices.getString(i));
        }

        JSONArray edges = jsonObject.getJSONArray("edges");
        for (int i = 0; i < edges.length(); i++) {
            JSONObject edge = edges.getJSONObject(i);
            graph.addEdge(edge.getString("start"), edge.getString("end"), edge.getInt("weight"));
        }

        return graph;
    }

    public static void saveResults(JSONObject result, String outputFile) throws IOException {
        File outputDir = new File("results");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        FileWriter file = new FileWriter("results/" + outputFile);
        file.write(result.toString(4));
        file.close();
    }

    public static void main(String[] args) {
        String[] inputFiles = {"src/main/resources/input_test1.json", "src/main/resources/input_test2.json", "src/main/resources/input_test3.json", "src/main/resources/input_test4.json", "src/main/resources/input_test5.json"};

        for (String inputFile : inputFiles) {
            try {
                Graph graph = loadGraph(inputFile);
                JSONObject result = primAlgorithm(graph);
                String outputFileName = inputFile.replace("src/main/resources/input", "output").replace(".json", ".json");
                saveResults(result, outputFileName);
                System.out.println("Processed " + inputFile + " -> " + outputFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}