package de.unifrankfurt.faststrings.core;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReplaceRegexString implements CharSequence {

	private SubstringString string;

	private char[] appendix;

	private int size;

	public ReplaceRegexString(String value) {
		this(new SubstringString(value));
	}

	public ReplaceRegexString(SubstringString string) {
		this.string = string;
		size = string.charArray().length;
	}

	@Override
	public int length() {
		return string.charArray().length;
	}

	@Override
	public char charAt(int index) {
		return string.charArray()[index];
	}

	public String replaceAll(String regex, SubstringString replacement) {
		Matcher matcher = Pattern.compile(regex, Pattern.LITERAL).matcher(this);

		// should do the same as java.regex.Matcher.replaceAll(String)
		boolean result = matcher.find();

		int pos = 0;

        if (result) {
            char[] array = string.charArray();
			StringBuffer sb = new StringBuffer(array.length);
            do {
            	appendFromArray(sb, pos, matcher.start());
            	sb.append(replacement);
            	pos = matcher.end();
            } while (matcher.find());

            appendFromArray(sb, pos, array.length);

            copyFromBuffer(sb);
        }

		return this.toString();
	}

	private void copyFromBuffer(StringBuffer sb) {
		char[] array = string.charArray();
		int length = array.length;

		if (sb.length() > length) {
			appendix = new char[sb.length() - array.length];
			size = -1;
		} else {
			size = sb.length();
		}

		for (int i = 0; i < sb.length(); i++) {
			if (i < length) {
				array[i] = sb.charAt(i);
			} else {
				appendix[i - length] = sb.charAt(i);
			}
		}
	}

	private void appendFromArray(StringBuffer sb, int start, int end) {
		for (int i = start; i < end; i++) {
			sb.append(string.charArray()[i]);
		}

	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return new ReplaceRegexString(string.substring(start, end));
	}

	@Override
	public String toString() {
		char[] array = string.charArray();
		StringBuilder sb;
		if (size == -1) {
			sb = new StringBuilder(array.length + appendix.length);
			sb.append(array);
			sb.append(appendix);
		} else {
			sb = new StringBuilder(size);
			sb.append(array, 0, size);
		}
		return sb.toString();
	}
}
