package org.semanticweb.semtoo.model;

import java.util.ArrayList;
import java.util.List;

public class NaryAtom implements Atom {
	private String predicate;
	private List<Term> terms;
	
	public NaryAtom(String atom, String prefix) {
		terms = new ArrayList<>();
		
		predicate = prefix + atom.substring(0, atom.indexOf("(") - 1);
		String body = atom.substring(atom.indexOf("(") + 1, atom.length() - 1);
		
		String[] strs = body.split(",");
		
		for(String s : strs) {
			s = s.trim();
			if(s.startsWith("?")) terms.add(new Variable(s.substring(1)));
			else terms.add(new Constant(s));
		}
	}
	
	public String getPredicateName() {
		return predicate;
	}
	
	public List<Term> getTerms() {
		return terms;
	}
	
	@Override
	public String toString() {
		String str = predicate + "(";
		
		for(int i = 0; i < terms.size(); i++) {
			if(i == terms.size() - 1) str += terms.get(i) + ")";
			else str += terms.get(i) + " ,";
		}
		
		return str;
	}
}
