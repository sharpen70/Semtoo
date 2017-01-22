package org.semanticweb.semtoo.neo4j;

import java.util.Collection;
import java.util.Map;

import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.semanticweb.semtoo.model.GraphNode;

public class Neo4jUpdate {

	public static void createNodes(Collection<GraphNode> nodes, Transaction tc) {
		for(GraphNode n : nodes) {
			createNode(n, tc);
		}
	}
	
	public static void createNode(GraphNode node, String label, Transaction tc) {
		tc.run("CREATE (" + node.neo4jName + ":" + label + " {info})", Values.parameters("info", node.info));
		tc.success();
	}
	
	public static void createNode(GraphNode node, Transaction tc) {
		tc.run("CREATE (" + node.neo4jName + " {info})", Values.parameters("info", node.info));
		tc.success();
	}
	
	public static void matchAndcreateRelation(String from, String to, String relationLabel, Transaction tc) {
		tc.run("MATCH (a {iri:{a_iri}}), (b {iri:{b_iri}}) CREATE (a)-[:" + relationLabel + "]->(b)", 
				Values.parameters("a_iri", from, "b_iri", to));
		tc.success();
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
		tc.run("MATCH (a {iri:{a_iri}})-[r:" + relationLabel + "]->(b {iri:{b_iri}}) DELETE r", 
				Values.parameters("a_iri", a_iri, "b_iri", b_iri));
		tc.success();
	}	
}
