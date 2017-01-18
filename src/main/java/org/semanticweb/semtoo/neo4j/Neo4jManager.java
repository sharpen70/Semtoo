package org.semanticweb.semtoo.neo4j;

import java.util.Collection;
import java.util.Map;

import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.semanticweb.semtoo.Graph.GraphNode;

public class Neo4jManager {
	private Driver driver;
	private static final AuthToken token = AuthTokens.basic("neo4j", "123");
	private static final String url = "bolt://localhost:7687";

	
	public Neo4jManager() {
		driver = GraphDatabase.driver(url, token);		
	}		
	
	public Session getSession() {
		return driver.session();
	}	
	
	
	public void close() {
		driver.close();
	}
}
