package org.spearce.jgit.blame;


/**
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 *
 */
class CommitOrigin implements IOrigin {		
	CommitOrigin parent;
	public CommitOrigin(CommitOrigin parent) {
		super();
		this.parent = parent;
	}
	
	public Object[] getData() {
		return null;
	}

	public IOrigin[] getParents() {
		return new IOrigin[0];
	}
	
	
}