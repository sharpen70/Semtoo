package org.semanticweb.semtoo;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.semanticweb.semtoo.model.CQuery;

public class ICAR implements ICQA {
	@Override
	public void answer(CQuery q) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void repair(GraphDatabaseService db) {
		try(Transaction tx = db.beginTx()) {
			
		}
	}
}
