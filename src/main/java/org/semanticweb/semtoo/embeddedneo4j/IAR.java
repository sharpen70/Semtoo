package org.semanticweb.semtoo.embeddedneo4j;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.semanticweb.semtoo.graph.GraphNode.NODE_KEY;
import org.semanticweb.semtoo.graph.GraphNode.NODE_LABEL;

public class IAR {
	private GraphDatabaseService db;
	private static final boolean test = false;
	
	public IAR(GraphDatabaseService _db) {
		db = _db;
	}
	
	public void detectConflicts() {
		try(Transaction tx = db.beginTx()) {
			String tstatement = "CYPHER PLANNER = RULE MATCH (a:" + NODE_LABEL.TBOXENTITY + ")-[:SubOf*0..]->(b:" + NODE_LABEL.TBOXENTITY + "),"
					+ " (c:" + NODE_LABEL.TBOXENTITY + ")-[:SubOf*0..]->(nb:" + NODE_LABEL.NEGATION + ")"  
					+ " USING SCAN nb:" + NODE_LABEL.NEGATION + " WHERE b." + NODE_KEY.NODE_IRI + " = nb." + NODE_KEY.POSITIVE_NODE_IRI
					+ " WITH a, c MATCH (i)-[:is]->(a), (i)-[:is]->(c) WHERE i:" + NODE_LABEL.INDIVIDUAL + " OR i:" + NODE_LABEL.DUALINDIVIDUAL
					+ " WITH i"
					+ " MATCH (i)-[r:is]-(ignore)"
					+ " DETACH DELETE i"
					+ " RETURN count(DISTINCT r) as rel, count(DISTINCT i) as num";
			
			String ostatement = "CYPHER PLANNER = RULE MATCH (a:" + NODE_LABEL.TBOXENTITY + ")-[:SubOf*0..]->(b:" + NODE_LABEL.TBOXENTITY + "),"
					+ " (c:" + NODE_LABEL.TBOXENTITY + ")-[:SubOf*0..]->(nb:" + NODE_LABEL.NEGATION + ")"  
					+ " USING SCAN nb:" + NODE_LABEL.NEGATION + " WHERE b." + NODE_KEY.NODE_IRI + " = nb." + NODE_KEY.POSITIVE_NODE_IRI
					+ " WITH a, c MATCH (i)-[:is]->(a), (i)-[:is]->(c) WHERE i:" + NODE_LABEL.INDIVIDUAL + " OR i:" + NODE_LABEL.DUALINDIVIDUAL 
					+ " DETACH DELETE i";
			
			String _statement = "MATCH (a)-[:SubOf*0..]->(n:" + NODE_LABEL.NEGATION + "), (b)-[:SubOf*0..]->(p:" + NODE_LABEL.TBOXENTITY
					+ " {" + NODE_KEY.NODE_IRI + ":n." + NODE_KEY.POSITIVE_NODE_IRI + "}) WITH DISTINCT a, b MATCH (a)<-[:is]-(i)-[:is]->(b)"
					+ " DETACH DELETE i";
			
			if(test) {
				Result re = db.execute(tstatement);
				
				Map<String, Object> result = re.next();
				long num = (long)(result.get("num"));
				long rel = (long)(result.get("rel"));
				
				System.out.println(num + " idividual nodes removed, " + rel + " assertions removed");
			}
			
			else db.execute(_statement);
			
			tx.success();
		}	
	}
}
