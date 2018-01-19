package org.semanticweb.semtoo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.RelType;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.node_labels;

public class ICF {
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
		
		SFG forgetting = new SFG(db);
		IAR iar = new IAR();
		
		long start = System.currentTimeMillis();
		iar.repair(db);
		long end = System.currentTimeMillis();
		long repair_time = end - start;
		
		start = System.currentTimeMillis();
		forgetting.ofg(concepts);
		end = System.currentTimeMillis();
		long forget_time = end - start;
		
		System.out.println("Repair with " + repair_time + " ms, Forget with " + forget_time + " ms");
		
		Map<String, Integer> meta = calMeta(db);
		System.out.println("Ontology result size tbox: " + meta.get("tbox") + ", abox: " + meta.get("abox"));
		
		db.shutdown();
		FileUtils.deleteDirectory(tmp_database);
	}
	
	public static Map<String, Integer> calMeta(GraphDatabaseService db) {
		
		Map<String, Integer> re = new HashMap<>();
		try(Transaction tx = db.beginTx()) {
			int tbox = 0, abox = 0;
			ResourceIterable<Relationship> origin_relationships = db.getAllRelationships();
			for(Relationship rel : origin_relationships) {
				if(rel.isType(RelType.is)) {
					if(!rel.getEndNode().hasLabel(node_labels.PROPERTY_CLASS)) abox++;
				}
				else tbox++;
			}
			
			re.put("tbox", tbox);
			re.put("abox", abox);	
		}
		return re;
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
