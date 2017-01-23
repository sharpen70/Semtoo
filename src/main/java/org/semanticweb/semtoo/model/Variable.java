package org.semanticweb.semtoo.model;

public class Variable implements Term {
	private String name;
	
	public Variable(String _name) {
		name = _name;
	}
	
	@Override
	public String getName() {
		return "v_" + name;
	}
	
	@Override
	public String getFullName() {
		return "v_" + name;
	}
	
	@Override
	public String toString() {
		return "?" + name;
	}
}
