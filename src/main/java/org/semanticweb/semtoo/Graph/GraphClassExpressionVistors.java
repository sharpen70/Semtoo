package org.semanticweb.semtoo.Graph;

import java.util.HashMap;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;

public class GraphClassExpressionVistors {
	private HashMap<ClassExpressionType, OWLClassExpressionVisitor> vistors = new HashMap<>();
	
	public OWLClassExpressionVisitor getVistor(ClassExpressionType t) {
		return vistors.get(t);
	}
	
	private OWLClassExpressionVisitor atomicClassVistors = new OWLClassExpressionVisitor() {
	};
	
	private OWLClassExpressionVisitor eRestrictionClassVistor = new OWLClassExpressionVisitor() {
	}; 
	
	private OWLClassExpressionVisitor subClassVistors = new OWLClassExpressionVisitor() {
		public void visit(OWLClass ce) {
			
		};
	};
	
	private OWLClassExpressionVisitor superClassVistors = new OWLClassExpressionVisitor() {
	};
}
