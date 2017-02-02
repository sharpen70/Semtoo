package org.semanticweb.semtoo.embeddedneo4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.schema.Schema;
import org.semanticweb.semtoo.embeddedneo4j.SemtooDatabaseMeta.RelType;
import org.semanticweb.semtoo.embeddedneo4j.SemtooDatabaseMeta.node_labels;
import org.semanticweb.semtoo.embeddedneo4j.SemtooDatabaseMeta.property_key;
import org.semanticweb.semtoo.util.Helper;

public class SemtooDatabase {
	private GraphDatabaseService graphdb;
	private String dbDir;
	
	public static final String NEG_PREFIX = "neg_";
	public static final String INV_PREFIX = "inv_";
	public static final String PRT_PREFIX = "prt_";
	
	private static Map<String, SemtooDatabase> databases = new HashMap<>();
	
	private SemtooDatabase(String pathtoDB) {
		dbDir = pathtoDB;
		graphdb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(pathtoDB));
		registerShutDownHook(graphdb);
	}
	
	public static synchronized SemtooDatabase getDatabase(String pathtoDB, boolean clean) {
		if(clean) {
			File _db = new File(pathtoDB);
			if(_db.exists()) {
				System.out.println("Clean the original database ...");
				Helper.recusiveDelete(_db);
				databases.remove(pathtoDB);
			}
		}
			
		SemtooDatabase db = databases.get(pathtoDB);
		if(db == null) {
			db = new SemtooDatabase(pathtoDB);
			db.initialize();
			databases.put(pathtoDB, db);
		}
		return db;
	}
	
	public void initialize() {
		createNodeIndex();
	}
	
	public GraphDatabaseService getEmbeddedDB() {
		return graphdb;
	}
	
	public static String getNegStringiri(String iri) {
		return NEG_PREFIX + iri;
	}
	
	public static String getPRStringiri(String iri) {
		return PRT_PREFIX + iri;
	}
	
	public static String getInverseStringiri(String iri) {
		return INV_PREFIX + iri; 
	}
	
	public static String getDualStringiri(String subject_iri, String object_iri) {
		return subject_iri + "_" + object_iri;
	}
	
	public void createNegationRelation(String p_iri, String np_iri) {
//		try(Transaction tx = graphdb.beginTx()) {
			Node node_from = graphdb.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, p_iri);
			Node node_to = graphdb.findNode(node_labels.NEGATION, property_key.NODE_IRI, getNegStringiri(np_iri));
			
			node_from.createRelationshipTo(node_to, RelType.SubOf);
			
//			tx.success();
//		}
	}
	
	public void createSubOfRelation(String from_iri, String to_iri) {
//		try(Transaction tx = graphdb.beginTx()) {
			Node node_from = graphdb.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, from_iri);
			Node node_to = graphdb.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, to_iri);
			
			node_from.createRelationshipTo(node_to, RelType.SubOf);
			
//			tx.success();
//		}
	}
	
	public void createIsRelation(String idv_iri, String class_iri) {
//		try(Transaction tx = graphdb.beginTx()) {
			Node idv = graphdb.findNode(node_labels.INDIVIDUAL, property_key.NODE_IRI, idv_iri);
			Node cls = graphdb.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, class_iri);
			idv.createRelationshipTo(cls, RelType.is);
			
//			tx.success();
//		}
	}
	
	public void createDualRelation(String idv_subject, String idv_object, String property_iri, boolean forDB) {
//		try(Transaction tx = graphdb.beginTx()) {
			String dual_iri = getDualStringiri(idv_subject, idv_object);
			String inv_dual_iri = getDualStringiri(idv_object, idv_subject);
			
			Node dual = graphdb.findNode(node_labels.DUALINDIVIDUAL, property_key.NODE_IRI, dual_iri);
			Node inv_dual = graphdb.findNode(node_labels.DUALINDIVIDUAL, property_key.NODE_IRI, inv_dual_iri);
			Node subject = graphdb.findNode(node_labels.INDIVIDUAL, property_key.NODE_IRI, idv_subject);
			Node object = graphdb.findNode(node_labels.INDIVIDUAL, property_key.NODE_IRI, idv_object);
			
			if(dual == null) {
				dual = graphdb.createNode(node_labels.DUALINDIVIDUAL);
				dual.setProperty(property_key.NODE_IRI, dual_iri);
				dual.setProperty(property_key.SUBJECT_IRI, idv_subject);
				dual.setProperty(property_key.OBJECT_IRI, idv_object);
			}
			if(inv_dual == null) {
				dual = graphdb.createNode(node_labels.DUALINDIVIDUAL);
				dual.setProperty(property_key.NODE_IRI, inv_dual_iri);
				dual.setProperty(property_key.SUBJECT_IRI, idv_object);
				dual.setProperty(property_key.OBJECT_IRI, idv_subject);
			}
			
			Node ppt, inv_ppt, pptcls, inv_pptcls;
			
			if(forDB) {
				ppt = graphdb.findNode(node_labels.TBOXENTITY, property_key.IRI_LOWER, property_iri);
				inv_ppt = graphdb.findNode(node_labels.TBOXENTITY, property_key.IRI_LOWER, getInverseStringiri(property_iri));
				pptcls = graphdb.findNode(node_labels.TBOXENTITY, property_key.IRI_LOWER, getPRStringiri(property_iri));
				inv_pptcls = graphdb.findNode(node_labels.TBOXENTITY, property_key.IRI_LOWER, getPRStringiri(getInverseStringiri(property_iri)));
			}
			else {
				ppt = graphdb.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, property_iri);
				inv_ppt = graphdb.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, getInverseStringiri(property_iri));
				pptcls = graphdb.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, getPRStringiri(property_iri));
				inv_pptcls = graphdb.findNode(node_labels.TBOXENTITY, property_key.NODE_IRI, getPRStringiri(getInverseStringiri(property_iri)));
			}
			
			dual.createRelationshipTo(ppt, RelType.is);
			inv_dual.createRelationshipTo(inv_ppt, RelType.is);
			subject.createRelationshipTo(pptcls, RelType.is);
			object.createRelationshipTo(inv_pptcls, RelType.is);
			
//			tx.success();
//		}
	}
	
	public void createIndividualNode(String iri) {
//		try(Transaction tx = graphdb.beginTx()) {
			Node node = graphdb.createNode(node_labels.INDIVIDUAL);
			node.setProperty(property_key.NODE_IRI, iri);
			
//			tx.success();
//		}
	}
	
	public void createAtomicClassNode(String iri) {
//		try(Transaction tx = graphdb.beginTx()) {
			Node node = graphdb.createNode(node_labels.TBOXENTITY, node_labels.CLASS);
			node.setProperty(property_key.NODE_IRI, iri);
			
//			tx.success();
//		}
	}
	
	public void createNegationNode(String iri) {
//		try(Transaction tx = graphdb.beginTx()) {
			String niri = getNegStringiri(iri);
			Node node = graphdb.findNode(node_labels.NEGATION, property_key.NODE_IRI, niri);
			if(node == null) {
				node = graphdb.createNode(node_labels.NEGATION);
				node.setProperty(property_key.NODE_IRI, niri);
				node.setProperty(property_key.POSITIVE_NODE_IRI, iri);
			}
			
//			tx.success();
//		}
	}
	
	public void createPropertyNode(String iri, boolean inverse) {
//		try(Transaction tx = graphdb.beginTx()) {
			Node node = graphdb.createNode(node_labels.TBOXENTITY, node_labels.PROPERTY);
			if(inverse) {
				node.setProperty(property_key.NODE_IRI, getInverseStringiri(iri));
				node.setProperty(property_key.PROPERTY_IRI, iri);
			}
			else node.setProperty(property_key.NODE_IRI, iri);
//			tx.success();
//		}
	}
	
	public void createPRClassNode(String iri, boolean inverse) {
//		try(Transaction tx = graphdb.beginTx()) {
			Node node = graphdb.createNode(node_labels.TBOXENTITY, node_labels.PROPERTY_CLASS);
			
			if(inverse) iri = getInverseStringiri(iri);
			
			node.setProperty(property_key.NODE_IRI, getPRStringiri(iri));
			node.setProperty(property_key.PROPERTY_IRI, iri);
			
//			tx.success();
//		}
	}
	
	private void setUpUniqueConstrants() {
		try(Transaction tx = graphdb.beginTx()) {
			Schema schema = graphdb.schema();
			
			schema.constraintFor(node_labels.TBOXENTITY)
							.assertPropertyIsUnique(property_key.NODE_IRI)
							.create();
			
			schema.constraintFor(node_labels.INDIVIDUAL)
							.assertPropertyIsUnique(property_key.NODE_IRI)
							.create();
			
			schema.constraintFor(node_labels.DUALINDIVIDUAL)
							.assertPropertyIsUnique(property_key.NODE_IRI)
							.create();
			
			schema.constraintFor(node_labels.NEGATION)
							.assertPropertyIsUnique(property_key.NODE_IRI)
							.create();
			
			tx.success();
		}
	}
	
	private void createNodeIndex() {
		try(Transaction tx = graphdb.beginTx()) {
			Schema schema = graphdb.schema();
			
			schema.indexFor(node_labels.TBOXENTITY)
							.on(property_key.NODE_IRI)
							.create();
			
			schema.indexFor(node_labels.INDIVIDUAL)
							.on(property_key.NODE_IRI)
							.create();
			
			schema.indexFor(node_labels.DUALINDIVIDUAL)
							.on(property_key.NODE_IRI)
							.create();

			schema.indexFor(node_labels.NEGATION)
							.on(property_key.NODE_IRI)
							.create();
			
			//Special case for DB dump file
			schema.indexFor(node_labels.TBOXENTITY)
							.on(property_key.IRI_LOWER)
							.create();
			
			tx.success();
		}
		
		try(Transaction tx = graphdb.beginTx()) {
			graphdb.schema().awaitIndexesOnline(10, TimeUnit.SECONDS);
			tx.success();
		}
	}
	
	public void clearDatabase() {
//		Helper.recusiveDelete(new File(dbDir));
		try(Transaction tx = graphdb.beginTx()) {
			graphdb.execute("MATCH (n) DETACH DELETE n");
			tx.success();
		}
	}
	
	private static void registerShutDownHook(GraphDatabaseService graphdb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphdb.shutdown();
			}
		});
	}
}
