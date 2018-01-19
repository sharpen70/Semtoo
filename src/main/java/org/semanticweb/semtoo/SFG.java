package org.semanticweb.semtoo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.RelType;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.node_labels;

public class SFG extends FG {
	public SFG(GraphDatabaseService _db) {
		super(_db);
	}

	public void fg(List<String> iris) {
		for(String iri : iris) fg(iri);
	}
	
	public void fg(String iri) {
		Node n = preprocess(iri);
		if(n != null) {
			try(Transaction tx = db.beginTx()) {
				for(Relationship inRel : n.getRelationships(RelType.SubOf, Direction.INCOMING)) {
					for(Relationship outRel : n.getRelationships(RelType.SubOf, Direction.OUTGOING)) {
						Node start = inRel.getStartNode();
						Node end = outRel.getEndNode();
						if(start.hasLabel(node_labels.INDIVIDUAL))
							start.createRelationshipTo(end, RelType.is);
						else 
							start.createRelationshipTo(end, RelType.SubOf);
						outRel.delete();
					}
					inRel.delete();
				}
				tx.success();
			}
		}
	}
	
	public void ofg(List<String> iris) {
		Set<Node> tofg = new HashSet<>();
		for(String iri : iris) {
			Node n = preprocess(iri);
			if(n != null) tofg.add(n);
		}
		try(Transaction tx = db.beginTx()) {
			Evaluator eval = new Evaluator() {
				@Override
				public Evaluation evaluate(Path path) {
					Node n = path.endNode();
					boolean includes = tofg.contains(n);
					return Evaluation.of(includes, !includes);
				}
			};
			TraversalDescription td = db.traversalDescription()
					.relationships(RelType.SubOf, Direction.OUTGOING)
					.evaluator(eval);
			
			Traverser traverser = td.traverse(tofg);
			for(Path p : traverser) {
				Node start = p.startNode();
				Node end = p.endNode();
				for(Relationship rel : start.getRelationships(Direction.INCOMING)) {
					Node new_start = rel.getStartNode();
					if(!tofg.contains(new_start)) {
						if(new_start.hasLabel(node_labels.INDIVIDUAL))
							new_start.createRelationshipTo(end, RelType.is);
						else
							new_start.createRelationshipTo(end, RelType.SubOf);
					}
				}
			}
			tx.success();
		}
		
		try(Transaction tx = db.beginTx()) {
			for(Node n : tofg) {
				for(Relationship rel : n.getRelationships()) rel.delete();
				n.delete();
			}
			tx.success();
		}
	}
	
}
