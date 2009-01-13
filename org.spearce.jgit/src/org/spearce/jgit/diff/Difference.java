package org.spearce.jgit.diff;

/**
 *
 */
public class Difference implements IDifference {
	final int startA;

	final int endA;

	final int startB;

	final int endB;

	/**
	 * @param startA
	 * @param endA
	 * @param startB
	 * @param endB
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

}
