package core;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

public class Main {
	public static void main(String[] args) throws IOException, ParseException, InvalidTokenOffsetsException{
		Path indexDirPath = Paths.get("../documents/" + args[1] + "/index").toAbsolutePath();
		
		if (args[0].equals("-b")) { // Build
			Path dataDirPath = Paths.get("../documents/" + args[1] + "/contents").toAbsolutePath();
			Indexer indexer = new Indexer(indexDirPath);
			System.out.println(String.format("%d files added to the index.", indexer.makeIndex(dataDirPath)));
			indexer.close();
		}
		
		if (args[0].equals("-s")) { // Search
			Searcher searcher = new Searcher(indexDirPath);
			while (true) {
				System.out.println("Enter query:");
				Scanner scanner = new Scanner(System.in);
				String query = scanner.nextLine();
				searcher.search(query, 3);
			}
		}
		
		if (args[0].equals("-t")) {
			Path bgFilePath = Paths.get("../documents/.bg").toAbsolutePath();
			PLSA plsa = new PLSA(indexDirPath, bgFilePath);
			plsa.getTopics(10, 0.95);
		}
	}
}
