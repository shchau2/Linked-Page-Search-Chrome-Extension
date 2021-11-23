package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class PLSA {
	IndexReader indexReader;
	int d; // number of documents;
	int w; // number of words;
	String[] words;
	int[][] wordCounts;
	double[] bgWordDistribution;
	
	public PLSA(Path indexDirPath, Path bgFilePath) throws IOException {
		Directory indexDir = FSDirectory.open(indexDirPath);
		indexReader = DirectoryReader.open(indexDir);
		
		LeafReader leafReader = indexReader.leaves().get(0).reader();
		Terms terms = leafReader.terms("Content");
		
		d = indexReader.numDocs();
		w = (int) terms.size();	
		words = new String[w];
		wordCounts = new int[d][w];
		
		int termID = 0;
		TermsEnum it = terms.iterator();
		while (it.next() != null) {
			Term term = new Term("Content", it.term());
			words[termID] = term.text();
			
			PostingsEnum postings = leafReader.postings(term, PostingsEnum.FREQS);
			while (postings.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
				int docID = postings.docID();
				int freq = postings.freq();
				wordCounts[docID][termID] = freq;
			}
			
			termID += 1;
		}
		initBgDistribution(w, bgFilePath);
	}
	
	public void getTopics(int k, double lambda) throws IOException {
		double[][][] probWordInDocFromTopic = new double[w][d][k];
		double[][] probWordInDocFromBg = new double[w][d];
		
		double[][] docTopicCoverage = new double[d][k];
		double[][] wordDistribution = new double[k][w];
		
		// Initialize
		for (int i = 0; i < d; i++) {
			docTopicCoverage[i] = getRandomDistribution(k);
		}
		for (int i = 0; i < k; i++) {
			wordDistribution[i] = getRandomDistribution(w);
		}
		
		double prevLogLikelihood = Double.NEGATIVE_INFINITY;
		for (int iter = 0; iter < 500; iter++) {
			// E step
			for (int wi = 0; wi < w; wi++) {
				for (int di = 0; di < d; di++) {
					double probNonBg = 0;
					for (int ki = 0; ki < k; ki++) {
						probWordInDocFromTopic[wi][di][ki] = docTopicCoverage[di][ki] * wordDistribution[ki][wi];
						probNonBg += probWordInDocFromTopic[wi][di][ki];
					}
					normalize(probWordInDocFromTopic[wi][di]);
					
					probNonBg *= 1 - lambda;
					probWordInDocFromBg[wi][di] = lambda * bgWordDistribution[wi];
					if (probWordInDocFromBg[wi][di] != 0) {
						probWordInDocFromBg[wi][di] /= (lambda * bgWordDistribution[wi] + probNonBg);
						assert(!Double.isNaN(probWordInDocFromBg[wi][di]));
					}
				}
			}
			
			// M step
			for (int di = 0; di < d; di++) {
				for (int ki = 0; ki < k; ki++) {
					docTopicCoverage[di][ki] = 0;
					for (int wi = 0; wi < w; wi++) {
						docTopicCoverage[di][ki] += wordCounts[di][wi] * (1 - probWordInDocFromBg[wi][di]) * probWordInDocFromTopic[wi][di][ki];
					}	
				}
				normalize(docTopicCoverage[di]);
			}	
			
			for (int ki = 0; ki < k; ki++) {
				for (int wi = 0; wi < w; wi++) {
					wordDistribution[ki][wi] = 0;
					for (int di = 0; di < d; di++) {
						wordDistribution[ki][wi] += wordCounts[di][wi] * (1 - probWordInDocFromBg[wi][di]) * probWordInDocFromTopic[wi][di][ki];
					}
				}
				normalize(wordDistribution[ki]);
			}
			
			double logLikelihood = 0;
			for (int di = 0; di < d; di++) {
				double docLogLikelihood = 0;
				for (int wi = 0; wi < w; wi++) {
					double wordLogLikelihood = lambda * bgWordDistribution[wi];
					for (int ki = 0; ki < k; ki++) {
						wordLogLikelihood += (1 - lambda) * docTopicCoverage[di][ki] * wordDistribution[ki][wi];
					}
					if (wordLogLikelihood != 0) {
						wordLogLikelihood = Math.log(wordLogLikelihood);						
					}
					docLogLikelihood += wordCounts[di][wi] * wordLogLikelihood;
				}
				logLikelihood += docLogLikelihood;
			}
			if (logLikelihood - prevLogLikelihood < 1) {
				System.out.println("Topic analysis completed in " + iter + " iterations.");
				System.out.println(indexReader.document(1).get("Title"));
				int argMaxCoverage = argmax(docTopicCoverage[1]);
				int[] distributionSorted = argsort(wordDistribution[argMaxCoverage]);
				int len = distributionSorted.length;
				for (int i = 0; i < 20; i++) {
					System.out.println(words[distributionSorted[len-i-1]]);					
				}
				return;
			}
			prevLogLikelihood = logLikelihood;
		}
	}
	
	private void initBgDistribution(int w, Path bgFilePath) throws IOException {
		List<String> bgEntries = Files.readAllLines(bgFilePath);
		HashMap<String, Long> bgWordCounts = new HashMap<String, Long>();
		for (String entry : bgEntries) {
			String word = entry.split("\t")[0].toLowerCase();
			long freq = Long.parseLong(entry.split("\t")[1]);
			bgWordCounts.put(word, freq);
		}
		
		bgWordDistribution = new double[w];
		for (int i = 0; i < w; i++) {
			if (bgWordCounts.get(words[i]) != null)
			{
				bgWordDistribution[i] = bgWordCounts.get(words[i]);
			}
		}
		normalize(bgWordDistribution);
	}
	
	private double[] getRandomDistribution(int size) {
		double[] ret = new double[size];
		Random rd = new Random();
		for (int i = 0; i < size; i++) {
			ret[i] = rd.nextDouble();
		}
		normalize(ret);
		return ret;
	}
	
	private void normalize(double[] vec) {
		double sum = 0;
	    for (double val : vec) {
	        sum += val;
	    }
	    if (sum != 0) {
	    	for (int i = 0; i < vec.length; i++) {
	    		vec[i] = vec[i] / sum;
	    	}
	    }
	}
	
	private int argmax(double[] vec) {
		int arg = -1;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < vec.length; i++) {
			if (vec[i] > max) {
				arg = i;
				max = vec[i];
			}
		}
		return arg;
	}
	
	private int[] argsort(double[] vec) {
		Double[][] vecPair = new Double[vec.length][2];
		for (int i = 0; i < vec.length; i++) {
			vecPair[i][0] = vec[i];
			vecPair[i][1] = (double) i;
		}
		Arrays.sort(vecPair, new Comparator<Double[]>() {
			public int compare(Double[] a, Double[] b) {
				return Double.compare(a[0], b[0]);
			}
		});
		
		int[] ret = new int[vec.length];
		for (int i = 0; i < vec.length; i++) {
			ret[i] = vecPair[i][1].intValue();
		}
		return ret;
	}
	
	private void debugPrint(double[] vec) {
		DecimalFormat df = new DecimalFormat("0.000");
		for (double val : vec) {
	        System.out.print(df.format(val) + ", ");
	    }
	}
}
