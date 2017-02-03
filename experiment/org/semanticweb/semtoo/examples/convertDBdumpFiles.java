package org.semanticweb.semtoo.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.semanticweb.semtoo.util.CSVConverter;

public class convertDBdumpFiles {
	public static final String default_prefix = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";
	public static final String datasets = "../IJCAI17/Databases";
	public static final String big_datasets = "../IJCAI17/big";
	
	public static void main(String[] args) throws IOException {
		File ds = new File(datasets);
		
		for(File f : ds.listFiles()) {
			CSVConverter.convertOWLDBfile(f.getAbsolutePath(), default_prefix);
		}
	}

}
