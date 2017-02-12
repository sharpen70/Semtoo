package org.semanticweb.semtoo.preprocessing;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPairwiseVoidVisitor;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseBuilder;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.node_labels;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.property_key;
import org.semanticweb.semtoo.util.DLliteFilter;

public class OWLTransfer {
	private StDatabaseBuilder db;
	private OWLOntology o;
	
	public OWLTransfer(OWLOntology _o, StDatabaseBuilder _db) {
		o = _o;
		db = _db;
	}
	
	private class aboxAxiomVistor implements OWLAxiomVisitor {
		
		public void visit(OWLClassAssertionAxiom axiom) {
			OWLClassExpression exp = axiom.getClassExpression();
			OWLIndividual idv = axiom.getIndividual();
			
			db.createIsRelation(idv.toStringID(), exp.accept(getExpIRI));
		};
		public void visit(OWLObjectPropertyAssertionAxiom axiom) {
			OWLIndividual subject = axiom.getSubject();
			OWLIndividual object = axiom.getObject();
			OWLObjectPropertyExpression property = axiom.getProperty();
			
			String property_iri = getPropertyIRI(property);
			
			db.createDualRelation(subject.toStringID(), object.toStringID(), property_iri, false);
		}		
	}
	
	private class tboxAxiomVistor implements OWLAxiomVisitor {
		
		public void visit(OWLSubClassOfAxiom axiom) {
			String sub_iri = axiom.getSubClass().accept(getExpIRI);
			String super_iri = axiom.getSuperClass().accept(getExpIRI);
			db.createSubOfRelation(sub_iri, super_iri);
		};
		public void visit(OWLEquivalentClassesAxiom axiom) {
			OWLPairwiseVoidVisitor<OWLClassExpression> _vistor = new OWLPairwiseVoidVisitor<OWLClassExpression>() {
				@Override
				public void visit(OWLClassExpression a, OWLClassExpression b) {
					String a_iri = a.accept(getExpIRI);
					String b_iri = b.accept(getExpIRI);
					db.createSubOfRelation(a_iri, b_iri);
					db.createSubOfRelation(b_iri, a_iri);
				}
			};
			axiom.forEach(_vistor);
		};
		public void visit(OWLDisjointClassesAxiom axiom) {
			OWLPairwiseVoidVisitor<OWLClassExpression> _visitor = new OWLPairwiseVoidVisitor<OWLClassExpression>() {
				@Override
				public void visit(OWLClassExpression a, OWLClassExpression b) {
					String a_iri = a.accept(getExpIRI);
					String b_iri = b.accept(getExpIRI);
					db.createNegationNode(b_iri);
					db.createNegationRelation(a_iri, b_iri);
				}
			};
			axiom.forEach(_visitor);
		};
		public void visit(OWLSubObjectPropertyOfAxiom axiom) {
			OWLObjectPropertyExpression subp = axiom.getSubProperty();
			OWLObjectPropertyExpression superp = axiom.getSuperProperty();
			
			String subp_iri = getPropertyIRI(subp);
			String superp_iri = getPropertyIRI(superp);
			String i_subp_iri = getPropertyIRI(subp.getInverseProperty());
			String i_superp_iri = getPropertyIRI(superp.getInverseProperty());
			
			db.createSubOfRelation(subp_iri, superp_iri);
			db.createSubOfRelation(i_subp_iri, i_superp_iri);
			
			String rt_subp_iri = getPRClassIRI(subp);
			String rt_superp_iri = getPRClassIRI(superp);
			String rt_i_subp_iri = getPRClassIRI(subp.getInverseProperty());
			String rt_i_superp_iri = getPRClassIRI(superp.getInverseProperty());
			
			db.createSubOfRelation(rt_subp_iri, rt_superp_iri);
			db.createSubOfRelation(rt_i_subp_iri, rt_i_superp_iri);
		};
		public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
			OWLPairwiseVoidVisitor<OWLObjectPropertyExpression> _vistor = new OWLPairwiseVoidVisitor<OWLObjectPropertyExpression>() {
				@Override
				public void visit(OWLObjectPropertyExpression a, OWLObjectPropertyExpression b) {
					String a_iri = getPropertyIRI(a);
					String ia_iri = getPropertyIRI(a.getInverseProperty());
					String b_iri = getPropertyIRI(b);
					String ib_iri = getPropertyIRI(b.getInverseProperty());
					
					db.createNegationNode(b_iri);
					db.createNegationNode(ib_iri);
					db.createNegationRelation(a_iri, b_iri);
					db.createNegationRelation(ia_iri, ib_iri);
					
					String ra_iri = getPRClassIRI(a);
					String ria_iri = getPRClassIRI(a.getInverseProperty());
					String rb_iri = getPRClassIRI(b);
					String rib_iri = getPRClassIRI(b.getInverseProperty());
					
					db.createNegationNode(rb_iri);
					db.createNegationNode(rib_iri);
					db.createNegationRelation(ra_iri, rb_iri);
					db.createNegationRelation(ria_iri, rib_iri);
				}
			};
			axiom.forEach(_vistor);
		};
	}
	
	//Transfer entities of Class and Property to nodes in the graph
	//This process should be done before CVS import
	public void createTboxEntitiesFromOWL() {
		GraphDatabaseService embeddedDB = db.getEmbeddedDB();
		
		System.out.println("Creating nodes for Ontology Tbox Entities ...");
		long start = System.currentTimeMillis();
		try(Transaction tx = embeddedDB.beginTx()) {
			OWLEntityVisitor entityVisitor = new OWLEntityVisitor() {
				public void visit(OWLClass ce) {
					db.createAtomicClassNode(ce.toStringID());
				};
				public void visit(OWLObjectProperty property) {
					db.createPropertyNode(property.toStringID(), true);
					db.createPropertyNode(property.toStringID(), false);
					db.createPRClassNode(property.toStringID(), true);
					db.createPRClassNode(property.toStringID(), false);
				};
			};
		
			o.signature().forEach(e -> e.accept(entityVisitor));			
			tx.success();
		}
		long end = System.currentTimeMillis();
		System.out.println("Done with " + (end - start) + " ms");
	}
	
	//If CVS import is demanded, this process should be done after CVS import, 
	//Otherwise it will mess up index
	public void loadTboxAxiomsToGraph() {
		GraphDatabaseService embeddedDB = db.getEmbeddedDB();
		
		System.out.println("Creating relations with Tbox axioms ...");
		long start = System.currentTimeMillis();
		try(Transaction tx = embeddedDB.beginTx()) {
			o.axioms().filter(new DLliteFilter("R")).forEach(a -> a.accept(new tboxAxiomVistor()));
			tx.success();
		}
		long end = System.currentTimeMillis();
		System.out.println("Done with " + (end - start) + " ms");
	}
	
	//If CVS import is demanded, this process should be done after CVS import, 
	//Otherwise it will mess up index
	public void loadAboxAxiomsToGraph() {
		GraphDatabaseService embeddedDB = db.getEmbeddedDB();
		
		System.out.println("Creating relations with Abox axioms ...");
		long start = System.currentTimeMillis();
		try(Transaction tx = embeddedDB.beginTx()) {
			o.axioms().filter(new DLliteFilter("R")).forEach(a -> a.accept(new aboxAxiomVistor()));
			tx.success();
		}
		long end = System.currentTimeMillis();
		System.out.println("Done with " + (end - start) + " ms");
	}
	
	//Be careful with LOAD CSV in embedded API, it may not use regular index when 
	//combining other operations of embedded API
	//If you have to combine it with other operations, make sure it's called in the initial stage
	public void import_cvs(String clsAssertioncsv, String pptAssertincsv) throws IOException {
		clsAssertioncsv = new File(clsAssertioncsv).getCanonicalPath();
		pptAssertincsv = new File(pptAssertincsv).getCanonicalPath();
		
		GraphDatabaseService embeddedDB = db.getEmbeddedDB();
		
		String cypher1 = "USING PERIODIC COMMIT 2000"
				+ " LOAD CSV WITH HEADERS FROM \"file:///" + clsAssertioncsv + "\" AS clsa"
				+ " MATCH (cls:" + node_labels.TBOXENTITY + " {" + property_key.IRI_LOWER + ":clsa.class})"
				+ " MERGE (idv:" + node_labels.INDIVIDUAL + " {" + property_key.NODE_IRI + ":clsa.idv})"
				+ " CREATE (idv)-[:is]->(cls)"; 
		
		String cypher2 = "USING PERIODIC COMMIT 2000"
				+ " LOAD CSV WITH HEADERS FROM \"file:///" + pptAssertincsv + "\" AS ppta"
				+ " MERGE (a:" + node_labels.INDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.subject})"
				+ " MERGE (ab:" + node_labels.DUALINDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.subject + ppta.object})"
				+ " ON CREATE SET ab." + property_key.SUBJECT_IRI + "=ppta.subject, ab." + property_key.OBJECT_IRI + "=ppta.object";
		
		String cypher2_1 = "USING PERIODIC COMMIT 2000"
				+ " LOAD CSV WITH HEADERS FROM \"file:///" + pptAssertincsv + "\" AS ppta"
				+ " MERGE (b:" + node_labels.INDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.object})"
				+ " MERGE (ba:" + node_labels.DUALINDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.object + ppta.subject})"
				+ " ON CREATE SET ba." + property_key.SUBJECT_IRI + "=ppta.object, ba." + property_key.OBJECT_IRI + "=ppta.subject";
		
		String cypher3 = "USING PERIODIC COMMIT 2000"
				+ " LOAD CSV WITH HEADERS FROM \"file:///" + pptAssertincsv + "\" AS ppta"
				+ " MATCH (p:" + node_labels.TBOXENTITY + " {" + property_key.IRI_LOWER + ": ppta.property})"
				+ " MATCH (ip:" + node_labels.TBOXENTITY + " {" + property_key.IRI_LOWER + ":\"inv_\" + ppta.property})"
				+ " MATCH (ab:" + node_labels.DUALINDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.subject + ppta.object})"
				+ " MATCH (ba:" + node_labels.DUALINDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.object + ppta.subject})"
				+ " CREATE (ab)-[:is]->(p)"
				+ " CREATE (ba)-[:is]->(ip)";
		
		String cypher3_1 = "USING PERIODIC COMMIT 2000"
				+ " LOAD CSV WITH HEADERS FROM \"file:///" + pptAssertincsv + "\" AS ppta"
				+ " MATCH (rp:" + node_labels.TBOXENTITY + " {" + property_key.IRI_LOWER + ":\"prt_\" + ppta.property})"
				+ " MATCH (a:" + node_labels.INDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.subject})"
				+ " MERGE (a)-[:is]->(rp)";
		
		String cypher3_2 = "USING PERIODIC COMMIT 2000"
				+ " LOAD CSV WITH HEADERS FROM \"file:///" + pptAssertincsv + "\" AS ppta"
				+ " MATCH (rip:" + node_labels.TBOXENTITY + " {" + property_key.IRI_LOWER + ":\"prt_inv_\" + ppta.property})"
				+ " MATCH (b:" + node_labels.INDIVIDUAL + " {" + property_key.NODE_IRI + ":ppta.object})"
				+ " MERGE (b)-[:is]->(rip)";
		
		long start, end;
		System.out.println("Begin inserting class assertion ...");
		start = System.currentTimeMillis();
		
		embeddedDB.execute(cypher1);
		
		end = System.currentTimeMillis();
		System.out.println("Done class assertion with " + (end - start) + " ms");
		
		System.out.println("Begin inserting property assertion with nodes ...");
		start = System.currentTimeMillis();
		
		embeddedDB.execute(cypher2);
		
		end = System.currentTimeMillis();
		System.out.println("Done creating nodes with " + (end - start) + " ms");
		
		
		System.out.println("Begin inserting property assertion with relation ...");
		start = System.currentTimeMillis();
		
		embeddedDB.execute(cypher2_1);
		
		end = System.currentTimeMillis();
		System.out.println("Done creating relations with " + (end - start) + " ms");
		
		System.out.println("Begin inserting property assertion with relation ...");
		start = System.currentTimeMillis();
		
		embeddedDB.execute(cypher3);
		
		end = System.currentTimeMillis();
		System.out.println("Done creating relations with " + (end - start) + " ms");
		
		System.out.println("Begin inserting property assertion with relation ...");
		start = System.currentTimeMillis();
		
		embeddedDB.execute(cypher3_1);
		
		end = System.currentTimeMillis();
		System.out.println("Done creating relations with " + (end - start) + " ms");
		
		System.out.println("Begin inserting property assertion with relation ...");
		start = System.currentTimeMillis();
		
		embeddedDB.execute(cypher3_2);
		
		end = System.currentTimeMillis();
		System.out.println("Done creating relations with " + (end - start) + " ms");
	}
	
	public void loadOntologyToGraph() {
		GraphDatabaseService embeddedDB = db.getEmbeddedDB();
		
		long start = System.currentTimeMillis();
		//Transfer entities of Individual, Class and Property to nodes in the graph 
		System.out.println("Creating nodes for Ontology Entities ...");
		
		try(Transaction tx = embeddedDB.beginTx()) {
			OWLEntityVisitor entityVisitor = new OWLEntityVisitor() {
				public void visit(OWLClass ce) {
					db.createAtomicClassNode(ce.toStringID());
				};
				public void visit(OWLObjectProperty property) {
					db.createPropertyNode(property.toStringID(), true);
					db.createPropertyNode(property.toStringID(), false);
					db.createPRClassNode(property.toStringID(), true);
					db.createPRClassNode(property.toStringID(), false);
				};
				public void visit(OWLNamedIndividual idv) {
					db.createIndividualNode(idv.toStringID());
				}
			};
		
			o.signature().forEach(e -> e.accept(entityVisitor));			
			tx.success();
		}
		/////////////////////////////////////////////////////////////////////////
		// This is a special modification of nodes for the database dump file, //
		// turning every node name into lowercase                              //
		/////////////////////////////////////////////////////////////////////////
		
		
//		try(Transaction tx = embeddedDB.beginTx()) {
//			String s = "MATCH (n:TBEN) SET n.iri_lower=lower(n.iri)";
//			embeddedDB.execute(s);
//			ResourceIterator<Node> nodes = embeddedDB.findNodes(node_labels.TBOXENTITY);
//			while(nodes.hasNext()) {
//				Node node = nodes.next();
//				node.setProperty(property_key.IRI_LOWER, node.getProperty(property_key.NODE_IRI).toString().toLowerCase());
//			}
//			nodes.close();
//			tx.success();
//		}
		
//		long end = System.currentTimeMillis();
//		System.out.println("Done with " + (end - start) + " ms");
//		
//		System.out.println("Creating relations with Tbox axioms ...");
//		start = System.currentTimeMillis();
//		try(Transaction tx = embeddedDB.beginTx()) {
//			o.axioms().filter(new DLliteFilter("R")).forEach(a -> a.accept(new tboxAxiomVistor()));
//			tx.success();
//		}
//		end = System.currentTimeMillis();
//		System.out.println("Done with " + (end - start) + " ms");
		
//		System.out.println("Inserting nodes with Abox axioms ...");
//		start = System.currentTimeMillis();
//		try(Transaction tc = session.beginTransaction()) {	
//			o.axioms().filter(new DLliteFilter("R")).forEach(a -> a.accept(new aboxAxiomVistor(tc)));
//		}
//		end = System.currentTimeMillis();
//		System.out.println("Done with " + (end - start) + " ms");
	}
	
	private static String getPropertyIRI(OWLObjectPropertyExpression exp) {
		String piri = exp.getNamedProperty().toStringID();
		if(exp instanceof OWLObjectInverseOf) return StDatabaseBuilder.getInverseStringiri(piri);
		else return piri;
	}
	
	private static String getPRClassIRI(OWLObjectPropertyExpression exp) {
		String piri = exp.getNamedProperty().toStringID();
		if(exp instanceof OWLObjectInverseOf) return StDatabaseBuilder.getPRStringiri(StDatabaseBuilder.getInverseStringiri(piri));
		else return StDatabaseBuilder.getPRStringiri(piri);	
	}
	
	private OWLClassExpressionVisitorEx<String> getExpIRI = new OWLClassExpressionVisitorEx<String>() {
		public String visit(OWLClass ce) {
			return ce.toStringID();
		};
		
		public String visit(OWLObjectSomeValuesFrom svf) {
			OWLObjectPropertyExpression exp = svf.getProperty();
			
			if(exp instanceof OWLObjectProperty) return StDatabaseBuilder.getPRStringiri(exp.getNamedProperty().toStringID());
			else return StDatabaseBuilder.getPRStringiri(StDatabaseBuilder.getInverseStringiri(exp.getNamedProperty().toStringID()));
		}
	};
}
