package org.semanticweb.semtoo.examples;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import static org.neo4j.driver.v1.Values.parameters;

public class ConnectNeo4j {
	
	public static void main(String[] args) {
		Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "123" ) );
		System.out.println("Connecting to Neo4j");
		try ( Session session = driver.session() )
		{
			System.out.println("Entering Session");
		    try (Transaction tx = session.beginTransaction()) {
		    	System.out.println("Clearing database");
		    	tx.run("MATCH (a) DETACH DELETE a");
		    	tx.success();
		    }
		    
		    try ( Transaction tx = session.beginTransaction() )
		    {
		        tx.run( "CREATE (a:Person {name: {namd}, title: {title}})",
		                parameters( "namd", "Arthur", "title", "King" ) );
		        tx.success();
		    }

		    try ( Transaction tx = session.beginTransaction() )
		    {
		        StatementResult result = tx.run( "MATCH (a:Person) WHERE a.name = {name} " +
		                                         "RETURN a.name AS name, a.title AS title",
		                                         parameters( "name", "Arthur" ) );
		        while ( result.hasNext() )
		        {
		            Record record = result.next();
		            System.out.println( String.format( "%s %s", record.get( "title" ).asString(), record.get( "name" ).asString() ) );
		        }
		    }

		}

		driver.close();
	}

}
