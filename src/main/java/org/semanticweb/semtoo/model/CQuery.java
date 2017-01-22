package org.semanticweb.semtoo.model;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.semtoo.tools.Helper;

public class CQuery {
	private List<Variable> queryVariable;
	private List<Atom> body;
	
	public CQuery(String q, String prefix) {
		queryVariable = new ArrayList<>();
		body = new ArrayList<>();
		
		String[] qs = q.split("<-");
		
		List<String> _body = Helper.getRegMatches(qs[1].trim(), "[A-Za-z][^\\(,]*\\([^()]*\\)");
		List<String> variables = Helper.getRegMatches(qs[0].trim(), "\\?([^,\\s\\)]*)");
		
		for(String s : _body) body.add(new Atom(s, prefix));
		for(String s : variables) queryVariable.add(new Variable(s));
	}
	
	public List<Variable> getQueryVariable() {
		return queryVariable;
	}
	
	public List<Atom> getConjuncts() {
		return body;
	}
	
	@Override
	public String toString() {
		String str = "Q(";
		for(int i = 0; i < queryVariable.size(); i++) {
			if(i == queryVariable.size() - 1) str += queryVariable.get(i) + ")";
			else str += queryVariable.get(i) + ", ";
		}
		str += " <- ";
		for(int i = 0; i < body.size(); i++) {
			if(i == body.size() - 1) str += body.get(i);
			else str += body.get(i) + ", ";
		}
		return str;
	}
}
