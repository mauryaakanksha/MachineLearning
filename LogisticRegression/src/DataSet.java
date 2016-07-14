import java.util.*;

import org.jscience.mathematics.vector.Float64Matrix;
import org.jscience.mathematics.vector.Float64Vector;

public class DataSet {
	public Float64Matrix train_x;
	public ArrayList<Float64Vector> imageSet = new ArrayList<Float64Vector>();
	public ArrayList<Integer> train_y = new ArrayList<Integer>();
	public void initializeMatrix()  {
		train_x = Float64Matrix.valueOf(imageSet);
	}

}
