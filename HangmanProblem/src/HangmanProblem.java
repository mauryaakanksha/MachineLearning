
/**
 * @author Akanksha
 *
 */
import java.io.*;
import java.util.*;


public class HangmanProblem {

	/**
	 * @param args
	 */
	private static int WORD_LENGTH =5;
	private static int ALPHABETS = 26;
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//FileReader wordListFile = new FileReader("D:/UCSD/Spring2016/CSE250A/Homework#1/Homework#1/hw1_word_counts_05.txt");
		FileReader wordListFile = new FileReader("hw1_word_counts_05.txt");
		BufferedReader wordListReader = new BufferedReader(wordListFile);
		
		FileReader evidenceFile = new FileReader("evidence.txt");
		BufferedReader evidenceReader = new BufferedReader(evidenceFile);
		
		try {
            String line ="";
            Double totalCount =0.0;
            ArrayList<Integer> wordCount = new ArrayList<Integer>();
            ArrayList<Double> wordProbability = new ArrayList<Double>();
            ArrayList<String> wordList = new ArrayList<String>();
            Map<Integer, Set<Integer>> wordIdxMap = new HashMap<> ();

            
            while ((line = wordListReader.readLine()) != null) {
            	String[] str = line.split(" ");
            	String word = str[0];
            	
            	if( word.length() != WORD_LENGTH) {
            		continue;
            	}
            	
            	Integer count = Integer.parseInt(str[1]);
            	
            	wordList.add(word);
            	wordCount.add(count);
            	totalCount += count;            	

            }
            
            // Calculate the probability of each word and store.
            // Store the probability values and the index of word 
            // corresponding to that probability
            for (int i =0; i < wordCount.size(); i++) {
            	Integer count = wordCount.get(i);
            	Double prob = wordCount.get(i)/totalCount;            	
            	wordProbability.add(prob);
            	
            	if ( wordIdxMap.containsKey(count)) {
            		wordIdxMap.get(count).add(i);           		
            	} else {            		
            		Set<Integer> idxSet = new HashSet<Integer>();
            		idxSet.add(i);
            		wordIdxMap.put(count,idxSet);
            	}          	
            }
            
            // sort the word count
            wordCount.sort(null);
            
            //print 5 most frequently used words            
            System.out.println("10 most frequently used words");
            int k =0;
            int size = wordCount.size();
            while (k < 10) {
            	int cnt = wordCount.get(size - 1 - k);
            	
            	if ( wordIdxMap.containsKey(cnt)) {
            		Set<Integer> idxSet = wordIdxMap.get(cnt);    
            		for (Integer index : idxSet) {
            			System.out.println(wordList.get(index) + " " + String.valueOf(cnt));
            			k++;
            		}            		
            	}
            }
            
            System.out.println("10 least frequently used words");
            k =0;
            
            while (k < 10) {
            	int cnt = wordCount.get(k);
            	
            	if ( wordIdxMap.containsKey(cnt)) {
            		Set<Integer> idxSet = wordIdxMap.get(cnt);    
            		for (Integer index : idxSet) {
            			System.out.println(wordList.get(index) + " " + String.valueOf(cnt));
            			k++;
            		}            		
            	}
            }
          

            String evidence;
            while ((evidence = evidenceReader.readLine()) != null) {
            	String[] str = evidence.split(";");
            	String correctlyGuessd = str[0].substring(1,  str[0].length() -1);
            	String incorrectlyGuessd = str[1].substring(1,  str[1].length() -1);
            	String[] incorrectAlphabets  = incorrectlyGuessd.split(",");

            	Double[] letterProbability = new Double[ALPHABETS];
            	String[] alphabets = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
            	
            	for ( int i =0 ; i < ALPHABETS; i++) {
            		
            		String letter = alphabets[i];           		
            		if (Arrays.asList(incorrectAlphabets).contains(letter)|| correctlyGuessd.contains(letter)){
            			letterProbability[i] = 0.0;   
            			continue;
            		} 
            		
            		Double predictiveProbability =0.0;           		
        			Double denominator = 0.0;       			
        			
        			for ( int w_Idx =0 ; w_Idx < wordList.size(); w_Idx++) {       				
        				String word_1 = wordList.get(w_Idx);            				
            			Double prob = 1.0;
            			
            			for ( int c1 =0; c1 <correctlyGuessd.length(); c1++){ 
             				if ( correctlyGuessd.charAt(c1) != '-' ){
            					if ( correctlyGuessd.charAt(c1) != word_1.charAt(c1)) {
            						prob = 0.0;
            					    break;
            					}                				
            				} else {
            					if (Arrays.asList(incorrectAlphabets).contains(String.valueOf(word_1.charAt(c1))) ||
            							correctlyGuessd.contains(String.valueOf(word_1.charAt(c1)))) {
            						prob = 0.0;
            						break;
            					}
            				}
            			}
            			
            			if (prob == 0) {
            				continue;
            			}
            			
            			prob = wordProbability.get(w_Idx);
            			denominator += prob;        				
        			}
            		
            		for(int wordIdx = 0; wordIdx < wordList.size(); wordIdx++) {
            			
            			String word = wordList.get(wordIdx);
            			int probLetterGivenWord = 0;
 
            			for ( int c =0; c <correctlyGuessd.length(); c++){

            				if ( correctlyGuessd.charAt(c) == '-' && word.charAt(c) == letter.charAt(0) ) {
            					probLetterGivenWord =1;
            				} 
            			} 
            			

            			if ( probLetterGivenWord == 0) {
            				continue;
            			}
           			
            			Double numerator =1.0;
            			
            			for ( int c1 =0; c1 <correctlyGuessd.length(); c1++){
            				
            				if ( correctlyGuessd.charAt(c1) != '-' ){
            					if ( correctlyGuessd.charAt(c1) != word.charAt(c1)) {
            						numerator = 0.0;
            						break;
            					}
            					
            				} else {
            					        					
            					if (Arrays.asList(incorrectAlphabets).contains(String.valueOf(word.charAt(c1))) || 
            							correctlyGuessd.contains(String.valueOf(word.charAt(c1)))) {
            						numerator = 0.0;            		
            						break;
            					}			
                    			
            				}
            			}
            			
            			if ( numerator == 0.0) {
            				continue;
            			}
            			
            			numerator = wordProbability.get(wordIdx);
            			Double posteriorProbability = numerator/denominator;            			
            			predictiveProbability += probLetterGivenWord*posteriorProbability;
            		}
            	            	        	
            		letterProbability[i] = predictiveProbability;
            	}

            	Double maxProb = letterProbability[0];
            	int maxProbIdx = 0;
            	
            	for (int i = 0 ; i < ALPHABETS; i++) {
            		if (letterProbability[i] > maxProb ) {
            			maxProb = letterProbability[i];
            			maxProbIdx =i;
            		}
            	}
            	
            	System.out.println("Best Guess : " + alphabets[maxProbIdx] );
            	System.out.println("Probability  : " + maxProb );
            
            }
            
                        
		} catch (FileNotFoundException e) {
            System.out.println("File not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }

	}

}