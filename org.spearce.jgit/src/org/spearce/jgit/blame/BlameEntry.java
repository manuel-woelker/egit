package org.spearce.jgit.blame;

/**Blame entry, associating line numbers in the blamed file with origins
 * The output of blame is a list of these, covering all lines
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 * 
 */
public class BlameEntry {
	/**
	 *  line range in the original file
	 */
	public Range originalRange;

	/**
	 *  origin to blame for the lines in the original range
	 */
	public IOrigin suspect;

	/**
	 * start line number in the suspects version
	 */
	public int suspectStart;

	/**
	 *  indicates if the suspect has been found guilty
	 *  this is initially false - in dubio pro reo
	 */
	public boolean guilty = false;

	@Override
	public String toString() {
		
		return String.format("(%d -> %d,  %d)", Integer.valueOf(originalRange.start), Integer.valueOf(suspectStart), Integer.valueOf(originalRange.length));
	}
	
	
}