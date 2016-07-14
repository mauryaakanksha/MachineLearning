import java.io.BufferedReader;
import java.io.FileReader;

public class EstimationMaximization {

	DataInstance[] dataSet = new DataInstance[267];
	double p[] = new double[23];
	int allZ[][] = new int[8388608][23];
	
	public void initializeDataSet() {
		try {
			for ( int i =0; i <this.dataSet.length; i++) {
				this.dataSet[i] = new DataInstance();
			}
			
			BufferedReader br = new BufferedReader(new FileReader("spectX.txt"));
			
			String line = "";
			int lineCount = 0;
			
			while((line = br.readLine()) != null) {
				String[] arr = line.split(" ");
				
				for(int i =0 ; i<arr.length; i++) {
					this.dataSet[lineCount].X[i] = Integer.parseInt(arr[i]);
				}
				lineCount++;
			}
			
			br = new BufferedReader(new FileReader("spectY.txt"));
			lineCount = 0;
			
			while((line = br.readLine()) != null) {
				this.dataSet[lineCount].Y = Integer.parseInt(line.trim());
				lineCount++;
			}
			
			for (int i =0; i< Math.pow(2, 23); i++) {
				String binary = Integer.toBinaryString(i);

				for ( int j =0 ; j <(23 - binary.length()); j++) {
					allZ[i][j] = 0;
				}
				
				for ( int j = 0 ; j <binary.length(); j++) {
					allZ[i][j+ 23 -binary.length()]  = Character.getNumericValue(binary.charAt(j));
				}		
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public double getProbYGivenZ(int y , int[] z) {
		int retVal = 0;
		for (int i =0; i<z.length; i++) {
			if(z[i] == 1) {
				retVal = 1;
			}
		}
		if( y ==1) return retVal;
		else return (1-retVal);
		
	}
	
	public double getProbZiGivenXi(int i , int zi, int xi) {
		double retVal = 0d;
		if( xi ==1) {
			retVal = this.p[i];
		}
		else retVal = 0;
		
		if( zi ==1) return retVal;
		else return (1-retVal);
	}
	
	
	public double getProbZGivenX(int x[], int z[]) {
		
		double probability = 1d;
		for( int i =0; i <x.length ; i++) {
			probability *= getProbZiGivenXi(i,z[i], x[i]);
		}
		return probability;
	}
	
	
	public double getProbYGivenX(int y , int[] x) {
		
		int []z = new int[23];
		double prob = 0d;
		
		for (int i =0; i< Math.pow(2, 23); i++) {
			
			for ( int j =0; j < 23; j++) {
				z[j] = allZ[i][j];
				
			}
			prob += getProbZGivenX(x,z)*getProbYGivenZ(y,z);
		
		}
		return prob;
		
	}
	
	public double getLogLikelihood(DataInstance[] dataSet) {
		double loglikelihood = 0d;
		int cnt = 0;
		for(DataInstance t:dataSet) {
			double prob = getProbYGivenX(t.Y, t.X);
			loglikelihood += Math.log(prob);
			if (( t.Y == 0  && (1-prob) >= 0.5) || (t.Y == 1 && prob <= 0.5)) {
				cnt++;
			}
		}
		System.out.println("  Mistakes M = " + cnt);
		return (loglikelihood/dataSet.length);
	}
	public double getProbZiXiGivenXandY(int i, int[]x, int y) {
		double numer = x[i]*y*p[i];
		double prod =1;
		
		for( int j =0; j <x.length; j++){
			prod *= Math.pow((1-p[j]), x[j]);
		}
		double denom = (1-prod);
		return numer/denom;
	}
	
	public void updateParameters() {
		double newP[] = new double[23];
		for(int i =0; i <p.length; i++){
			double numer = 0d;
			int sumXi =0;
			for( int j =0; j <dataSet.length; j++) {
				DataInstance t = dataSet[j];
				numer += getProbZiXiGivenXandY(i, t.X, t.Y);
				
				if( t.X[i] == 1) sumXi++;
			}
			newP[i] = numer/sumXi;
		}
		
		for(int i =0; i <p.length; i++) {
			p[i] = newP[i];
		}
		
	}
	public void iterateSolutions() {
		double x =0d;
		
		for ( int i = 0 ; i <= 256; i++) {
			
			if ( i > 0) x = Math.log(i)/Math.log(2);
			
			if( x%1 == 0) {
				System.out.println("Iteration " + i);
				System.out.println("  Log-Likelihood L = " + String.format("%.4f", this.getLogLikelihood(dataSet)));
				System.out.println();
			}
			this.updateParameters();
		}
	}
	
	public static void main(String args[]) {
		EstimationMaximization a = new EstimationMaximization();
		for( int i = 0 ; i< a.p.length ; i++) {
			a.p[i] = 2.0/23.0;
		}
		
		a.initializeDataSet();
		a.iterateSolutions();
	}
}
