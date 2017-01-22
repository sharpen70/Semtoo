package org.semanticweb.semtoo.model;

public class Constant implements Term {
	private String name;
	
	public Constant(String _name) {
		name = _name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
