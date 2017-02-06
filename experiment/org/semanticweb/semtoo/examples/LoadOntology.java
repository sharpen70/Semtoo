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
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLException;

import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.semtoo.Forgetting;
import org.semanticweb.semtoo.embeddedneo4j.SemtooDatabase;
import org.semanticweb.semtoo.graph.DBFileReader;
import org.semanticweb.semtoo.graph.GraphInserter;
import org.semanticweb.semtoo.graph.GraphManager;
import org.semanticweb.semtoo.neo4j.Neo4jManager;
import org.semanticweb.semtoo.neo4j.Neo4jUpdate;
import org.semanticweb.semtoo.preprocessing.DBTransfer;
import org.semanticweb.semtoo.preprocessing.DBTransfer_server;
import org.semanticweb.semtoo.preprocessing.OWLTransfer;


public class LoadOntology {
	public static final String csv_store = "./CSVStore/";
	public static final String[] testcases_small = {"u1p0", "u1p15e-4", "u1p5e-2", "u1p2e-1", "u5p0", "u5p15e-4", "u5p5e-2", "u5p2e-1"};
	public static final String[] testcases_big = {/*"u10p0", "u10p15e-4",*/ "u10p5e-2", "u10p2e-1", "u20p15e-4", "u20p5e-2"};
	public static final String neo4j_datapath = "/home/sharpen/neo4j/data/databases/";
	public static final String new_datapath = "/home/sharpen/data/databases/";
	
	public static void main(String[] args) throws Exception {
//		if(args.length < 2) throw new Exception("Need at least two variables, the first OWLOnotlogy file, the second Abox DB file.");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		File lubm = new File("./resources/LUBM_DLlite.owl");
		File lubm_data = new File("./resources/u1p0.sql");

//		File lubm = new File(args[0]);
//		File lubm_data = new File(args[1]);

//		File lubm_data = new File("../IJCAI17/Databases/u5p2e-1.sql");
//		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("./resources/pizza.owl"));			

		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(lubm);	
		
//		SemtooDatabase semtoodb = SemtooDatabase.getDatabase("/home/sharpen/neo4j/data/databases/u1p15e-4.db", true);
//		
//		OWLTransfer owltf = new OWLTransfer(ontology, semtoodb);
//		owltf.createTboxEntitiesFromOWL();
//		owltf.import_cvs("./CSVStore/u1p0_cls.csv", "./CSVStore/u1p0_ppt.csv");
//		owltf.loadTboxAxiomsToGraph();
		
		for(String i : testcases_small) {
			System.out.println("Loading Test case " + i);
			SemtooDatabase semtoodb = SemtooDatabase.getDatabase(neo4j_datapath + i + ".db", true);
			
			OWLTransfer owltf = new OWLTransfer(ontology, semtoodb);
			owltf.createTboxEntitiesFromOWL();
			owltf.import_cvs(csv_store + i + "_cls.csv", csv_store + i + "_ppt.csv");
			owltf.loadTboxAxiomsToGraph();
			
			semtoodb.shutdown();
		}
		
//		GraphManager gm = new GraphManager();
//		gm.clearGraph();
//		gm.loadOntologyToGraph(ontology);
//		
//		DBTransfer_server dbtf_s = new DBTransfer_server();
//		dbtf_s.loadDBfiletoGraphDB("./CSVStore/u5p0_cls.csv", "./CSVStore/u5p0_ppt.csv");
//		
//		DBFileReader reader = new DBFileReader();
//		reader.addPrefix(default_prefix);
//		reader.readDBFileToGraph(lubm_data);
	}

}
