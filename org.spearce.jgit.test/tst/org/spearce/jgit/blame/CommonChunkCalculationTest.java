package org.spearce.jgit.blame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.spearce.jgit.diff.CommonChunk;
import org.spearce.jgit.diff.Difference;

public class CommonChunkCalculationTest extends TestCase {
	static class TestDifference extends Difference {

		public TestDifference(int startA, int endA, int startB, int endB) {
			super(startA, endA, startB, endB);
		}

		TestDifference inverted() {
			return new TestDifference(getStartB(), getEndB(), getStartA(),
					getEndA());
		}
	}

	public void testNoDifference() throws Exception {
		int length = 100;
		List<TestDifference> differences = new ArrayList<TestDifference>();
		List<CommonChunk> expected = Arrays
				.asList(new CommonChunk(0, 0, length));
		assertComputation(differences, expected, length, length);
	}

	public void testSimpleCommonPrefix() throws Exception {
		int length = 100;
		List<TestDifference> differences = Arrays.asList(new TestDifference(3,
				length, 7, length));
		List<CommonChunk> expected = Arrays.asList(new CommonChunk(0, 0, 3));
		assertComputation(differences, expected, length, length);
	}

	public void testCommonPrefix() throws Exception {
		List<TestDifference> differences = Arrays.asList(new TestDifference(3,
				10, 3, 20));
		List<CommonChunk> expected = Arrays.asList(new CommonChunk(0, 0, 3));
		assertComputation(differences, expected, 10, 20);
	}

	public void testSimpleCommonSuffix() throws Exception {
		List<TestDifference> differences = Arrays.asList(new TestDifference(0,
				10, 0, 20));
		List<CommonChunk> expected = Arrays.asList(new CommonChunk(11, 21, 29));
		assertComputation(differences, expected, 40, 50);
	}

	public void testSimpleInfix() throws Exception {
		List<TestDifference> differences = Arrays.asList(new TestDifference(0,
				10, 0, 10), new TestDifference(20, 30, 20, 40));
		List<CommonChunk> expected = Arrays.asList(new CommonChunk(11, 11, 9));
		assertComputation(differences, expected, 30, 40);
	}

	public void testSimpleAdditionFront() throws Exception {
		List<TestDifference> differences = Arrays.asList(new TestDifference(0,
				12, 0, -1));
		List<CommonChunk> expected = Arrays.asList(new CommonChunk(13, 0, 19));
		assertComputation(differences, expected, 32, 20);
	}

	public void testSimpleAdditionMiddle() throws Exception {
		List<TestDifference> differences = Arrays.asList(new TestDifference(10,
				12, 10, -1));
		List<CommonChunk> expected = Arrays.asList(new CommonChunk(0, 0, 10),
				new CommonChunk(13, 10, 20));
		assertComputation(differences, expected, 33, 30);
	}

	public void testSimpleAdditionEnd() throws Exception {
		List<TestDifference> differences = Arrays.asList(new TestDifference(10,
				12, 10, -1));
		List<CommonChunk> expected = Arrays.asList(new CommonChunk(0, 0, 10));
		assertComputation(differences, expected, 12, 10);
	}

	public void testSimpleAdditionTwoDifferences() throws Exception {
		List<TestDifference> differences = Arrays.asList(new TestDifference(3,
				-1, 3, 5), new TestDifference(6, -1, 9, 14));
		List<CommonChunk> expected = Arrays.asList(new CommonChunk(0, 0, 3),
				new CommonChunk(3, 6, 3));
		assertComputation(differences, expected, 6, 15);
	}

	private void assertComputation(List<TestDifference> differences,
			List<CommonChunk> expected, int lengthA, int lengthB) {
		{
			List<CommonChunk> commonChunks = Scoreboard.computeCommonChunks(
					differences, lengthA, lengthB);
			assertEquals(expected, commonChunks);
		}
		// inverse check
		{
			List<TestDifference> invertedDifferences = new ArrayList<TestDifference>();
			for (TestDifference difference : differences) {
				invertedDifferences.add(difference.inverted());
			}
			List<CommonChunk> invertedExpected = new ArrayList<CommonChunk>();
			for (CommonChunk commonChunk : expected) {
				invertedExpected.add(new CommonChunk(commonChunk.getBstart(),
						commonChunk.getAstart(), commonChunk.getLength()));
			}
			List<CommonChunk> commonChunks = Scoreboard.computeCommonChunks(
					invertedDifferences, lengthB, lengthA);
			assertEquals(invertedExpected, commonChunks);
		}
	}
}
