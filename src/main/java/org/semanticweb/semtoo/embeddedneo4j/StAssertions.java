package org.semanticweb.semtoo.embeddedneo4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class StAssertions {
	private Map<Long, Set<Long>> assertions;
	
	public StAssertions() {
		assertions = new HashMap<>();
	}
	
	public void add(Long idv, Long cls) {
		Set<Long> assertionNodes = assertions.get(idv);
		
		if(assertionNodes == null) {
			assertionNodes = new HashSet<>();
			assertionNodes.add(cls);
			assertions.put(idv, assertionNodes);
		}
		else assertionNodes.add(cls);	
	}
	
	public static Map<Long, Set<Long>> assertionsInnerJoin(StAssertions a, StAssertions b) {
		Map<Long, Set<Long>> M, S, join = new HashMap<>();
		if(a.assertions.size() < b.assertions.size()) { S = a.assertions; M = b.assertions;}
		else { S = b.assertions; M = a.assertions;}
		
//		System.out.println(S.size());
		for(Entry<Long, Set<Long>> e : S.entrySet()) {
			Set<Long> v = M.get(e.getKey());
			
			if(v != null) {
				Set<Long> sv = e.getValue();
				sv.addAll(v);
				join.put(e.getKey(), sv);
			}
		}
		
		return join;
	}
}
