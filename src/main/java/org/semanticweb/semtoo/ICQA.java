package org.semanticweb.semtoo;

import org.neo4j.graphdb.GraphDatabaseService;
import org.semanticweb.semtoo.model.CQuery;

public interface ICQA {
	
	public void repair(GraphDatabaseService db);
	
	public void answer(CQuery q);
	
}
