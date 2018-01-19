package org.semanticweb.semtoo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseBuilder;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.RelType;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.node_labels;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.property_key;
import org.semanticweb.semtoo.graph.GraphNode.NODE_KEY;
import org.semanticweb.semtoo.graph.GraphNode.NODE_LABEL;

public class FG {
	public GraphDatabaseService db;
	
	private final boolean test = true;
	
	public FG(GraphDatabaseService _db) {
		db = _db;
	}
	
	public Node preprocess(String iri) {
		boolean go = true;
		Node n;
		try(Transaction tx = db.beginTx()) {
			Node NOTHING = db.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, 
					StDatabaseBuilder.getNegStringiri(OWLManager.getOWLDataFactory().getOWLThing().toStringID()));
			
			n = db.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, iri);
			Node neg = db.findNode(node_labels.NEGATION, property_key.NODE_IRI, StDatabaseBuilder.getNegStringiri(iri));
			
			//Remove Tbox Axiom of form SubClassOf(A, A) 
			for(Relationship rel : n.getRelationships(RelType.SubOf, Direction.OUTGOING)) {
				if(rel.getEndNode().equals(n)) rel.delete();
				
				//Check if the given class A has axiom of form Disjoint(A, A)
				if(rel.getEndNode().equals(NOTHING)) go = false;
			}
			
			//Given class A to forget, change all B -> ~A to A -> ~B
			if(go && neg != null) {
				StDatabaseBuilder builder = new StDatabaseBuilder(db);
				for(Relationship rel : neg.getRelationships(RelType.SubOf, Direction.INCOMING)) {
					Node dj = rel.getStartNode();
					Node neg_dj = builder.createNegationNode(dj.getProperty(property_key.NODE_IRI).toString());
					n.createRelationshipTo(neg_dj, RelType.SubOf);
					rel.delete();
				}
				neg.delete();
			}
			
			//Remove the contradiction class and make its subclasses contradiction class
			if(!go) {
				for(Relationship rel : n.getRelationships(RelType.SubOf, Direction.INCOMING)) {
					Node follower = rel.getEndNode();
					follower.createRelationshipTo(NOTHING, RelType.SubOf);
				}
				try {
					for(Relationship rel : n.getRelationships()) {
						rel.delete();
					}
					n.delete();
				} catch(Exception e) { }
			}
			tx.success();
		}
		if(go) return n;
		else return null;
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
	
	
	//Forgets a set of concepts
	
	//Forgets a set of concepts simply one by one
	public void naiveForget(Collection<String> conceptsToforget) {
		for(String iri : conceptsToforget) forget(iri);
	}
	
	//Check out the neighbors of forgetting concepts, then forget by relating all these neighbors
	public void neighborRelatedForget(Collection<String> conceptsToforget) {
		ArrayList<String> concepts = new ArrayList<>();
		
		long start, end;
		
		start = System.currentTimeMillis();
		for(String iri : conceptsToforget) {
			removeMeaningless(iri);
			changeNegationDirection(iri);
			
			if(isContradictionClass(iri)) {
				removeContradictionClass(iri);
			}
			else {
				concepts.add(iri);
			}
		}
		
		try(Transaction tx = db.beginTx()) {
			String statement = "MATCH (a:" + NODE_LABEL.TBOXENTITY + ") " +
							   "WHERE a." + NODE_KEY.NODE_IRI + " IN {iris} " +
							   "SET a:" + NODE_LABEL.FORGET;
			
			Map<String, Object> param = new HashMap<>();
			param.put("iris", concepts);
			db.execute(statement, param);
			tx.success();
		}
		end = System.currentTimeMillis();
		if(test) System.out.println("Tag forget concepts with " + (end - start) + " ms");
		
		start = System.currentTimeMillis();
		try(Transaction tx = db.beginTx()) {
			String statement = "MATCH " +
							   "(a)-[r1:SubOf|is]->(f1:" + NODE_LABEL.FORGET + "), " +
							   "(f2:" + NODE_LABEL.FORGET + ")-[:SubOf]->(b) " +
							   "WHERE NOT a:" + NODE_LABEL.FORGET + " AND NOT b:" + NODE_LABEL.FORGET +
							   " AND (a)-[*2..]->(b) " +
							   "FOREACH(ignore in CASE type(r1) WHEN \"SubOf\" THEN [1] ELSE [] END | MERGE (a)-[r:SubOf]->(b))" +
							   "FOREACH(ignore in CASE type(r1) WHEN \"is\" THEN [1] ELSE [] END | MERGE (a)-[r:is]->(b))";
			
			db.execute(statement);
			tx.success();
		}
		end = System.currentTimeMillis();
		if(test) System.out.println("Connect relative nodes with " + (end -start) + " ms");
		
		start = System.currentTimeMillis();
		try(Transaction tx = db.beginTx()) {
			db.execute("MATCH (delete:" + NODE_LABEL.FORGET + ") DETACH DELETE delete");
			tx.success();
		}
		end = System.currentTimeMillis();
		if(test) System.out.println("Delete forget nodes with " + (end - start) + " ms");
	}
	
	//Forgets One Single Concept
	public void forget(String iriOfConceptToforget) {
		if(test) System.out.println("Forgetting concept: " + iriOfConceptToforget);
		
		long start, end;
		
		start = System.currentTimeMillis();
		removeMeaningless(iriOfConceptToforget);
		end = System.currentTimeMillis();
		if(test) System.out.println("Remove Meaningless with " + (end -start) + " ms");
		
		start = System.currentTimeMillis();
		changeNegationDirection(iriOfConceptToforget);				
		end = System.currentTimeMillis();
		if(test) System.out.println("Change Negation with " + (end -start) + " ms");
		
		if(isContradictionClass(iriOfConceptToforget)) {
			start = System.currentTimeMillis();
			removeContradictionClass(iriOfConceptToforget);
			end = System.currentTimeMillis();
			if(test) System.out.println("Remove Contradiction with " + (end -start) + " ms");
		}
		else {
			start = System.currentTimeMillis();
			eliminateClass(iriOfConceptToforget);
			end = System.currentTimeMillis();
			if(test) System.out.println("Eliminate class with " + (end -start) + " ms");
		}		
	}
	
	//Remove Tbox Axiom of form SubClassOf(A, A) 
	private void removeMeaningless(String classIRI) {
		try(Transaction tx = db.beginTx()) {
			String statement = "MATCH (n:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":{iri}}) "
					+ "-[r:SubOf]->(n) DELETE r";
			
			Map<String, Object> param = new HashMap<>();
			param.put("iri", classIRI);
			db.execute(statement, param);
			
			tx.success();
		}
	}
	
	//Check if the given class A has axiom of form Disjoint(A, A)
	private boolean isContradictionClass(String classIRI) {
		String execString = "MATCH (a:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI 
					+ ":{iri}})-[:SubOf]->(b:" + NODE_LABEL.NEGATION + " {" + NODE_KEY.POSITIVE_NODE_IRI 
					+ ":{thing_iri}}) RETURN a";
		
		try(Transaction tx = db.beginTx()) {
			Map<String, Object> param = new HashMap<>();
			param.put("iri", classIRI);
			Result result = db.execute(execString, param);
			if(result.hasNext()) return true;
		}
		return false;
	}
	
	//Remove the contradiction class and make its subclasses contradiction class
	private void removeContradictionClass(String classIRI) {
		try(Transaction tx = db.beginTx()) {
			String exec = "MATCH (a:" + NODE_LABEL.TBOXENTITY + ")-[:SubOf]->(b:" + NODE_LABEL.TBOXENTITY 
						  + " {" + NODE_KEY.NODE_IRI + ":{biri}})"
	//					  + "REMOVE b:" + NODE_LABEL.TBOXENTITY + " SET b:" + NODE_LABEL.FORGET
					  	  + " DETACH DELETE b "
						  + " WITH a"
						  + " MATCH (nthing:" + NODE_LABEL.NEGATION 
						  + " {" + NODE_KEY.POSITIVE_NODE_IRI + ":{thing_iri}})"
						  + " CREATE (a)-[:SubOf]->(nthing)";
			
			Map<String, Object> param = new HashMap<>();
			param.put("biri", classIRI);
			db.execute(exec, param);
			
			tx.success();
		}
	}
	
	//Given class A to forget, change all B -> ~A to A -> ~B
	private void changeNegationDirection(String classIRI) {
		String execString = "MATCH (a:" + NODE_LABEL.TBOXENTITY + ")-[r:SubOf]->(nb:" + NODE_LABEL.NEGATION 
							+ " {" + NODE_KEY.POSITIVE_NODE_IRI + ":{iri}}) " + "DETACH DELETE nb " +
						   "MERGE (na:" + NODE_LABEL.NEGATION + " {" + NODE_KEY.POSITIVE_NODE_IRI + ":a." + NODE_KEY.NODE_IRI + "}) "
						   	+ "ON CREATE SET na." + NODE_KEY.NODE_IRI + " = {neg} + a." + NODE_KEY.NODE_IRI + 
						   " WITH na " +
						   "MATCH (b:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":{iri}}) " +
						   "CREATE (b)-[:SubOf]->(na)";
		
		try(Transaction tx = db.beginTx()) {
			Map<String, Object> param = new HashMap<>();
			param.put("iri", classIRI);
			param.put("neg", StDatabaseBuilder.NEG_PREFIX);
			db.execute(execString, param);
			
			tx.success();
		}
	}
	
	//Build relations go through class A and remove class A
	private void eliminateClass(String classIRI) {
		try(Transaction tx = db.beginTx()) {
			String exeString1 = "MATCH (a:" + NODE_LABEL.INDIVIDUAL + ")-[:is]->(remove:" + NODE_LABEL.TBOXENTITY 
							+ " {" + NODE_KEY.NODE_IRI + ":{iri}})-[:SubOf]->(b) MERGE (a)-[:is]->(b)";
			String exeString2 = "MATCH (a:" + NODE_LABEL.TBOXENTITY + ")-[:SubOf]->(remove:" + NODE_LABEL.TBOXENTITY 
							+ " {" + NODE_KEY.NODE_IRI + ":{iri}})-[:SubOf]->(b) MERGE (a)-[:SubOf]->(b)"
							+ " DETACH DELETE remove";
			
			String exeString = "MATCH (a)-[r1:SubOf|is]->(remove:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI
							+ ":{iri}})-[:SubOf]->(b)"
					   		+ " FOREACH(ignore in CASE type(r1) WHEN \"SubOf\" THEN [1] ELSE [] END | MERGE (a)-[:SubOf]->(b))"
					        + " FOREACH(ignore in CASE type(r1) WHEN \"is\" THEN [1] ELSE [] END | MERGE (a)-[:is]->(b))"
					        + " DETACH DELETE remove";
			
			Map<String, Object> param = new HashMap<>();
			param.put("iri", classIRI);
			
			db.execute(exeString, param);
//			db.execute(exeString1, param);
//			db.execute(exeString2, param);
			tx.success();
		}
	}
}
