package org.semanticweb.semtoo.embeddedneo4j;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.RelType;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.node_labels;
import org.semanticweb.semtoo.embeddedneo4j.StDatabaseMeta.property_key;

public class StTraversal {
	private GraphDatabaseService db;
	
	public StTraversal(GraphDatabaseService _db) {
		db = _db;
	}
	
	public TraversalDescription getTargetTraDes(Label target_label) {
		TraversalDescription td = db.traversalDescription()
				.relationships(RelType.SubOf, Direction.INCOMING)
				.relationships(RelType.is, Direction.INCOMING)
				.evaluator(new Evaluator() {
					@Override
					public Evaluation evaluate(Path path) {
						Node end = path.endNode();
						boolean get = end.hasLabel(target_label);

						return Evaluation.of(get, !get);
					}
				});
		
		return td;
	}
	
	public void getConceptInstances(Node atom, HashSet<Long> restrict) {
		HashSet<Long> instances = new HashSet<>();
		TraversalDescription td = getTargetTraDes(node_labels.INDIVIDUAL);
		Traverser traverser = td.traverse(atom);
		
		for(Path p : traverser) {
			Node endNode = p.endNode();
			long endid = endNode.getId();
			instances.add(endid);
		}
		
	}
	
	public void getRoleInstances(Node atom, Set<Long> object_restrict, 
			Set<Long> subject_restrict) {
		Set<Long> instances_subejct = new HashSet<>();
		Set<Long> instances_obbejct = new HashSet<>();
		
		TraversalDescription td = getTargetTraDes(node_labels.DUALINDIVIDUAL);
		Traverser traverser = td.traverse(atom);
		
		for(Path p : traverser) {
			Node endNode = p.endNode();

			long sid = (long)(endNode.getProperty(property_key.SUBJECT_ID));
			long oid = (long)(endNode.getProperty(property_key.OBJECT_ID));
			
			instances_subejct.add(sid);
			instances_obbejct.add(oid);
		}
		
	}
	
	public Map<Long, Set<Long>> assertionsInnerJoin(Map<Long, Set<Long>> a, Map<Long, Set<Long>> b) {
		Map<Long, Set<Long>> M, S;
		if(a.size() < b.size()) { S = a; M = b;}
		else { S = b; M = a;}
		
		for(Entry<Long, Set<Long>> e : S.entrySet()) {
			Set<Long> v = M.get(e.getKey());
			
			if(v != null) e.getValue().addAll(v);
			else S.remove(e.getKey());
		}
		
		return S;
	}
	
	public StAssertions getSourcePath(Node concept) {
		StAssertions assertions = new StAssertions();
		TraversalDescription td = getTargetTraDes(node_labels.INDIVIDUAL);
		
		Traverser traverser = td.traverse(concept);
		
		for(Path p : traverser) {
			Long endId = p.endNode().getId();	
			assertions.add(endId, p);
		}
		
		return assertions;
	}
	
	public StAssertions getSourceAssertions(Node concept) {
		StAssertions assertions = new StAssertions();
		Label target_label = node_labels.INDIVIDUAL;
		if(concept.hasLabel(node_labels.PROPERTY)) target_label = node_labels.DUALINDIVIDUAL;
		
		TraversalDescription td = getTargetTraDes(target_label);
		
		Traverser traverser = td.traverse(concept);
		
		for(Path p : traverser) {
			Long endId = p.endNode().getId();
			Long lastRelId = p.lastRelationship().getId();
			
			if(!lastRelId.equals(null)) {
				assertions.add(endId, lastRelId);
			}
		}
		
		return assertions;
	}
}
