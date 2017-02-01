package org.semanticweb.semtoo.embeddedneo4j;

import java.io.File;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.semanticweb.semtoo.embeddedneo4j.SemtooDatabaseMeta.node_labels;
import org.semanticweb.semtoo.embeddedneo4j.SemtooDatabaseMeta.property_key;

public class SemtooDatabase {
	private GraphDatabaseService graphdb;
	
	public SemtooDatabase(String pathtoDB) {
		graphdb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(pathtoDB));
		registerShutDownHook(graphdb);
	}
	
	public void initialize() {
		createNodeIndex();
	}
	
	public void createAtomicClassNode(String iri) {
		try(Transaction tx = graphdb.beginTx()) {
			Node node = graphdb.createNode(node_labels.TBOXENTITY, node_labels.CLASS);
			node.setProperty(property_key.NODE_IRI, iri);
			
			tx.success();
		}
	}
	
	public void createPropertyNode(String iri) {
		try(Transaction tx = graphdb.beginTx()) {
			Node node = graphdb.createNode(node_labels.TBOXENTITY, node_labels.PROPERTY);
			node.setProperty(property_key.NODE_IRI, iri);
			tx.success();
		}
	}
	
	public void createPRClassNode(String iri) {
		try(Transaction tx = graphdb.beginTx()) {
			
			Node node = graphdb.createNode(node_labels.TBOXENTITY, node_labels.PROPERTY_CLASS);
			node.setProperty(property_key.NODE_IRI, iri);
			node.setProperty(property_key.PROPERTY_IRI, iri);
			tx.success();
		}
	}
	
	private void setUpUniqueConstrants() {
		try(Transaction tx = graphdb.beginTx()) {
			graphdb.schema().constraintFor(node_labels.TBOXENTITY)
							.assertPropertyIsUnique(property_key.NODE_IRI)
							.create();
			
			graphdb.schema().constraintFor(node_labels.INDIVIDUAL)
							.assertPropertyIsUnique(property_key.NODE_IRI)
							.create();
			
			graphdb.schema().constraintFor(node_labels.DUALINDIVIDUAL)
							.assertPropertyIsUnique(property_key.NODE_IRI)
							.create();
			
			graphdb.schema().constraintFor(node_labels.NEGATION)
							.assertPropertyIsUnique(property_key.NODE_IRI)
							.create();
			
			tx.success();
		}
	}
	
	private void createNodeIndex() {
		try(Transaction tx = graphdb.beginTx()) {
			graphdb.schema().indexFor(node_labels.TBOXENTITY)
							.on(property_key.NODE_IRI)
							.create();
			
			graphdb.schema().indexFor(node_labels.INDIVIDUAL)
							.on(property_key.NODE_IRI)
							.create();
			
			graphdb.schema().indexFor(node_labels.DUALINDIVIDUAL)
							.on(property_key.NODE_IRI)
							.create();

			graphdb.schema().indexFor(node_labels.NEGATION)
							.on(property_key.NODE_IRI)
							.create();
			
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
