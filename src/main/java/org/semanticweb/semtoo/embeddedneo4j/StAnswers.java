package org.semanticweb.semtoo.embeddedneo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StAnswers {
	public int colNum;
	public Map<Integer, Integer> colMap;
	public List<long[]>  result;
	
	
	public StAnswers(Map<Integer, Integer> _colMap) {
		colNum = _colMap.size();
		colMap = _colMap;
		
		result = new ArrayList<>();
	}
	
	public StAnswers(int... cols) {
		colNum = cols.length;
		colMap = new HashMap<>();
		
		for(int i : cols) {
			colMap.put(cols[i], i);
		}
		
		result = new ArrayList<>();
	}
	
	public void add(long[] ids) {
		if(ids.length < colNum) throw new RuntimeException("Require " + colNum + " param");
		long[] entry = new long[colNum];
		
		for(int i = 0; i < colNum; i++) {
			entry[i] = ids[i];
		}
		
		result.add(entry);
	}
	
	public StAnswers hashJoin(StAnswers M, int[] ons) {
		Map<long[], List<long[]>> hashtable = new HashMap<>();
		
		int begin = colMap.size();
		int[] extra = new int[M.colMap.size()];
		int extra_size = 0;
		
		for(Entry<Integer, Integer> e : M.colMap.entrySet()) if(colMap.get(e.getKey()) == null) {
			colMap.put(e.getKey(), begin++);
			extra[extra_size++] = e.getValue();
		}

		StAnswers joinResult = new StAnswers(colMap);
				
		for(long[] row : result) {
			long[] colon = new long[ons.length];
			for(int i = 0; i < ons.length; i++) colon[i] = row[colMap.get(ons[i])];
			List<long[]> rows = hashtable.putIfAbsent(colon, new ArrayList<>());
			rows.add(row);
		}
		
		List<long[]> mr = M.result;
		
		for(long[] row : mr) {
			long[] colon = new long[ons.length];
			for(int i = 0; i < ons.length; i++) colon[i] = row[M.colMap.get(ons[i])];
			List<long[]> rows = hashtable.get(colon);
			
			if(rows != null) {
				for(long[] _row : rows) {
					long[] merge = new long[colMap.size()];
					int j;
					for(j = 0; j < _row.length; j++) merge[j] = _row[j];
					for(int i = 0; i < extra_size; i++) merge[j++] = row[extra[i]];
					joinResult.add(merge);
				}
			}
		}
		
		return joinResult;
	}
}
