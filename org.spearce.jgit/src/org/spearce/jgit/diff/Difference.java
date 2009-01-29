package org.spearce.jgit.diff;

/** Difference between two files
 * 
 * This represents a sequence of lines that are different between two files A and B 
 *
 */
public class Difference implements IDifference {
	final int startA;

	final int endA;

	final int startB;

	final int endB;

	/** Standard constructor
	 * @param startA start in file A (0-based line index)
	 * @param endA end in file A (0-based line index)
	 * @param startB start in file B (0-based line index)
	 * @param endB end in file B (0-based line index)
	 */
	public Difference(int startA, int endA, int startB, int endB) {
		super();
		this.startA = startA;
		this.endA = endA;
		this.startB = startB;
		this.endB = endB;
	}

	public int getStartA() {
		return startA;
	}

	public int getEndA() {
		return endA;
	}

	public int getStartB() {
		return startB;
	}

	public int getEndB() {
		return endB;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + endA;
		result = prime * result + endB;
		result = prime * result + startA;
		result = prime * result + startB;
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
		Difference other = (Difference) obj;
		if (endA != other.endA)
			return false;
		if (endB != other.endB)
			return false;
		if (startA != other.startA)
			return false;
		if (startB != other.startB)
			return false;
		return true;
	}

	public int getLengthA() {
		return endA < 0 ? 0 : endA - startA +1;
	}

	public int getLengthB() {
		return endB < 0 ? 0 : endB - startB +1;
	}

}
