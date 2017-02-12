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
import org.semanticweb.semtoo.FG;
import org.semanticweb.semtoo.IAR;

public class ForgettingPerformance {
	public static final String[] testcases_small = {"u1p0", "u1p15e-4", "u1p5e-2", "u1p2e-1", "u5p0", "u5p15e-4", "u5p5e-2", "u5p2e-1"};
	
	private static final String data_path = "/home/sharpen/neo4j/data/databases/";
	private static final String concepts_path = "./Benchmark/forget_concepts/";
//	private static final String result = "./result/naive_forgetting/";
	private static final String result = "./result/ic_f/";
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		String fg_file = null;
		String database = null;
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("-fg")) fg_file = args[++i];
			if(args[i].equals("-db")) database = args[++i];
		}
		
		if(fg_file == null) { System.out.println("File of concepts to forget needed"); return; }
		if(database == null) { System.out.println("no databate provided"); return; }
		
		System.out.println("Forgetting " + new File(fg_file).getName() + " with " + new File(database).getName());
		singleTest(database, fg_file);
	}
	
	public static void singleTest(String testdb, String testconcept) throws IOException {
		File c = new File(testconcept);
		File database = new File(testdb);
		File tmp_database = new File(database.getParentFile().getAbsolutePath() + "/temp_" + database.getName());
		
		long start = System.currentTimeMillis();
		FileUtils.copyDirectory(database, tmp_database);
		long end = System.currentTimeMillis();
		System.out.println("copy with " + (end - start) + " ms");
		
		List<String> concepts = readConcepts(c);
		
		start = System.currentTimeMillis();
		GraphDatabaseService db = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(tmp_database)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "4g")
				.newGraphDatabase();
		end = System.currentTimeMillis();
		System.out.println("Start database with " + (end - start) + " ms");
		
		FG forgetting = new FG(db);
		IAR iar = new IAR();
		
		start = System.currentTimeMillis();
		iar.repair(db);
		end = System.currentTimeMillis();
		System.out.println("Repair with " + (end - start) + " ms");
		
		start = System.currentTimeMillis();
		forgetting.ofg(concepts);
		end = System.currentTimeMillis();
		System.out.println("Forget with " + (end - start) + " ms");
		
		db.shutdown();
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
				
				
				FG forgetting = new FG(db);
				
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
		Result re = db.execute("MATCH (n)-[r]-(m) return count(distinct r) as num");
		
		return (long)(re.next().get("num"));
	}
}


