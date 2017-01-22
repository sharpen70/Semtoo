package org.semanticweb.semtoo;

import java.util.Collection;
import java.util.List;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.semanticweb.semtoo.model.Atom;
import org.semanticweb.semtoo.model.CQuery;
import org.semanticweb.semtoo.model.GraphNode;
import org.semanticweb.semtoo.model.Term;
import org.semanticweb.semtoo.model.Variable;
import org.semanticweb.semtoo.neo4j.Neo4jManager;

public class IAR implements QueryAnswering {
	private Neo4jManager m = null;
	
	public IAR(Neo4jManager manager) {
		m = manager;
	}
	
	private void detectConflicts(Session session) {
		try(Transaction tc = session.beginTransaction()) {
			String statement = "MATCH (a:Individual)-[:is]->(b)-[0*..]->(c:OWLEntity), "
					+ "(a)-[:is]-(nb)-[0*..]->(n:" + GraphNode.NODE_LABEL.NEGATION +") "
					+ "WHERE n.{key1} = c.{key2} "
					+ "DELETE (a)-[:is]->(b), (a)-[:is]->(nb)";
			tc.run(statement, Values.parameters("key1", GraphNode.NODE_KEY.POSITIVE_NODE_IRI, "key2", GraphNode.NODE_KEY.NODE_IRI));
			tc.success();
		}
	}
	
	public Collection<String> answer(CQuery q) {
		List<Variable> vs = q.getQueryVariable();
		List<Atom> conjuncts = q.getConjuncts();
		
		String statement = "MATCH ";
		for(Atom a : conjuncts) {
			List<Term> terms = a.getTerms();
			if(terms.size() < 2) {
				Term t = terms.get(0);
				//String node_pattern = "(" +  + " {iri:" + a.getPredicateName() + "})";
			}
			if(terms.size() == 2) {}
		}
		
		return null;
	}
}
