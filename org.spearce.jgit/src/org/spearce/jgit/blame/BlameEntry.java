package org.spearce.jgit.blame;

/**
 * Blame entry, associating line numbers in the blamed file with origins The
 * output of blame is a list of these, covering all lines
 * 
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 * 
 */
public class BlameEntry {
	/**
	 * line range in the original file
	 */
	public Range originalRange;

	/**
	 * origin to blame for the lines in the original range
	 */
	public IOrigin suspect;

	/**
	 * start line number in the suspects version
	 */
	public int suspectStart;

	/**
	 * indicates if the suspect has been found guilty this is initially false -
	 * in dubio pro reo
	 */
	public boolean guilty = false;

	@Override
	public String toString() {

		return String.format("(%d -> %d,  %d)", Integer
				.valueOf(originalRange.start), Integer.valueOf(suspectStart),
				Integer.valueOf(originalRange.length));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (guilty ? 1231 : 1237);
		result = prime * result
				+ ((originalRange == null) ? 0 : originalRange.hashCode());
		result = prime * result + ((suspect == null) ? 0 : suspect.hashCode());
		result = prime * result + suspectStart;
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
		BlameEntry other = (BlameEntry) obj;
		if (guilty != other.guilty)
			return false;
		if (originalRange == null) {
			if (other.originalRange != null)
				return false;
		} else if (!originalRange.equals(other.originalRange))
			return false;
		if (suspect == null) {
			if (other.suspect != null)
				return false;
		} else if (!suspect.equals(other.suspect))
			return false;
		if (suspectStart != other.suspectStart)
			return false;
		return true;
	}

}