package org.spearce.jgit.blame;

/** Difference interface, represents a run of different lines between to objects (think files).
 *
 * This represents one entry in a diff, a complete diff between two files would
 * be a non-overlapping collection of IDifferences 
 * 
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 *
 */
public interface IDifference {
	
	/** starting index (0-based) of difference in file A (i.e. 0-based line number in a file)
	 * @return index of difference in file A
	 */
	int getStartA();
	
	/** ending index (0-based) of difference in file A (i.e. 0-based line number in a file)
	 * note: if this is -1, that means the section in B was not present in A
	 * @return index of difference in file A
	 */
	int getEndA();
	
	/** starting index (0-based) of difference in file B (i.e. 0-based line number in a file)
	 * @return index of difference in file B
	 */
	int getStartB();

	/** ending index (0-based) of difference in file B (i.e. 0-based line number in a file)
	 * note: if this is -1, that means the section in A was not present in B
	 * @return index of difference in file A
	 */
	int getEndB();
}
