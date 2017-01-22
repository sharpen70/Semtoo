package org.semanticweb.semtoo.Graph;

import java.util.Map;
import java.util.function.Predicate;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLPairwiseVoidVisitor;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.semtoo.model.GraphNode;
import org.semanticweb.semtoo.model.GraphNode.NODE_KEY;
import org.semanticweb.semtoo.model.GraphNode.NODE_TYPE;
import org.semanticweb.semtoo.neo4j.Neo4jManager;
import org.semanticweb.semtoo.neo4j.Neo4jUpdate;
import org.semanticweb.semtoo.tools.DLliteFilter;

public class GraphManager {
	private Neo4jManager neo4jmanager = null;
	
	public GraphManager(Neo4jManager m) {
		neo4jmanager = m;
	}	
	
	private class loadAxiomVistor implements OWLAxiomVisitor {
		private Transaction tc;
		
		public loadAxiomVistor(Transaction transaction) {
			tc = transaction;
		}
		
		public void visit(OWLClassAssertionAxiom axiom) {
			OWLClassExpression exp = axiom.getClassExpression();
			OWLIndividual idv = axiom.getIndividual();
			
			String iri = exp.accept(getExpIRI);
			
			Neo4jUpdate.matchAndcreateRelation(idv.toStringID(), iri, "is", tc);;
		};
		public void visit(OWLObjectPropertyAssertionAxiom axiom) {
			OWLIndividual subject = axiom.getSubject();
			OWLIndividual object = axiom.getObject();
			OWLObjectPropertyExpression property = axiom.getProperty();
			
			GraphNode node1 = new GraphNode(subject, object);
			GraphNode node2 = new GraphNode(object, subject);
			Neo4jUpdate.createNode(node1, tc);
			Neo4jUpdate.createNode(node2, tc);
			
			String so_iri = node1.info.get(NODE_KEY.NODE_IRI);
			String os_iri = node2.info.get(NODE_KEY.NODE_IRI);
			String p_iri = GraphNode.getPropertyIRI(property);
			String ip_iri = GraphNode.getPropertyIRI(property.getInverseProperty());
			
			String statement1 = "Match (subject_object {{iri}:{so_iri}}), (p {{iri}:{p_iri}}), "
					+ "(object_subject {{iri}:{os_iri}}), (ip {{iri}:{ip_iri}}) "
					+ "CREATE (subject_object)-[:is]->(p), (object_subject)-[:is]->(ip)";
			
			tc.run(statement1, Values.parameters("iri", NODE_KEY.NODE_IRI, "so_iri", so_iri, 
					"p_iri", p_iri, "os_iri", os_iri, "ip_iri", ip_iri));
			
			String subject_iri = subject.toStringID();
			String object_iri = object.toStringID();
			String rp_iri = GraphNode.getPRctClassIRI(property);
			String rip_iri = GraphNode.getPRctClassIRI(property.getInverseProperty());
			
			String statement2 = "Match (subject {{iri}:{siri}}), (p {{iri}:{piri}}), "
					+ "(object {{iri}:{oiri}}) (ip {{iri}:{ipiri}}) "
					+ "CREATE (subject)-[:is]->(p), (object)-[:is]->(ip)";
			
			tc.run(statement2, Values.parameters("iri", NODE_KEY.NODE_IRI, "siri", subject_iri,
					"piri", rp_iri, "oiri", object_iri, "ipiri", rip_iri));
			tc.success();
		}
		public void visit(OWLDisjointClassesAxiom axiom) {
			OWLPairwiseVoidVisitor<OWLClassExpression> _visitor = new OWLPairwiseVoidVisitor<OWLClassExpression>() {
				@Override
				public void visit(OWLClassExpression a, OWLClassExpression b) {
					String a_iri = a.accept(getExpIRI);
					GraphNode negationNode = GraphNode.getGraphNode(b);
					negationNode.toNegation();
					
					tc.run("Match (a {iri:{a_iri}}) Merge (b:" + GraphNode.NODE_LABEL.NEGATION + " {iri:{b_info}.iri}) "
							+ "ON CREATE SET b:" + GraphNode.NODE_LABEL.NEGATION + ", b = {b_info} CREATE (a)-[:SubOf]->(b)", 
							Values.parameters("a_iri", a_iri, "b_info", negationNode.info));
					tc.success();
				}
			};
			axiom.forEach(_visitor);
		};
		public void visit(OWLSubClassOfAxiom axiom) {
			String sub_iri = axiom.getSubClass().accept(getExpIRI);
			String super_iri = axiom.getSuperClass().accept(getExpIRI);
			Neo4jUpdate.matchAndcreateRelation(sub_iri, super_iri, "SubOf", tc);
		};
		public void visit(OWLEquivalentClassesAxiom axiom) {
			OWLPairwiseVoidVisitor<OWLClassExpression> _vistor = new OWLPairwiseVoidVisitor<OWLClassExpression>() {
				@Override
				public void visit(OWLClassExpression a, OWLClassExpression b) {
					String a_iri = a.accept(getExpIRI);
					String b_iri = a.accept(getExpIRI);
					
					tc.run("Match (a {iri:{a_iri}}), (b {iri:{b_iri}}) CREATE (a)-[:SubOf]->(b), (b)-[:SubOf]->(a)",
							Values.parameters("a_iri", a_iri, "b_iri", b_iri));
					tc.success();
				}
			};
			axiom.forEach(_vistor);
		};
		public void visit(OWLSubObjectPropertyOfAxiom axiom) {
			OWLObjectPropertyExpression subp = axiom.getSubProperty();
			OWLObjectPropertyExpression superp = axiom.getSuperProperty();
			
			
			if(subp.getClass().equals(superp.getClass())) {
				
			}
			else {
				
			}
		};
		public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
			
		};
	}
	
	public void loadOntologyToGraph(OWLOntology o) {
		Session session = neo4jmanager.getSession();
		
		long start = System.currentTimeMillis();
		
		//Set the Graph to empty Graph 
		try(Transaction tc = session.beginTransaction()) {
			Neo4jUpdate.clearGraph(tc);
		}
		
		//Transfer entities of Individual, Class and Property to nodes in the graph 
		System.out.println("Turning ontology class into nodes ...");
		
		try(Transaction tc = session.beginTransaction()) {
			OWLEntityVisitor entityVisitor = new OWLEntityVisitor() {
				public void visit(OWLClass ce) {
					Neo4jUpdate.createNode(new GraphNode(ce), tc);
				};
				public void visit(OWLObjectProperty property) {
					GraphNode p = new GraphNode(property, false, false);
					GraphNode ip = new GraphNode(property, false, true);
					GraphNode rp = new GraphNode(property, true, false);
					GraphNode rip = new GraphNode(property, true, true);
					
					Neo4jUpdate.createNode(p, tc);
					Neo4jUpdate.createNode(ip, tc);
					Neo4jUpdate.createNode(rp, tc);
					Neo4jUpdate.createNode(rip, tc);
				};
				public void visit(OWLNamedIndividual idv) {
					Neo4jUpdate.createNode(new GraphNode(idv), "Individual", tc);
				}
			};
			o.signature().filter(dl_lite_entityOnly).forEach(e -> e.accept(entityVisitor));			
		}
		long end = System.currentTimeMillis();
		System.out.println("Create nodes Transaction closed in " + (end - start) + " ms");
		
		try(Transaction tc = session.beginTransaction()) {
			Neo4jUpdate.addLabelToAll("OWLEntity", tc);
		}
		
		//Build Index on for Nodes on IRI property
		System.out.println("Build Index for class iri ...");
		try(Transaction tc = session.beginTransaction()) {
			Neo4jUpdate.buildIndex("OWLEntity", "iri", tc); 
		}		
		
		//Build relations between Nodes according Tbox Axioms
		System.out.println("Creating relations between nodes according to axioms ...");
		start = System.currentTimeMillis();
		try(Transaction tc = session.beginTransaction()) {			
			o.axioms().filter(new DLliteFilter("R")).forEach(a -> a.accept(new loadAxiomVistor(tc)));
		}
		end = System.currentTimeMillis();
		System.out.println("Create relations Transaction closed in " + (end - start) + " ms");
		
		session.close();
	}
	
	private OWLClassExpression fromMapToExpression(Map<String, Object> record, OWLDataFactory df) {
		String iri = record.get("iri").toString();
		String type = record.get("type").toString();
		
		OWLClassExpression exp = null;
		
		if(type == NODE_TYPE.AtomicClass.toString()) {
			exp = df.getOWLClass(IRI.create(iri));
		}
		else {
			OWLClass thing = df.getOWLThing();
			String p_iri = record.get(GraphNode.NODE_KEY.PROPERTY_IRI).toString();
			OWLObjectProperty p = df.getOWLObjectProperty(IRI.create(p_iri));
			
			if(type == NODE_TYPE.PropertyRestrictionClass.toString()) {
				exp = df.getOWLObjectSomeValuesFrom(p, thing);
			}
			else if(type == NODE_TYPE.InverseOfPropertyRestrictionClass.toString()) {
				OWLObjectInverseOf invP = df.getOWLObjectInverseOf(p);
				exp = df.getOWLObjectSomeValuesFrom(invP, thing);
			}
		}
		return exp;
	}
	
	public void loadGraphToOntology(IRI ontologyIRI, String savepath) throws OWLException {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology o = m.createOntology(ontologyIRI);
		OWLDataFactory df = m.getOWLDataFactory();
		
		try(Session session = neo4jmanager.getSession()) {
			try(Transaction tc = session.beginTransaction()) {
				String statement = "MATCH (a)-[:SubOf]->(b) RETURN a as subject, b as object";
				StatementResult result = tc.run(statement);
				
				while(result.hasNext()) {
					Record r = result.next();
					Map<String, Object> subject = r.get("subject").asMap();
					Map<String, Object> object = r.get("object").asMap();
					
					OWLClassExpression subject_exp = fromMapToExpression(subject, df);
					OWLClassExpression object_exp = fromMapToExpression(object, df);
					
					OWLAxiom axiom = df.getOWLSubClassOfAxiom(subject_exp, object_exp);
					AddAxiom add_axiom = new AddAxiom(o, axiom);
					m.applyChange(add_axiom);
				}	
			}
			try(Transaction tc = session.beginTransaction()) {
				StatementResult result = tc.run("MATCH (a)-[:SubOf]->(b:" + GraphNode.NODE_LABEL.NEGATION + ") RETURN a, b");
				
				while(result.hasNext()) {
					Record r = result.next();
					Map<String, Object> a = r.get("a").asMap();
					Map<String, Object> b = r.get("b").asMap();
					
					OWLClassExpression a_exp = fromMapToExpression(a, df);
					OWLClassExpression b_exp = fromMapToExpression(b, df);
					
					OWLAxiom axiom = df.getOWLDisjointClassesAxiom(a_exp, b_exp);
					AddAxiom add_axiom = new AddAxiom(o, axiom);
					m.applyChange(add_axiom);
				}
			}
			try(Transaction tc = session.beginTransaction()) {
				StatementResult result = tc.run("MATCH (a {{key1}:{value}})-[:is]->(b) RETURN a.{key2}, b",
						Values.parameters("key1", GraphNode.NODE_KEY.NODE_TYPE, "value", NODE_TYPE.NamedIndividual,
								"key2", GraphNode.NODE_KEY.NODE_IRI));
				
				while(result.hasNext()) {
					Record r = result.next();
					String a_iri = r.get("a").asString();
					Map<String, Object> b = r.get("b").asMap();
					
					OWLNamedIndividual idv = df.getOWLNamedIndividual(IRI.create(a_iri));
					OWLClassExpression b_exp = fromMapToExpression(b, df);
					
					OWLAxiom axiom = df.getOWLClassAssertionAxiom(b_exp, idv);
					AddAxiom add_axiom = new AddAxiom(o, axiom);
					m.applyChange(add_axiom);
				}
			}
		}
		
		m.saveOntology(o, IRI.create(savepath));
	}
	
	public void close() {
		neo4jmanager.close();
	}
	
	private Predicate<OWLEntity> dl_lite_entityOnly = new Predicate<OWLEntity>() {
		@Override
		public boolean test(OWLEntity t) {
			return t.isOWLClass() || t.isOWLObjectProperty() || t.isOWLNamedIndividual();
		}
	};
	
	private OWLClassExpressionVisitorEx<String> getExpIRI = new OWLClassExpressionVisitorEx<String>() {
		public String visit(OWLClass ce) {
			return ce.getIRI().toString();
		};
		
		public String visit(OWLObjectSomeValuesFrom svf) {
			OWLObjectPropertyExpression exp = svf.getProperty();
			
			return GraphNode.getPRctClassIRI(exp);				
		}
	};
}

