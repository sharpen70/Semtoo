package org.semanticweb.semtoo;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.semanticweb.semtoo.graph.GraphAnalyzer;

public class ICTolerant_Forgetting {
	private ICTolerant_QA icSemantic;
	private Forgetting f;
	
	public ICTolerant_Forgetting(ICTolerant_QA ic) {
		icSemantic = ic;
		f = new Forgetting();
	}
	
	public void forget(Collection<String> iris) {
		f.neighborRelatedForget(iris);
		icSemantic.consistentBase();
	}
	
	public static void main(String[] args) {
		final ExecutorService exec = Executors.newFixedThreadPool(1);
		
		Callable<String> call = new Callable<String>() {
			@Override
			public String call() throws Exception {
				ICTolerant_Forgetting icf = new ICTolerant_Forgetting(new IAR());
				icf.forget(GraphAnalyzer.getRandomGroupConcepts(30));
				
				return "Forget successfully";
			}
		};
		
		Future<String> future = exec.submit(call);
		
		try {
			String msg = future.get(3600, TimeUnit.SECONDS);
			System.out.println("Thread return: " + msg);
		} catch (InterruptedException e) {
			System.out.println("Thread interupted ...");
			e.printStackTrace();
		} catch (ExecutionException e) {
			System.out.println("Fail to execute ...");
			e.printStackTrace();
		} catch (TimeoutException e) {
			System.out.println("Task time out ...");
		}
	}
}
