package org.semanticweb.semtoo.embeddedneo4j;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

public class SemtooDatabaseMeta {
	public static class node_labels {
		public static final Label TBOXENTITY = Label.label("TBEN");
		public static final Label CLASS = Label.label("CLS");
		public static final Label NEGATION = Label.label("NEG");
		public static final Label INDIVIDUAL = Label.label("IDV");
		public static final Label DUALINDIVIDUAL = Label.label("DUAL");
		public static final Label PROPERTY = Label.label("PTY");
		public static final Label PROPERTY_CLASS = Label.label("PTYCLS");
	}
	
	public static class property_key {
		public static final String NODE_IRI = "iri";
		public static final String NODE_DESCRIBTION = "description";
		public static final String NODE_TYPE = "type";
		public static final String POSITIVE_NODE_IRI = "piri";
		public static final String PROPERTY_IRI = "ppiri";		
		public static final String SUBJECT_IRI = "subiri";
		public static final String OBJECT_IRI = "objiri";
		public static final String IRI_LOWER = "iri_lower";
	}
	
	public static enum RelType implements RelationshipType { SubOf, is }
}
