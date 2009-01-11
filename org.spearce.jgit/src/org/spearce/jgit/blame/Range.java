package org.spearce.jgit.blame;

/**
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 * 
 */
public class Range {
	int start;

	int length;

	Range(int start, int length) {
		this.start = start;
		this.length = length;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + length;
		result = prime * result + start;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Range other = (Range) obj;
		if (length != other.length)
			return false;
		if (start != other.start)
			return false;
		return true;
	}

}