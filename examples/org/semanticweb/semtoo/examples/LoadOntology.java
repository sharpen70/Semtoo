package org.semanticweb.semtoo.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;

import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;

import com.google.common.base.Predicate;
import com.google.common.io.Files;

public class LoadOntology {
	public static final IRI EXAMPLE_IRI = IRI.create("http://protege.stanford.edu/ontologies/pizza/pizza.owl");

	
	public static void main(String[] args) throws OWLException {
		// TODO Auto-generated method stub
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(EXAMPLE_IRI);
		
		System.out.println("Axioms of Pizza Ontology");
		
		
		Consumer<OWLAxiom> addToGraph = new Consumer<OWLAxiom>() {

			@Override
			public void accept(OWLAxiom t) {
				// TODO Auto-generated method stub
				
			}
		};
		
		try {
//			PrintWriter writer = new PrintWriter("test/pizzaowl", "UTF-8");
//			Consumer<OWLAxiom> print = (p) -> writer.println(p);						
//			ontology.axioms().forEach(print);
//			writer.close();
//			
//			PrintWriter writer2 = new PrintWriter("test/sigOfpizzaowl", "UTF-8");
//			ontology.signature().forEach(p -> writer2.println(p));

//			writer2.close();
//			
//			PrintWriter writer3 = new PrintWriter("test/classofpizzaowl", "UTF-8");
//			ontology.classesInSignature().forEach(p -> writer3.println(p));
//			writer3.close();
			
			PrintWriter writer4 = new PrintWriter("test/individualofpizzaowl", "UTF-8");
			ontology.individualsInSignature().forEach(p -> writer4.println(p));
			ontology.rboxAxioms(Imports.INCLUDED);
			writer4.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		ontology.signature().forEach(System.out::println);
		
	}

}