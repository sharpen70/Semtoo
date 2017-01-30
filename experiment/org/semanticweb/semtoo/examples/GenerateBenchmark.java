package org.semanticweb.semtoo.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;

import org.semanticweb.semtoo.graph.GraphAnalyzer;
import org.semanticweb.semtoo.graph.GraphAnalyzer.ORDER;

public class GenerateBenchmark {
	public static final int[] LIMITS = {10, 20, 40, 100};
	public static final String groupConcepts = "./Benchmark/forget_concepts/group";
	public static final String randomConcepts = "./Benchmark/forget_concepts/random";
	public static final String leastDegree = "./Benchmark/forget_concepts/degree";
	
	public static void main(String[] args) throws FileNotFoundException {
		File groupConcepts_dir = new File(groupConcepts);
		if(!groupConcepts_dir.exists()) groupConcepts_dir.mkdirs();
		
		for(int i : LIMITS) {
			Collection<String> concepts = GraphAnalyzer.getRandomGroupConcepts(i);
			File record = new File(groupConcepts + "/group_" + i);
			PrintWriter writer = new PrintWriter(record);
			for(String concept : concepts) {
				writer.println(concept);
			}
			writer.close();
		}
		
		File randomConcepts_dir = new File(randomConcepts);
		if(!randomConcepts_dir.exists()) randomConcepts_dir.mkdirs();
		
		for(int i : LIMITS) {
			Collection<String> concepts = GraphAnalyzer.getRandomDiscreteConcepts(i);
			File record = new File(randomConcepts + "/discrete_" + i);

			PrintWriter writer = new PrintWriter(record);
			for(String concept : concepts) {
				writer.println(concept);
			}
			writer.close();
		}
		
		File leastDegreeConcepts_dir = new File(leastDegree);
		if(!leastDegreeConcepts_dir.exists()) leastDegreeConcepts_dir.mkdirs();
		
		for(int i : LIMITS) {
			Collection<String> concepts = GraphAnalyzer.getConceptsByDegree(i, ORDER.ASC);
			File record = new File(leastDegree + "/degree_" + i);

			PrintWriter writer = new PrintWriter(record);
			for(String concept : concepts) {
				writer.println(concept);
			}
			writer.close();
		}
	}
}
