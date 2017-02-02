package org.semanticweb.semtoo.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
	public static void recusiveDelete(File f) {
		if(f.isDirectory()) {
			for(File cf : f.listFiles()) recusiveDelete(cf);
		}
		f.delete();
	}
	
	public static List<String> getRegMatches(String str, String regex) {
		List<String> matches = new ArrayList<>();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		
		while(matcher.find()) {
			if(matcher.groupCount() == 0) matches.add(matcher.group());
			else matches.add(matcher.group(1));
		}
		
		return matches;
	}
	
	//Return the first group of the first match, if exists
	public static String singleMatch(String str, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		
		if(matcher.find()) {
			if(matcher.groupCount() == 0) return matcher.group();
			else return matcher.group(1);
		}		
		
		return null;
	}
}
