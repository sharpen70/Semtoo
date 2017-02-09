package org.semanticweb.semtoo.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

//Currently this class only deal with DL-lite core

public class DLliteFilter implements Predicate<OWLAxiom> {
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
	
	public DLliteFilter(String... fragments) {
		constructs = new HashSet<>();
		
		for(String s : fragments) {
			for(DLlite_Constructs construct : DLlite_Constructs.values()) {
				if(s.equals(construct.toString())) constructs.add(construct);
			}	
		}
	}
	
	public boolean isDLLiteClassExp(OWLClassExpression exp) {
		OWLClassExpressionVisitorEx<Boolean> ev = new OWLClassExpressionVisitorEx<Boolean>() {
			@Override
			public Boolean visit(OWLClass ce) {
				return true;
			}
			@Override
			public Boolean visit(OWLObjectSomeValuesFrom ce) {
				if(ce.getFiller().isOWLThing()) return true;
				else return false;
			}
			@Override
			public Boolean visit(OWLObjectIntersectionOf ce) {
				if(constructs.contains(DLlite_Constructs.INTERSECTION)) return true;
				else return false;			
			}
		};
		
		Boolean b = exp.accept(ev);
		if(b == null) return false;
		else return b;
	}	

	private OWLAxiomVisitorEx<Boolean> av = new OWLAxiomVisitorEx<Boolean>() {
		public Boolean visit(OWLClassAssertionAxiom axiom) {
			return true;
		};
		
		public Boolean visit(OWLObjectPropertyAssertionAxiom axiom) {
			return true;
		};
		
		public Boolean visit(OWLDisjointClassesAxiom axiom) {
			for(OWLClassExpression exp : axiom.classExpressions().collect(Collectors.toList())) {
				if(!isDLLiteClassExp(exp)) return false;
			}
			return true;
		};
		
		public Boolean visit(OWLDisjointObjectPropertiesAxiom axiom) {
			if(constructs.contains(DLlite_Constructs.ROLE)) return true;
			else return false;
		};
		
		public Boolean visit(OWLEquivalentClassesAxiom axiom) {
			List<OWLClassExpression> exps = axiom.classExpressions().collect(Collectors.toList());
			for(OWLClassExpression e : exps) {
				if(!isDLLiteClassExp(e)) return false;
			}
			return true;
		};
		
		public Boolean visit(OWLSubClassOfAxiom axiom) {
			OWLClassExpression exp1 = axiom.getSubClass();
			OWLClassExpression exp2 = axiom.getSuperClass();
			
			return isDLLiteClassExp(exp1) && isDLLiteClassExp(exp2);
		};
		
		public Boolean visit(OWLSubObjectPropertyOfAxiom axiom) {
			if(constructs.contains(DLlite_Constructs.ROLE)) return true;
			else return false;
		};
	};
	
	@Override
	public boolean test(OWLAxiom t) {
		Boolean b = t.accept(av);
		if(b == null) return false;
		return b;
	}
}
