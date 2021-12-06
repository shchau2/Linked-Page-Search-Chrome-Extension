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
	public static void main(String[] args) throws IOException, ParseException, InvalidTokenOffsetsException {
		if (args.length > 1) {
			String dirPath = args[1];
			Path indexDirPath = Paths.get(dirPath + "index").toAbsolutePath();
			
			if (args[0].equals("-b")) { // Build
				Path dataDirPath = Paths.get(dirPath + "contents").toAbsolutePath();
				Indexer indexer = new Indexer(indexDirPath);
				System.out.println(String.format("Build: %d files have been added", indexer.makeIndex(dataDirPath)));
				indexer.close();
			}
			else if (args[0].equals("-s")) { // Search
				Searcher searcher = new Searcher(indexDirPath);
				searcher.search(args[2], 10);
			}
			else if (args[0].equals("-t")) {
				Path bgFilePath = Paths.get(".background").toAbsolutePath();
				PLSA plsa = new PLSA(indexDirPath, bgFilePath);
				plsa.getTopics(Integer.parseInt(args[2]), Double.parseDouble(args[3]));
			}
		}
		else {
			System.out.println("Usage:\n"
					+ "For building index: -b [directory path]\n"
					+ "For searching: -s [directory path] [query]\n"
					+ "For topic analysis: -t [directory path] [number of topics] [probability of background]");
		}
	}
}
