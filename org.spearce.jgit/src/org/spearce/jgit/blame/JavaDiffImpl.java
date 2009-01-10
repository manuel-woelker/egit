package org.spearce.jgit.blame;

import java.util.List;

import org.incava.util.diff.Diff;
import org.incava.util.diff.Difference;

/** Diff implementation backed by the incava diff library
 * @see "http://www.incava.org/projects/java/java-diff/"
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 *
 */
public class JavaDiffImpl implements IDiff {
	private static class JavaDifferenceImpl implements IDifference {
		final Difference difference;

		public JavaDifferenceImpl(Difference difference) {
			super();
			this.difference = difference;
		}

		public int getEndA() {
			return difference.getAddedEnd();
		}

		public int getEndB() {
			return difference.getDeletedEnd();
		}

		public int getStartA() {
			return difference.getAddedStart();
		}

		public int getStartB() {
			return difference.getDeletedStart();
		}

		@Override
		public String toString() {
		
			return String.format("Diff <%d,%d  %d,%d>", Integer.valueOf(getStartA()), Integer.valueOf(getEndA()), Integer.valueOf(getStartB()), Integer.valueOf(getEndB()));
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	public IDifference[] diff(Object[] a, Object[] b) {
		Diff diff = new Diff(a, b);
		List<Difference> differences = diff.diff();
		IDifference[] result = new IDifference[differences.size()];
		int i = 0;
		for (Difference difference : differences) {
			result[i] = new JavaDifferenceImpl(difference);
			i++;
		}
		return result;
	}

}
