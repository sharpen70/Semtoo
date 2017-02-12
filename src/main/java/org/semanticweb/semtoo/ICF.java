package org.semanticweb.semtoo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

public class ICF {
	private GraphDatabaseService db;
	private ICQA icqa;
	
	public ICF(ICQA ic) {
		icqa = ic;
	}
	
	public void forget(GraphDatabaseService _db, List<String> iris) {
		icqa.repair(_db);
		FG f = new FG(_db);
		f.ofg(iris);
	}
	
	public static void main(String[] args) throws IOException {
		String fg_file = null;
		String database = null;
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("-fg")) fg_file = args[++i];
			if(args[i].equals("-db")) database = args[++i];
		}
		
		if(fg_file == null) { System.out.println("File of concepts to forget needed"); return; }
		if(database == null) { System.out.println("no databate provided"); return; }
		
		System.out.println("Forgetting " + new File(database).getName() + " with " + new File(fg_file).getName());
		singleTest(database, fg_file);
		
//		final ExecutorService exec = Executors.newFixedThreadPool(1);
//		
//		Callable<String> call = new Callable<String>() {
//			@Override
//			public String call() throws Exception {
//				return "Forget successfully";
//			}
//		};
//		
//		Future<String> future = exec.submit(call);
//		
//		try {
//			String msg = future.get(3600, TimeUnit.SECONDS);
//			System.out.println("Thread return: " + msg);
//		} catch (InterruptedException e) {
//			System.out.println("Thread interupted ...");
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			System.out.println("Fail to execute ...");
//			e.printStackTrace();
//		} catch (TimeoutException e) {
//			System.out.println("Task time out ...");
//		}
	}
	
	public static void singleTest(String testdb, String testconcept) throws IOException {
		File c = new File(testconcept);
		File database = new File(testdb);
		File tmp_database = new File("temp_" + database.getName());
		
		FileUtils.copyDirectory(database, tmp_database);
		
		List<String> concepts = readConcepts(c);
		
		GraphDatabaseService db = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(tmp_database)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "4g")
				.newGraphDatabase();
		
		FG forgetting = new FG(db);
		IAR iar = new IAR();
		
		long start = System.currentTimeMillis();
		iar.repair(db);
		long end = System.currentTimeMillis();
		System.out.println("Repair with " + (end - start) + " ms");
		
		start = System.currentTimeMillis();
		forgetting.ofg(concepts);
		end = System.currentTimeMillis();
		System.out.println("Forget with " + (end - start) + " ms");
		
		db.shutdown();
		FileUtils.deleteDirectory(tmp_database);
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
}
