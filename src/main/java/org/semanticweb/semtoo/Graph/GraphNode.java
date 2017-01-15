package org.semanticweb.semtoo.Graph;

import java.util.HashMap;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

//enum NodeType {Individual, AtomicClass, RoleRestrictionClass, Top, Bottom}

public class GraphNode {
	public String _nodetype;
	public HashMap<String, String> _info;
	
	//This string name is for identification in relation creation of neo4j
	public String neo4jName;
	
	public GraphNode(String nodetype, String... info) {
		if(info.length % 2 != 0) {
			throw new RuntimeException("The number of Parameters has to be even");
		}
		
		this._nodetype = nodetype;
		this._info = new HashMap<String, String>();
		
		for(int i = 0; i < info.length - 1; i += 2) {
			this._info.put(info[i], info[i + 1]);
		}
	}
	
	public GraphNode(OWLEntity e) {
		String iri = e.getIRI().toString();
		String description = iri.split("#")[1];
		
		_nodetype = e.getEntityType().toString();
		
		//Temporary Solution, need to revise in the future
		neo4jName = description;
		
		_info.put("iri", iri);
		_info.put("description", description);
	}
	
	public GraphNode(OWLObjectSomeValuesFrom e) {
		OWLObjectPropertyExpression p = e.getProperty();
		
	}
}
