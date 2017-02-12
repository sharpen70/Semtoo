package org.semanticweb.semtoo;

import java.util.ArrayList;
import java.util.Collection;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.semtoo.graph.GraphNode;
import org.semanticweb.semtoo.graph.GraphNode.NODE_KEY;
import org.semanticweb.semtoo.graph.GraphNode.NODE_LABEL;
import org.semanticweb.semtoo.neo4j.Neo4jManager;
import org.semanticweb.semtoo.neo4j.Neo4jUpdate;

public class Forgetting_old {
	private Neo4jManager manager = null;
	
	private static final String THING_IRI = OWLManager.getOWLDataFactory().getOWLThing().toStringID();
	
	public Forgetting_old() {
		manager = Neo4jManager.getManager();
	}
	
	public void restore() {
		try(Session session = manager.getSession()) {
			try(Transaction tc = session.beginTransaction()) {
				String statement = "MATCH (n:" + NODE_LABEL.FORGET + ")"
						+ " REMOVE n:" + NODE_LABEL.FORGET 
						+ " SET n:" + NODE_LABEL.TBOXENTITY
						+ " MATCH [r:tSubOf|tis] DELETE r "
						+ " MATCH [r:rSubOf] SET r:SubOf "
						+ " MATCH [r:ris] SET r:is";
				tc.run(statement);
				tc.success();
			}
		}
	}
	
	//Forgets a set of concepts
	
	//Forgets a set of concepts simply one by one
	public void naiveForget(Collection<String> conceptsToforget) {
		try(Session session = manager.getSession()) {
			for(String iri : conceptsToforget) forget(iri, session);
		}
	}
	
	//Check out the neighbors of forgetting concepts, then forget by relating all these neighbors
	public void neighborRelatedForget(Collection<String> conceptsToforget) {
		ArrayList<String> concepts = new ArrayList<>();
		
		try(Session session = manager.getSession()) {
			for(String iri : conceptsToforget) {
				try(Transaction tc = session.beginTransaction()) {
					removeMeaningless(iri, tc);
					changeNegationDirection(iri, tc);				
				}
				if(isContradictionClass(iri, session)) {
					try(Transaction tc = session.beginTransaction()) {
						removeContradictionClass(iri, tc);
					}					
				}
				else {
					concepts.add(iri);
				}
			}
			
			try(Transaction tc = session.beginTransaction()) {
				String statement = "MATCH (a:" + NODE_LABEL.TBOXENTITY + ") " +
								   "WHERE a." + NODE_KEY.NODE_IRI + " IN {iris} " +
								   "SET a:" + NODE_LABEL.FORGET;
				tc.run(statement, Values.parameters("iris", concepts));
				tc.success();
			}
			try(Transaction tc = session.beginTransaction()) {
				String statement = "MATCH " +
								   "(a)-[r1:SubOf|is]->(f1:" + NODE_LABEL.FORGET + "), " +
								   "(f2:" + NODE_LABEL.FORGET + ")-[r2:SubOf]->(b) " +
								   "WHERE NOT a:" + NODE_LABEL.FORGET + " AND NOT b:" + NODE_LABEL.FORGET +
								   " AND (a)-[*2..]->(b) " +
								   "FOREACH(ignore in CASE type(r) WHEN \"SubOf\" THEN [1] ELSE [] END | SET r1:rSubOf MERGE (a)-[r:SubOf]->(b)) ON CREATE SET r:tSubOf" +
								   "FOREACH(ignore in CASE type(r) WHEN \"is\" THEN [1] ELSE [] END | SET r1:ris MERGE (a)-[r:is]->(b)) ON CREATE SET r:tis"
								   + " SET r2:rSubOf";
				
				tc.run(statement);
//				tc.run("MATCH (delete:" + NODE_LABEL.FORGET + ") DETACH DELETE delete");
				tc.run("MATCH (del:" + NODE_LABEL.FORGET + ") REMOVE del:" + NODE_LABEL.TBOXENTITY);
				tc.success();
			}
		}
	}
	
	//Forgets One Single Concept
	public void forget(String iriOfConceptToforget) {
		try(Session session = manager.getSession()) {
			forget(iriOfConceptToforget, session);
		}
	}
	
	public void forget(String iriOfConceptToforget, Session session) {
		System.out.println("Forgetting concept: " + iriOfConceptToforget);
		try(Transaction tc = session.beginTransaction()) {
			removeMeaningless(iriOfConceptToforget, tc);
			changeNegationDirection(iriOfConceptToforget, tc);				
		}
		
		if(isContradictionClass(iriOfConceptToforget, session)) {
			try(Transaction tc = session.beginTransaction()) {
				removeContradictionClass(iriOfConceptToforget, tc);
			}
		}
		else {
			try(Transaction tc = session.beginTransaction()) {
				eliminateClass(iriOfConceptToforget, tc);
			}
		}		
	}
	
	//Remove Tbox Axiom of form SubClassOf(A, A) 
	private void removeMeaningless(String classIRI, Transaction tc) {
		String statement = "MATCH (n:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":{iri}}) "
				+ "-[r:SubOf]->(n) DELETE r";
		tc.run(statement, Values.parameters("iri", classIRI));
		tc.success();
	}
	
	//Check if the given class A has axiom of form Disjoint(A, A)
	private boolean isContradictionClass(String classIRI, Session session) {
		String execString = "MATCH (a:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI 
					+ ":{iri}})-[:SubOf]->(b:" + NODE_LABEL.NEGATION + " {" + NODE_KEY.POSITIVE_NODE_IRI 
					+ ":{thing_iri}}) RETURN a";
		
		try(Transaction tc = session.beginTransaction()) {
			StatementResult result = tc.run(execString, Values.parameters("iri", classIRI, "thing_iri", THING_IRI));
			if(result.hasNext()) return true;
		}
		return false;
	}
	
	//Remove the contradiction class and make its subclasses contradiction class
	private void removeContradictionClass(String classIRI, Transaction tc) {
		String exec = "MATCH (a:" + NODE_LABEL.TBOXENTITY + ")-[:SubOf]->(b:" + NODE_LABEL.TBOXENTITY 
					  + " {" + NODE_KEY.NODE_IRI + ":{biri}}) "
					  + "REMOVE b:" + NODE_LABEL.TBOXENTITY + " SET b:" + NODE_LABEL.FORGET
//				  	  + "DETACH DELETE b "
					  + " WITH a, b"
					  + "MATCH (b)-[r:SubOf]-(ignoreme) "
					  + "SET r:rSubOf "
					  + "MATCH (nthing:" + NODE_LABEL.NEGATION 
					  + " {" + NODE_KEY.POSITIVE_NODE_IRI + ":{thing_iri}}) "
					  + "CREATE (a)-[:tSubOf]->(nthing)";

		tc.run(exec, Values.parameters("biri", classIRI, "thing_iri", THING_IRI));
		tc.success();
	}
	
	//Given class A to forget, change all B -> ~A to A -> ~B
	private void changeNegationDirection(String classIRI, Transaction tc) {
		String execString = "MATCH (a:" + NODE_LABEL.TBOXENTITY + ")-[r:SubOf]->(nb:" + NODE_LABEL.NEGATION 
							+ " {" + NODE_KEY.POSITIVE_NODE_IRI + ":{iri}}) " + "DETACH DELETE nb " +
						   "MERGE (na:" + NODE_LABEL.NEGATION + " {" + NODE_KEY.POSITIVE_NODE_IRI + ":a." + NODE_KEY.NODE_IRI + "}) "
						   	+ "ON CREATE SET na." + NODE_KEY.NODE_IRI + " = {neg} + a." + NODE_KEY.NODE_IRI + 
						   	", na." + NODE_KEY.NODE_DESCRIBTION + " = a." + NODE_KEY.NODE_DESCRIBTION +
						   " WITH na " +
						   "MATCH (b:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":{iri}}) " +
						   "CREATE (b)-[:SubOf]->(na)";
		tc.run(execString, Values.parameters("iri", classIRI,
				 "neg", GraphNode.NEG_PREFIX));
		tc.success();
	}
	
	//Build relations go through class A and remove class A
	private void eliminateClass(String classIRI, Transaction tc) {
		String exeString = "MATCH (a)-[r1:SubOf|is|tSubOf|tis]->(mv:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI
						+ ":{iri}})-[r2:SubOf|tSubOf]->(b:" + NODE_LABEL.TBOXENTITY + ")"
						+ " WHERE NOT a:" + NODE_LABEL.FORGET
				   		+ " FOREACH(ignore in CASE type(r1) WHEN \"SubOf\" THEN [1] ELSE [] END | SET r1:rSubOf MERGE (a)-[r:SubOf]->(b)) ON CREATE SET r:tSubOf"
				        + " FOREACH(ignore in CASE type(r1) WHEN \"is\" THEN [1] ELSE [] END | SET r1:ris MERGE (a)-[r:is]->(b)) ON CREATE SET r:tis"
				        + " FOREACH(ignore in CASE type(r1) WHEN \"tSubOf\" THEN [1] ELSE [] END | DELETE r1 MERGE (a)-[r:SubOf]->(b)) ON CREATE SET r:tSubOf"
				        + " FOREACH(ignore in CASE type(r1) WHEN \"tis\" THEN [1] ELSE [] END | DELETE r1 MERGE (a)-[r:is]->(b)) ON CREATE SET r:tis"
				        + " FOREACH(ignore in CASE type(r2) WHEN \"SubOf\" THEN [1] ELSE [] END | SET r2:rSubOf"
				        + " FOREACH(ignore in CASE type(r2) WHEN \"tSubOf\" THEN [1] ELSE [] END | DELETE r2"
						+ " REMOVE mv:" + NODE_LABEL.TBOXENTITY + " SET mv:" + NODE_LABEL.FORGET;
//				        + " DETACH DELETE remove";
		tc.run(exeString, Values.parameters("iri", classIRI));
		tc.success();
	}
}
