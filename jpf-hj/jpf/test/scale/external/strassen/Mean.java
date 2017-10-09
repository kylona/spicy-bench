package scale.external.strassen;

import java.util.Collections;
import java.util.List;

public class Mean {

	public static final String execTimeOutputFormat = "%20s %20s: %9.3f ms \n";
	List<Double> execTimes;
	String name;

	Mean(String n, List<Double> t) {
		execTimes = t;
		name = n;
		Collections.sort(execTimes);
		execTimes.remove(execTimes.size() - 1);

		System.out.println("Execution - Summary: ");
		System.out.printf(execTimeOutputFormat, new Object[]{name, " Best Time", execTimes.get(0)});
		System.out.printf(execTimeOutputFormat, new Object[]{name, " Worst Time", execTimes.get(execTimes.size() - 1)});
		System.out.printf(execTimeOutputFormat, new Object[]{name, " Arith. Mean Time", arithmeticMean()});
		System.out.printf(execTimeOutputFormat, new Object[]{name, " Geo. Mean Time", geometricMean()});
		System.out.printf(execTimeOutputFormat, new Object[]{name, " Std. Dev Time", standardDeviation()});
		System.out.printf(execTimeOutputFormat, new Object[]{name, " Lower Confidence", confidenceLow()});
		System.out.printf(execTimeOutputFormat, new Object[]{name, " Higher Confidence", confidenceHigh()});
		System.out.printf(execTimeOutputFormat, new Object[]{name, " Error", confidenceHigh() - arithmeticMean()});

		System.out.println();
	}

	private Double arithmeticMean() {
		double sum = 0;

		for (int i = 0; i < execTimes.size(); i++) {
			final Double execTime = execTimes.get(i);
			sum += execTime;
		}

		return (sum / execTimes.size());
	}

	private Double geometricMean() {
		double prod = 1;

		for (int i = 0; i < execTimes.size(); i++) {
			final Double execTime = execTimes.get(i);
			prod *= execTime;
		}

		return Math.pow(prod, 1.0 / execTimes.size());
	}

	private Double standardDeviation() {
		double mean = arithmeticMean();

		double temp = 0;
		for (int i = 0; i < execTimes.size(); i++) {
			final Double execTime = execTimes.get(i);
			temp += ((mean - execTime) * (mean - execTime));
		}

		return Math.sqrt(temp / execTimes.size());
	}

	private Double confidenceLow() {
		double mean = arithmeticMean();
		standardDeviation();

		return mean - (1.96d * standardDeviation() / Math.sqrt(execTimes.size())); 
	}

	private Double confidenceHigh() {
		double mean = arithmeticMean();
		standardDeviation();

		return mean + (1.96d * standardDeviation() / Math.sqrt(execTimes.size())); 
	}

}

