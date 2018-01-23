package org.semanticweb.semtoo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.semanticweb.semtoo.embeddedneo4j.StAssertions;
import org.semanticweb.semtoo.embeddedneo4j.StTraversal;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.node_labels;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.property_key;

public class UFG extends FG {	
	public UFG(GraphDatabaseService _db) {
		super(_db);
	}
	
	public boolean ufg(List<String> concepts) {
	//	KB.cleanBot(db);
		
		try(Transaction tx = db.beginTx()) {
			ResourceIterator<Node> negs = db.findNodes(node_labels.NEGATION);
			StTraversal tv = new StTraversal(db);
			List<Path> re = new ArrayList<>();
			
			while(negs.hasNext()) {
				Node neg = negs.next();
				Node p = db.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, neg.getProperty(property_key.POSITIVE_NODE_IRI));
				StAssertions lead2neg = tv.getSourcePath(neg);
				StAssertions lead2p = tv.getSourcePath(p);
				
				Map<Long, Set<Object>> join = StAssertions.assertionsInnerJoin(lead2neg, lead2p);
			
				for(Set<Object> v : join.values()) {
					for(Object path : v) {
						re.add((Path)path);
					}
				}
			}
			
			for(Path p : re) {
				Stack<Node> stack = new Stack<>();
				Iterable<Node> nodes = p.nodes();
				Iterator<Node> it = nodes.iterator();
				
				while(it.hasNext()) {
					stack.push(it.next());
				}
				
				//Skip the node of individual
				stack.pop();
				
				Node next_concept = stack.pop();
				
				while(!stack.empty()) {							
					if(concepts.contains(next_concept.getProperty(property_key.NODE_IRI))) {
						Iterable<Relationship> rels = next_concept.getRelationships();
						next_concept = stack.pop();
						for(Relationship r : rels) {
							Node _next_concept = r.getEndNode();
							if(_next_concept != next_concept && 
									!concepts.contains(next_concept.getProperty(property_key.NODE_IRI))) {
								return false;
							}
						}
					}
					else break;
				}
			}
			
			tx.success();
		}	
		
		ofg(concepts);

		return true;
	}
}
