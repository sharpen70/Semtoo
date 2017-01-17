package org.semanticweb.semtoo.model;

import java.util.ArrayList;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;

public class Clause {
	private Atom head;
	private ArrayList<Atom> body = new ArrayList<>();
	
	private OWLAxiomVisitor toClause = new OWLAxiomVisitor() {
		public void visit(OWLObjectPropertyAssertionAxiom axiom) {
			
		};
		public void visit(OWLClassAssertionAxiom axiom) {
			
		};
		public void visit(OWLSubClassOfAxiom axiom) {
			
		};
		public void visit(OWLSubDataPropertyOfAxiom axiom) {
			
		};
	};
	
	public Clause(OWLAxiom axiom) {
		axiom.accept(toClause);
	}	
}
