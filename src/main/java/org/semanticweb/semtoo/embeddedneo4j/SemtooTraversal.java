package org.semanticweb.semtoo.embeddedneo4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
import org.semanticweb.semtoo.embeddedneo4j.SemtooDatabaseMeta.RelType;
import org.semanticweb.semtoo.embeddedneo4j.SemtooDatabaseMeta.node_labels;
import org.semanticweb.semtoo.embeddedneo4j.SemtooDatabaseMeta.property_key;

public class SemtooTraversal {
	private GraphDatabaseService db;
	
	public SemtooTraversal(GraphDatabaseService _db) {
		db = _db;
	}
	
	private void retainCommon(Set<Long> tids, Set<Long> ids) {
		Set<Long> loopset = tids;
		Set<Long> checkset = ids;
		
		if(tids.size() > ids.size()) {
			loopset = ids;
			checkset = tids;
		}
		
		for(Long id : loopset) {
			if(!checkset.contains(id)) tids.remove(id);
		}
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
	
	public void getConceptInstances(Node atom, Set<Long> restrict) {
		Set<Long> instances = new HashSet<>();
		
		TraversalDescription td = getTargetTraDes(node_labels.INDIVIDUAL);
		Traverser traverser = td.traverse(atom);
		
		for(Path p : traverser) {
			Node endNode = p.endNode();
			long endid = endNode.getId();
			instances.add(endid);
		}
		
		retainCommon(restrict, instances);
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
	
	public Map<Long, Set<Long>> getSourceAssertions(Node concept, Map<Long, Set<Long>> restrict) {
		Map<Long, Set<Long>> assertions = new HashMap<>();
		Label target_label = node_labels.INDIVIDUAL;
		if(concept.hasLabel(node_labels.PROPERTY)) target_label = node_labels.DUALINDIVIDUAL;
		
		TraversalDescription td = getTargetTraDes(target_label);
		
		Traverser traverser = td.traverse(concept);
		
		for(Path p : traverser) {
			Iterator<Node> reverseNodes = p.reverseNodes().iterator();
			long endid = reverseNodes.next().getId();
			long beforeEndid = reverseNodes.next().getId();
			
			Set<Long> assertionNodes = assertionss.get(endid);
			if(assertionNodes == null) {
				Set<Long> _assertionNodes = new HashSet<>();
				_assertionNodes.add(beforeEndid);
				assertions.put(endid, _assertionNodes);
			}
			else assertionNodes.add(beforeEndid);
		}
		
		for(Long key: assertions.keySet()) {
			assertions.get(key).addAll(restrict.get(key));
		}
		
		return assertions;
	}
}
