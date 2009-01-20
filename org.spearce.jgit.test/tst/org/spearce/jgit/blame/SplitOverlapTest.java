package org.spearce.jgit.blame;

import java.util.Arrays;
import java.util.List;

import org.spearce.jgit.diff.CommonChunk;
import org.spearce.jgit.log.Origin;

import junit.framework.TestCase;

public class SplitOverlapTest extends TestCase {

	public void testPerfectMatch() throws Exception {
		// <ttttttttt> < entry (t = target origin, i.e. current suspect)
		// <xxxxxxxxx> < common chunk
		// <ppppppppp> < result (p = parent origin, i.e. potential suspect)
		Origin parent = new Origin(null, null, "target");
		Origin target = new Origin(null, null, "target");

		BlameEntry blameEntry = new BlameEntry();
		blameEntry.originalRange = new Range(17, 4);
		blameEntry.suspectStart = 50;
		blameEntry.suspect = target;
		List<BlameEntry> actual = Scoreboard.splitOverlap(blameEntry, parent,
				new CommonChunk(70, 50, 4));
		BlameEntry expected1 = new BlameEntry();
		expected1.originalRange = new Range(17, 4);
		expected1.suspectStart = 70;
		expected1.suspect = parent;
		List<BlameEntry> expected = Arrays.asList(expected1);
		assertEquals(expected, actual);
	}

	public void testSplitMiddle() throws Exception {
		// <ttttttttt> < entry
		// ---<xxx>--- < common chunk
		// <t><ppp><t> < result
		Origin parent = new Origin(null, null, "target");
		Origin target = new Origin(null, null, "target");

		BlameEntry blameEntry = new BlameEntry();
		blameEntry.originalRange = new Range(17, 12);
		blameEntry.suspectStart = 50;
		blameEntry.suspect = target;
		List<BlameEntry> actual = Scoreboard.splitOverlap(blameEntry, parent,
				new CommonChunk(73, 53, 4));
		BlameEntry expected1 = new BlameEntry();
		expected1.originalRange = new Range(17, 3);
		expected1.suspectStart = 50;
		expected1.suspect = target;
		BlameEntry expected2 = new BlameEntry();
		expected2.originalRange = new Range(20, 4);
		expected2.suspectStart = 73;
		expected2.suspect = parent;
		BlameEntry expected3 = new BlameEntry();
		expected3.originalRange = new Range(24, 5);
		expected3.suspectStart = 57;
		expected3.suspect = target;
		List<BlameEntry> expected = Arrays.asList(expected1, expected2,
				expected3);
		assertEquals(expected, actual);
	}

	public void testSplitCommonChunkBigger() throws Exception {
		// ---<ttt>--- < entry
		// <xxxxxxxxx> < common chunk
		// ---<ppp>--- < result
		Origin parent = new Origin(null, null, "target");
		Origin target = new Origin(null, null, "target");

		BlameEntry blameEntry = new BlameEntry();
		blameEntry.originalRange = new Range(17, 12);
		blameEntry.suspectStart = 50;
		blameEntry.suspect = target;
		List<BlameEntry> actual = Scoreboard.splitOverlap(blameEntry, parent,
				new CommonChunk(73, 40, 30));
		BlameEntry expected1 = new BlameEntry();
		expected1.originalRange = new Range(17, 12);
		expected1.suspectStart = 83;
		expected1.suspect = parent;
		List<BlameEntry> expected = Arrays.asList(expected1);
		assertEquals(expected, actual);
	}

	public void testSplitCommonChunkStartsBefore() throws Exception {
		// ---<tttttt> < entry
		// <xxxxxx>--- < common chunk
		// ---<ppp><t> < result
		Origin parent = new Origin(null, null, "target");
		Origin target = new Origin(null, null, "target");

		BlameEntry blameEntry = new BlameEntry();
		blameEntry.originalRange = new Range(17, 12);
		blameEntry.suspectStart = 50;
		blameEntry.suspect = target;
		List<BlameEntry> actual = Scoreboard.splitOverlap(blameEntry, parent,
				new CommonChunk(73, 40, 20));
		BlameEntry expected1 = new BlameEntry();
		expected1.originalRange = new Range(17, 10);
		expected1.suspectStart = 83;
		expected1.suspect = parent;
		BlameEntry expected2 = new BlameEntry();
		expected2.originalRange = new Range(27, 2);
		expected2.suspectStart = 60;
		expected2.suspect = target;
		List<BlameEntry> expected = Arrays.asList(expected1, expected2);
		assertEquals(expected, actual);
	}

	public void testSplitCommonChunkEndsAfter() throws Exception {
		// <tttttt>--- < entry
		// ---<xxxxxx> < common chunk
		// <t><ppp>--- < result
		Origin parent = new Origin(null, null, "target");
		Origin target = new Origin(null, null, "target");

		BlameEntry blameEntry = new BlameEntry();
		blameEntry.originalRange = new Range(17, 12);
		blameEntry.suspectStart = 50;
		blameEntry.suspect = target;
		List<BlameEntry> actual = Scoreboard.splitOverlap(blameEntry, parent,
				new CommonChunk(73, 55, 20));
		BlameEntry expected1 = new BlameEntry();
		expected1.originalRange = new Range(17, 5);
		expected1.suspectStart = 50;
		expected1.suspect = target;
		BlameEntry expected2 = new BlameEntry();
		expected2.originalRange = new Range(22, 7);
		expected2.suspectStart = 73;
		expected2.suspect = parent;
		List<BlameEntry> expected = Arrays.asList(expected1, expected2);
		assertEquals(expected, actual);
	}

}
