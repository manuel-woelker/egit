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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.spearce.jgit.diff.CommonChunk;
import org.spearce.jgit.diff.IDiff;
import org.spearce.jgit.diff.IDifference;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.revwalk.RevCommit;
import org.spearce.jgit.revwalk.RevWalk;
import org.spearce.jgit.util.IntList;
import org.spearce.jgit.util.RawParseUtils;

/**
 * Main structure for performing the blame algorithm
 * 
 * Name and structure are lifted from the cgit blame implementation (cf.
 * builtin-blame.c)
 * 
 * 
 */
class Scoreboard {
	LinkedList<BlameEntry> blameEntries = new LinkedList<BlameEntry>();

	Origin finalOrigin;

	private final IDiff diff;

	private final Repository repository;

	private final RevWalk revWalk;

	Scoreboard(RevWalk revWalk, Origin finalObject, IDiff diff) {
		super();
		this.revWalk = revWalk;
		this.repository = revWalk.getRepository();
		this.finalOrigin = finalObject;
		this.diff = diff;
		BlameEntry blameEntry = new BlameEntry();
		byte[] bytes = finalObject.getBytes();
		IntList lines = RawParseUtils.lineMap(bytes, 0, bytes.length);
		blameEntry.originalRange = new Range(0, lines.size() - 1);
		blameEntry.suspect = finalObject;
		blameEntry.suspectStart = 0;
		blameEntries.add(blameEntry);
	}

	List<BlameEntry> assingBlame() {
		try {

			while (true) {
				RevCommit commit = revWalk.next();
				BlameEntry todo = null;
				/* find one suspect to break down */
				boolean done = true;
				for (BlameEntry blameEntry : blameEntries) {
					if (blameEntry.suspect.commit.equals(commit)) {
						todo = blameEntry;
						done = false;
						break;
					}
					if (!blameEntry.guilty) {
						// break;
						done = false;

					}
				}
				if (done) {
					break; // all done
				}
				if (commit == null) {
					throw new RuntimeException("Internal error");
				}
				if (todo == null) {
					continue;
				}
				Origin suspect = todo.suspect;
				passBlame(todo.suspect);

				// Plead guilty for remaining entries
				List<BlameEntry> guilty = new ArrayList<BlameEntry>();
				for (BlameEntry blameEntry : blameEntries) {
					if (suspect.equals(blameEntry.suspect)) {
						blameEntry.guilty = true;
						guilty.add(blameEntry);
					}
				}
				if (!guilty.isEmpty()) {
					System.out.println(suspect + " pleading guilty for:");
				}
				for (BlameEntry blameEntry : guilty) {
					System.out.println("\t" + blameEntry);
				}

			}
			return blameEntries;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void passBlame(Origin suspect) {
		// Simplified quite a lot
		Origin[] scapegoats = findScapegoats(suspect);
		for (Origin scapegoat : scapegoats) {
			if (suspect.getObjectId().equals(scapegoat.getObjectId())) {
				// file has not changed, pass all blame
				passWholeBlameToParent(suspect, scapegoat);
				return;
			}
		}
		for (Origin scapegoat : scapegoats) {
			passBlameToParent(suspect, scapegoat);
		}
	}

	private void passWholeBlameToParent(Origin target, Origin parent) {
		for (ListIterator<BlameEntry> it = blameEntries.listIterator(); it
				.hasNext();) {
			BlameEntry blameEntry = it.next();
			if (!blameEntry.suspect.equals(target)) {
				continue; // not what we are looking for
			}
			blameEntry.suspect = parent;
		}
	}

	private void passBlameToParent(Origin target, Origin parent) {
		byte[] parentBytes = parent.getBytes();
		byte[] targetBytes = target.getBytes();
		IntList parentLines = RawParseUtils.lineMap(parentBytes, 0,
				parentBytes.length);
		IntList targetLines = RawParseUtils.lineMap(targetBytes, 0,
				targetBytes.length);
		IDifference[] differences = diff.diff(parentBytes, parentLines,
				targetBytes, targetLines);

		System.out.println("Inspecting " + target);
		List<CommonChunk> commonChunks = computeCommonChunks(Arrays
				.asList(differences), parentBytes.length, targetBytes.length);
		System.out.println(commonChunks);
		for (CommonChunk commonChunk : commonChunks) {
			blameChunk(target, parent, commonChunk);
		}
	}

	private void blameChunk(Origin target, Origin parent,
			CommonChunk commonChunk) {
		for (ListIterator<BlameEntry> it = blameEntries.listIterator(); it
				.hasNext();) {
			BlameEntry blameEntry = it.next();
			if (blameEntry.guilty || !(blameEntry.suspect.equals(target))) {
				continue; // not what we are looking for
			}
			if (commonChunk.getBstart() + commonChunk.getLength() <= blameEntry.suspectStart) {
				continue; // common chunk ends before this entry starts
			}
			if (commonChunk.getBstart() < blameEntry.suspectStart
					+ blameEntry.originalRange.length) {
				List<BlameEntry> newBlameEntries = blameOverlap(blameEntry,
						parent, commonChunk);
				it.remove();
				for (BlameEntry newBlameEntry : newBlameEntries) {
					it.add(newBlameEntry);
				}
			}

		}
	}

	private List<BlameEntry> blameOverlap(BlameEntry blameEntry, Origin parent,
			CommonChunk commonChunk) {
		List<BlameEntry> split = splitOverlap(blameEntry, parent, commonChunk);
		return split;
	}

	// It is known that lines between tlno to same came from parent, and e
	// has an overlap with that range. it also is known that parent's
	// line plno corresponds to e's line tlno.
	//	 
	// <---- e ----->
	// <------>
	// <------------>
	// <------------>
	// <------------------>
	//	 
	// Split e into potentially three parts; before this chunk, the chunk
	// to be blamed for the parent, and after that portion.
	// [from builtin-blame.c split_overlap( )]
	//	 
	//	  
	// tlno = commonChunk.bstart
	// plno = commonChunk.astart
	// same = commonChunk.bstart+commonChunk.length

	static List<BlameEntry> splitOverlap(BlameEntry blameEntry, Origin parent,
			CommonChunk commonChunk) {
		List<BlameEntry> result = new LinkedList<BlameEntry>();
		// prechunk that can not be blamed on this parent
		BlameEntry split = new BlameEntry();
		if (blameEntry.suspectStart < commonChunk.getBstart()) {
			BlameEntry pre = new BlameEntry();
			pre.suspect = blameEntry.suspect;
			pre.suspectStart = blameEntry.suspectStart;
			pre.originalRange = new Range(blameEntry.originalRange.start,
					commonChunk.getBstart() - blameEntry.suspectStart);
			result.add(pre);
			split.originalRange = new Range(blameEntry.originalRange.start
					+ (commonChunk.getBstart() - blameEntry.suspectStart), 0);
			split.suspectStart = commonChunk.getAstart();
		} else {
			split.originalRange = new Range(blameEntry.originalRange.start, 0);
			split.suspectStart = commonChunk.getAstart()
					+ (blameEntry.suspectStart - commonChunk.getBstart());
		}

		split.suspect = parent;
		result.add(split);

		int same = commonChunk.getBstart() + commonChunk.getLength();
		// postchunk that can not be blamed on this parent
		int chunkEnd;
		if (same < blameEntry.suspectStart + blameEntry.originalRange.length) {
			BlameEntry post = new BlameEntry();
			post.suspect = blameEntry.suspect;
			int shiftStart = same - blameEntry.suspectStart;
			int numLines = blameEntry.suspectStart
					+ blameEntry.originalRange.length - same;
			post.originalRange = new Range(blameEntry.originalRange.start
					+ shiftStart, numLines);
			post.suspectStart = blameEntry.suspectStart + shiftStart;
			result.add(post);
			chunkEnd = post.originalRange.start;
		} else {
			chunkEnd = blameEntry.originalRange.start
					+ blameEntry.originalRange.length;
		}
		split.originalRange.length = chunkEnd - split.originalRange.start;

		// System.out.println("split "+ blameEntry);
		int sum = 0;
		for (BlameEntry each : result) {
			// System.out.println("\t-> "+ each);
			sum += each.originalRange.length;
		}
		if (sum != blameEntry.originalRange.length) {
			throw new RuntimeException("Internal error splitting blameentries");
		}
		return result;
	}

	static List<CommonChunk> computeCommonChunks(
			List<? extends IDifference> differences, int lengthA, int lengthB) {
		List<CommonChunk> result = new LinkedList<CommonChunk>();
		// check no differences -> all in common
		if (differences.isEmpty()) {
			result.add(new CommonChunk(0, 0, lengthA));
			return result;
		}
		IDifference firstDifference = differences.get(0);
		// commmon prefix
		int commonPrefixLength = Math.min(firstDifference.getStartA(),
				firstDifference.getStartB());
		if (commonPrefixLength > 0) {
			result.add(new CommonChunk(0, 0, commonPrefixLength));
		}
		Iterator<? extends IDifference> it = differences.iterator();
		IDifference previousDifference = it.next();

		for (; it.hasNext();) {
			IDifference nextDifference = it.next();
			int lastChangedLineA = previousDifference.getEndA();
			if (lastChangedLineA == -1)
				lastChangedLineA = previousDifference.getStartA() - 1;
			int firstCommonLineA = lastChangedLineA + 1;

			int lastChangedLineB = previousDifference.getEndB();
			if (lastChangedLineB == -1)
				lastChangedLineB = previousDifference.getStartB() - 1;
			int firstCommonLineB = lastChangedLineB + 1;
			int commonLengthA = nextDifference.getStartA() - firstCommonLineA;
			int commonLengthB = nextDifference.getStartB() - firstCommonLineB;
			if (commonLengthA != commonLengthA) {
				throw new RuntimeException("lengths not equal: "
						+ commonLengthA + "!=" + commonLengthB);
			}
			result.add(new CommonChunk(firstCommonLineA, firstCommonLineB,
					commonLengthA));
			previousDifference = nextDifference;
		}

		// common suffix
		IDifference lastDifference = differences.get(differences.size() - 1);
		int lastChangedLineA = lastDifference.getEndA();
		if (lastChangedLineA == -1)
			lastChangedLineA = lastDifference.getStartA() - 1;
		int firstCommonLineA = lastChangedLineA + 1;

		int lastChangedLineB = lastDifference.getEndB();
		if (lastChangedLineB == -1)
			lastChangedLineB = lastDifference.getStartB() - 1;
		int firstCommonLineB = lastChangedLineB + 1;
		int commonSuffixLength = Math.min(lengthA - firstCommonLineA, lengthB
				- firstCommonLineB);

		if (commonSuffixLength > 0) {
			result.add(new CommonChunk(firstCommonLineA, firstCommonLineB,
					commonSuffixLength));
		}

		// Sanity check
		// TODO:

		return result;
	}

	/**
	 * get scapegoat origins for this origin, i.e. origins for parent commits
	 * 
	 * a scapegoat in this context is a origin (for a parent commit) to that
	 * this origin can pass blame on
	 * 
	 * 
	 * Note: currently only the same filename is used, that means renames and
	 * copies are not found
	 * 
	 * @param origin
	 *            the origin for which to retrieve the scapegoats
	 * 
	 * @return collection of scapegoat parent origins
	 */
	Origin[] findScapegoats(Origin origin) {
		RevCommit commit = origin.commit;
		try {
			ArrayList<Origin> resultList = new ArrayList<Origin>();
			for (RevCommit parent : commit.getParents()) {
				resultList.add(new Origin(repository, parent, origin.filename));
			}
			return resultList.toArray(new Origin[0]);
		} catch (Exception e) {
			throw new RuntimeException(
					"could not retrieve scapegoats for commit " + commit, e);
		}
	}

}