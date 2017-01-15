package org.semanticweb.semtoo.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;
import org.semanticweb.semtoo.Graph.GraphManager;


public class LoadOntology {
	public static File pizzaowl = new File("resources/pizza.owl");
	public static final IRI EXAMPLE_IRI = IRI.create(pizzaowl);
	
	public static void main(String[] args) throws OWLException {
		// TODO Auto-generated method stub
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		System.out.println("Loading Ontology ...");
		
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(EXAMPLE_IRI);		
		
		GraphManager gm = new GraphManager();
		
		System.out.println("Transfering to Graph ...");
		
		long startTime = System.currentTimeMillis();	
		gm.ontologyToGraph(ontology);
		long endTime = System.currentTimeMillis();
		
		System.out.println("Transfer complete. Time used: " + (endTime - startTime) + " milliseconds" );
//		try {
//			PrintWriter writer = new PrintWriter("test/pizzaowl", "UTF-8");
//			Consumer<OWLAxiom> print = (p) -> writer.println(p);						
//			ontology.axioms().forEach(addToGraph);
//		
//			writer.close();
//			
//			PrintWriter writer2 = new PrintWriter("test/sigOfpizzaowl", "UTF-8");
//			ontology.signature().filter(GraphManager.logicalEntityOnly).forEach(p -> writer2.println(p));
//			writer2.close();
//			
//			PrintWriter writer3 = new PrintWriter("test/classofpizzaowl", "UTF-8");
//			ontology.classesInSignature().forEach(p -> writer3.println(p));
//			writer3.close();
//			
//			PrintWriter writer4 = new PrintWriter("test/individualofpizzaowl", "UTF-8");
//			ontology.individualsInSignature().forEach(p -> writer4.println(p));
//			ontology.rboxAxioms(Imports.INCLUDED);
//			writer4.close();
//			
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(ontology));
//		
//		OWLOntologyWalkerVisitor visitor = new OWLOntologyWalkerVisitor(walker) {
//			@Override
//			public void visit(OWLObjectSomeValuesFrom desc) {
//				System.out.println(desc);
//				System.out.println("   " + getCurrentAxiom());
//			}
//		};
//		
//		
//		walker.walkStructure(visitor);

//		ontology.signature().forEach(System.out::println);
		
	}

}
