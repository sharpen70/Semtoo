package org.semanticweb.semtoo.embeddedneo4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

public class SemtooDatabaseInserter {
	private BatchInserter inserter;
	
	public SemtooDatabaseInserter(String pathtoDB) throws IOException {
		Map<String, String> config = new HashMap<>();
		config.put("dbms.pagecache.memory", "512m");
		inserter = BatchInserters.inserter(new File(pathtoDB).getAbsoluteFile());
	}
	
	public void insertClassAssertion(String idv_iri, String cls_iri) {
	}
	
	public void insertPropertyAssertion(String subject_iri, String object_iri, String ppt_iri) {
		
	}
}
