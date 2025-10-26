import json
import networkx as nx
import matplotlib.pyplot as plt

input_file = "src/input_test5.json"
output_file = "results/output_test5.json"

with open(input_file) as f:
    graph_data = json.load(f)
with open(output_file) as f:
    mst_data = json.load(f)

G = nx.Graph()
for node in graph_data["nodes"]:
    G.add_node(node)
for e in graph_data["edges"]:
    G.add_edge(e["from"], e["to"], weight=e["weight"])

mst_edges = set((e["from"], e["to"]) for e in mst_data["mstEdges"])
edge_colors = ['red' if (u, v) in mst_edges or (v, u) in mst_edges else 'gray' for u, v in G.edges]

pos = nx.spring_layout(G)
nx.draw(G, pos, with_labels=True, edge_color=edge_colors, width=2)
nx.draw_networkx_edge_labels(G, pos, edge_labels={(e["from"], e["to"]): e["weight"] for e in graph_data["edges"]})
plt.show()
