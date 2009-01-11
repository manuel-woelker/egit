package org.spearce.jgit.blame;

import org.spearce.jgit.lib.ObjectId;

/**
 * Origin object representing the origin of a part of the file, usually there
 * should be one origin for each commit
 * 
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 * 
 */
public interface IOrigin {
	/**
	 * get the ObjectId of data, used for identifying identical versions
	 * 
	 * @return object id
	 */
	ObjectId getObjectId();

	/**
	 * get the state at the current revision
	 * 
	 * @return object data, for a file this should be an array of strings
	 *         containing the file lines
	 */
	Object[] getData();

	/**
	 * get parent origins for this origin, for commits these are the parent
	 * commits this is used to trace the ancestry of the file
	 * 
	 * @return collection of parent origins
	 */
	IOrigin[] getParents();
}