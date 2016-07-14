import java.io.PrintWriter;
import java.util.Random;


public class MaxLikelihood  {

	public static void main (String args[])throws Exception {
		
		Random rand = new Random();
		
		int bits[] = new int[10];
		
		int numberOfSamples =  400000;
		int n = 10;
		double alpha =0.25;
		int decimalNumber;
		int Z = 128;
		int k = 8;
		double probability, numerator =0d, denominator =0d;
		double ratios [] = new double[1000000];
		PrintWriter writer = new PrintWriter("likelihoodWeighting.txt", "UTF-8");

		for ( int sample =0 ;  sample < numberOfSamples ; sample++) {
			
			decimalNumber =0;
			probability =0.0;
			//generate a random number
			for ( int i =0 ;i < n ; i++) {	
				
				if ( rand.nextDouble() < 0.5) {
					bits[i] =1;
				} else {
					bits[i] =0;
				}

				decimalNumber += Math.pow(2, i)*bits[i];
			}

			probability = ((1-alpha)/(1+alpha))*(Math.pow(alpha, Math.abs(Z - decimalNumber)));

			if ( bits[k-1] == 1){
				numerator += 1*probability;
			}
			
			denominator += probability;

			double ratio = numerator/denominator;
			ratios[sample] = ratio;
			writer.println(ratio);
			
		}
		writer.close();	
			
	}
	
}
