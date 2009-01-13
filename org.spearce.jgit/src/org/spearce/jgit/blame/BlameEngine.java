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

import java.util.List;

import org.spearce.jgit.diff.impl.wicket.WicketDiffImpl;
import org.spearce.jgit.lib.Constants;
import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.revwalk.RevCommit;
import org.spearce.jgit.revwalk.RevSort;
import org.spearce.jgit.revwalk.RevWalk;

/**
 * 
 */
public class BlameEngine {
	private Repository repository;

	private final RevWalk revWalk;

	/**
	 * @param repository
	 */
	public BlameEngine(final Repository repository) {
		this(new RevWalk(repository));
	}

	/**
	 * @param revWalk
	 */
	public BlameEngine(RevWalk revWalk) {
		this.revWalk = revWalk;
		this.repository = revWalk.getRepository();
	}

	/**
	 * @param path
	 *            file path
	 * @return list of blame entries linking line numbers to originating commit
	 */
	public List<BlameEntry> blame(String path) {
		try {
			ObjectId headId = repository.resolve(Constants.HEAD);
			RevCommit lastCommit = revWalk.parseCommit(headId);
			return blame(lastCommit, path);
		} catch (Exception e) {
			throw new RuntimeException("Internal error in BlameEngine", e);
		}
	}

	/**
	 * @param commit
	 *            commit from which to start the blame process
	 * @param path
	 *            file path
	 * @return list of blame entries linking line numbers to originating commit
	 */
	public List<BlameEntry> blame(RevCommit commit, String path) {
		try {
			Origin finalOrigin = new Origin(repository, commit, path);
			revWalk.sort(RevSort.TOPO);
			revWalk.reset();
			revWalk.markStart(commit);
			Scoreboard scoreboard = new Scoreboard(revWalk, finalOrigin,
					new WicketDiffImpl());
			return scoreboard.assingBlame();
		} catch (Exception e) {
			throw new RuntimeException("Internal error in BlameEngine", e);
		}
	}

	/**
	 * print it pretty
	 * 
	 * @param blameEntries
	 *            entries to print
	 */
	public void prettyPrint(List<BlameEntry> blameEntries) {
		int lineno = 1;
		for (BlameEntry blameEntry : blameEntries) {
			Object[] data = blameEntry.suspect.getData();

			for (int i = blameEntry.suspectStart; i < blameEntry.suspectStart
					+ blameEntry.originalRange.length; i++) {
				System.out.println(String.format("%3d: [%.120s] %s", Integer
						.valueOf(lineno), blameEntry.suspect, data[i]));
				lineno++;
			}
		}
	}

}
