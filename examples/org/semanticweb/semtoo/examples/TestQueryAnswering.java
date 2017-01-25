package org.semanticweb.semtoo.examples;

import org.semanticweb.semtoo.IAR;
import org.semanticweb.semtoo.exception.QueryCreateException;
import org.semanticweb.semtoo.model.CQuery;

public class TestQueryAnswering {
	public static final String prefixForA = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";
	public static final String[] queries = 
		   {"Q(?0) <- Person(?0),Work(?0),Publication(?0),Organization(?0)",
				   
            "Q(?0,?1) <- Person(?0),takesCourse(?0,?1)",
            
            "Q(?0,?1) <- Employee(?0),publicationAuthor(?1,?0)",
            
            "Q(?0,?1,?2,?3,?4,?5) <- FullProfessor(?0),publicationAuthor(?1,?0),teacherOf(?0,?2),advisor(?3,?0),GraduateStudent(?3),degreeFrom(?0,?4),degreeFrom(?3,?5)",
            
            "Q(?0) <- Organization(?0)",
            
            "Q(?0) <- Employee(?0)",
            
            "Q(?0,?1) <- Person(?0),teacherOf(?0,?1),Course(?1)",
            
            "Q(?0,?1,?2) <- Student(?0),advisor(?0,?1),Faculty(?1),takesCourse(?0,?2),teacherOf(?1,?2),Course(?2)",
            
            "Q(?0,?1) <- Student(?0),takesCourse(?0,?1),Course(?1),teacherOf(?2,?1),Faculty(?2),worksFor(?2,?3),Department(?3),memberOf(?0,?3)",
            
		    "Q(?0) <- Publication(?0),publicationAuthor(?0,?1),Professor(?1),publicationAuthor(?0,?2),Student(?2)"};
	
	public static void main(String[] args) throws QueryCreateException {
			CQuery q = new CQuery(queries[1], prefixForA);
			
//			CQuery q1 = new CQuery("Q(?0, ?1) <- R(?0, ?1), B(?0)", TestForgetting._prefix + "#"); 
//			System.out.println(q1);
			IAR qa = new IAR();
			
			//qa.detectConflicts();
			long start = System.currentTimeMillis();
			qa.answer(q);
			long end = System.currentTimeMillis();
			System.out.println("USING " + (end - start) + " ms ...");
	}

}
