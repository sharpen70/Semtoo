package org.semanticweb.semtoo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.semtoo.embeddedneo4j.StAssertions;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseBuilder;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.RelType;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.node_labels;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.property_key;
import org.semanticweb.semtoo.embeddedneo4j.StTraversal;

public class KB {
	public static void cleanBot(GraphDatabaseService db) {
		try(Transaction tx = db.beginTx()) {
			Node NOTHING = db.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, 
					StDatabaseBuilder.getNegStringiri(OWLManager.getOWLDataFactory().getOWLThing().toStringID()));
			
			TraversalDescription td = db.traversalDescription()
					.relationships(RelType.SubOf, Direction.INCOMING)
					.relationships(RelType.is, Direction.INCOMING)
					.evaluator(new Evaluator() {
						@Override
						public Evaluation evaluate(Path path) {
							Node end = path.endNode();
							boolean get = end.hasLabel(node_labels.INDIVIDUAL);

							return Evaluation.of(get, !get);
						}
					});
			
			if(NOTHING != null) {
				Traverser traverser = td.traverse(NOTHING);
				
				for(Path p : traverser) {
					Relationship r = p.lastRelationship();
					if(r != null) r.delete();
				}
				
				tx.success();
			}
		}
	}
	
	public static List<Path> confs(GraphDatabaseService db) {
		List<Path> re = new ArrayList<>();
		
		try(Transaction tx = db.beginTx()) {
			ResourceIterator<Node> negs = db.findNodes(node_labels.NEGATION);
			StTraversal tv = new StTraversal(db);
			
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
			
			tx.success();
		}
		
		return re;
	}
}
