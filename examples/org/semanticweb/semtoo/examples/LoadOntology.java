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
import org.semanticweb.semtoo.graph.GraphManager;
import org.semanticweb.semtoo.neo4j.Neo4jManager;
import org.semanticweb.semtoo.neo4j.Neo4jUpdate;


public class LoadOntology {
	public static final String prefix = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";
	public static final String databases = "C:/Users/s5051530/Downloads/SaQAI/SaQAI_latest/Databases";
	
	public static void main(String[] args) throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		File lubm = new File("./resources/LUBM_DLlite.owl");
		File lubm_data = new File("./resources/u1p0.sql");
//		File lubm_data = new File("C:/Users/s5051530/Downloads/SaQAI/SaQAI_latest/Databases/u5p2e-1.sql");
		//	OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("./resources/pizza.owl"));			
		
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(lubm);	
		
		Neo4jManager m = Neo4jManager.getManager();
	
		try(Session session = m.getSession()) {
			try(Transaction tc = session.beginTransaction()) {
					Neo4jUpdate.clearGraph(tc);
			}
		}
	
		GraphManager gm = new GraphManager();
		gm.loadOntologyToGraph(ontology);
		DBFileReader reader = new DBFileReader();
		reader.addPrefix(prefix);
		reader.readDBFileToGraph(lubm_data);
		
//		String conceptIRI = "http://www.co-ode.org/ontologies/pizza/pizza.owl#FourSeasons";
//		Forgetting f = new Forgetting(m);
//		f.forget(conceptIRI);
		gm.close();
	}

}
