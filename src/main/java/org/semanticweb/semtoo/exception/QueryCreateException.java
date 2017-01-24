package org.semanticweb.semtoo.exception;

public class QueryCreateException extends QueryException {
	@Override
	public String getMessage() {
		return "Head variables are not consistent with body variables";
	}
}
