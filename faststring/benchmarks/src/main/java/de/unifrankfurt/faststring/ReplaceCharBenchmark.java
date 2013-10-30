package de.unifrankfurt.faststring;

import static de.unifrankfurt.faststring.Constants.*;
import de.unifrankfurt.faststring.yabt.BeforeEveryCall;
import de.unifrankfurt.faststring.yabt.Benchmark;
import de.unifrankfurt.faststring.yabt.BenchmarkRunner;
import de.unifrankfurt.faststrings.core.ReplaceCharString;

public class ReplaceCharBenchmark {

	private String base;
	private ReplaceCharString base_;

	@BeforeEveryCall
	public void init() {
		base = REPLACE_BASE_STRING;
		base_ = new ReplaceCharString(REPLACE_BASE_STRING);
	}

	@Benchmark
	public String replaceCharNormal() {
		return base.replace(OLD_CHAR, NEW_CHAR);
	}

	@Benchmark
	public ReplaceCharString replaceCharOpt() {
		return base_.replace(OLD_CHAR, NEW_CHAR);
	}


	public static void main(String[] args) {
		BenchmarkRunner.start(ReplaceCharBenchmark.class);
	}
}