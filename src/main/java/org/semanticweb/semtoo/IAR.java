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
	
	public IAR() {
		m = Neo4jManager.getManager();
	}
	
	public void detectConflicts() {
		try(Session session = m.getSession()) {
			try(Transaction tc = session.beginTransaction()) {
				String statement = "MATCH (a)-[r1:is]->(b)-[*0..]->(c), "
						+ "(a)-[r2:is]->(nb)-[*0..]->(n:" + NODE_LABEL.NEGATION +") "
						+ "WHERE n." + NODE_KEY.POSITIVE_NODE_IRI + " = c." + NODE_KEY.NODE_IRI + " "
						+ "DELETE r1, r2";
				tc.run(statement);
				tc.success();
			}	
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
				if(t instanceof Constant) node_pattern += " {" + NODE_KEY.NODE_IRI + ":\"" + t.getName() + "\"}" ;
				node_pattern += ")-[*]->(" + a.getPredicateName() + ":" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":\"" + a.getFullName() + "\"})";
			}
			if(terms.size() == 2) {
				Term first = terms.get(0);
				Term second = terms.get(1);
				boolean fC = first instanceof Constant;
				boolean sC = second instanceof Constant;
				String neo4jname = first.getName() + "_" + second.getName();
				
				node_pattern += "(" + neo4jname + ":" + NODE_LABEL.DUALINDIVIDUAL + "{";
				if(fC) node_pattern += NODE_KEY.SUBJECT_IRI + ":\"" + first.getFullName() + "\"";
				if(fC && sC) node_pattern += ",";
				if(sC) node_pattern += NODE_KEY.OBJECT_IRI + ":\"" + second.getFullName() + "\"";
				node_pattern += "})-[*]->(" + a.getPredicateName() + ":" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":\"" + a.getFullName() + "\"})";
				
				if(!fC) node_pattern += ", (" + first.getName() + ")-[:Subject]->(" + neo4jname + ")";
				if(!sC) node_pattern += ", (" + second.getName() + ")-[:Object]->(" + neo4jname + ")";
			}
			statement += node_pattern;
			if(i != conjuncts.size() - 1) statement += ", ";
		}
		
		statement += " RETURN DISTINCT ";
		
		for(int i = 0; i < vs.size(); i++) {
			statement += vs.get(i).getName() + "." + NODE_KEY.NODE_IRI;
			if(i != vs.size() - 1) statement += ", ";
		}
		
		System.out.println(statement);
		
		try(Session session = m.getSession()) {
			try(Transaction tc = session.beginTransaction()) {
				StatementResult re = tc.run(statement);
				
				while(re.hasNext()) {
					Record record = re.next();
				//	System.out.println("Answer: " );
					for(Pair<String, Value> p : record.fields()) {
				//		System.out.println(p.key() + ":" + p.value());
					}
				//	System.out.print("\n");
				}
			}
		}
		
		return null;
	}
}
