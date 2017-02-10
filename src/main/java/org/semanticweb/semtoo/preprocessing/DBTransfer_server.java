package org.semanticweb.semtoo.preprocessing;

import java.io.File;
import java.io.IOException;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.node_labels;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.property_key;
import org.semanticweb.semtoo.graph.GraphNode.NODE_KEY;
import org.semanticweb.semtoo.graph.GraphNode.NODE_LABEL;
import org.semanticweb.semtoo.neo4j.Neo4jManager;

public class DBTransfer_server {
	private Neo4jManager m;
	
	public DBTransfer_server() {
		m = Neo4jManager.getManager();
	}
	
	public void loadDBfiletoGraphDB(String clsAssertioncsv, String pptAssertincsv) throws IOException {
		
		clsAssertioncsv = new File(clsAssertioncsv).getCanonicalPath().replaceAll("\\\\", "/");
		pptAssertincsv = new File(pptAssertincsv).getCanonicalPath().replaceAll("\\\\", "/");
		
		String cypher1 = "USING PERIODIC COMMIT 2000"
				+ " LOAD CSV WITH HEADERS FROM \"file:///" + clsAssertioncsv + "\" AS clsa"
				+ " MATCH (cls:" + node_labels.TBOXENTITY + " {" + property_key.IRI_LOWER + ":clsa.class})"
				+ " MERGE (idv:" + node_labels.INDIVIDUAL + " {" + property_key.NODE_IRI + ":clsa.idv})"
				+ " CREATE (idv)-[:is]->(cls)";
		
		String cypher2 = "USING PERIODIC COMMIT 2000"
				+ " LOAD CSV WITH HEADERS FROM \"file:///" + pptAssertincsv + "\" AS ppta"
				+ " MERGE (a:" + node_labels.INDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.subject})"
				+ " MERGE (ab:" + node_labels.DUALINDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.subject + ppta.object})"
				+ " ON CREATE SET ab." + property_key.SUBJECT_IRI + "=ppta.subject, ab." + property_key.OBJECT_IRI + "=ppta.object";
		
		String cypher2_1 = "USING PERIODIC COMMIT 2000"
				+ " LOAD CSV WITH HEADERS FROM \"file:///" + pptAssertincsv + "\" AS ppta"
				+ " MERGE (b:" + node_labels.INDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.object})"
				+ " MERGE (ba:" + node_labels.DUALINDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.object + ppta.subject})"
				+ " ON CREATE SET ba." + property_key.SUBJECT_IRI + "=ppta.object, ba." + property_key.OBJECT_IRI + "=ppta.subject";
		
		String cypher3 = "USING PERIODIC COMMIT 2000"
				+ " LOAD CSV WITH HEADERS FROM \"file:///" + pptAssertincsv + "\" AS ppta"
				+ " MATCH (p:" + node_labels.TBOXENTITY + " {" + property_key.IRI_LOWER + ": ppta.property})"
				+ " MATCH (ip:" + node_labels.TBOXENTITY + " {" + property_key.IRI_LOWER + ":\"inv_\" + ppta.property})"
				+ " MATCH (ab:" + node_labels.DUALINDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.subject + ppta.object})"
				+ " MATCH (ba:" + node_labels.DUALINDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.object + ppta.subject})"
				+ " CREATE (ab)-[:is]->(p)"
				+ " CREATE (ba)-[:is]->(ip)";
		
		String cypher3_2 = "USING PERIODIC COMMIT 2000"
				+ " LOAD CSV WITH HEADERS FROM \"file:///" + pptAssertincsv + "\" AS ppta"
				+ " MATCH (rp:" + node_labels.TBOXENTITY + " {" + property_key.IRI_LOWER + ":\"prt_\" + ppta.property})"
				+ " MATCH (a:" + node_labels.INDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.subject})"
				+ " MERGE (a)-[:is]->(rp)";
		
		String cypher3_1 = "USING PERIODIC COMMIT 2000"
				+ " LOAD CSV WITH HEADERS FROM \"file:///" + pptAssertincsv + "\" AS ppta"
				+ " MATCH (rip:" + node_labels.TBOXENTITY + " {" + property_key.IRI_LOWER + ":\"prt_inv_\" + ppta.property})"
				+ " MATCH (b:" + node_labels.INDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.object})"
				+ " MERGE (b)-[:is]->(rip)";

		System.out.println(cypher1 + "\n");
		System.out.println(cypher2 + "\n");
		System.out.println(cypher2_1 + "\n");
		System.out.println(cypher3 + "\n");
		System.out.println(cypher3_1 + "\n");
		System.out.println(cypher3_2 + "\n");
		
		try(Session session = m.getSession()) {
			try(Transaction tc = session.beginTransaction()) {	
				tc.run("CREATE INDEX ON :" + NODE_LABEL.INDIVIDUAL + "(" + NODE_KEY.NODE_IRI + ")");
				tc.run("CREATE INDEX ON :" + NODE_LABEL.DUALINDIVIDUAL + "(" + NODE_KEY.NODE_IRI + ")");
//				tc.run("CREATE INDEX ON :" + NODE_LABEL.DUALINDIVIDUAL + "(" + NODE_KEY.SUBJECT_IRI + ")");
//				tc.run("CREATE INDEX ON :" + NODE_LABEL.DUALINDIVIDUAL + "(" + NODE_KEY.OBJECT_IRI + ")");
				tc.success();
			}
		}
		
		long start, end;
		System.out.println("Begin inserting class assertion ...");
		start = System.currentTimeMillis();
		try(Session session = m.getSession()) {
			session.run(cypher1);
			session.run(cypher2);
			session.run(cypher2_1);
			session.run(cypher3);
			session.run(cypher3_1);
			session.run(cypher3_2);
		}
		end = System.currentTimeMillis();
		System.out.println("Done class assertion with " + (end - start) + " ms");
	}
}
