package org.semanticweb.semtoo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.semanticweb.semtoo.embeddedneo4j.StAssertions;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.RelType;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.node_labels;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.property_key;
import org.semanticweb.semtoo.embeddedneo4j.StTraversal;
import org.semanticweb.semtoo.graph.GraphNode.NODE_KEY;
import org.semanticweb.semtoo.graph.GraphNode.NODE_LABEL;
import org.semanticweb.semtoo.model.CQuery;
import org.semanticweb.semtoo.util.Helper;

public class IAR implements ICQA{
	private static final boolean test = false;
	
	public IAR() {
	}
	
	@Override
	public void repair(GraphDatabaseService db) {
		try(Transaction tx = db.beginTx()) {
			ResourceIterator<Node> negs = db.findNodes(node_labels.NEGATION);
			StTraversal tv = new StTraversal(db);
			
			List<Long> causes_ids = new ArrayList<>();
			
			while(negs.hasNext()) {
				Node neg = negs.next();
				Node p = db.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, neg.getProperty(property_key.POSITIVE_NODE_IRI));
				StAssertions lead2neg = tv.getSourceAssertions(neg);
				StAssertions lead2p = tv.getSourceAssertions(p);
				
				Map<Long, Set<Long>> join = StAssertions.assertionsInnerJoin(lead2neg, lead2p);
				
				for(Set<Long> v : join.values()) {
					for(Long id : v) {
						Relationship rel = db.getRelationshipById(id);
						
						if(rel.hasProperty(property_key.CAUSES)) {
							causes_ids.add((Long)rel.getProperty(property_key.CAUSES));					
						}
						rel.delete();
					}
				}
			}
			
			for(Long cause_id : causes_ids ) {
				try {
					db.getRelationshipById(cause_id).delete();
				}
				catch (Exception nfe) {
					//It's OK
				}
			}
			
			tx.success();
		}
	}
	
	@Override
	public void answer(CQuery q) {

	}
}
