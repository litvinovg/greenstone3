/*
 * search.java
 *
 * Created on 25 February 2003, 02:25
 */


/**
 *
 * @author  kjdon@cs.waikato.ac.nz
 * @version 
 */

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.index.Term;

public class Search  {

    public static void main (String args[]) {
	
	if (args.length == 0) {
	    System.out.println("Usage: Search <index directory>");
	    return;
	}
        try {
	    Searcher searcher = new IndexSearcher(args[0]);
	    Analyzer analyzer = new StandardAnalyzer();

	    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    while (true) {
		System.out.print("Query: ");
		String line = in.readLine();
		
		if (line.length() == -1)
		    break;
		
		Term term = new Term("content",line);
		
		Query query = new TermQuery(term);
		System.out.println("Searching for: " + query.toString("content"));
		
		Hits hits = searcher.search(query);
		System.out.println(hits.length() + " total matching documents");
		
		final int HITS_PER_PAGE=10;
		
		for (int start = 0; start < hits.length(); start += HITS_PER_PAGE) {
		    int end = Math.min(hits.length(), start + HITS_PER_PAGE);
		    for (int i = start; i < end; i++) {
			Document doc = hits.doc(i);
			String node_id= doc.get("nodeID");
			System.out.println(i + ". ID: "+node_id);
		    }
		    
		    if (hits.length() > end) {
			System.out.print("more (y/n) ? ");
			line = in.readLine();
			if (line.length() == 0 || line.charAt(0) == 'n')
			    break;
		    }
		}
		
	    }
	    
	    searcher.close();
	}
	catch (Exception e) {
	    System.out.println(" caught a " + e.getClass() +
			       "\n with message: " + e.getMessage());
        }
    }
}
