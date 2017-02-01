package org.semanticweb.semtoo.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import java.util.stream.Stream;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLException;

import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.semtoo.Forgetting;
import org.semanticweb.semtoo.graph.DBFileReader;
import org.semanticweb.semtoo.graph.GraphInserter;
import org.semanticweb.semtoo.graph.GraphManager;
import org.semanticweb.semtoo.neo4j.Neo4jManager;
import org.semanticweb.semtoo.neo4j.Neo4jUpdate;


public class LoadOntology {
	public static final String default_prefix = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";
	public static final String databases = "C:/Users/s5051530/Downloads/SaQAI/SaQAI_latest/Databases";
	
	public static void main(String[] args) throws Exception {
//		if(args.length < 2) throw new Exception("Need at least two variables, the first OWLOnotlogy file, the second Abox DB file.");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		File lubm = new File("./resources/LUBM_DLlite.owl");
		File lubm_data = new File("./resources/u1p0.sql");

//		File lubm = new File(args[0]);
//		File lubm_data = new File(args[1]);

//		File lubm_data = new File("../IJCAI17/Databases/u5p2e-1.sql");
//		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("./resources/pizza.owl"));			
		
//		try(Session session = Neo4jManager.getManager().getSession()) {
//			try(Transaction tc = session.beginTransaction()) {
//				GraphInserter.addPropertyAssertion("a", "b", "c", tc);
//			}
//		}
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(lubm);	
		
		GraphManager gm = new GraphManager();
		gm.clearGraph();
		gm.loadOntologyToGraph(ontology);
		
		DBFileReader reader = new DBFileReader();
		reader.addPrefix(default_prefix);
		reader.readDBFileToGraph(lubm_data);
		
		gm.close();
	}

}
