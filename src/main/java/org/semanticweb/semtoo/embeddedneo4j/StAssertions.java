package org.semanticweb.semtoo.embeddedneo4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class StAssertions {
	private Map<Long, Set<Object>> assertions;
	
	public StAssertions() {
		assertions = new HashMap<>();
	}
	
	public Set<Object> rels() {
		Set<Object> re = new HashSet<>();
		
		for(Set<Object> s : assertions.values()) {
			re.addAll(s);
		}
		
		return re;
	}
	
	public void add(Long idv, Object cls) {
		Set<Object> assertionNodes = assertions.get(idv);
		
		if(assertionNodes == null) {
			assertionNodes = new HashSet<>();
			assertionNodes.add(cls);
			assertions.put(idv, assertionNodes);
		}
		else assertionNodes.add(cls);	
	}
	
	public static Map<Long, Set<Object>> assertionsInnerJoin(StAssertions a, StAssertions b) {
		Map<Long, Set<Object>> M, S, join = new HashMap<>();
		if(a.assertions.size() < b.assertions.size()) { S = a.assertions; M = b.assertions;}
		else { S = b.assertions; M = a.assertions;}
		
//		System.out.println(S.size());
		for(Entry<Long, Set<Object>> e : S.entrySet()) {
			Set<Object> v = M.get(e.getKey());
			
			if(v != null) {
				Set<Object> sv = e.getValue();
				sv.addAll(v);
				join.put(e.getKey(), sv);
			}
		}
		
		return join;
	}
}
