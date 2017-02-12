package org.semanticweb.semtoo.examples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.semtoo.Forgetting_old;
import org.semanticweb.semtoo.graph.GraphAnalyzer;
import org.semanticweb.semtoo.graph.GraphAnalyzer.ORDER;
import org.semanticweb.semtoo.neo4j.Neo4jManager;

public class TestForgetting {
	static final String _prefix = "http://org.semanticweb.semtoo/example";
	
	public static void addAxiom(List<OWLAxiom> axioms, OWLOntologyManager m, OWLOntology o) {
		for(OWLAxiom a : axioms) {
			AddAxiom ad = new AddAxiom(o, a);
			m.applyChange(ad);
		}
	}
	
	public static OWLOntology createTestOntology() throws OWLException {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		
		OWLOntology o = m.createOntology(IRI.create(_prefix));
		
		OWLDataFactory df = m.getOWLDataFactory();
		
		List<OWLAxiom> axioms = new ArrayList<OWLAxiom>();
		
		PrefixManager prefix = new DefaultPrefixManager(_prefix + "#");
		
		OWLClass A = df.getOWLClass("A", prefix);
		OWLClass B = df.getOWLClass("B", prefix);
		OWLClass C = df.getOWLClass("C", prefix);
		OWLClass D = df.getOWLClass("D", prefix);
		
		OWLObjectProperty R = df.getOWLObjectProperty("R", prefix);
		OWLObjectProperty L = df.getOWLObjectProperty("L", prefix);
		OWLObjectProperty N = df.getOWLObjectProperty("N", prefix);
		
		OWLIndividual a = df.getOWLNamedIndividual("a", prefix);
		OWLIndividual b = df.getOWLNamedIndividual("b", prefix);
		
		axioms.add(df.getOWLSubClassOfAxiom(A, B));
		axioms.add(df.getOWLSubClassOfAxiom(D, C));
		
		OWLClassExpression rR = df.getOWLObjectSomeValuesFrom(R, df.getOWLThing());
		
		axioms.add(df.getOWLSubObjectPropertyOfAxiom(R, L));
		axioms.add(df.getOWLSubClassOfAxiom(B, rR));
	//	axioms.add(df.getOWLDisjointClassesAxiom(B, C));
	//	axioms.add(df.getOWLDisjointObjectPropertiesAxiom(R, N));
		
		//axioms.add(df.getOWLDisjointClassesAxiom(B, B));
		
		axioms.add(df.getOWLClassAssertionAxiom(B, b));
		axioms.add(df.getOWLClassAssertionAxiom(D, a));
		axioms.add(df.getOWLClassAssertionAxiom(A, a));
		axioms.add(df.getOWLObjectPropertyAssertionAxiom(R, a, b));
		addAxiom(axioms, m, o);
		
		return o;
	}
	
	public static void main(String[] args) throws OWLException {
//		GraphManager gm = new GraphManager();
//		gm.loadOntologyToGraph(createTestOntology());
		Neo4jManager m  = Neo4jManager.getManager();
		
//		Collection<String> concepts = GraphAnalyzer.getConceptsByDegree(30, ORDER.ASC);
		Collection<String> concepts = GraphAnalyzer.getRandomGroupConcepts(30);
		
		long start = System.currentTimeMillis();
		for(String iri : concepts) {
			System.out.println(iri);
		}
		long end = System.currentTimeMillis();
		
		System.out.println("Done with " + (end -start) + " ms\n");
		
//		Forgetting f = new Forgetting();
//		f.restoreDiscard();
//		
//		System.out.println("Going with Batch Forgetting ...");
//		start = System.currentTimeMillis();
//		f.naiveForget(concepts);
//		end = System.currentTimeMillis();
//		System.out.println("Done with " + (end -start) + " ms\n");
		
//		fs.add(_prefix + "#B");
//		fs.add(_prefix + "#A");
		
		//f.forget(_prefix + "#B");
		//f.neighborRelatedForget(fs);
		
	}
}
