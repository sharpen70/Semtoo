package org.semanticweb.semtoo.model;

import java.util.ArrayList;
import java.util.List;

public class BinaryAtom implements Atom {
	private String predicate;
	private Term first_term;
	private Term second_term;
	
	public BinaryAtom(String atom, String prefix) {
		predicate = prefix + atom.substring(0, atom.indexOf("(") - 1);
		String body = atom.substring(atom.indexOf("(") + 1, atom.length() - 1);
		
		String[] strs = body.split(",");
		
		String str1 = strs[0].trim();
		String str2 = strs[1].trim();
		
		if(str1.startsWith("?")) first_term = new Variable(str1.substring(1));
		else first_term = new Constant(str1);
		
		if(str2.startsWith("?")) second_term = new Variable(str2.substring(1));
		else second_term = new Constant(str2);

	}
	
	public String getPredicateName() {
		return predicate;
	}
	
	public Term getFirst() {
		return first_term;
	}
	
	public Term getSecond() {
		return second_term;
	}
	
	@Override
	public String toString() {
		return predicate + "(" + first_term + ", " + second_term + ")";
	}
}
