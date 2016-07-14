import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

public class ViterbiAlgorithm {

	private int numStates = 26;
	private int obsStates = 2;
	private int numObs = 150000;
	double smallDoubleValue = -1*Math.pow(2, 24);
	double initialState[] = new double [numStates];
	double emissionMatrix[][] = new double[numStates][obsStates];
	double transitionMatrix[][] = new double[numStates][numStates];
	
	int observations[] = new int [numObs];
	int prevState[][] = new int [numStates][numObs];
	double likelihood[][] = new double[numStates][numObs];
	
	int stateSequence [] = new int[numObs];
	char [] letters = { 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};

	public void initializeData() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("initialStateDistribution.txt"));
			
			String line ="";
			int index =0;
			while((line = br.readLine()) != null) {

				initialState[index++] = Math.log(Double.parseDouble(line));				
			}
			br.close();
			br = new BufferedReader(new FileReader("observations.txt"));
			index = 0;
			while((line = br.readLine()) != null) {
				
				String arr[] = line.split(" ");
				for(String obs: arr) {
					observations[index++] = Integer.parseInt(obs);
				}
			}
			br.close();
			
			br = new BufferedReader(new FileReader("transitionMatrix.txt"));
			int rowIndex = 0, columnIndex = 0;
			while((line = br.readLine()) != null) {				
				String arr[] = line.split(" ");
				columnIndex =0;
				for(String x: arr) {
					transitionMatrix[rowIndex][columnIndex] = Math.log(Double.parseDouble(x));
					columnIndex++;
				}
				rowIndex++;
			}
			br.close();
			
			br = new BufferedReader(new FileReader("emissionMatrix.txt"));
			rowIndex = 0;
			columnIndex = 0;
			while((line = br.readLine()) != null) {				
				String arr[] = line.split("\t");
				columnIndex =0;
				for(String x: arr) {
					emissionMatrix[rowIndex][columnIndex] = Math.log(Double.parseDouble(x));
					columnIndex++;
				}
				rowIndex++;
			}
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void computeLikelihood() {
		for (int t =0; t <numObs; t++) {
			for( int i=0; i<numStates; i++) {
				if( t == 0) {
					likelihood[i][t]= initialState[i]+ emissionMatrix[i][observations[t]];
				} else {
					double currentMaxLikelihood = smallDoubleValue;
					int currentPrevState = -1;
					double temp = 0d;
					for( int k =0; k <numStates; k++) {
						temp = likelihood[k][t-1] + transitionMatrix[k][i];
						if (temp > currentMaxLikelihood ){
							currentMaxLikelihood = temp;
							currentPrevState = k;
						}
					}
					
					currentMaxLikelihood += emissionMatrix[i][observations[t]];
					likelihood[i][t] = currentMaxLikelihood;
					prevState[i][t] = currentPrevState;
				}
			}
		}
	}
	
	public char getLetter(int state) {
		return letters[state];
	}
	
	public void printStateSequence() {
		try {
			StringBuffer sb = new StringBuffer();
			PrintWriter writer = new PrintWriter("stateSequence.txt", "UTF-8");
			int currentState = stateSequence[1];
			for(int i =1; i<stateSequence.length; i++) {
				writer.println(stateSequence[i]);
				if( stateSequence[i] != currentState) {
					sb.append(getLetter(currentState));
					currentState = stateSequence[i];
				}
			}
			sb.append(getLetter(currentState));
			String message = sb.toString();
			System.out.println(message);
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	public void computeStateSequence() {
		double finalLikelihood = smallDoubleValue;
		int finalState = -1;
		for(int i =0; i <numStates; i++) {
			if(likelihood[i][numObs-1] > finalLikelihood) {
				finalLikelihood = likelihood[i][numObs -1];
				finalState = i;
			}
		}
		stateSequence[numObs -1] = finalState;
		for(int j = numObs -2 ; j >=1 ; j--) {
			stateSequence[j] = prevState[stateSequence[j+1]][j+1];
		}
	}
	public static void main(String args[]) {
		ViterbiAlgorithm a = new ViterbiAlgorithm();

		a.initializeData();
		a.computeLikelihood();
		a.computeStateSequence();
		a.printStateSequence();
	}
	
}
