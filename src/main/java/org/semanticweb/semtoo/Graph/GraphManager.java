package org.semanticweb.semtoo.Graph;

import java.util.Map;
import java.util.function.Predicate;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLPairwiseVoidVisitor;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.semtoo.Graph.GraphNode.NODE_TYPE;
import org.semanticweb.semtoo.neo4j.Neo4jManager;
import org.semanticweb.semtoo.neo4j.Neo4jUpdate;
import org.semanticweb.semtoo.tools.DLlite;

public class GraphManager {
	private Neo4jManager neo4jmanager = null;
	
	public GraphManager(Neo4jManager m) {
		neo4jmanager = m;
	}	
	
	public void loadOntologyToGraph(OWLOntology o) {
		Session session = neo4jmanager.getSession();
		
		long start = System.currentTimeMillis();
		
		//Set the Graph to empty Graph 
		try(Transaction tc = session.beginTransaction()) {
			Neo4jUpdate.clearGraph(tc);
		}
		
		//Transfer entities of Individual, Class and Property to nodes in the graph 
		System.out.println("Turning ontology class into nodes ...");
		
		try(Transaction tc = session.beginTransaction()) {
			OWLEntityVisitor entityVisitor = new OWLEntityVisitor() {
				public void visit(OWLClass ce) {
					Neo4jUpdate.createNode(new GraphNode(ce), tc);
				};
				public void visit(OWLObjectProperty property) {
					GraphNode node = new GraphNode(property, NODE_TYPE.PropertyRestrictionClass);
					GraphNode inode = new GraphNode(property, NODE_TYPE.InverseOfPropertyRestrictionClass);
					
					Neo4jUpdate.createNode(node, tc);
					Neo4jUpdate.createNode(inode, tc);
				};
				public void visit(OWLNamedIndividual idv) {
					Neo4jUpdate.createNode(new GraphNode(idv), tc);
				}
			};
			o.signature().filter(dl_lite_entityOnly).forEach(e -> e.accept(entityVisitor));			
		}
		long end = System.currentTimeMillis();
		System.out.println("Create nodes Transaction closed in " + (end - start) + " ms");
		
		//Build Index on for Nodes on IRI property
		System.out.println("Build Index for class iri ...");
		try(Transaction tc = session.beginTransaction()) {
			Neo4jUpdate.buildIndex("OWLEntity", "iri", tc); 
		}		
		
		//Build relations between Nodes according Tbox Axioms
		System.out.println("Creating relations between nodes according to axioms ...");
		start = System.currentTimeMillis();
		try(Transaction tc = session.beginTransaction()) {
			OWLAxiomVisitor visitor = new OWLAxiomVisitor() {
				public void visit(OWLClassAssertionAxiom axiom) {
					OWLClassExpression exp = axiom.getClassExpression();
					OWLIndividual idv = axiom.getIndividual();
					
					String iri = exp.accept(getExpIRI);
					
					Neo4jUpdate.matchAndcreateRelation(idv.toStringID(), iri, "is", tc);;
				};
				public void visit(OWLObjectPropertyAssertionAxiom axiom) {
					String subject_iri = axiom.getSubject().toStringID();
					String object_iri = axiom.getObject().toStringID();
					
					String p_iri = getPRctClassIRI(axiom.getProperty());
					String ip_iri = getPRctClassIRI(axiom.getProperty().getInverseProperty());
					
					Neo4jUpdate.matchAndcreateRelation(subject_iri, p_iri, "is", tc);
					Neo4jUpdate.matchAndcreateRelation(object_iri, ip_iri, "is", tc);
				}
				public void visit(OWLDisjointClassesAxiom axiom) {
					OWLPairwiseVoidVisitor<OWLClassExpression> visitor = new OWLPairwiseVoidVisitor<OWLClassExpression>() {
						@Override
						public void visit(OWLClassExpression a, OWLClassExpression b) {
							String a_iri = a.accept(getExpIRI);
							GraphNode negationNode = GraphNode.getNode(b);
							negationNode.toNegation();
							Neo4jUpdate.mergeAndcreateRelation(a_iri, negationNode, "SubOf", tc);
						}
					};
					axiom.forEach(visitor);
				};
				public void visit(OWLSubClassOfAxiom axiom) {
					String sub_iri = axiom.getSubClass().accept(getExpIRI);
					String super_iri = axiom.getSuperClass().accept(getExpIRI);
					Neo4jUpdate.matchAndcreateRelation(sub_iri, super_iri, "SubOf", tc);
				};
			};
			
			DLlite dllitecore = new DLlite();
			o.axioms().filter(dllitecore.getAxiomFilter()).forEach(a -> a.accept(visitor));
		}
		end = System.currentTimeMillis();
		System.out.println("Create relations Transaction closed in " + (end - start) + " ms");
		
		session.close();
	}
	
	private OWLClassExpression fromMapToExpression(Map<String, Object> record, OWLDataFactory df) {
		String iri = record.get("iri").toString();
		String type = record.get("type").toString();
		
		OWLClassExpression exp = null;
		
		if(type == NODE_TYPE.AtomicClass.toString()) {
			exp = df.getOWLClass(IRI.create(iri));
		}
		else if(type == NODE_TYPE.PropertyRestrictionClass.toString()) {
			
		}
		else if(type == NODE_TYPE.InverseOfPropertyRestrictionClass.toString()) {
			
		}
		return exp;
	}
	
	public OWLOntology loadGraphToOntology(IRI ontologyIRI) throws OWLException {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology o = m.createOntology(ontologyIRI);
		OWLDataFactory df = m.getOWLDataFactory();
		
		try(Session session = neo4jmanager.getSession()) {
			try(Transaction tc = session.beginTransaction()) {
				String statement = "MATCH (a)-[:SubOf]->(b) RETURN a as subject, b as object";
				StatementResult result = tc.run(statement);
				
				while(result.hasNext()) {
					Record r = result.next();
					Map<String, Object> subject = r.get("subject").asMap();
					Map<String, Object> object = r.get("object").asMap();
					
					String subtype = subject.get("type").toString();
					if(subtype == "AtomicClass") {
						
					}
					String sub_iri = r.get("iri", "");
					String obj_iri = r.get("iri", "");
					
				}
			}
		}
		return null;
	}
	
	public void close() {
		neo4jmanager.close();
	}
	
	private Predicate<OWLEntity> dl_lite_entityOnly = new Predicate<OWLEntity>() {
		@Override
		public boolean test(OWLEntity t) {
			return t.isOWLClass() || t.isOWLObjectProperty() || t.isOWLNamedIndividual();
		}
	};
	
	private OWLClassExpressionVisitorEx<String> getExpIRI = new OWLClassExpressionVisitorEx<String>() {
		public String visit(OWLClass ce) {
			return ce.getIRI().toString();
		};
		
		public String visit(OWLObjectSomeValuesFrom svf) {
			OWLObjectPropertyExpression exp = svf.getProperty();
			
			return getPRctClassIRI(exp);				
		}
	};
	
	private String getPRctClassIRI(OWLObjectPropertyExpression exp) {
		if(exp instanceof OWLObjectInverseOf) return NODE_TYPE.InverseOfPropertyRestrictionClass + "_" + ((OWLObjectInverseOf)exp).getNamedProperty().getIRI().toString();
		else return NODE_TYPE.PropertyRestrictionClass + "_" + ((OWLObjectProperty)exp).getIRI().toString();	
	}

}

