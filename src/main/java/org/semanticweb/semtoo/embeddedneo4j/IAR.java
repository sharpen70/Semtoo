package org.semanticweb.semtoo.embeddedneo4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.semanticweb.semtoo.embeddedneo4j.SemtooDatabaseMeta.RelType;
import org.semanticweb.semtoo.embeddedneo4j.SemtooDatabaseMeta.node_labels;
import org.semanticweb.semtoo.embeddedneo4j.SemtooDatabaseMeta.property_key;
import org.semanticweb.semtoo.graph.GraphNode.NODE_KEY;
import org.semanticweb.semtoo.graph.GraphNode.NODE_LABEL;
import org.semanticweb.semtoo.util.Helper;

public class IAR {
	private GraphDatabaseService db;
	private static final boolean test = false;
	
	public IAR(GraphDatabaseService _db) {
		db = _db;
	}
	
	private class LeafEvaluator implements Evaluator {
		private Label leafLabel;
		
		public LeafEvaluator(Label label) {
			leafLabel = label;
		}
		@Override
		public Evaluation evaluate(Path path) {
			boolean get = path.endNode().hasLabel(leafLabel);
			return Evaluation.of(get, !get);
		}
	}
	
	public void traversal() {
		Set<Long> re = new HashSet<>();
		
		try(Transaction tx = db.beginTx()) {
			ResourceIterator<Node> nit = db.findNodes(node_labels.NEGATION);
			
			while(nit.hasNext()) {
				Set<Long> lc1 = new HashSet<>();
				Set<Long> lc2 = new HashSet<>();
				
				Node ng = nit.next();
				Node pn = db.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, ng.getProperty(property_key.POSITIVE_NODE_IRI));
				Traverser t1 = getLeaf(ng, node_labels.INDIVIDUAL);
				Traverser t2 = getLeaf(pn, node_labels.INDIVIDUAL);
				
				for(Path p : t1) {
					lc1.add(p.endNode().getId());
				}
				
				for(Path p : t2) {
					lc2.add(p.endNode().getId());
				}
				Helper.getIntersection(lc1, lc2, re);
			}
		}
	}
	
	public Traverser getLeaf(final Node start, final Label leafLabel) {
		TraversalDescription td = db.traversalDescription().depthFirst()
				.relationships(RelType.SubOf, Direction.INCOMING)
				.relationships(RelType.is, Direction.INCOMING)
				.evaluator(new LeafEvaluator(leafLabel));
		
		return td.traverse(start);
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
