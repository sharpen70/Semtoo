package org.semanticweb.semtoo.model;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.semtoo.exception.QueryCreateException;
import org.semanticweb.semtoo.util.Helper;

public class CQuery {
	private List<Variable> queryVariable;
	private List<NaryAtom> body;
	
	public CQuery(String q, String prefix) {
		queryVariable = new ArrayList<>();
		body = new ArrayList<>();
		
		String[] qs = q.split("<-");
		
		List<String> _body = Helper.getRegMatches(qs[1].trim(), "[A-Za-z][^\\(,]*\\([^()]*\\)");
		List<String> variables = Helper.getRegMatches(qs[0].trim(), "\\?([^,\\s\\)]*)");
		List<String> bodyVariables = Helper.getRegMatches(qs[1].trim(), "\\?([^,\\s\\)]*)");
		
		for(String s : _body) body.add(new NaryAtom(s, prefix));
		
		for(String s : variables) {
			if(!bodyVariables.contains(s)) throw new QueryCreateException();
			queryVariable.add(new Variable(s));
		}
	}
	
	public List<Variable> getQueryVariable() {
		return queryVariable;
	}
	
	public List<NaryAtom> getConjuncts() {
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
