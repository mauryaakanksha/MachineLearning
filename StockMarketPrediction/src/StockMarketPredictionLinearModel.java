import java.io.*;
import org.jscience.mathematics.number.*;
import org.jscience.mathematics.vector.*;

public class StockMarketPredictionLinearModel {
	
	double stockDataIn2000[] = new double[249];
	double stockDataIn2001[] = new double[248];
	double estimatedCoefficients[] = new double[4];
	
	public void readData() {
		try {
			BufferedReader br = new BufferedReader (new FileReader ( "nasdaq00.txt"));
			String line = "";
			int index = 0;
			double val = 0d;

			while ((line = br.readLine()) != null) {
				val = Double.parseDouble(line);
				stockDataIn2000[index++] = val;
			}
	
			index = 0;
			br = new BufferedReader (new FileReader ( "nasdaq01.txt"));

			while ((line = br.readLine()) != null) {
				val = Double.parseDouble(line);
				stockDataIn2001[index++] = val;
			}

			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void calculateCoefficients() {
		LinearEquation[] eqns = new LinearEquation[4];
		
		for (int i=0; i<eqns.length; i++) {
			eqns[i]= new LinearEquation();
		}
	
		for(int t=4; t <stockDataIn2000.length; t++) {
			for(int i =0; i<eqns.length; i++) {
				
				eqns[i].constantTerm += stockDataIn2000[t-i-1]*stockDataIn2000[t];
				eqns[i].indexOfa1 += stockDataIn2000[t-i-1]*stockDataIn2000[t-1];
				eqns[i].indexOfa2 += stockDataIn2000[t-i-1]*stockDataIn2000[t-2];
				eqns[i].indexOfa3 += stockDataIn2000[t-i-1]*stockDataIn2000[t-3];
				eqns[i].indexOfa4 += stockDataIn2000[t-i-1]*stockDataIn2000[t-4];	
			}
		}
		Float64Vector rows[] = new Float64Vector[4];
		double rhs_values[] = new double[4];
		
		for(int i =0; i<eqns.length ; i++) {
			//System.out.println(eqns[i].indexOfa1 + eqns[i].indexOfa2+eqns[i].indexOfa3+eqns[i].indexOfa4);

			rows[i] = Float64Vector.valueOf(eqns[i].indexOfa1,eqns[i].indexOfa2,eqns[i].indexOfa3,eqns[i].indexOfa4);
		}
		
		Float64Matrix LHS_matrix = Float64Matrix.valueOf(rows[0], rows[1], rows[2], rows[3]);
		Float64Vector RHS_vector = Float64Vector.valueOf(eqns[0].constantTerm, eqns[1].constantTerm, eqns[2].constantTerm, eqns[3].constantTerm );
		
		Vector<Float64> solution = LHS_matrix.solve(RHS_vector);
		System.out.println("Solution: " + solution);
		
		for(int i =0; i<4; i++) {
			estimatedCoefficients[i]  = solution.get(i).doubleValue();
		}
		
	}
	
	public double calculateMeanSqaureError(double[] dataSet) {
		double sumSquaredDiff =0d, actualValue, estimatedValue, diff, squaredDiff;
		
		for(int t =4; t<dataSet.length; t++) {
			actualValue = dataSet[t];
			estimatedValue =0d;
			for(int j=1; j<=4; j++) {
				estimatedValue += dataSet[t-j]*estimatedCoefficients[j-1];
			}
			diff = estimatedValue - actualValue;
			squaredDiff = Math.pow(diff, 2);
			sumSquaredDiff += squaredDiff;
		}
		double meanSquaredError = sumSquaredDiff/(dataSet.length -4);
		return meanSquaredError;
	}
	
	public static void main (String args[]) {
		StockMarketPredictionLinearModel lm = new StockMarketPredictionLinearModel();
		lm.readData();
		lm.calculateCoefficients();
		double mseFor2000 = lm.calculateMeanSqaureError(lm.stockDataIn2000);
		double mseFor2001 = lm.calculateMeanSqaureError(lm.stockDataIn2001);
		System.out.println("Mean Squared Error (2000): " + String.format("%.2f", mseFor2000));
		System.out.println("Mean Squared Error (2001): " + String.format("%.2f", mseFor2001));
	}
}
