package org.semanticweb.semtoo.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSVConverter {
	private static final String csv_store = "./CSVStore";
	public static void convertOWLDBfile(String filepath, String prefix) throws FileNotFoundException {
		File dbfile = new File(filepath);
		String fileName = dbfile.getName();
		
		String name = fileName.substring(0, fileName.indexOf("."));

		
		File csv_dir = new File(csv_store);
		if(!csv_dir.exists()) csv_dir.mkdirs();
		
		File csv_classAssertion = new File(csv_store + "/" + name + "_cls.csv");
		File csv_propertyAssertion = new File(csv_store + "/" + name + "_ppt.csv");		
				
		Scanner scanner = new Scanner(dbfile);
		PrintWriter writer1 = new PrintWriter(csv_classAssertion);
		PrintWriter writer2 = new PrintWriter(csv_propertyAssertion);
		
		writer1.println("\"class\",\"idv\"");
		writer2.println("\"property\",\"subject\",\"object\"");

		
		int read_count = 0;
		int rc = 0;
		
		long start = System.currentTimeMillis();
		
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if(line.contains("COPY")) {
				String predicate_name = Helper.singleMatch(line, "^COPY\\s([^\\s(]*)");
				if(predicate_name == null) break;
				String predicate_iri = prefix + predicate_name;
				
				String data = scanner.nextLine();
				while(!data.matches("^\\\\.")) {
					Pattern data_pattern = Pattern.compile("[\\S]+");
					Matcher matcher = data_pattern.matcher(data);
					
					String term1 = null;
					String term2 = null;
					
					if(matcher.find()) {
						term1 = matcher.group();
						if(matcher.find()) {
							term2 = matcher.group();
						}
					}
					
					if(term1 != null) {
						if(term2 != null) writer2.println("\"" + predicate_iri + "\",\"" + term1 + "\",\"" + term2 + "\"");
						else writer1.println("\"" + predicate_iri + "\",\"" + term1 + "\"");
						rc++;
						read_count++;
					}
					
					if(rc >= 10000) {
						rc = 0;
						long end = System.currentTimeMillis();
						System.out.println("Done reading of " + read_count + " assertion records with " + (end - start) + " ms");
					}
					if(scanner.hasNextLine()) data = scanner.nextLine();
					else break;
				}
			}
		}
		scanner.close();
		writer1.close();
		writer2.close();
	}
}
