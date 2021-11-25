package core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
	private IndexWriter indexWriter;
	
	public Indexer(Path indexDirPath) throws IOException {
		Directory indexDir = FSDirectory.open(indexDirPath);
		IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
		indexWriter = new IndexWriter(indexDir, conf);
	}
	
	private Document makeDocument(File file) throws IOException {
		Document document = new Document();
		Field titleField = new TextField("Title", file.getName(), Field.Store.YES);
		Field contentField = new TextField("Content", Files.readString(file.toPath()), Field.Store.YES);
		
		document.add(titleField);
		document.add(contentField);
		return document;
	}
	
	private void indexFile(File file) throws IOException {
		Document document = makeDocument(file);
		indexWriter.addDocument(document);
	}
	
	public int makeIndex(Path dataDirPath) throws IOException {
		indexWriter.deleteAll();
		File[] files = dataDirPath.toFile().listFiles();		
		for (File file : files) {
			if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead()) {
				indexFile(file);
			}
		}
		return indexWriter.getDocStats().numDocs;
		
	}
	
	public void close() throws CorruptIndexException, IOException {
		indexWriter.close();
	}
}
