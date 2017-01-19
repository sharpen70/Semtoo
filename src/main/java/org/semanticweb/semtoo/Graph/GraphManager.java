package org.semanticweb.semtoo.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPairwiseVoidVisitor;
import org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.semtoo.neo4j.Neo4jManager;
import org.semanticweb.semtoo.neo4j.Neo4jUpdate;

public class GraphManager {
	
	public GraphManager() {
		
	}
	
	private boolean isDLLiteClassExp(OWLClassExpression exp) {
		if(exp instanceof OWLClass) return true;
		if(exp instanceof OWLObjectSomeValuesFrom) return true;
		
		return false;
	}
	
	public Predicate<OWLEntity> dl_lite_classEntityOnly = new Predicate<OWLEntity>() {
		@Override
		public boolean test(OWLEntity t) {
			return t.isOWLClass() || t.isOWLObjectProperty();
		}
	};
	
	public Predicate<OWLAxiom> dl_lite_axiomOnly = new Predicate<OWLAxiom>() {
		public boolean test(OWLAxiom t) {
			if(!t.isLogicalAxiom()) return false;
			
			if(t instanceof OWLClassAssertionAxiom) return true;
			if(t instanceof OWLDisjointClassesAxiom) {
				for(OWLClassExpression exp : ((OWLDisjointClassesAxiom) t).classExpressions().collect(Collectors.toList())) {
					if(!isDLLiteClassExp(exp)) return false;
				}
				return true;
			}
			if(t instanceof OWLSubClassOfAxiom) {
				OWLClassExpression exp1 = ((OWLSubClassOfAxiom)t).getSubClass();
				OWLClassExpression exp2 = ((OWLSubClassOfAxiom)t).getSuperClass();
				
				return isDLLiteClassExp(exp1) && isDLLiteClassExp(exp2);
			}
			if(t instanceof OWLObjectPropertyAssertionAxiom) return true;
			return false;
		};
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
		if(exp instanceof OWLObjectInverseOf) return GraphNode.property_nodeType.invPRctClass + "_" + ((OWLObjectInverseOf)exp).getNamedProperty().getIRI().toString();
		else return GraphNode.property_nodeType.PRctClass + "_" + ((OWLObjectProperty)exp).getIRI().toString();	
	}
	
	public void ontologyToGraph(OWLOntology o) {
		Neo4jManager neo4jmanager = new Neo4jManager();
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
					GraphNode node = new GraphNode(property, GraphNode.property_nodeType.PRctClass);
					GraphNode inode = new GraphNode(property, GraphNode.property_nodeType.invPRctClass);
					
					Neo4jUpdate.createNode(node, tc);
					Neo4jUpdate.createNode(inode, tc);
				};
				public void visit(OWLNamedIndividual idv) {
					Neo4jUpdate.createNode(new GraphNode(idv), tc);
				}
			};
			o.signature().filter(dl_lite_classEntityOnly).forEach(e -> e.accept(entityVisitor));			
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
					
					//Neo4jUpdate.createNode(new GraphNode(idv), tc);
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
			o.axioms().filter(dl_lite_axiomOnly).forEach(a -> a.accept(visitor));
		}
		end = System.currentTimeMillis();
		System.out.println("Create relations Transaction closed in " + (end - start) + " ms");
		
		session.close();
		neo4jmanager.close();
	}
}

