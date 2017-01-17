package org.semanticweb.semtoo.Graph;


import org.neo4j.driver.v1.Transaction;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.semtoo.neo4j.Neo4jManager;

public class GraphOntologyVistors {
	private Neo4jManager m = null;
	private Transaction tc = null;
	
	public GraphOntologyVistors(Neo4jManager manager, Transaction transcation) {
		m = manager;
		tc = transcation;
	}
	
	public OWLAxiomVisitor getAxiomVisitor() {
		return axiomVisitor;
	}	
	
	public OWLEntityVisitor getEntityVisitor() {
		return entityVisitor;
	}
	
	private OWLAxiomVisitor axiomVisitor = new OWLAxiomVisitor() {
		public void visit(OWLSubClassOfAxiom axiom) {
			OWLClassExpression subExp = axiom.getSubClass();
			OWLClassExpression superExp = axiom.getSuperClass();

			GraphNode a = subExp.accept(classExpVisitor2);
			GraphNode b = superExp.accept(classExpVisitor2);
			
			m.createSubOfRelation(a, b, tc);
		}
		
		public void visit(OWLClassAssertionAxiom axiom) {
			
		};
	};
	
	private OWLClassExpressionVisitorEx<GraphNode> classExpVisitor2 = new OWLClassExpressionVisitorEx<GraphNode>() {
		public GraphNode visit(OWLClass ce) {
			GraphNode n = new GraphNode(ce);
			
			m.createNode(n, tc);
			return n;
		}
		
		public GraphNode visit(OWLObjectSomeValuesFrom ce) {
			return null;
		}
	};
	
	private OWLClassExpressionVisitor classExpVisitor = new OWLClassExpressionVisitor() {
		
	};
	
	private OWLEntityVisitor entityVisitor = new OWLEntityVisitor() {
		
	};

}
