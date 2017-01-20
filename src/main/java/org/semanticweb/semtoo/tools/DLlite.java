package org.semanticweb.semtoo.tools;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

//Currently this class only deal with DL-lite core

public class DLlite {
	public static enum DLlite_Constructs {
		FUNCTIONAL("F"),
		ROLE("R"),
		INTERSECTION("I");
		
		private String value;
		
		private DLlite_Constructs(String v) {
			value = v;
		}
		
		public String toString() {
			return value;
		}
	}
	
	private Set<DLlite_Constructs> constructs;
	
	public DLlite(String... fragments) {
		constructs = new HashSet<>();
		
		for(String s : fragments) {
			for(DLlite_Constructs construct : DLlite_Constructs.values()) {
				if(s.equals(construct.toString())) constructs.add(construct);
			}	
		}
	}
	
	public boolean isDLLiteClassExp(OWLClassExpression exp) {
		if(exp instanceof OWLClass) return true;
		if(exp instanceof OWLObjectSomeValuesFrom) return true;
		
		return false;
	}
	
	public Predicate<OWLAxiom> getAxiomFilter() {
		if(constructs.isEmpty()) return dllite_core_axiomOnly;
		else return null;
	}
	
	private Predicate<OWLAxiom> dllite_core_axiomOnly = new Predicate<OWLAxiom>() {
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
}
