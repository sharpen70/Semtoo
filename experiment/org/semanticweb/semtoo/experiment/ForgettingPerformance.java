package org.semanticweb.semtoo.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.semtoo.Forgetting;

public class ForgettingPerformance {
	private static final String ontology_path = "./Benchmark/lubm/LUBM_DLlite.owl";
	private static final String prefix = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";
	private static final String data_path = "./Benchmark/lubm/data";
	private static final String concepts_path = "./Benchmark/forget_concepts";
	private static final String result = "./result";
			
	public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException {
//		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
//		OWLOntology o = om.loadOntologyFromOntologyDocument(new File(ontology_path));
//		
//		GraphManager gm = new GraphManager();
//		DBFileReader reader = new DBFileReader();
//		reader.addPrefix(prefix);
//		
//		File data_dir = new File(data_path);
//		File[] datasets = data_dir.listFiles();
//		
//		for(File dataset : datasets) {
//			gm.clearGraph();
//			gm.loadOntologyToGraph(o);
//			
//			reader.readDBFileToGraph(dataset);
//		}
		File concepts_dir = new File(concepts_path);
		List<File> concepts_lists_file = getFile(concepts_dir);
		
		Forgetting forgetting = new Forgetting();
		
		PrintWriter writer = new PrintWriter(result);
		
		for(File f : concepts_lists_file) {
			System.out.println("Processing file " + f.getName() + "...");
			writer.println(f.getName());
			List<String> concepts = readConcepts(f);
			long start, end;
			
			forgetting.restore();
			
			start = System.currentTimeMillis();
			forgetting.naiveForget(concepts);
			end = System.currentTimeMillis();
			writer.println("Native forget with time: " + (end - start) + " ms");
			
			forgetting.restore();
			start = System.currentTimeMillis();
			forgetting.neighborRelatedForget(concepts);
			end = System.currentTimeMillis();
			writer.println("Neighbor forget with time: " + (end - start) + " ms");
			
			writer.println();
		}
//		List<List<String>> concepts_lists = new ArrayList<>();
//		for(File f : concepts_lists_file) {
//			concepts_lists.add(readConcepts(f));
//		}
//		
//		for(List<String> l : concepts_lists) {
//			for(String s : l) System.out.println(s);
//		}
	}
	
	private static List<String> readConcepts(File f) throws FileNotFoundException {
		List<String> concepts = new ArrayList<>();
		
		Scanner scanner = new Scanner(f);
		while(scanner.hasNext()) {
			concepts.add(scanner.next());
		}
		scanner.close();
		return concepts;
	}
	
	private static List<File> getFile(File dir) {
		List<File> files = new ArrayList<>();
		if(!dir.isDirectory()) files.add(dir);
		else {
			for(File f : dir.listFiles()) files.addAll(getFile(f));
		}
		return files;
	}
}
