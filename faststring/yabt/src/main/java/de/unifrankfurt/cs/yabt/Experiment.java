package de.unifrankfurt.cs.yabt;

import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.ReflectionUtils.withAnnotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.apache.commons.math.stat.StatUtils;

public class Experiment<T> {

	private T benchmarkInstance;

	private Class<T> benchmarkClass;

	private Set<Method> beforeCall;
	private Set<Method> benchmarks;

	private Result result;


	public Experiment(Class<T> benchmarkClass) {
		this.benchmarkClass = benchmarkClass;

		instantiateClass();
		findMethods(benchmarkClass);

	}

	@SuppressWarnings("unchecked")
	private void findMethods(Class<T> benchmarkClass) {
		benchmarks = getAllMethods(benchmarkClass, withAnnotation(Benchmark.class));
		beforeCall = getAllMethods(benchmarkClass, withAnnotation(BeforeEveryCall.class));

	}

	public void runBenchmarkClass(int runs, int warmUpRuns, int measureRuns, int initRuns) {
		instantiateClass();
		result = new Result(runs, measureRuns);

		System.out.println("starting initial warm-up phase");

		for (Method m : benchmarks) {
			System.out.println("starting warm-up phase for benchmark " + m.getName());
			doWarmUp(warmUpRuns, m);
		}

		System.out.println("--- warm up is over " + initRuns + " test runs are started now...");

		for (int i = 0; i < initRuns; i++) {
			System.out.println("Starting test run " + i);
			for (Method m : benchmarks) {
				measure(measureRuns, m, true);
			}
		}

		System.out.println("--- test runs over " + runs + " measurement runs are started now...");

		for (int i = 0; i < runs; i++) {
			System.out.println("Starting run " + i);

			for (Method m : benchmarks) {

				System.out.println("starting measurement of " + m.getName());

				double[] results = measure(measureRuns, m);

				System.out.println("mean: " + StatUtils.mean(results) + ";  " + StatUtils.variance(results));

			}

		}
	}

	private double[] measure(int measureRuns, Method m) {
		return measure(measureRuns, m, false);
	}

	private double[] measure(int measureRuns, Method m, boolean soft) {
		double[] results = new double[measureRuns];

		GcWatcher watcher = new GcWatcher();

		do {
			watcher.reset();
			for (int i = 0; i < measureRuns; i++) {
				invokeBeforeCalls();

				results[i] = invokeBenchmark(m);
			}
			if (!soft && watcher.wasGcActive()) {
				System.err.println("GC run while runnig the benchmark, measurement will be repeated");
			}

		} while(!soft && watcher.wasGcActive());

		return results;

	}

	private void instantiateClass() {
		try {
			benchmarkInstance = benchmarkClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException(
					"given class could not be instantiated "
							+ "via default c'tor: " + benchmarkClass.getName(),	e);
		}
	}

	private void doWarmUp(int runs, Method m) {
		for (int i = 0; i < runs; i++) {
			invokeBeforeCalls();
			invokeBenchmark(m);
		}
	}

	private void invokeBeforeCalls() {
		try {
			for (Method beforeMethod : beforeCall) {
				beforeMethod.invoke(benchmarkInstance);
			}
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private long invokeBenchmark(Method m) {
		try {
			long before = System.nanoTime();

			m.invoke(benchmarkInstance);
			long measurement = System.nanoTime() - before;
			return measurement;
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new IllegalStateException(e);
		}

	}
}
