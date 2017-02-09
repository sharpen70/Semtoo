package org.semanticweb.semtoo.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.semtoo.embeddedneo4j.Forgetting;
import org.semanticweb.semtoo.embeddedneo4j.IAR;

public class ForgettingPerformance {
	public static final String[] testcases_small = {"u1p0", "u1p15e-4", "u1p5e-2", "u1p2e-1", "u5p0", "u5p15e-4", "u5p5e-2", "u5p2e-1"};
	
	private static final String data_path = "/home/sharpen/neo4j/data/databases/";
	private static final String concepts_path = "./Benchmark/forget_concepts/";
//	private static final String result = "./result/naive_forgetting/";
	private static final String result = "./result/ic_f/";
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		GraphDatabaseService db = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(new File(data_path + "u1p2e-1.db"))
				.setConfig(GraphDatabaseSettings.pagecache_memory, "4g")
				.newGraphDatabase();
		
		IAR iar = new IAR(db);
		
		long start = System.currentTimeMillis();
		iar.traversal();
		long end = System.currentTimeMillis();
		System.out.println("Remove conflicts with " + (end - start) + " ms");
//		singleTest("u5p2e-1", concepts_path + "degree/degree_100");
	}
	
	public static void singleTest(String testowl, String testconcept) throws IOException {
		File c = new File(testconcept);
		File database = new File(data_path + testowl + ".db");
		File tmp_database = new File(data_path + testowl + "_temp.db");
		
		FileUtils.copyDirectory(database, tmp_database);
		
		List<String> concepts = readConcepts(c);
		
		GraphDatabaseService db = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(tmp_database)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "4g")
				.newGraphDatabase();
		
		Forgetting forgetting = new Forgetting(db);
		IAR iar = new IAR(db);
		
		long start = System.currentTimeMillis();
		iar.traversal();
		long end = System.currentTimeMillis();
		System.out.println("Remove conflicts with " + (end - start) + " ms");
		
//		start = System.currentTimeMillis();
//		forgetting.naiveForget(concepts);
//		end = System.currentTimeMillis();
//		System.out.println("Done with " + (end - start) + " ms");
		
		FileUtils.deleteDirectory(tmp_database);
	}
	
	public static void batchTest() throws IOException {
		File concepts_dir = new File(concepts_path);
		List<File> concepts_lists_file = getFile(concepts_dir);
		
		File result_dir = new File(result);
		if(!result_dir.exists()) result_dir.mkdirs();
		
		for(String s : testcases_small) {
			File database = new File(data_path + s + ".db");
			File copy_database = new File(data_path + s + "_temp.db");
			
			PrintWriter writer = new PrintWriter(result + s);
			
			
			for(File f : concepts_lists_file) {
				String concept_case = f.getName();
				 
				FileUtils.copyDirectory(database, copy_database);
				
				GraphDatabaseService db = new GraphDatabaseFactory()
						.newEmbeddedDatabaseBuilder(copy_database)
						.setConfig(GraphDatabaseSettings.pagecache_memory, "4g")
						.newGraphDatabase();
				
				
				System.out.println("Forgetting file " + concept_case + "...");
				writer.println(concept_case);
				writer.println("original node size: " + getDBNodeSizeNum(db) + ", relation size: " + getDBRelSizeNum(db));
				List<String> concepts = readConcepts(f);
				long start, end;
				
				
				Forgetting forgetting = new Forgetting(db);
				
				start = System.currentTimeMillis();
				forgetting.naiveForget(concepts);
				end = System.currentTimeMillis();
				writer.println("Native forget with time: " + (end - start) + " ms");
				writer.println("Node size: " + getDBNodeSizeNum(db) + ", relation size: " + getDBRelSizeNum(db));
				
				writer.println();
				
				db.shutdown();
				FileUtils.deleteDirectory(copy_database);
			}
			writer.close();
		}
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
	
	public static long getDBNodeSizeNum(GraphDatabaseService db) {
		Result re = db.execute("MATCH (n) return count(n) as num");
		
		return (long)(re.next().get("num"));
	}

	public static long getDBRelSizeNum(GraphDatabaseService db) {
		Result re = db.execute("MATCH (n)-[r]-(m) return distinct count(r) as num");
		
		return (long)(re.next().get("num"));
	}
}


