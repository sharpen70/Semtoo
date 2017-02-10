package org.semanticweb.semtoo.embeddedneo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StTraversalResult {
	public int colNum;
	public Map<Integer, Integer> colMap;
	public List<long[]>  result;
	
	
	public StTraversalResult(int... cols) {
		colNum = cols.length;
		colMap = new HashMap<>();
		
		for(int i : cols) {
			colMap.put(cols[i], i);
		}
	}
	
	public void add(long... ids) {
		if(ids.length < colNum) throw new RuntimeException("Require " + colNum + " param");
		long[] entry = new long[colNum];
		
		for(int i = 0; i < colNum; i++) {
			entry[i] = ids[i];
		}
		
		result.add(entry);
	}
	
	public StTraversalResult hashJoin(StTraversalResult M, int[] ons) {
		Map<long[], List<long[]>> hashtable = new HashMap<>();
		
		Map<Integer, Integer> newColMap = new HashMap<>();
		int index = 0;
		for(int i : colMap.keySet()) newColMap.put(i, index++);
		for(int i : M.colMap.keySet()) if(newColMap.get(i) == null) {newColMap.put(i, index++);}
		
		StTraversalResult joinResult = new StTraversalResult();
				
		for(long[] row : result) {
			long[] colon = new long[ons.length];
			for(int i = 0; i < ons.length; i++) colon[i] = row[colMap.get(ons[i])];
			List<long[]> rows = hashtable.getOrDefault(colon, new ArrayList<>());
			rows.add(row);
		}
		
		List<long[]> mr = M.result;
		
		for(long[] row : mr) {
			long[] colon = new long[ons.length];
			for(int i = 0; i < ons.length; i++) colon[i] = row[M.colMap.get(ons[i])];
			List<long[]> rows = hashtable.get(colon);
			
			if(rows != null) {
				
			}
		}
		
		return joinResult;
	}
}
