package org.semanticweb.semtoo.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.semanticweb.semtoo.graph.GraphNode.NODE_KEY;
import org.semanticweb.semtoo.graph.GraphNode.NODE_LABEL;
import org.semanticweb.semtoo.neo4j.Neo4jManager;

public class GraphAnalyzer {
	public static Neo4jManager m = Neo4jManager.getManager();
	public static enum ORDER {ASC, DESC}
	
	public static Collection<String> getConceptsByDegree(int limit, ORDER order) {
		List<String> result = new ArrayList<>();
		String desc;
		
		if(order == ORDER.ASC) desc = "";
		else desc = "DESC";
		
		String exec = "MATCH (n:" + NODE_LABEL.CLASS + ")--(b)" 
				+ " RETURN n." + NODE_KEY.NODE_IRI + " AS IRI, count(*) AS Degrees "
				+ "Order By Degrees " + desc 
				+ " LIMIT " + limit;
		
		try(Session session = m.getSession()) {
			try(Transaction tc = session.beginTransaction()) {
				StatementResult re = tc.run(exec);
				
				while(re.hasNext()) {
					Record r = re.next();
					result.add(r.get("IRI").asString());
				}
			}
		}
		
		return result;
	}
	
	public static Collection<String> getRandomGroupConcepts(int limit) {
		List<String> result = new ArrayList<>();
		List<Record> iris;
		
		String getClasses = "MATCH (n:" + NODE_LABEL.CLASS + ") RETURN n." + NODE_KEY.NODE_IRI + " AS IRIS";
		String getGroup =  "MATCH (n:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":{target}})"
				+ "-[:SubOf*0..6]-(g:" + NODE_LABEL.CLASS + ")"
						+ " RETURN DISTINCT g." + NODE_KEY.NODE_IRI + " AS TIRIS LIMIT " + limit;
				
		try(Session session = m.getSession()) {
			try(Transaction tc = session.beginTransaction()) {
				StatementResult re = tc.run(getClasses);
				iris = re.list();
			}
			
			Random rand = new Random();
			int target = rand.nextInt(iris.size());
			String target_iri = iris.get(target).get("IRIS").asString();
			
			try(Transaction tc = session.beginTransaction()) {
				StatementResult re = tc.run(getGroup, Values.parameters("target", target_iri));
				while(re.hasNext()) {
					Record r = re.next();
					result.add(r.get("TIRIS").asString());
				}
			}
		}
		
		return result;
	}
	
	public static Collection<String> getRandomDiscreteConcepts(int limit) {
		Set<String> result = new HashSet<>();
		
		try(Session session = m.getSession()) {
			try(Transaction tc = session.beginTransaction()) {
				String statement = "MATCH (n:" + NODE_LABEL.CLASS + ") RETURN n." + NODE_KEY.NODE_IRI + " AS IRIS";
				StatementResult re = tc.run(statement);
				List<Record> records = re.list();
				
				Random rand = new Random();
				
				while(result.size() < limit) {
					int t = rand.nextInt(records.size());
					result.add(records.get(t).get("IRIS").asString());
				}
			}
		}
		
		return result;
	}
}
