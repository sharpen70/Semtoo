package org.semanticweb.semtoo.graph;

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
import org.semanticweb.semtoo.model.GraphNode.NODE_LABEL;
import org.semanticweb.semtoo.model.GraphNode.NODE_TYPE;
import org.semanticweb.semtoo.neo4j.Neo4jManager;
import org.semanticweb.semtoo.neo4j.Neo4jUpdate;
import org.semanticweb.semtoo.util.DLliteFilter;

public class GraphManager {
	private Neo4jManager neo4jmanager = null;
	
	public GraphManager() {
		neo4jmanager = Neo4jManager.getManager();
	}	
	
	private class loadAxiomVistor implements OWLAxiomVisitor {
		private Transaction tc;
		
		public loadAxiomVistor(Transaction transaction) {
			tc = transaction;
		}
		
//		public void visit(OWLClassAssertionAxiom axiom) {
//			OWLClassExpression exp = axiom.getClassExpression();
//			OWLIndividual idv = axiom.getIndividual();
//			
//			String iri = exp.accept(getExpIRI);
//			
//			Neo4jUpdate.matchAndcreateRelation(idv.toStringID(), iri, "is", tc);;
//		};
//		public void visit(OWLObjectPropertyAssertionAxiom axiom) {
//			OWLIndividual subject = axiom.getSubject();
//			OWLIndividual object = axiom.getObject();
//			OWLObjectPropertyExpression property = axiom.getProperty();
//			
//			GraphNode node1 = new GraphNode(subject, object);
//			GraphNode node2 = new GraphNode(object, subject);
//			
//			String addDual = "MATCH (subject {" + NODE_KEY.NODE_IRI + ":{s_iri}}), (object {" + NODE_KEY.NODE_IRI + ":{o_iri}}) " 
//							+ "CREATE (" + node1.neo4jName + ":" + NODE_LABEL.DUALINDIVIDUAL + " {node1_info}), "
//							+ "(" + node2.neo4jName + ":" + NODE_LABEL.DUALINDIVIDUAL + " {node2_info}) "
//							+ "CREATE (subject)-[:Subject]->(" + node1.neo4jName + "), "
//									+ "(subject)-[:Object]->(" + node2.neo4jName + "), "
//									+ "(object)-[:Subject]->(" + node2.neo4jName + "), "
//									+ "(object)-[:Object]->(" +node1.neo4jName + ")";
//			
//			tc.run(addDual, Values.parameters("node1_info", node1.info, 
//					"node2_info", node2.info, "s_iri", subject.toStringID(), "o_iri", object.toStringID()));
//			
//			//Neo4jUpdate.createNode(node1, NODE_LABEL.INDIVIDUAL, tc);
//			//Neo4jUpdate.createNode(node2, NODE_LABEL.INDIVIDUAL, tc);
//			
//			String so_iri = node1.info.get(NODE_KEY.NODE_IRI);
//			String os_iri = node2.info.get(NODE_KEY.NODE_IRI);
//			String p_iri = GraphNode.getPropertyIRI(property);
//			String ip_iri = GraphNode.getPropertyIRI(property.getInverseProperty());
//			
//			String statement1 = "MATCH (subject_object {" + NODE_KEY.NODE_IRI + ":{so_iri}}), (p {" + NODE_KEY.NODE_IRI + ":{p_iri}}), "
//					+ "(object_subject {" + NODE_KEY.NODE_IRI + ":{os_iri}}), (ip {" + NODE_KEY.NODE_IRI + ":{ip_iri}}) "
//					+ "CREATE (subject_object)-[:is]->(p), (object_subject)-[:is]->(ip)";
//			
//			tc.run(statement1, Values.parameters("so_iri", so_iri, 
//					"p_iri", p_iri, "os_iri", os_iri, "ip_iri", ip_iri));
//			
//			String subject_iri = subject.toStringID();
//			String object_iri = object.toStringID();
//			String rp_iri = GraphNode.getPRctClassIRI(property);
//			String rip_iri = GraphNode.getPRctClassIRI(property.getInverseProperty());
//			
//			String statement2 = "MATCH (subject {" + NODE_KEY.NODE_IRI + ":{siri}}), (p {" + NODE_KEY.NODE_IRI + ":{piri}}), "
//					+ "(object {" + NODE_KEY.NODE_IRI + ":{oiri}}), (ip {" + NODE_KEY.NODE_IRI + ":{ipiri}}) "
//					+ "CREATE (subject)-[:is]->(p), (object)-[:is]->(ip)";
//			
//			tc.run(statement2, Values.parameters("siri", subject_iri,
//					"piri", rp_iri, "oiri", object_iri, "ipiri", rip_iri));
//			tc.success();
//		}
		
		public void visit(OWLDisjointClassesAxiom axiom) {
			OWLPairwiseVoidVisitor<OWLClassExpression> _visitor = new OWLPairwiseVoidVisitor<OWLClassExpression>() {
				@Override
				public void visit(OWLClassExpression a, OWLClassExpression b) {
					String a_iri = a.accept(getExpIRI);
					GraphNode negationNode = GraphNode.getClassNodeByClassExpression(b, true);
					
					tc.run("MATCH (a:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":{a_iri}}) "
							+ "MERGE (b:" + GraphNode.NODE_LABEL.NEGATION + " {" + NODE_KEY.NODE_IRI + ":{b_info}.iri}) "
							+ "ON CREATE SET b = {b_info} CREATE (a)-[:SubOf]->(b)", 
							Values.parameters("a_iri", a_iri, "b_info", negationNode.info));
					tc.success();
				}
			};
			axiom.forEach(_visitor);
		};
		public void visit(OWLSubClassOfAxiom axiom) {
//			System.out.println(axiom);
			String sub_iri = axiom.getSubClass().accept(getExpIRI);
			String super_iri = axiom.getSuperClass().accept(getExpIRI);
			Neo4jUpdate.matchAndcreateRelation(sub_iri, super_iri, NODE_LABEL.TBOXENTITY, "SubOf", tc);
		};
		public void visit(OWLEquivalentClassesAxiom axiom) {
			OWLPairwiseVoidVisitor<OWLClassExpression> _vistor = new OWLPairwiseVoidVisitor<OWLClassExpression>() {
				@Override
				public void visit(OWLClassExpression a, OWLClassExpression b) {
					String a_iri = a.accept(getExpIRI);
					String b_iri = b.accept(getExpIRI);
					
					tc.run("MATCH (a:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":{a_iri}}), "
							+ "(b:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":{b_iri}}) CREATE (a)-[:SubOf]->(b) CREATE (b)-[:SubOf]->(a)",
							Values.parameters("a_iri", a_iri, "b_iri", b_iri));
					tc.success();
				}
			};
			axiom.forEach(_vistor);
		};
		public void visit(OWLSubObjectPropertyOfAxiom axiom) {
			OWLObjectPropertyExpression subp = axiom.getSubProperty();
			OWLObjectPropertyExpression superp = axiom.getSuperProperty();
			
			String subp_iri = GraphNode.getPropertyIRI(subp);
			String superp_iri = GraphNode.getPropertyIRI(superp);
			String i_subp_iri = GraphNode.getPropertyIRI(subp.getInverseProperty());
			String i_superp_iri = GraphNode.getPropertyIRI(superp.getInverseProperty());
			
			Neo4jUpdate.matchAndcreateRelation(subp_iri, superp_iri, NODE_LABEL.TBOXENTITY, "SubOf", tc);
			Neo4jUpdate.matchAndcreateRelation(i_subp_iri, i_superp_iri, NODE_LABEL.TBOXENTITY, "SubOf", tc);
			
			String rt_subp_iri = GraphNode.getPRctClassIRI(subp);
			String rt_superp_iri = GraphNode.getPRctClassIRI(superp);
			String rt_i_subp_iri = GraphNode.getPRctClassIRI(subp.getInverseProperty());
			String rt_i_superp_iri = GraphNode.getPRctClassIRI(superp.getInverseProperty());
			
			Neo4jUpdate.matchAndcreateRelation(rt_subp_iri, rt_superp_iri, NODE_LABEL.TBOXENTITY, "SubOf", tc);
			Neo4jUpdate.matchAndcreateRelation(rt_i_subp_iri, rt_i_superp_iri, NODE_LABEL.TBOXENTITY, "SubOf", tc);
		};
		public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
			OWLPairwiseVoidVisitor<OWLObjectPropertyExpression> _vistor = new OWLPairwiseVoidVisitor<OWLObjectPropertyExpression>() {
				@Override
				public void visit(OWLObjectPropertyExpression a, OWLObjectPropertyExpression b) {
					GraphNode nb_node = GraphNode.getPropertyNodeByExpession(b, true);
					GraphNode nib_node = GraphNode.getPropertyNodeByExpession(b.getInverseProperty(), true);
					String a_iri = GraphNode.getPropertyIRI(a);
					String ia_iri = GraphNode.getPropertyIRI(a.getInverseProperty());
					
					String statement1 = "MATCH (a:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":{a_iri}}) MATCH (ia:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":{ia_iri}}) "
									+ "MERGE (nb:" + GraphNode.NODE_LABEL.NEGATION + " {" + NODE_KEY.NODE_IRI + ":{nb_info}." + NODE_KEY.NODE_IRI + "}) ON CREATE SET nb = {nb_info} "
									+ "MERGE (nib:" + GraphNode.NODE_LABEL.NEGATION + " {" + NODE_KEY.NODE_IRI + ":{nib_info}." + NODE_KEY.NODE_IRI + "}) ON CREATE SET nib = {nib_info} "
									+ "CREATE (a)-[:SubOf]->(nb), (ia)-[:SubOf]->(nib)";
					
					tc.run(statement1, Values.parameters("a_iri", a_iri, 
							"ia_iri", ia_iri, "nb_info", nb_node.info, "nib_info", nib_node.info));
					tc.success();
					
					GraphNode nrb_node = GraphNode.getRestrictionNodeByExpression(b, true);
					GraphNode nrib_node = GraphNode.getRestrictionNodeByExpression(b.getInverseProperty(), true);
					String ra_iri = GraphNode.getPRctClassIRI(a);
					String ria_iri = GraphNode.getPRctClassIRI(a.getInverseProperty());
					
					String statement2 = "MATCH (ra:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":{ra_iri}}) MATCH (ria:" + NODE_LABEL.TBOXENTITY + " {" + NODE_KEY.NODE_IRI + ":{ria_iri}}) "
							+ "MERGE (nrb:" + GraphNode.NODE_LABEL.NEGATION + " {" + NODE_KEY.NODE_IRI + ":{nrb_info}." + NODE_KEY.NODE_IRI + "}) ON CREATE SET nrb = {nrb_info} "
							+ "MERGE (nrib:" + GraphNode.NODE_LABEL.NEGATION + " {" + NODE_KEY.NODE_IRI + ":{nrib_info}." + NODE_KEY.NODE_IRI + "}) ON CREATE SET nrib = {nrib_info} "
							+ "CREATE (ra)-[:SubOf]->(nrb), (ria)-[:SubOf]->(nrib)";
			
					tc.run(statement2, Values.parameters("ra_iri", ra_iri,
					"ria_iri", ria_iri, "nrb_info", nrb_node.info, "nrib_info", nrib_node.info));
					tc.success();
				}
			};
			axiom.forEach(_vistor);
		};
	}
	
	public void loadTboxToGraph(OWLOntology o) {
		try(Session session = neo4jmanager.getSession()) {
			try(Transaction tc = session.beginTransaction()) {
				
			}
		}
	}
	
	public void loadOntologyToGraph(OWLOntology o) {
		Session session = neo4jmanager.getSession();
		
		long start = System.currentTimeMillis();
		
		//Transfer entities of Individual, Class and Property to nodes in the graph 
		
		//Build Index on for Nodes on IRI property
		System.out.println("Build index for TBox Entities on IRI ...");
		
		try(Transaction tc = session.beginTransaction()) {
			Neo4jUpdate.buildIndex(NODE_LABEL.TBOXENTITY, NODE_KEY.NODE_IRI, tc); 
			Neo4jUpdate.buildIndex(NODE_LABEL.TBOXENTITY, NODE_KEY.IRI_LOWER, tc);
			Neo4jUpdate.buildIndex(NODE_LABEL.NEGATION, NODE_KEY.NODE_IRI, tc);
		}
		
		System.out.println("Inserting nodes for Ontology Entities ...");
		
		try(Transaction tc = session.beginTransaction()) {
			OWLEntityVisitor entityVisitor = new OWLEntityVisitor() {
				public void visit(OWLClass ce) {
					Neo4jUpdate.createNode(new GraphNode(ce), NODE_LABEL.TBOXENTITY, tc);
				};
				public void visit(OWLObjectProperty property) {
					GraphNode p = new GraphNode(property, false, false);
					GraphNode ip = new GraphNode(property, false, true);
					GraphNode rp = new GraphNode(property, true, false);
					GraphNode rip = new GraphNode(property, true, true);
					
					Neo4jUpdate.createNode(p, NODE_LABEL.TBOXENTITY, tc);
					Neo4jUpdate.createNode(ip, NODE_LABEL.TBOXENTITY, tc);
					Neo4jUpdate.createNode(rp, NODE_LABEL.TBOXENTITY, tc);
					Neo4jUpdate.createNode(rip, NODE_LABEL.TBOXENTITY, tc);
				};
				public void visit(OWLNamedIndividual idv) {
					Neo4jUpdate.createNode(new GraphNode(idv), NODE_LABEL.INDIVIDUAL, tc);
				}
			};
			o.signature().filter(dl_lite_entityOnly).forEach(e -> e.accept(entityVisitor));			
		}
		
		try(Transaction tc = session.beginTransaction()) {
			tc.run("MATCH (n:" + NODE_LABEL.TBOXENTITY + ") SET n." + NODE_KEY.IRI_LOWER + " = LOWER(n." + NODE_KEY.NODE_IRI + ")");
			tc.success();
		}
		
		long end = System.currentTimeMillis();
		System.out.println("Done with " + (end - start) + " ms");
		
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
