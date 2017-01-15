package org.semanticweb.semtoo.neo4j;

import java.util.Collection;
import java.util.Map;

import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.semanticweb.semtoo.Graph.GraphNode;

public class Neo4jManager {
	private Driver driver;
	private static final AuthToken token = AuthTokens.basic("neo4j", "123");
	private static final String url = "bolt://localhost:7687";

	
	public Neo4jManager() {
		driver = GraphDatabase.driver(url, token);		
	}
	
	public void createNodes(Collection<GraphNode> nodes, Transaction tc) {
		for(GraphNode n : nodes) {
			createNode(n, tc);
		}
	}
	
	public void createNode(String labels, String properties, Transaction tc) {
		tc.run("CREATE (n" + labels + " " + properties + ")", Values.parameters("info", properties));
		tc.success();		
	}
	
	public void createNode(Collection<String> labels, Map<String, String> properties, Transaction tc) {
		String labeledNode = "n";
		for(String s : labels) labeledNode += ":" + s;
		
		tc.run("CREATE (" + labeledNode + ") SET n = {info}", Values.parameters("info", properties));
		tc.success();
	}
	
	public void createNode(GraphNode node, Transaction tc) {
		tc.run("CREATE (" + node.neo4jName + ":" + node._nodetype + ") SET n = {info}", Values.parameters("info", node._info));
		tc.success();
	}
	
	public void mergeNode(String labels, String properties, Transaction tc) {
		tc.run("MERGE (n" + labels + " " + properties + ")", Values.parameters("info", properties));
		tc.success();		
	}
	
	public void createRelation() {
		
	}
	
	// As in our graph, there exist only one relation that represents entailment, which we denote as "Entails"
	// This function create a relation from node a to node b
	public void createSubOfRelation(GraphNode a, GraphNode b, Transaction tc) {
		tc.run("CREATE (" + a.neo4jName + ")-[:Entails]->(" + b.neo4jName + ")");
	}
	
	public Session getSession() {
		return driver.session();
	}
	
	public void clearGraph(Transaction tc) {
		tc.run("MATCH (n) DETACH DELETE n");
		tc.success();
	}
	
	public void addLabelToAll(String labelToAdd, Transaction tc) {
		tc.run("MATCH (n) SET n:" + labelToAdd);
		tc.success();
	}
	
	public void buildIndex(String label, String property, Transaction tc) {
		tc.run("CREATE INDEX ON:" + label + "(" + property + ")");
		tc.success();
	}
	
	public void close() {
		driver.close();
	}
}
