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
	
	public static void mergeAndcreateRelation(String from, GraphNode to, String relationLabel, Transaction tc) {
		tc.run("Match (a:ClassEntity {iri:{a_iri}}) Merge (b:ClassEntity {iri:{b_info}.iri}) "
				+ "ON CREATE SET b = {b_info} CREATE (a)-[:" + relationLabel + "]->(b)", 
				Values.parameters("a_iri", from, "b_info", to._info));
		tc.success();
	}
	public static void matchAndcreateRelation(String from, String to, String relationLabel, Transaction tc) {
		tc.run("MATCH (a:ClassEntity {iri:{a_iri}}), (b:ClassEntity {iri:{b_iri}}) CREATE (a)-[:" + relationLabel + "]->(b)", Values.parameters("a_iri", from, "b_iri", to));
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
	
	public static void deleteRelation(String a_iri, String b_iri, String relationLabel, Transaction tc) {
		tc.run("MATCH (a {iri:{a_iri}})-[r:" + relationLabel + "]->(b {iri:{b_iri}}) DELETE r", Values.parameters("a_iri", a_iri, "b_iri", b_iri));
		tc.success();
	}	
}
