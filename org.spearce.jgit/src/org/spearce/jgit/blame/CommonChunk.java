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

	@Override
	public String toString() {
		return String.format("Common <%d:%d  %d>", Integer.valueOf(astart),
				Integer.valueOf(bstart), Integer.valueOf(length));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + astart;
		result = prime * result + bstart;
		result = prime * result + length;
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
		CommonChunk other = (CommonChunk) obj;
		if (astart != other.astart)
			return false;
		if (bstart != other.bstart)
			return false;
		if (length != other.length)
			return false;
		return true;
	}

}