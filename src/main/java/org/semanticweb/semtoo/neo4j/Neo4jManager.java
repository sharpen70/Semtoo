package org.semanticweb.semtoo.neo4j;

import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

public class Neo4jManager {
	private Driver driver;
	private static final AuthToken token = AuthTokens.basic("neo4j", "123");
	private static final String url = "bolt://localhost:7687";
			
	public Neo4jManager() {
		
	}
	
	public void connect() {
		driver = GraphDatabase.driver(url, token);
	}
	
	public void close() {
		driver.close();
	}
}
