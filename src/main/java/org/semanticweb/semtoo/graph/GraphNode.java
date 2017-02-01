package org.semanticweb.semtoo.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;


public class GraphNode {
	public Map<String, String> info = new HashMap<>();
	
	public static class NODE_TYPE  {
		public static final String PropertyRestrictionClass = "PRctCls";
		public static final String InverseOfPropertyRestrictionClass = "invPRctCls";
		public static final String Property = "PR";
		public static final String InverseOfProperty = "invPR";
		public static final String AtomicClass = "AtomicCls";
		public static final String NamedIndividual ="NamedIdv";
		public static final String AnonymousIndividual = "AnonyIdv";
		public static final String DualIndividual = "DualIdv";
	}
	
	public static class NODE_LABEL {
		public static final String NEGATION = "NEG";
		public static final String TBOXENTITY = "TBEN";
		public static final String INDIVIDUAL = "IDV";
		public static final String DUALINDIVIDUAL = "DUAL";
		public static final String DISCARD = "DISC";
		public static final String PROPERTY = "PP";
		public static final String CLASS = "CLS";
		public static final String FORGET = "F";
	}
	
	public static class NODE_KEY {
		public static final String NODE_IRI = "iri";
		public static final String NODE_DESCRIBTION = "description";
		public static final String NODE_TYPE = "type";
		public static final String POSITIVE_NODE_IRI = "piri";
		public static final String PROPERTY_IRI = "ppiri";		
		public static final String SUBJECT_IRI = "subiri";
		public static final String OBJECT_IRI = "objiri";
		public static final String IRI_LOWER = "iri_lower";
	}
	
	public static final String NEG_PREFIX = "neg_";
	public static final String INV_PREFIX = "inv_";
	public static final String PRT_PREFIX = "prt_";
	
	//This string name is for identification in relation creation of neo4j
	public String neo4jName = null;
	
	public static String getPropertyIRI(OWLObjectPropertyExpression exp) {
		String piri = exp.getNamedProperty().toStringID();
		if(exp instanceof OWLObjectInverseOf) return GraphNode.INV_PREFIX + piri;
		else return piri;
	}
	
	public static String getPRctClassIRI(OWLObjectPropertyExpression exp) {
		String piri = exp.getNamedProperty().toStringID();
		if(exp instanceof OWLObjectInverseOf) return GraphNode.PRT_PREFIX + GraphNode.INV_PREFIX + piri;
		else return GraphNode.PRT_PREFIX + piri;	
	}
	
//	public GraphNode(String iri) {
////		labels.add(NODE_LABEL.OWLENTITY);
//		
//		info.put(NODE_KEY.NODE_IRI, iri);
//		neo4jName = iri.split("#")[1];
//		info.put(NODE_KEY.NODE_TYPE, NODE_TYPE.NamedIndividual);
//		info.put(NODE_KEY.NODE_DESCRIBTION, neo4jName);
//	}
//	
//	public GraphNode(String a_iri, String b_iri) {
//		neo4jName = a_iri.split("#")[1] + "_" + b_iri.split("#")[1];
//		
//		info.put(NODE_KEY.NODE_IRI, a_iri + "_" + b_iri);
//		info.put(NODE_KEY.SUBJECT_IRI, a_iri);
//		info.put(NODE_KEY.OBJECT_IRI, b_iri);
//		info.put(NODE_KEY.NODE_TYPE, NODE_TYPE.DualIndividual);
//		info.put(NODE_KEY.NODE_DESCRIBTION, neo4jName);
//	}
	
	public GraphNode(OWLIndividual idv) {
		String iri = idv.toStringID();
		
		info.put(NODE_KEY.NODE_IRI, iri);
		neo4jName = iri.split("#")[1];
		info.put(NODE_KEY.NODE_DESCRIBTION, neo4jName);
	}
	
	public GraphNode(OWLIndividual a, OWLIndividual b) {
		String a_iri = a.toStringID();
		String b_iri = b.toStringID();

		neo4jName = a_iri.split("#")[1] + "_" + b_iri.split("#")[1];
		
		info.put(NODE_KEY.NODE_IRI, a_iri + "_" + b_iri);
		info.put(NODE_KEY.SUBJECT_IRI, a_iri);
		info.put(NODE_KEY.OBJECT_IRI, b_iri);
		info.put(NODE_KEY.NODE_DESCRIBTION, neo4jName);
	}
	
	public GraphNode(OWLObjectProperty p, boolean restriction, boolean inverse) {
		String iri = p.toStringID();
		
		if(inverse) {
			if(restriction) {
				neo4jName = PRT_PREFIX + INV_PREFIX + iri.split("#")[1];
				info.put(NODE_KEY.NODE_IRI, PRT_PREFIX + INV_PREFIX + iri);
				info.put(NODE_KEY.PROPERTY_IRI, iri);
				info.put(NODE_KEY.NODE_TYPE, NODE_TYPE.InverseOfPropertyRestrictionClass);
			}
			else {
				neo4jName = INV_PREFIX + iri.split("#")[1];
				info.put(NODE_KEY.NODE_IRI, INV_PREFIX + iri);
				info.put(NODE_KEY.PROPERTY_IRI, iri);
				info.put(NODE_KEY.NODE_TYPE, NODE_TYPE.InverseOfProperty);
			}
		}
		else {
			if(restriction) {
				neo4jName = PRT_PREFIX + iri.split("#")[1];
				info.put(NODE_KEY.NODE_IRI, PRT_PREFIX + iri);
				info.put(NODE_KEY.PROPERTY_IRI, iri);
				info.put(NODE_KEY.NODE_TYPE, NODE_TYPE.PropertyRestrictionClass);
			}
			else {
				neo4jName = iri.split("#")[1];
				info.put(NODE_KEY.NODE_IRI, iri);
				info.put(NODE_KEY.NODE_TYPE, NODE_TYPE.Property);
			}
		}
		
		info.put(NODE_KEY.NODE_DESCRIBTION, neo4jName);
	}
	
	public GraphNode(OWLClass c) {
		String iri = c.getIRI().toString();
		String description = iri.split("#")[1];
		
		//Temporary Solution, need to revise in the future
		neo4jName = description;
		
		info.put(NODE_KEY.NODE_IRI, iri);		
		info.put(NODE_KEY.NODE_DESCRIBTION, description);		
		info.put(NODE_KEY.NODE_TYPE, NODE_TYPE.AtomicClass);
	}
	
	public static GraphNode getRestrictionNodeByExpression(OWLObjectPropertyExpression exp, boolean negation) {
		OWLObjectProperty p = exp.getNamedProperty();
		GraphNode n;
		
		if(exp instanceof OWLObjectInverseOf) n = new GraphNode(p, true, true);
		else n = new GraphNode(p, true, false);
		
		if(negation) n.toNegation();
		
		return n;
	}
	
	public static GraphNode getClassNodeByClassExpression(OWLClassExpression exp, boolean negation) {
		GraphNode n = null;
		
		if(exp instanceof OWLObjectSomeValuesFrom) n = 
				getRestrictionNodeByExpression(((OWLObjectSomeValuesFrom)exp).getProperty(), negation);
		else if(exp instanceof OWLClass) {
			n = new GraphNode((OWLClass)exp);
			if(negation) n.toNegation();
		}
		
		return n;
	}
	
	public static GraphNode getPropertyNodeByExpession(OWLObjectPropertyExpression exp, boolean negation) {
		GraphNode n = null;
		OWLObjectProperty p = exp.getNamedProperty();
		
		if(exp instanceof OWLObjectInverseOf) n = new GraphNode(p, false, true);
		else n = new GraphNode(p, false, false);
		
		if(negation) n.toNegation();
		
		return n;
	}
	
	private void toNegation() {
		String iri = info.get(NODE_KEY.NODE_IRI);
		String new_iri = NEG_PREFIX + iri;
		
		info.put(NODE_KEY.POSITIVE_NODE_IRI, iri);
		info.put(NODE_KEY.NODE_IRI, new_iri);
	}	
}
