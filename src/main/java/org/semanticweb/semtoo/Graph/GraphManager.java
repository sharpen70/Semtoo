package org.semanticweb.semtoo.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.semtoo.neo4j.Neo4jManager;

public class GraphManager {
	
	public GraphManager() {
		
	}
	
	public static Predicate<OWLEntity> logicalEntityOnly = new Predicate<OWLEntity>() {
		@Override
		public boolean test(OWLEntity t) {
			return t.isIndividual() || t.isOWLClass() || t.isOWLObjectProperty();
		}
	};
	
	public static Predicate<OWLAxiom> logicalAxiomOnly = new Predicate<OWLAxiom>() {
		@Override
		public boolean test(OWLAxiom t) {
			// TODO Auto-generated method stub
			return t.isLogicalAxiom();
		}
	};
	
	public void ontologyToGraph(OWLOntology o) {
		Neo4jManager neo4jmanager = new Neo4jManager();
		Session session = neo4jmanager.getSession();
		
		long start = System.currentTimeMillis();

		//Transfer entities of Individual, Class and Property to nodes in the graph 
		try(Transaction tc = session.beginTransaction()) {
			//Set the Graph to empty Graph 
			neo4jmanager.clearGraph(tc);

			Consumer<OWLEntity> addNode = new Consumer<OWLEntity>() {		
				@Override
				public void accept(OWLEntity t) {
					ArrayList<String> nodeTypes = new ArrayList<String>();
					nodeTypes.add("OWLEntity");
					
					if(t.isIndividual()) nodeTypes.add("Individual");
					if(t.isOWLClass()) nodeTypes.add("Class");
					if(t.isOWLObjectProperty()) nodeTypes.add("Role");
					
					String iri = t.getIRI().toString();
					String description = iri.split("#")[1];
					
					HashMap<String, String> info = new HashMap<String, String>();
					info.put("description", description);
					info.put("iri", iri);
					
					neo4jmanager.createNode(nodeTypes, info, tc);
				}
			};
			o.signature().filter(logicalEntityOnly).forEach(addNode);			
		}
		long end = System.currentTimeMillis();
		System.out.println("Create transaction closed in " + (end - start) + " ms");
		
		//Build Index on for Nodes on IRI property
		try(Transaction tc = session.beginTransaction()) {
			neo4jmanager.buildIndex("OWLEntity", "iri", tc);
		}		
		
		//Build relations between Nodes according Tbox Axioms
		try(Transaction tc = session.beginTransaction()) {
			GraphOntologyVistors vs = new GraphOntologyVistors(neo4jmanager, tc);
			OWLAxiomVisitor v = vs.getAxiomVisitor();

			o.axioms().filter(logicalAxiomOnly).forEach(p -> p.accept(v));
		}
		
		session.close();
		neo4jmanager.close();
	}
}
