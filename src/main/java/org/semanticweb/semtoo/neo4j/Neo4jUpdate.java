package org.semanticweb.semtoo.neo4j;

import java.util.Collection;
import java.util.Map;

import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.semanticweb.semtoo.Graph.GraphNode;

public class Neo4jUpdate {

	public static void createNodes(Collection<GraphNode> nodes, Transaction tc) {
		for(GraphNode n : nodes) {
			createNode(n, tc);
		}
	}
	
	public static void createNode(String labels, String properties, Transaction tc) {
		tc.run("CREATE (n" + labels + " " + properties + ")", Values.parameters("info", properties));
		tc.success();		
	}
	
	public static void createNode(Collection<String> labels, Map<String, String> properties, Transaction tc) {
		String labeledNode = "n";
		for(String s : labels) labeledNode += ":" + s;
		
		tc.run("CREATE (" + labeledNode + ") SET n = {info}", Values.parameters("info", properties));
		tc.success();
	}
	
	public static void createNode(GraphNode node, Transaction tc) {
		//tc.run("CREATE (" + node.neo4jName + ":" + node._nodetype + ") SET n = {info}", Values.parameters("info", node._info));
		tc.run("CREATE (" + node.neo4jName + ":ClassEntity {info})", Values.parameters("info", node._info));
		tc.success();
	}
	
	public static void mergeNode(String labels, String properties, Transaction tc) {
		tc.run("MERGE (n" + labels + " " + properties + ")", Values.parameters("info", properties));
		tc.success();		
	}
	
	public static void createSubOfRelation(String from, String to, Transaction tc) {
		tc.run("MATCH (a {iri:" + from + "}), (b {iri:" + to + "})");
		tc.run("CREATE (a)-[:SubOf]->(b)");
		tc.success();
	}
	
	// As in our graph, there exist only one relation that represents entailment, which we denote as "Entails"
	// This function create a relation from node a to node b
	public static void createSubOfRelation(GraphNode a, GraphNode b, Transaction tc) {
		tc.run("CREATE (" + a.neo4jName + ")-[:Entails]->(" + b.neo4jName + ")");
	}
	
	public static void clearGraph(Transaction tc) {
		tc.run("MATCH (n) DETACH DELETE n");
		tc.success();
	}
	
	public static void addLabelToAll(String labelToAdd, Transaction tc) {
		tc.run("MATCH (n) SET n:" + labelToAdd);
		tc.success();
	}
	
	public static void buildIndex(String label, String property, Transaction tc) {
		tc.run("CREATE INDEX ON:" + label + "(" + property + ")");
		tc.success();
	}
	
}
