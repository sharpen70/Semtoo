package org.semanticweb.semtoo.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import java.util.stream.Stream;

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


public class LoadOntology {
	public static final String prefix = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";
	
	public static void main(String[] args) throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		File lubm = new File("./resources/LUBM_DLlite.owl");
		File lubm_data = new File("./resources/u1p0.sql");
		//	OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("./resources/pizza.owl"));			
		
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(lubm);	
		
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
