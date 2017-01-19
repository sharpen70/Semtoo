package org.semanticweb.semtoo.Graph;

import java.util.HashMap;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor;


public class GraphNode {
	public HashMap<String, String> _info = new HashMap<>();
	
	public static enum property_nodeType {P, invP, PRctClass, invPRctClass};
	
	//This string name is for identification in relation creation of neo4j
	public String neo4jName = null;
	
	public GraphNode(String nodetype, String... info) {
		if(info.length % 2 != 0) {
			throw new RuntimeException("The number of Parameters has to be even");
		}
		
		this._info = new HashMap<String, String>();
		
		for(int i = 0; i < info.length - 1; i += 2) {
			this._info.put(info[i], info[i + 1]);
		}
	}
	
	public GraphNode(OWLClass c) {
		String iri = c.getIRI().toString();
		String description = iri.split("#")[1];
		
		//Temporary Solution, need to revise in the future
		neo4jName = description;
		
		_info.put("iri", iri);		
		_info.put("description", description);		
		_info.put("type", "AtomicClass");
	}
	
	public GraphNode(OWLObjectProperty p, property_nodeType t) {
		String iri = p.getIRI().toString();
		String[] iri_s = iri.split("#");
		
		//Temporary Solution, need to revise in the future
		neo4jName = t + "_" + iri_s[1];
		
		_info.put("iri", t + "_" + iri);
		_info.put("description", neo4jName);		
		_info.put("type", "PropertyRestrictionClass");
	}
	
	public void toNegation() {
		String new_iri = "Negation_" + _info.get("iri");
		_info.put("iri", new_iri);
		_info.put("Negation", "");
	}
	
	public static GraphNode getNode(OWLClassExpression svf) {
		if(svf instanceof OWLObjectSomeValuesFrom) {
			OWLObjectPropertyExpression exp = ((OWLObjectSomeValuesFrom)svf).getProperty();
		
			if(exp instanceof OWLObjectInverseOf) return new GraphNode(((OWLObjectInverseOf)exp).getNamedProperty(), property_nodeType.invPRctClass);
			else return new GraphNode((OWLObjectProperty)exp, property_nodeType.PRctClass);	
		}
		if(svf instanceof OWLClass) {
			return new GraphNode((OWLClass)svf);
		}
		
		return null;
	}		
	
	public GraphNode(OWLIndividual idv) {
		if(idv instanceof OWLNamedIndividual) {
			String iri = idv.toStringID();
			//Temporary Solution, need to revise in the future
			neo4jName = iri.split("#")[1];
			
			_info.put("iri", iri);
			_info.put("description", neo4jName);
			_info.put("type", "NamedIndividual");
		}
		else {
			String iri = idv.toStringID();
			//Temporary Solution, need to revise in the future
			neo4jName = iri;
			
			_info.put("iri", iri);
			_info.put("description", neo4jName);
			_info.put("type", "AnonymousIndividual");
		}
	}	
}
