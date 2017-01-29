package org.semanticweb.semtoo.util;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public class RandomGenerator {
	public static Collection<String> getConcepts(OWLOntology o, float rate, boolean group) {
		List<OWLEntity> entities = o.signature().collect(Collectors.toList());
		int size = entities.size();
		int f_num = (int)(size * rate);
		
		Random rand = new Random();
		
		return null;
	}
}
