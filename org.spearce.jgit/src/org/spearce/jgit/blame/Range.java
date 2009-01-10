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
}