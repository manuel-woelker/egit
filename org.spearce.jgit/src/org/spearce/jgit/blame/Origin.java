/*
 * Copyright (C) 2008, Manuel Woelker <manuel.woelker@gmail.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Git Development Community nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.spearce.jgit.blame;

import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.lib.ObjectLoader;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.lib.TreeEntry;
import org.spearce.jgit.revwalk.RevCommit;
import org.spearce.jgit.revwalk.RevTree;

/**
 * Origin object representing the origin of a part of the file, usually there
 * should be one origin for each commit and path
 * 
 * 
 */
public class Origin {

	final RevCommit commit;

	/**
	 * @return the associated commit
	 */
	public RevCommit getCommit() {
		return commit;
	}

	final String filename;

	private final Repository repository;

	/**
	 * creates a new Commit origin for a given commit and path
	 * 
	 * @param repository
	 *            git repository for this origin
	 * @param commit
	 *            the commit object for this origin
	 * @param filename
	 *            the path of the file in this commit
	 */
	public Origin(Repository repository, RevCommit commit, String filename) {
		super();
		this.repository = repository;
		this.commit = commit;
		this.filename = filename;
	}

	/**
	 * get the ObjectId of the file, used for identifying identical versions
	 * 
	 * @return object id
	 */
	public ObjectId getObjectId() {
		try {
			RevTree revTree = commit.getTree();
			TreeEntry blobEntry = repository.mapTree(revTree).findBlobMember(
					filename);
			if (blobEntry == null) {
				return ObjectId.zeroId();
			}
			return blobEntry.getId();
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving data for origin "
					+ this, e);
		}
	}

	/**
	 * get the file contents at this commit
	 * 
	 * @return an array of strings containing the file lines
	 */
	public byte[] getBytes() {
		try {
			RevTree revTree = commit.getTree();
			TreeEntry blobEntry = repository.mapTree(revTree).findBlobMember(
					filename);
			if (blobEntry == null) {
				// does not exist yet
				return new byte[0];
			}
			ObjectLoader objectLoader = repository.openBlob(blobEntry.getId());
			return objectLoader.getBytes();
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving data for origin "
					+ this);
		}
	}

	@Override
	public String toString() {
		return filename + " --> " + commit;
	}

}