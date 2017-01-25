package org.semanticweb.semtoo.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.semanticweb.semtoo.model.GraphNode;
import org.semanticweb.semtoo.model.GraphNode.NODE_KEY;
import org.semanticweb.semtoo.model.GraphNode.NODE_LABEL;
import org.semanticweb.semtoo.neo4j.Neo4jManager;
import org.semanticweb.semtoo.util.Helper;


//This file reader only accepts the dump file of PostgreSQL database

public class DBFileReader {
	private Neo4jManager m = null;
	private String prefix;
	
	public DBFileReader() {
		m = Neo4jManager.getManager();
		prefix = "";
	}
	
	public void addPrefix(String pf) {
		prefix = pf;
	}
	
	public void readDBFileToGraph(File f) throws FileNotFoundException {
		Scanner scanner = new Scanner(f);
		
		int read_count = 0;
		int rc = 0;
		int insert_count = 0;
		int ic = 0;
		
		long start = System.currentTimeMillis();
		
		try(Session session = m.getSession()) {
			try(Transaction tc = session.beginTransaction()) {	
				tc.run("CREATE INDEX ON :" + NODE_LABEL.INDIVIDUAL + "(" + NODE_KEY.NODE_IRI + ")");
				tc.run("CREATE INDEX ON :" + NODE_LABEL.DUALINDIVIDUAL + "(" + NODE_KEY.NODE_IRI + ")");
				tc.success();
			}
			
			try(Transaction tc = session.beginTransaction()) {				
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(line.contains("COPY")) {
					String predicate_name = Helper.singleMatch(line, "^COPY\\s([^\\s(]*)");
					if(predicate_name == null) break;
					String predicate_iri = prefix + predicate_name;
					
					String data = scanner.nextLine();
					while(!data.matches("^\\\\.")) {
						Pattern data_pattern = Pattern.compile("[\\S]+");
						Matcher matcher = data_pattern.matcher(data);
						
						String term1 = null;
						String term2 = null;
						
						if(matcher.find()) {
							term1 = matcher.group();
							if(matcher.find()) {
								term2 = matcher.group();
							}
						}
						
						if(term1 != null) {
							if(term2 != null) {
								String ab_iri = term1 + "_" + term2;
								String ba_iri = term2 + "_" + term1;
								String inv_predicate_iri = GraphNode.INV_PREFIX + predicate_iri;
								String prt_iri = GraphNode.PRT_PREFIX + predicate_iri;
								String inv_prt_iri = GraphNode.PRT_PREFIX + GraphNode.INV_PREFIX + predicate_iri;
								
								String statement = "MERGE (a:" + NODE_LABEL.INDIVIDUAL + " {" + NODE_KEY.NODE_IRI + ":\"" + term1 + "\"})"
										+ " MERGE (b:" + NODE_LABEL.INDIVIDUAL + " {" + NODE_KEY.NODE_IRI + ":\"" + term2 + "\"})"
										+ " MERGE (ab:" + NODE_LABEL.DUALINDIVIDUAL + " {" + NODE_KEY.NODE_IRI + ":\"" + ab_iri + "\"})"
										+ " ON CREATE SET ab." + NODE_KEY.SUBJECT_IRI + "=\"" + term1 + "\", ab." + NODE_KEY.OBJECT_IRI + "=\"" + term2 + "\""
										+ " MERGE (ba:" + NODE_LABEL.DUALINDIVIDUAL + " {" + NODE_KEY.NODE_IRI + ":\"" + ba_iri + "\"})"
										+ " ON CREATE SET ba." + NODE_KEY.SUBJECT_IRI + "=\"" + term2 + "\", ba." + NODE_KEY.OBJECT_IRI + "=\"" + term1 + "\""
										+ " MERGE (a)-[:Subject]->(ab) MERGE (a)-[:Object]->(ba)"
										+ " MERGE (b)-[:Subject]->(ba) MERGE (b)-[:Object]->(ab)"
										+ " WITH a, b, ab, ba"
										+ " MATCH (p:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.IRI_LOWER + ":\"" + predicate_iri + "\"}),"
										+ " (ip:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.IRI_LOWER + ":\"" + inv_predicate_iri + "\"}),"
										+ " (rp:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.IRI_LOWER + ":\"" + prt_iri + "\"}),"
										+ " (rip:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.IRI_LOWER + ":\"" + inv_prt_iri + "\"})"
										+ " CREATE (ab)-[:is]->(p), (ba)-[:is]->(ip)"
										+ " MERGE (a)-[:is]->(rp) MERGE (b)-[:is]->(rip)";
								tc.run(statement);
								tc.success();
							}
							else {
								String statement = "MERGE (a:" + NODE_LABEL.INDIVIDUAL + " {" + NODE_KEY.NODE_IRI + ":\"" + term1 + "\"}) "
										+ "WITH a MATCH (p:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.IRI_LOWER + ":\"" + predicate_iri + "\"}) "
												+ "CREATE (a)-[:is]->(p)";
								tc.run(statement);
								tc.success();
							}
							rc++;
							ic++;
						}
						
						if(rc >= 100) {
							read_count += rc;
							rc = 0;
							long end = System.currentTimeMillis();
							System.out.println("Done reading of " + read_count + " assertion records with " + (end - start) + " ms");
						}
					//	if(read_count >= 2000) break;
//						if(ic >= 10000) {
//							tc.close();
//							tc = session.beginTransaction();
//							insert_count += ic;
//							ic = 0;
//							long end = System.currentTimeMillis();
//							System.out.println("Done inserting of " + insert_count + " assertion records with " + (end - start) / 1000 + " ms");
//						}
						data = scanner.nextLine();
					}
					//if(read_count >= 2000) break;
				}
			}
			}//tc.close();
		}
		scanner.close();
	}
}
