package org.semanticweb.semtoo.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
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
}
