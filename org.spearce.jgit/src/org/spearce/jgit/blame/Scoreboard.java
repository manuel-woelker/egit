package org.spearce.jgit.blame;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 *
 */
class Scoreboard {
		LinkedList<BlameEntry> blameEntries = new LinkedList<BlameEntry>();
		IOrigin finalOrigin;
		private final IDiff diff;
		Scoreboard(IOrigin finalObject, IDiff diff) {
			super();
			this.finalOrigin = finalObject;
			this.diff = diff;
			BlameEntry blameEntry = new BlameEntry();
			blameEntry.originalRange = new Range(0, finalObject.getData().length);
			blameEntry.suspect = finalObject;
			blameEntry.suspectStart = 0;
			blameEntries.add(blameEntry);
		}
		
		
		List<BlameEntry> assingBlame() {
			while(true) {
				BlameEntry todo = null;
				/* find one suspect to break down */
				for (BlameEntry blameEntry : blameEntries) {
					if (!blameEntry.guilty) {
						todo = blameEntry;
						break;
					}
				}
				if (todo == null) {
					break; // all done
				}
				IOrigin suspect = todo.suspect;
				passBlame(todo.suspect);
				// Plead guilty for remaining entries
				for (BlameEntry blameEntry : blameEntries) {
					if (suspect.equals(blameEntry.suspect)) {
						blameEntry.guilty = true;
					}
				}

			}
			return blameEntries;
		}


		private void passBlame(IOrigin suspect) {
			// Simplified quite a lot
			for (IOrigin parent : suspect.getParents()) {
				passBlameToParent(suspect, parent);
			}
		}

		private void passBlameToParent(IOrigin target, IOrigin parent) {
		    IDifference[] differences = diff.diff(parent.getData(), target.getData());
		       
			List<CommonChunk> commonChunks = computeCommonChunks(Arrays.asList(differences), parent.getData().length, target.getData().length);
			System.out.println(commonChunks);
			for (CommonChunk commonChunk : commonChunks) {
				blameChunk(target, parent, commonChunk);
			}
		}


		private void blameChunk(IOrigin target, IOrigin parent,
				CommonChunk commonChunk) {
			for (ListIterator<BlameEntry> it = blameEntries.listIterator(); it
					.hasNext();) {
				BlameEntry blameEntry = it.next();
				if (blameEntry.guilty || !(blameEntry.suspect.equals(target))) {
					continue; // not what we are looking for
				}
				if (commonChunk.bstart + commonChunk.length <= blameEntry.suspectStart) {
					continue; // common chunk ends before this entry starts
				}
				if (commonChunk.bstart < blameEntry.suspectStart
						+ blameEntry.originalRange.length) {
					List<BlameEntry> newBlameEntries = blameOverlap(blameEntry,
							parent, commonChunk);
					it.remove();
					for(BlameEntry newBlameEntry: newBlameEntries) {
						it.add(newBlameEntry);
					}
				}

			}
		}


		private List<BlameEntry> blameOverlap(BlameEntry blameEntry, IOrigin parent, CommonChunk commonChunk) {
			List<BlameEntry> split = splitOverlap(blameEntry, parent, commonChunk);
			return split;
		}

		/*
		 * It is known that lines between tlno to same came from parent, and e
		 * has an overlap with that range.  it also is known that parent's
		 * line plno corresponds to e's line tlno.
		 *
		 *                <---- e ----->
		 *                   <------>
		 *                   <------------>
		 *             <------------>
		 *             <------------------>
		 *
		 * Split e into potentially three parts; before this chunk, the chunk
		 * to be blamed for the parent, and after that portion.
		 * 
		 * tlno = commonChunk.bstart
		 * plno = commonChunk.astart
		 * same = commonChunk.bstart+commonChunk.length
		 * 
		 */
		private List<BlameEntry> splitOverlap(BlameEntry blameEntry,
				IOrigin parent, CommonChunk commonChunk) {
			List<BlameEntry> result = new LinkedList<BlameEntry>();
			// prechunk that can not be blamed on this parent
			BlameEntry split = new BlameEntry();
			if(blameEntry.suspectStart < commonChunk.bstart) {
				BlameEntry pre = new BlameEntry();
				pre.suspect = blameEntry.suspect;
				pre.suspectStart = blameEntry.suspectStart;
				pre.originalRange = new Range(blameEntry.originalRange.start, commonChunk.bstart - blameEntry.suspectStart);
				result.add(pre);
				split.originalRange = new Range(blameEntry.originalRange.start + (commonChunk.bstart - blameEntry.suspectStart), 0);
				split.suspectStart = commonChunk.astart;
			} else {
				split.originalRange = new Range(blameEntry.originalRange.start, 0);
				split.suspectStart = commonChunk.astart + (blameEntry.suspectStart - commonChunk.bstart);
			}
			
			split.suspect = parent;
			result.add(split);
			
			int same = commonChunk.bstart+commonChunk.length;
			// postchunk that can not be blamed on this parent
			int chunkEnd;
			if(same < blameEntry.suspectStart + blameEntry.originalRange.length) {
				BlameEntry post = new BlameEntry();
				post.suspect = blameEntry.suspect;
				int shiftStart = same-blameEntry.suspectStart;
				int numLines = blameEntry.suspectStart + blameEntry.originalRange.length - same;
				post.originalRange = new Range(blameEntry.originalRange.start + shiftStart, numLines);
				post.suspectStart = blameEntry.suspectStart + shiftStart;
				result.add(post);
				chunkEnd = post.originalRange.start;
			} else {
				chunkEnd = blameEntry.originalRange.start+blameEntry.originalRange.length;				
			}
			split.originalRange.length = chunkEnd - split.originalRange.start;
			
//			if (same < e->s_lno + e->num_lines) {
//				/* there is a post-chunk part not blamed on parent */
//				split[2].suspect = origin_incref(e->suspect);
//				split[2].lno = e->lno + (same - e->s_lno);
//				split[2].s_lno = e->s_lno + (same - e->s_lno);
//				split[2].num_lines = e->s_lno + e->num_lines - same;
//				chunk_end_lno = split[2].lno;
//			}
//			else
//				chunk_end_lno = e->lno + e->num_lines;
//			split[1].num_lines = chunk_end_lno - split[1].lno;
			
			return result;
		}


		private List<CommonChunk> computeCommonChunks(List<IDifference> differences,
				int lengthA, int lengthB) {
			List<CommonChunk> result = new LinkedList<CommonChunk>();
			// check no differences
			if(differences.isEmpty()) {
				result.add(new CommonChunk(0,0,lengthA));
				return result;
			}
			IDifference firstDifference = differences.get(0);
			// commmon prefix
			int commonPrefixLength = Math.min(firstDifference.getStartA(), firstDifference.getStartB());
			if(commonPrefixLength > 0) {
				result.add(new CommonChunk(0,0,commonPrefixLength));
			}
			// TODO: common lines in between
			// common suffix
			IDifference lastDifference = differences.get(differences.size()-1);
			int lastChangedLineA = lastDifference.getEndA();
			if(lastChangedLineA == -1) lastChangedLineA = lastDifference.getStartA();
			int lastChangedLineB = lastDifference.getEndB();
			if(lastChangedLineB == -1) lastChangedLineB = lastDifference.getStartB();
			int commonSuffixLength = Math.min(lengthA-lastChangedLineA, lengthB)+1;
			
			if(commonSuffixLength > 0) {				
				result.add(new CommonChunk(lengthA-commonSuffixLength,lengthB-commonSuffixLength,commonSuffixLength));
			}
			
			return result;
		}
	}