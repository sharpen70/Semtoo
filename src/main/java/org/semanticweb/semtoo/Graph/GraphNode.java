package org.semanticweb.semtoo.Graph;

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
	public Map<String, Object> info = new HashMap<>();
	public Set<String> labels = new HashSet<>();
	
	//public static enum property_nodeType {P, invP, PRctClass, invPRctClass};
	
	public static enum NODE_TYPE {
		PropertyRestrictionClass("PRctCls"),
		InverseOfPropertyRestrictionClass("invPRctCls"),
		AtomicClass("AtomicCls"),
		NamedIndividual("NamedIdv"),
		AnonymousIndividual("AnonyIdv");
		
		private String value;
		
		private NODE_TYPE(String t) {
			value = t;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	public static class NODE_KEY {
		public static final String NODE_IRI = "iri";
		public static final String NODE_DESCRIBTION = "description";
		public static final String NODE_TYPE = "type";
		public static final String POSITIVE_NODE_IRI = "piri";
		public static final String PROPERTY_IRI = "ppiri";		
	}
	
	//This string name is for identification in relation creation of neo4j
	public String neo4jName = null;
	
	public GraphNode(String nodetype, String... info) {
		if(info.length % 2 != 0) {
			throw new RuntimeException("The number of Parameters has to be even");
		}
		
		this.info = new HashMap<String, Object>();
		
		for(int i = 0; i < info.length - 1; i += 2) {
			this.info.put(info[i], info[i + 1]);
		}
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
	
	public GraphNode(OWLObjectProperty p, NODE_TYPE t) {
		String iri = p.getIRI().toString();
		String[] iri_s = iri.split("#");
		
		//Temporary Solution, need to revise in the future
		neo4jName = t + "_" + iri_s[1];
		
		info.put(NODE_KEY.NODE_IRI, t + "_" + iri);
		info.put(NODE_KEY.NODE_DESCRIBTION, neo4jName);
		info.put(NODE_KEY.PROPERTY_IRI, iri);
		info.put(NODE_KEY.NODE_TYPE, t.toString());
	}
	
	public void toNegation() {
		Object iri = info.get(NODE_KEY.NODE_IRI);
		String new_iri = "Negation_" + iri;
		
		info.put(NODE_KEY.POSITIVE_NODE_IRI, iri);
		info.put(NODE_KEY.NODE_IRI, new_iri);
		info.put("Negation", "");
	}
	
	public static GraphNode getNode(OWLClassExpression svf) {
		if(svf instanceof OWLObjectSomeValuesFrom) {
			OWLObjectPropertyExpression exp = ((OWLObjectSomeValuesFrom)svf).getProperty();
		
			if(exp instanceof OWLObjectInverseOf) return new GraphNode(((OWLObjectInverseOf)exp).getNamedProperty(), NODE_TYPE.InverseOfPropertyRestrictionClass);
			else return new GraphNode((OWLObjectProperty)exp, NODE_TYPE.PropertyRestrictionClass);	
		}
		if(svf instanceof OWLClass) {
			return new GraphNode((OWLClass)svf);
		}
		
		return null;
	}		
	
	public GraphNode(OWLIndividual idv) {
		String iri = idv.toStringID();
		info.put(NODE_KEY.NODE_IRI, iri);
		
		if(idv instanceof OWLNamedIndividual) {
			//Temporary Solution, need to revise in the future
			neo4jName = iri.split("#")[1];
			info.put(NODE_KEY.NODE_TYPE, NODE_TYPE.NamedIndividual);
		}
		else {
			//Temporary Solution, need to revise in the future
			neo4jName = iri;
			info.put(NODE_KEY.NODE_TYPE, NODE_TYPE.AnonymousIndividual);
		}
		
		info.put(NODE_KEY.NODE_DESCRIBTION, neo4jName);
	}	
}
