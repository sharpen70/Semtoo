package org.semanticweb.semtoo.experiment;

import java.io.File;
import java.io.FileNotFoundException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.semtoo.graph.DBFileReader;
import org.semanticweb.semtoo.graph.GraphManager;

public class ForgettingPerformance {
	private static final String ontology_path = "./Benchmark/lubm/LUBM_DLlite.owl";
	private static final String prefix = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";
	private static final String data_path = "./Benchmark/lubm/data";
	private static final String concepts_path = "./Benchmark/forget_concepts";
	
	public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException {
		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
		OWLOntology o = om.loadOntologyFromOntologyDocument(new File(ontology_path));
		
		GraphManager gm = new GraphManager();
		DBFileReader reader = new DBFileReader();
		reader.addPrefix(prefix);
		
		File data_dir = new File(data_path);
		File[] datasets = data_dir.listFiles();
		
		for(File dataset : datasets) {
			gm.clearGraph();
			gm.loadOntologyToGraph(o);
			
			reader.readDBFileToGraph(dataset);
		}
	}
}
