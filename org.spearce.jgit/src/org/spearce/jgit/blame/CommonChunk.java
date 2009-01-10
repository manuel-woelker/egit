package org.spearce.jgit.blame;

/**
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 * 
 */
class CommonChunk {
	int astart;

	int bstart;

	int length;

	public CommonChunk(int astart, int bstart, int length) {
		super();
		this.astart = astart;
		this.bstart = bstart;
		this.length = length;
	}
}