package org.spearce.jgit.diff;

import java.nio.charset.Charset;

import junit.framework.TestCase;

import org.spearce.jgit.util.IntList;
import org.spearce.jgit.util.RawParseUtils;

public class DiffImplTest extends TestCase {
	IDiff diff;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		diff = new org.spearce.jgit.diff.impl.wicket.WicketDiffImpl();
	}

	public void testSame() throws Exception {
		String a = "a b c d";
		String b = "a b c d";
		assertDiff(a, b, "");
	}

	public void testUnsame() throws Exception {
		String a = "1 2 3";
		String b = "a b c d";
		assertDiff(a, b, "0+3,0+4");
	}
	
	public void testInsertFront() throws Exception {
		String a = "a b c d";
		String b = "x y z a b c d";
		assertDiff(a, b, "0+0,0+3");
	}

	public void testInsertMiddle() throws Exception {
		String a = "a b c d";
		String b = "a b x y z c d";
		assertDiff(a, b, "2+0,2+3");
	}

	public void testInsertBack() throws Exception {
		String a = "a b c d";
		String b = "a b c d x y z";
		assertDiff(a, b, "4+0,4+3");
	}

	public void dontTestRemove() {
		// this is handled by the insert test cases above, since assertDiff tests both ways symmetrically
	}
	
	public void testReplaceFront() throws Exception {
		String a = "1 2 a b c d";
		String b = "x y z a b c d";
		assertDiff(a, b, "0+2,0+3");
	}

	public void testReplaceMiddle() throws Exception {
		String a = "a b 1 2 c d";
		String b = "a b x y z c d";
		assertDiff(a, b, "2+2,2+3");
	}

	public void testReplaceBack() throws Exception {
		String a = "a b c d 1 2";
		String b = "a b c d x y z";
		assertDiff(a, b, "4+2,4+3");
	}

	public void testComplex() throws Exception {
		// -------- 0 1 2 3 4 5 6 7 8 9 10 11
		String a = "a b c d e f g h i j k";
		String b = "b c 1 e f 3 4 h 6 k a  b";
		assertDiff(a, b, "0+1,0+0 3+1,2+1 6+1,5+2 8+2,8+1 11+0,10+2");
	}

	public void testLong() throws Exception {
		// -------- 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34
		String a = "a b c d e f g h i j k  l  m  n  o  p  q  r  s  t  u  v  w  x  y  z  aa ab ac ad";
		String b = "a b c d e f g h i j k  l  m  n  1  2  3  4  q  r  s  t  u  4  5  6  v  w  x  y  z  aa ab ac ad";
		assertDiff(a, b, "14+2,14+4 21+0,23+3");
	}

	private void assertDiff(String a, String b, String expectedString) {
		byte[] aBytes = toByteArray(a);
		byte[] bBytes = toByteArray(b);
		IntList aMap = RawParseUtils.lineMap(aBytes, 0, aBytes.length);
		IntList bMap = RawParseUtils.lineMap(bBytes, 0, bBytes.length);
		// a -> b
		{
			IDifference[] differences = diff.diff(aBytes, aMap, bBytes, bMap);
			StringBuilder sb = new StringBuilder();
			for (IDifference difference : differences) {
				sb.append(difference.getStartA()).append("+").append(
						difference.getLengthA()).append(",").append(
						difference.getStartB()).append("+").append(
						difference.getLengthB()).append(" ");
			}
			String actualString = sb.toString().trim();
			assertEquals(expectedString, actualString);
		}
		// b -> a
		{
			IDifference[] differences = diff.diff(bBytes, bMap, aBytes, aMap );
			StringBuilder sb = new StringBuilder();
			for (IDifference difference : differences) {
				sb.append(difference.getStartB()).append("+").append(
						difference.getLengthB()).append(",").append(
						difference.getStartA()).append("+").append(
						difference.getLengthA()).append(" ");
			}
			String actualString = sb.toString().trim();
			assertEquals(expectedString, actualString);
		}
	}

	private byte[] toByteArray(String a) {
		String[] lines = a.split(" ");
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			if(line.length() != 0) {
				sb.append(line).append("\n");
			}
		}
		return sb.toString().getBytes(Charset.forName("UTF-8"));
	}
}