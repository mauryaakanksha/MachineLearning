import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Matrix;
import org.jscience.mathematics.vector.Float64Vector;

public class ValueIteration {

	List<double[]> valueFuncIterations = new ArrayList<double[]>();
	double reward[] = new double[81];
	int policyFromOptimalValue[] = new int [81];
	double discountFactor = 0.9875d;
	Map<String,Double> transitionProb = new HashMap<String, Double>();
	
	int[] mazeStates = {3,11,12,15,16,17,20,22,23,24,26,29,30,31,34,35,39,43,48,52,53,56,57,58,59,60,61,62,66,70,71,79};
	int[] dragonStates = {47,49,51,65,67,69};
	
	List<Integer> mazeStatesList = new ArrayList<Integer>();
	List<Integer> dragonStatesList = new ArrayList<Integer>();
	
	public void readStatesList(){
		for(int i =0; i<mazeStates.length; i++){
			mazeStatesList.add(mazeStates[i]);
		}
		for(int i =0; i<dragonStates.length; i++) {
			dragonStatesList.add(dragonStates[i]);
		}
	}
	
	public void initializeInput() {
		this.readTransitionProb("prob_a1.txt",1);
		this.readTransitionProb("prob_a2.txt",2);
		this.readTransitionProb("prob_a3.txt",3);
		this.readTransitionProb("prob_a4.txt",4);
		this.readRewardFunction("rewards.txt");
		this.doSanityCheckTransition();
		this.readStatesList();
	}
	
	public void doSanityCheckTransition() {
		for ( int action =1; action <=4 ; action++) {
			for ( int i =1; i<=81; i++){
				double rowSum = 0d;
				for(int j =1; j<=81; j++) {
					Double prob = this.transitionProb.get(this.getkey(i,j,action));
					if(prob != null) rowSum += prob;
				}
				if((rowSum - 1d) > Math.exp(-5)) System.out.println("row prob != 1");
			}
		}
	}
	
	public String getkey(int s, int new_s, int action) {
		return s + "|" + new_s + "|" + action;
	}
	
	public void readTransitionProb(String filePath, int action) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = "";
			while (( line = br.readLine()) != null) {
				String arr[] = line.split("  ");
				this.transitionProb.put(this.getkey(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), action), Double.parseDouble(arr[2]));
			}	
			br.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readRewardFunction(String filePath) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = "";
			int index = 0;
			while (( line = br.readLine()) != null) {
				this.reward[index++] = Integer.parseInt(line);
			}		
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public double getSummation(int state, int action, double[] valueFunction) {
		
		double sum =0d;

		for ( int newState = 1; newState <=81; newState++) {
			Double prob = this.transitionProb.get(this.getkey(state, newState, action));
			if ( prob != null) sum += prob * valueFunction[newState - 1];

		}
		return sum;
	}
	
	public void iterateValueFunction() {
		double[] currValueFunc = valueFuncIterations.get(valueFuncIterations.size()-1);
		double[] newValueFunc = new double[81];
		
		for ( int state =1 ; state <= 81; state++){
			double currentSum = -100;
			for( int action = 1; action <=4; action++) {
				double sum = this.getSummation(state, action, currValueFunc);

				if( sum > currentSum) currentSum = sum;
			}
			
			newValueFunc[state-1] = this.reward[state - 1] + discountFactor*currentSum;

		}
		valueFuncIterations.add(newValueFunc);
	}
	
	public boolean isValueFunctionOptimal() {
		int i = valueFuncIterations.size()-1;
		int j = i-1;
		if( j >= 0) {
			double[] currValFunc = valueFuncIterations.get(i);
			double[] prevValFunc = valueFuncIterations.get(j);
			double diff = 0d;
			for(int k =0; k<81; k++) {
				diff += Math.pow(currValFunc[k] - prevValFunc[k], 2);
			}
			
			if( diff > Math.exp(-30)) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPolicyFunctionalOptimal(int[] oldPolicy, int[] newPolicy) {
		double diff = 0d;
		for ( int k =0; k< 81; k++) {
			diff += Math.pow(newPolicy[k]- oldPolicy[k], 2);
		}
		System.out.println(" Squared diff b/w policied = " + diff);
		if(diff > Math.exp(-20)) return false;
		else {
			return true;
		}
	}
	
	public void computeOptimumValueFunction() {
		double[] initialValueFunc = new double[81];
		valueFuncIterations.add(initialValueFunc);
		
		while (!isValueFunctionOptimal()) {
			this.iterateValueFunction();
		}
		this.verifyValueFunction(valueFuncIterations.get(valueFuncIterations.size()-1));
	}
	
	public int[] computePolicyFromValueFn(double[] valueFunc) {
		
		int[] policy = new int[81];
		for(int state =1; state <=81; state++) {
			double currentSum = -100;
			int currentAction = -1;
			
			for( int action =1; action <=4; action++) {
				double sum = this.getSummation(state, action, valueFunc);
				if(sum>currentSum) {
					currentSum = sum;
					currentAction = action;
				}
			}
			policy[state-1] = currentAction;
		}
		return policy;
	}
	
	public void verifyValueFunction( double[] valueFunction) {
		for(int i =1; i<=81; i++) {
			if(mazeStatesList.contains(i)) {
				assert(valueFunction[i-1]>0);
			} else if (dragonStatesList.contains(i)) {
				assert(valueFunction[i-1]<0);
			}
		}
	}
	
	public void printPolicyAndValueFunc(double[] valueFunc, int[] policy) {
		
		System.out.format("%n%n%5s %4s %6s%n", "state","V", "Policy");
		System.out.println("============ Maze States ===========");
		
		for( int i =1; i<=81; i++) {
			if(mazeStatesList.contains(i)) {
				System.out.format("%5d %.2f %6d%n", i, valueFunc[i-1], policy[i-1]);
			}
		}
		
		System.out.println("============ Dragons ===========");	
		for( int i =1; i<=81; i++) {
			if(dragonStatesList.contains(i)) {
				System.out.format("%5d %.2f %6d%n", i, valueFunc[i-1], policy[i-1]);
			}
		}
	}
	
	public void computePolicyIteratively() {
		int[] startPolicy = new int[81];
		for(int i=0; i <81; i++) {
			startPolicy[i] = 1;   // WEST
			//startPolicy[i] = 3;   // EAST
		}
		
		int[] policy = startPolicy;
		double[] valueFn; int counter =1;
		while (true) {
			valueFn = this.evaluateValueFnFromPolicy(policy);
			int[] newPolicy = this.computePolicyFromValueFn(valueFn);
			System.out.println("Iteration " + counter++);
			if( isPolicyFunctionalOptimal(policy, newPolicy)) {
				break;
			} else {
				policy = newPolicy;
			}	
		}
		this.printPolicyAndValueFunc(valueFn, policy);
	}
	
	public double[] evaluateValueFnFromPolicy(int[] policy) {
		double[][] probArr = new double[81][81];
		for(int state =1; state <= 81; state++) {
			for(int newState =1 ; newState <= 81; newState++) {
				int action = policy[state-1];
				Double prob = this.transitionProb.get(this.getkey(state, newState, action));
				if(prob != null ) probArr[state-1][newState-1] = prob;
			}
		}
		
		double[][] idenArr = new double[81][81];
		for(int state =1; state <= 81; state++) {
			for(int newState =1; newState <= 81; newState++){
				if(state==newState) {
					idenArr[state-1][state-1] = 1d;
				}
			}
		}
		
		Float64Matrix probMatrix = Float64Matrix.valueOf(probArr);
		Float64Matrix idenMatrix = Float64Matrix.valueOf(idenArr);
		
		Float64Vector rewardVect = Float64Vector.valueOf(this.reward);
		Float64Vector valueFnVector = (idenMatrix.minus(probMatrix.times(Float64.valueOf(discountFactor))).inverse()).times(rewardVect);
		
		double valueFn[] = new double[81];
		for(int i =0; i< valueFnVector.getDimension(); i++){
			valueFn[i] = valueFnVector.get(i).doubleValue();
		}
		return valueFn;
	}
	
	public static void main(String args[]) {
		ValueIteration obj = new ValueIteration();
		obj.initializeInput();
		
		obj.computeOptimumValueFunction();
		double optimalValueFn[] = obj.valueFuncIterations.get(obj.valueFuncIterations.size()-1);
		obj.policyFromOptimalValue = obj.computePolicyFromValueFn(optimalValueFn);
		
		System.out.println("=======Part(a),(b)-  Using value iteration =========");
		obj.printPolicyAndValueFunc(optimalValueFn, obj.policyFromOptimalValue);
		
		System.out.println("===============================");
		System.out.println("=======Part(c)- Using policy iteration =========");
		obj.computePolicyIteratively();
	}
}
	
