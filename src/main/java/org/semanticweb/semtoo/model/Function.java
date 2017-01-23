package org.semanticweb.semtoo.model;

import java.util.ArrayList;
import java.util.List;

public class Function implements Term {
	private String name;
	private List<Term> terms;
	
	public Function(String function) {
		
	}
	
	@Override
	public String getFullName() {
		// TODO Auto-generated method stub
		return null;
	}	
	
	public Function(String _name, List<Term> _terms) {
		terms = _terms;
		name = _name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public List<Term> getTerms() {
		return terms;
	}
}
