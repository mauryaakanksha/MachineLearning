import java.io.*;
import java.text.*;
import java.util.*;

import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Matrix;
import org.jscience.mathematics.vector.Float64Vector;

public class LogisticRegression {
	
	public DataSet trainingSet = new DataSet();
	public DataSet testSet = new DataSet();
	public Float64Vector estimatedWeightVector;
	
	public void readInput() {
		this.readFile("newTrain3.txt", trainingSet);
		this.readFile("newTrain5.txt", trainingSet);
		trainingSet.initializeMatrix();
		this.readFile("newTest3.txt", testSet);
		this.readFile("newTest5.txt", testSet);
		testSet.initializeMatrix();
	}
	
	private DataSet readFile(String filePath, DataSet data) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			
			int y=0;
			if(filePath.contains("Train5") || filePath.contains("Test5")){
				y=1;
			} 
			
			ArrayList<Float64Vector> imageSet = data.imageSet;
			ArrayList<Float64> outputList = new ArrayList<Float64>();
			
			while((line = br.readLine())!= null) {
				String arr[] = line.split(" ");
				ArrayList<Float64> image = new ArrayList<Float64>();
				for(int i =0; i<arr.length; i++) {
					image.add(Float64.valueOf(Double.parseDouble(arr[i])));
				}
				Float64Vector imageVector = Float64Vector.valueOf(image);
				imageSet.add(imageVector);
				data.train_y.add(y);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public double getLogLikelihood(DataSet ds, Float64Vector weightVector) {
		double logLikelihood =0d;
		int index =0;
		Float64Matrix imageDataSet = ds.train_x;
		
		for(int i =0; i<imageDataSet.getNumberOfRows(); i++) {
			Float64Vector x = imageDataSet.getRow(i);
			double dotProduct = x.times(weightVector).doubleValue();
			int y = ds.train_y.get(index++);
			double sigma1 = getSigma(dotProduct);
			double sigma2 = getSigma(dotProduct*-1);
			if(y==1){
				logLikelihood += (y*Math.log(sigma1));
			} else {
				logLikelihood += ((1-y)*Math.log(sigma2));
			}
		}
		return logLikelihood;
	}

	public Float64Vector getInitializedVector (int size, double initialValue) {
		List<Float64> arr = new ArrayList<Float64>();
		for(int i =0; i<size; i++) {
			arr.add(Float64.valueOf(initialValue));
		}
		Float64Vector v = Float64Vector.valueOf(arr);
		return v;
	}
	
	public Float64Matrix getNullMatrix(int row, int col) {
		double arr[][]= new double[row][col];
		for(int i =0; i<row; i++) {
			for(int j =0; j<col; j++){
				arr[i][j] = 0d;
			}
		}
		Float64Matrix v = Float64Matrix.valueOf(arr);
		return v;
	}
	
	public Float64Vector getGradient(DataSet ds, Float64Vector weightVector) {
		int index =0;
		Float64Matrix imageDataSet = ds.train_x;
		Float64Vector gradient = this.getInitializedVector(imageDataSet.getNumberOfColumns(), 0d);
		
		for(int i =0; i<imageDataSet.getNumberOfRows(); i++){
			Float64Vector x = imageDataSet.getRow(i);
			double dotProduct = x.times(weightVector).doubleValue();
			int y = ds.train_y.get(index++);
			double diff = y - getSigma(dotProduct);
			gradient = gradient.plus(x.times(diff));
		}
		return gradient;
	}
	
	public Float64Matrix getHessian(DataSet ds, Float64Vector weightVector) {
		int index =0;
		Float64Matrix imageDataSet = ds.train_x;
		int imageVectorLength = imageDataSet.getNumberOfColumns();
		Float64Matrix hessian = this.getNullMatrix(imageVectorLength, imageVectorLength);
		
		for(int  i=0; i<imageDataSet.getNumberOfRows(); i++) {
			Float64Vector x = imageDataSet.getRow(i);
			double dotProduct = x.times(weightVector).doubleValue();
			int y = ds.train_y.get(index++);
			double sigma1 = getSigma(dotProduct);
			double sigma2 = getSigma(-1*dotProduct);
			
			Float64Matrix a = Float64Matrix.valueOf(x);
			Float64Matrix b = Float64Matrix.valueOf(x).transpose();
			Float64Matrix c = b.times(a);
			Float64Matrix d = c.times(Float64.valueOf(sigma1*sigma2*-1));
			hessian = hessian.plus(d);
		}
		return hessian;
	}
	
	public Float64Vector getNextWeightVector (DataSet ds, Float64Vector weightVector) {
		Float64Vector nextWeightVector;
		Float64Matrix hessian = this.getHessian(ds, weightVector);
		Float64Vector gradient = this.getGradient(ds, weightVector);
		
		Float64Matrix a = hessian.inverse();
		Float64Vector b = a.times(gradient);
		nextWeightVector = weightVector.minus(b);
		return nextWeightVector;
	}
	
	public Float64Vector getNextWeightVectorUsingDescent(DataSet ds, Float64Vector weightVector) {
		Float64Vector nextWeightVector;
		Float64Vector gradient = this.getGradient(ds, weightVector);
		Float64Vector b = gradient.times(0.02/100);
		nextWeightVector = weightVector.plus(b);
		return nextWeightVector;
	}
	public Float64Vector iterateForWeightVector(DataSet ds, boolean useNewtons) {
		Float64Vector initialWeightVector = this.getInitializedVector(64, 0d);
		Float64Vector nextWeightVector, currentWeightVector;
		currentWeightVector = initialWeightVector;
		
		for( int i =1; i<30000; i++) {
			if(useNewtons) {
				nextWeightVector = getNextWeightVector(ds, currentWeightVector);
			} else {
				nextWeightVector = getNextWeightVectorUsingDescent(ds, currentWeightVector);
			}
			
			double logLikelihood = this.getLogLikelihood(ds, nextWeightVector);
			
			
			if ( (useNewtons && i<20) || (!useNewtons & i%100 ==0)) {
				System.out.println("Iteration " +i + ", log likelihood = " + String.format("%.8f", logLikelihood));
			} else if ( useNewtons && i>=20) {
				break;
			}
			
			currentWeightVector = nextWeightVector;		
		}
		return currentWeightVector;
	}
	
	public double calculateErrorRate(DataSet ds, Float64Vector weightVector) {
		int index =0;
		Float64Matrix imageDataSet = ds.train_x;
		int correct = 0;
		for(int i =0; i< imageDataSet.getNumberOfRows(); i++) {
			Float64Vector x = imageDataSet.getRow(i);
			double dotProduct = x.times(weightVector).doubleValue();
			int y = ds.train_y.get(index++);
			double sigma = getSigma(dotProduct);
			
			if(sigma>0.5) {
				if(y==1) correct++;
			} else {
				if(y==0) correct++;
			}
		}
		double correctRate = ((correct +0d)/imageDataSet.getNumberOfRows())*100;
		return (100 -correctRate);
	}
	public void printWeightVector(Float64Vector weightVector) {
		DecimalFormat decFormat = new DecimalFormat("0.0000");
		
		for(int i =0; i <weightVector.getDimension(); i++) {
			String output = decFormat.format(weightVector.get(i).doubleValue());
			System.out.print(String.format("%7s", output));
			System.out.print("  ");
			if((i+1)%8 == 0) System.out.println();
		}
	}
	
	public String getTruncatedFloatVector (Float64Vector x) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		
		for(int i =0; i<x.getDimension(); i++) {
			sb.append(Math.round(x.get(i).doubleValue()*10000)/10000.0);
			sb.append(", ");
		}
		sb.replace(sb.length()-2, sb.length(), "");
		sb.append("]");
		return sb.toString();
	}
	
	public double getSigma(double z) {
		return (1.0/(1+ Math.exp(-1*z)));
	}
	
	public static void main(String args[]) {
		LogisticRegression lr = new LogisticRegression();
		
		lr.readInput();
		lr.estimatedWeightVector = lr.iterateForWeightVector(lr.trainingSet, true);
		double errorRateTrainingSet = lr.calculateErrorRate(lr.trainingSet, lr.estimatedWeightVector);
		double errorRateTestSet =lr.calculateErrorRate(lr.testSet, lr.estimatedWeightVector);
		System.out.println();
		System.out.println("For trainig Set Error Rate = " + errorRateTrainingSet);
		System.out.println("For test Set Error Rate = " + errorRateTestSet);
		System.out.println();
		System.out.println("Print Weight Vector");
		System.out.println();
		lr.printWeightVector(lr.estimatedWeightVector);
		
	}
}

