package org.semanticweb.semtoo.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.semanticweb.semtoo.util.CSVConverter;

public class convertDBdumpFiles {
	public static final String default_prefix = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";
	
	public static void main(String[] args) throws IOException {
		CSVConverter.convertOWLDBfile("./resources/u1p0.sql", default_prefix);
	}

}
