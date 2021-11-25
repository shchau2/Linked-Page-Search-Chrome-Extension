package core;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
	IndexReader indexReader;
	IndexSearcher indexSearcher;
	Query query;
	QueryParser queryParser;

	public Searcher(Path indexDirPath) throws IOException {
		Directory indexDir = FSDirectory.open(indexDirPath);
		indexReader = DirectoryReader.open(indexDir);
		indexSearcher = new IndexSearcher(indexReader);
		queryParser = new QueryParser("Content", new StandardAnalyzer());
	}
	
	public void search(String queryStr, int n) throws IOException, ParseException, InvalidTokenOffsetsException {
		query = queryParser.parse(queryStr);
		TopDocs results = indexSearcher.search(query, n);
		
		Formatter formatter = new SimpleHTMLFormatter();
		QueryScorer scorer = new QueryScorer(query);
		Highlighter highlighter = new Highlighter(formatter, scorer);
		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 100);
		highlighter.setTextFragmenter(fragmenter);
		
		for (int i = 0; i < results.scoreDocs.length; i++) 
        {
            int docid = results.scoreDocs[i].doc;
            Document doc = indexReader.document(docid);
            String title = doc.get("Title");
            String text = doc.get("Content");
            
            
            System.out.println(title);
            String[] frags = highlighter.getBestFragments(new StandardAnalyzer(), null, text, 10);
            for (String frag : frags) 
            {
                System.out.println(frag);
            }
            System.out.println("=======================");
        }
	}
}
