package org.semanticweb.semtoo.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.semtoo.neo4j.Neo4jManager;
import org.semanticweb.semtoo.neo4j.Neo4jUpdate;

public class GraphManager {
	
	public GraphManager() {
		
	}
	
	public static Predicate<OWLEntity> dl_lite_classEntityOnly = new Predicate<OWLEntity>() {
		@Override
		public boolean test(OWLEntity t) {
			return t.isOWLClass() || t.isOWLObjectProperty();
		}
	};
	
	public void createRelations(OWLOntology o, Session session) {
		try(Transaction tr = session.beginTransaction()) {
			
		}
	}
	
	private OWLClassExpressionVisitorEx<String> getExpIRI = new OWLClassExpressionVisitorEx<String>() {
		public String visit(OWLClass ce) {
			return ce.getIRI().toString();
		};
		
		public String visit(OWLObjectSomeValuesFrom svf) {
			OWLObjectPropertyExpression exp = svf.getProperty();

			if(exp instanceof OWLObjectInverseOf) return GraphNode.property_nodeType.invPRctClass + "_" + ((OWLObjectInverseOf)exp).getNamedProperty().getIRI().toString();
			else return GraphNode.property_nodeType.PRctClass + "_" + ((OWLObjectProperty)exp).getIRI().toString();						
		}
	};
	
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
					System.out.println(ce);
					GraphNode node = new GraphNode(ce);
					
					Neo4jUpdate.createNode(node, tc);
				};
				public void visit(OWLObjectProperty property) {
					GraphNode node = new GraphNode(property, GraphNode.property_nodeType.PRctClass);
					GraphNode inode = new GraphNode(property, GraphNode.property_nodeType.invPRctClass);
					
					Neo4jUpdate.createNode(node, tc);
					Neo4jUpdate.createNode(inode, tc);
				};
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
		try(Transaction tc = session.beginTransaction()) {
			OWLAxiomVisitor visitor = new OWLAxiomVisitor() {
				public void visit(OWLClassAssertionAxiom axiom) {
					OWLClassExpression exp = axiom.getClassExpression();
					OWLIndividual idv = axiom.getIndividual();
					
					String iri = exp.accept(getExpIRI);
					
					Neo4jUpdate.createNode(new GraphNode(idv), tc);
					Neo4jUpdate.createSubOfRelation(idv.toStringID(), iri, tc);;
				};
				public void visit(OWLDisjointClassesAxiom axiom) {
					
				};
				public void visit(OWLSubClassOfAxiom axiom) {
					
				};
				public void visit(OWLObjectPropertyAssertionAxiom axiom) {
					
				}
			};
			o.axioms().filter(a -> a.isLogicalAxiom()).forEach(a -> a.accept(visitor));
		}
		
		session.close();
		neo4jmanager.close();
	}
}

