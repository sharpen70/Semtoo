package org.semanticweb.semtoo.graph;

import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.semanticweb.semtoo.graph.GraphNode.NODE_KEY;
import org.semanticweb.semtoo.graph.GraphNode.NODE_LABEL;

public class GraphInserter {
	public static void addClassAssertion(String idv_iri, String predicate_iri, Transaction tc) {
		String statement = "MERGE (a:" + NODE_LABEL.INDIVIDUAL + " {" + NODE_KEY.NODE_IRI + ":{idv_iri}}) "
				+ "WITH a MATCH (p:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.IRI_LOWER + ":{p_iri}}) "
						+ "CREATE (a)-[:is]->(p)";
		tc.run(statement, Values.parameters("idv_iri", idv_iri, "p_iri", predicate_iri));
		tc.success();	
	}
	
	public static void addPropertyAssertion(String term1, String term2, String property_iri, Transaction tc) {
		String ab_iri = term1 + "_" + term2;
		String ba_iri = term2 + "_" + term1;
		String inv_predicate_iri = GraphNode.INV_PREFIX + property_iri;
		String prt_iri = GraphNode.PRT_PREFIX + property_iri;
		String inv_prt_iri = GraphNode.PRT_PREFIX + GraphNode.INV_PREFIX + property_iri;
		
		String statement = 
				"MERGE (a:" + NODE_LABEL.INDIVIDUAL + " {" + NODE_KEY.NODE_IRI + ":{term1}})"
				+ " MERGE (b:" + NODE_LABEL.INDIVIDUAL + " {" + NODE_KEY.NODE_IRI + ":{term2}})"
				+ " MERGE (ab:" + NODE_LABEL.DUALINDIVIDUAL + " {" + NODE_KEY.NODE_IRI + ":{ab_iri}})"
				+ " ON CREATE SET ab." + NODE_KEY.SUBJECT_IRI + "={term1}, ab." + NODE_KEY.OBJECT_IRI + "={term2}"
				+ " MERGE (ba:" + NODE_LABEL.DUALINDIVIDUAL + " {" + NODE_KEY.NODE_IRI + ":{ba_iri}})"
				+ " ON CREATE SET ba." + NODE_KEY.SUBJECT_IRI + "={term2}, ba." + NODE_KEY.OBJECT_IRI + "={term1}"
				+ " WITH a, b, ab, ba"
				+ " MATCH (p:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.IRI_LOWER + ":{p_iri}}),"
				+ " (ip:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.IRI_LOWER + ":{inv_p_iri}}),"
				+ " (rp:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.IRI_LOWER + ":{prt_iri}}),"
				+ " (rip:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.IRI_LOWER + ":{inv_prt_iri}})"
				+ " CREATE (ab)-[:is]->(p), (ba)-[:is]->(ip)"
				+ " MERGE (a)-[:is]->(rp) MERGE (b)-[:is]->(rip)";
		System.out.println(statement);
		tc.run(statement, Values.parameters("term1", term1, "term2", term2, "ab_iri", ab_iri, "ba_iri", ba_iri, 
				"p_iri", property_iri, "inv_p_iri", inv_predicate_iri, "prt_iri", prt_iri, "inv_prt_iri", inv_prt_iri));
		tc.success();
	}
}
