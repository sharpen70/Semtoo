package org.semanticweb.semtoo.model;

public class Constant implements Term {
	private String name;
	private String prefix;
	
	public Constant(String _name, String _prefix) {
		name = _name;
		prefix = _prefix;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getFullName() {
		return prefix + name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
