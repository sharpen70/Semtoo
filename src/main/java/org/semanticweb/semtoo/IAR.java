package org.semanticweb.semtoo;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.util.Pair;
import org.semanticweb.semtoo.model.CQuery;
import org.semanticweb.semtoo.model.Constant;
import org.semanticweb.semtoo.model.GraphNode;
import org.semanticweb.semtoo.model.GraphNode.NODE_KEY;
import org.semanticweb.semtoo.model.GraphNode.NODE_LABEL;
import org.semanticweb.semtoo.model.NaryAtom;
import org.semanticweb.semtoo.model.Term;
import org.semanticweb.semtoo.model.Variable;
import org.semanticweb.semtoo.neo4j.Neo4jManager;

public class IAR implements ICTolerant_QA {
	private Neo4jManager m = null;
	
	public IAR(Neo4jManager manager) {
		m = manager;
	}
	
	public void detectConflicts(Session session) {
		try(Transaction tc = session.beginTransaction()) {
			String statement = "MATCH (a:Individual)-[:is]->(b)-[0*..]->(c:OWLEntity), "
					+ "(a)-[:is]-(nb)-[0*..]->(n:" + GraphNode.NODE_LABEL.NEGATION +") "
					+ "WHERE n.{key1} = c.{key2} "
					+ "DELETE (a)-[:is]->(b), (a)-[:is]->(nb)";
			tc.run(statement, Values.parameters("key1", GraphNode.NODE_KEY.POSITIVE_NODE_IRI, "key2", GraphNode.NODE_KEY.NODE_IRI));
			tc.success();
		}
	}
	
	public List<List<String>> answer(CQuery q) {
		List<List<String>> result = new ArrayList<>();
		
		List<Variable> vs = q.getQueryVariable();
		List<NaryAtom> conjuncts = q.getConjuncts();
		
		String statement = "MATCH ";
		
		for(int i = 0; i < conjuncts.size(); i++) {
			NaryAtom a = conjuncts.get(i);
			List<Term> terms = a.getTerms();
			String node_pattern = "";
			if(terms.size() < 2) {
				Term t = terms.get(0);
				node_pattern += "(" + t.getName() + ":" + NODE_LABEL.INDIVIDUAL;
				if(t instanceof Constant) node_pattern += " {{iri}:" + t.getName() + "}" ;
				node_pattern += ")-[*]->(" + a.getPredicateName() + " {{iri}:" + a.getFullName() + "})";
			}
			if(terms.size() == 2) {
				Term first = terms.get(0);
				Term second = terms.get(1);
				boolean fC = first instanceof Constant;
				boolean sC = second instanceof Constant;
				String neo4jname = first.getName() + "_" + second.getName();
				
				node_pattern += "(" + neo4jname + ":DUAL {";
				if(fC) node_pattern += "{subject}:" + first.getFullName();
				if(fC && sC) node_pattern += ",";
				if(sC) node_pattern += "{object}:" + second.getFullName();
				node_pattern += "})-[*]->(" + a.getPredicateName() + " {{iri}:" + a.getFullName() + "})";
				
				if(!fC) node_pattern += ", (" + first.getName() + ")-[:Subject]->(" + neo4jname + ")";
				if(!sC) node_pattern += ", (" + first.getName() + ")-[:Object]->(" + neo4jname + ")";
			}
			statement += node_pattern;
			if(i == conjuncts.size() - 1) statement += ", ";
		}
		
		statement += " RETURN ";
		
		for(int i = 0; i < vs.size(); i++) {
			statement += vs.get(i).getName();
			if(i == vs.size() - 1) statement += ", ";
		}
		
		System.out.println(statement);
		
//		try(Session session = m.getSession()) {
//			try(Transaction tc = session.beginTransaction()) {
//				StatementResult re = tc.run(statement, Values.parameters("iri", NODE_KEY.NODE_IRI, "subject", NODE_KEY.SUBJECT_IRI, 
//						"object", NODE_KEY.OBJECT_IRI));
//				
//				while(re.hasNext()) {
//					Record record = re.next();
//					System.out.print("Answer:" );
//					for(Pair<String, Value> p : record.fields()) {
//						System.out.print(p.key() + ": " + p.value());
//					}
//					System.out.print("\n");
//				}
//			}
//		}
		
		return null;
	}
}
