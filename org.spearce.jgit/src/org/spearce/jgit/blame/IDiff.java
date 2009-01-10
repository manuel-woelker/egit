package org.spearce.jgit.blame;

/** Generalized diff interface
 * 
 * As there is not standard diff implementation for java, abstract it 
 * for the moment until a library is found that satisfies both technical
 * and license considerations
 * 
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 *
 */
public interface IDiff {
	/** performs a diff on the two arrays finding runs where they differ.
	 * @note the equals method for the compared objects in these arrays should be "sane"
	 * @param a first array to diff
	 * @param b second array to diff
	 * @return a list of differences between these two objects
	 */
	IDifference[] diff(Object[] a, Object[] b);
}