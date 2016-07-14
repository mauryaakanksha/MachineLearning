
import java.io.*;
import java.util.*;

public class StatisticalLanguage {

	public Map<Integer, String> vocab = new LinkedHashMap<Integer, String> ();
	public Map<String, Integer> vocabIndex = new LinkedHashMap<String, Integer> ();

	public Map<Integer, Integer> wordCountMap = new LinkedHashMap <Integer, Integer> ();

	public Map<Integer, Double> unigram = new LinkedHashMap <Integer, Double> ();
	
	public Map<String, Double> bigram = new LinkedHashMap <String, Double> ();
	
	private void processInput() {
		
		try {
			BufferedReader br;
			String line;
			String arr[];
			
			br = new BufferedReader( new FileReader("vocab.txt"));			
			int count =1 ;			
			while (( line = br.readLine()) != null ) {
				arr = line.split("\t");
				vocab.put(count, arr[0].toUpperCase());
				vocabIndex.put(arr[0].toUpperCase(), count);
				count++;
			}
			br.close();
			
			
			br = new BufferedReader( new FileReader("unigram.txt"));
			count =1;			
			int totalWordCount =0;			
			while ((line = br.readLine()) != null){
				arr = line.split("\t");
				int wordCount = Integer.parseInt(arr[0]);
				wordCountMap.put(count, wordCount);
				totalWordCount += wordCount;
				count++;
			}			
			for (Integer wordIndex : wordCountMap.keySet()) {
				double prob = wordCountMap.get(wordIndex)/ (totalWordCount +0d);
				unigram.put(wordIndex, prob);
			}
			br.close();
			
			
			br = new BufferedReader( new FileReader("bigram.txt"));
			while ((line = br.readLine()) != null){
				arr = line.split("\t");
				int word1 = Integer.parseInt(arr[0]);
				int word2 = Integer.parseInt(arr[1]);
				String key = this.formKey(word1, word2);
				// prob (w2 | w1)
				int val = Integer.parseInt(arr[2]);
				bigram.put(key, val/(wordCountMap.get(word1) + 0d));							
			}	
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
 	}
	
	private String separator = "%";
	private String formKey(int v1, int v2) {
		return (v1 + separator + v2);
	}
	private int getVal( String key, int index) {
		return Integer.parseInt(key.split(separator)[index]);
	}
	
	private void solution() throws Exception{
		System.out.println("Problem (a)");		
		System.out.println();
		for(Integer wordIndex: unigram.keySet()) {
			String word = vocab.get(wordIndex);
			if(word.startsWith("B")){
				System.out.println("Word: " + word + ", Unigram Prob = " + unigram.get(wordIndex));
			}
		}
		System.out.println();
		System.out.println("======================================");
		System.out.println();
		System.out.println("Problem (b)");
		System.out.println();
		System.out.println("if first word is 'ONE', following word : ");
		TreeMap<Double, String> sortedWordList = new TreeMap<Double,String>();
		for ( String wordPair: bigram.keySet()) {
			Integer firstWordIndex = this.getVal(wordPair, 0);
			String firstWord = vocab.get(firstWordIndex);
			
			if(firstWord.equals("ONE")) {
				Integer secondWordIndex = this.getVal(wordPair, 1);
				String secondWord = vocab.get(secondWordIndex);
				sortedWordList.put(bigram.get(wordPair), secondWord);
			}
		}
		NavigableMap<Double,String> descendingMap = sortedWordList.descendingMap();
		int count =1;
		for(Double val: descendingMap.keySet()) {
			System.out.println("Word:" + descendingMap.get(val)+ ", Bigram Prob = " + val);
			if ( count >= 10) break;
			count++;
		}
		System.out.println();
		System.out.println("======================================");
		System.out.println();
		System.out.println("Problem (c)");
		System.out.println();
		System.out.println("Sentence: The stock market fell by one hundred points last week");
		String[] sentence1 = {"<s>","The","stock", "market", "fell", "by", "one", "hundred", "points", "last", "week"};
		double unigramLogC =0d, bigramLogC=0d;
		for(int i=1; i<sentence1.length;i++) {
			
			int wordIndex = vocabIndex.get(sentence1[i].toUpperCase());
			unigramLogC += Math.log(unigram.get(wordIndex));
			
			int prevWordIndex = vocabIndex.get(sentence1[i-1].toUpperCase());
			bigramLogC += Math.log(bigram.get(this.formKey(prevWordIndex, wordIndex)));
		}
		System.out.println("Unigram Prob = " + unigramLogC + ", Bigram Prob = " + bigramLogC);
		System.out.println();
		System.out.println("======================================");
		System.out.println();
		System.out.println("Problem (d)");
		System.out.println();
		System.out.println("Sentence: The fourteen officials sold fire insurance");
		String[] sentence2 = {"<s>", "The", "fourteen", "officials", "sold", "fire", "insurance"};
		double unigramLogD =0d, bigramLogD=0d;
		boolean bigramInfinity = false;
		for (int i=1; i<sentence2.length; i++) {
			int wordIndex = vocabIndex.get(sentence2[i].toUpperCase());
			double unigramProb = unigram.get(wordIndex);
			unigramLogD += Math.log(unigramProb);
			
			int prevWordIndex = vocabIndex.get(sentence2[i-1].toUpperCase());
			Double bigramProb = bigram.get(this.formKey(prevWordIndex, wordIndex));
			
			if ( bigramProb == null) {
				System.out.println("Bigram not observed: " + sentence2[i-1] + " " + sentence2[i]);
				bigramInfinity = true;
			} else {
				bigramLogD += Math.log(bigramProb);
			}
		}
		System.out.print("Unigram Prob = " + unigramLogD);
		if(!bigramInfinity) System.out.println(", Bigram Prob = " + bigramLogD);

		double probArray[][] = new double[6][2];
		for(int i=1; i<sentence2.length; i++){
			int wordIndex = vocabIndex.get(sentence2[i].toUpperCase());
			double unigramProb = unigram.get(wordIndex);
			
			int prevWordIndex = vocabIndex.get(sentence2[i-1].toUpperCase());
			Double bigramProb = bigram.get(this.formKey(prevWordIndex, wordIndex));
			
			if(bigramProb == null){
				bigramProb =0d;
			}
			System.out.println();
			System.out.println("Current word = " + sentence2[i] + " : Unigram = " + unigramProb + ", Bigram = " +bigramProb);
			probArray[i-1][0] = unigramProb;
			probArray[i-1][1] = bigramProb;
		}
		

		double lambda = 0.0;
		PrintWriter writer = new PrintWriter("plot.txt", "UTF-8");
		while (lambda <= 1.0) {
			double val = 0d;
			for( int i =0; i <probArray.length ; i++){
				val += Math.log(probArray[i][0]*(1-lambda) + probArray[i][1]*(lambda));
			}
			
			writer.println(String.valueOf(lambda)+ ", " + String.valueOf(val));
			
			lambda = lambda +0.001;
		}
		writer.close();
	}
	


	public static void main(String args[]) throws Exception{
		StatisticalLanguage sl = new StatisticalLanguage();
		sl.processInput();
		sl.solution();
	}
}


