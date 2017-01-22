package org.semanticweb.semtoo;

import java.util.ArrayList;
import java.util.Collection;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.semanticweb.semtoo.model.GraphNode;
import org.semanticweb.semtoo.neo4j.Neo4jManager;
import org.semanticweb.semtoo.neo4j.Neo4jUpdate;

public class Forgetting {
	Neo4jManager manager = null;
	
	public Forgetting(Neo4jManager m) {
		manager = m;
	}
	
	//Forgets a set of concepts
	
	//Forgets a set of concepts simply one by one
	public void naiveForget(Collection<String> conceptsToforget) {
		for(String iri : conceptsToforget) {
			forget(iri);
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
				String statement = "MATCH (a) " +
								   "WHERE a.iri IN {iris} " +
								   "SET a.forget = true";
				tc.run(statement, Values.parameters("iris", concepts));
				tc.success();
			}
			try(Transaction tc = session.beginTransaction()) {
				String statement = "MATCH (a)-[:SubOf*2..]->(b), " +
								   "(a)-[r:SubOf|is]->(f1 {forget:true}), " +
								   "(f2 {forget:true})-[:SubOf]->(b) " +
								   "WHERE NOT exists(a.forget) AND NOT exists(b.forget) " +
								   "CREATE (a)-[r]->(b)";
//								   "FOREACH(ignore in CASE type(r) WHEN \"SubOf\" THEN [1] ELSE [] END | CREATE (a)-[:SubOf]->(b)) " +
//								   "FOREACH(ignore in CASE type(r) WHEN \"is\" THEN [1] ELSE [] END | CREATE (a)-[:is]->(b))";
				
				tc.run(statement);
				tc.success();
			}
		}
	}
	
	//Forgets One Single Concept
	public void forget(String iriOfConceptToforget) {
		System.out.println("Forgetting concept: " + iriOfConceptToforget);
		try(Session session = manager.getSession()) {
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
	}
	
	//Remove Tbox Axiom of form SubClassOf(A, A) 
	private void removeMeaningless(String classIRI, Transaction tc) {
		Neo4jUpdate.deleteRelation(classIRI, classIRI, "is", tc);
	}
	
	//Check if the given class A has axiom of form Disjoint(A, A)
	private boolean isContradictionClass(String classIRI, Session session) {
		String neg_iri = GraphNode.NEG_PREFIX + classIRI;
		String execString = "MATCH (a {iri:{iri}})-[:SubOf]->(b {iri:{neg_iri}}) RETURN a";
		
		try(Transaction tc = session.beginTransaction()) {
			StatementResult result = tc.run(execString, Values.parameters("iri", classIRI, "neg_iri", neg_iri));
			if(result.hasNext()) return true;
		}
		return false;
	}
	
	//Remove the contradiction class and make its subclasses contradiction class
	private void removeContradictionClass(String classIRI, Transaction tc) {
		String exec = "MATCH (a)-[:SubOf]->(b {iri:{iri}}) "
				      + "MERGE (na {piri:a.iri}) ON CREATE na.iri = {neg} + a.iri, na.piri = a.iri "
					  + "CREATE (a)-[:SubOf]->(na) "
					  + "DETACH DELETE b";
		tc.run(exec, Values.parameters("iri", classIRI, "neg", GraphNode.NEG_PREFIX));
		tc.success();
	}
	
	//Given class A to forget, change all B -> ~A to A -> ~B
	private void changeNegationDirection(String classIRI, Transaction tc) {
		String neg_iri = GraphNode.NEG_PREFIX + classIRI;
		String execString = "MATCH (a)-[r:SubOf]->(nb {iri:{neg_iri}}) DELETE r DETACH DELETE nb " +
						   "MERGE (na {piri:a.iri}) ON CREATE na.iri = {neg} + a.iri, na.piri = a.iri " +
						   "WITH a " +
						   "MATCH (b {iri:{iri}})" +
						   "CREATE (b)-[:SubOf]->(a)";
		tc.run(execString, Values.parameters("neg_iri", neg_iri, "neg", GraphNode.NEG_PREFIX, "iri", classIRI));
		tc.success();
	}
	
	//Build relations go through class A and remove class A
	private void eliminateClass(String classIRI, Transaction tc) {
		String exeString = "MATCH (a)-[r:SubOf|is]->(remove {iri:{iri}})-[:SubOf]->(b) "
						+ "CREATE (a)-[r]->(b) "
						+ "DETACH DELETE remove";
		tc.run(exeString, Values.parameters("iri", classIRI));
		tc.success();
	}
}
