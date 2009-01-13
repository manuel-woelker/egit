package org.spearce.jgit.diff.impl.wicket;

import org.spearce.jgit.diff.Difference;
import org.spearce.jgit.diff.IDiff;
import org.spearce.jgit.diff.IDifference;
import org.spearce.jgit.util.IntList;

/**
 * Diff implementation backed by the Apache wicket diff module
 * 
 * @see "http://wicket.sourceforge.net/apidocs/wicket/util/diff/myers/package-summary.html"
 * 
 */
public class WicketDiffImpl implements IDiff {

	public IDifference[] diff(Object[] a, Object[] b) {
		MyersDiff myersDiff = new MyersDiff();
		try {
			Revision revision = myersDiff.diff(a, b);
			IDifference[] differences = new IDifference[revision.size()];
			for (int i = 0; i < revision.size(); i++) {
				Delta delta = revision.getDelta(i);
				Chunk original = delta.getOriginal();
				Chunk revised = delta.getRevised();
				int alast = original.last();
				int blast = revised.last();
				if (original.size() <= 0) {
					alast = -1;
				}
				if (revised.size() <= 0) {
					blast = -1;
				}
				Difference difference = new Difference(original.anchor(),
						alast, revised.anchor(), blast);
				differences[i] = difference;
			}
			return differences;
		} catch (DifferentiationFailedException e) {
			throw new RuntimeException("wicket diff failed", e);
		}
	}

	private static class Line {
		int hashCode = 0;

		private final byte[] bytes;

		private final int start;

		private final int end;

		public Line(byte[] bytes, int start, int end) {
			this.bytes = bytes;
			this.start = start;
			this.end = end;
		}

		@Override
		public int hashCode() {
			if (hashCode != 0) {
				return hashCode;
			}
			int result = 1;
			for (int i = start; i < end; i++) {
				byte element = bytes[i];
				result = 31 * result + element;
			}
			hashCode = result;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			Line other = (Line) obj;
			if ((end - start) != (other.end - other.start)) {
				return false;
			}
			for (int i = start, j = other.start; i < end; i++, j++) {
				if (bytes[i] != other.bytes[j]) {
					return false;
				}
			}
			return true;
		}
	}

	public IDifference[] diff(byte[] parentBytes, IntList parentLines,
			byte[] targetBytes, IntList targetLines) {
		Line[] parentLineArray = convertLines(parentBytes, parentLines);
		Line[] targetLineArray = convertLines(targetBytes, targetLines);
		return diff(parentLineArray, targetLineArray);
	}

	private Line[] convertLines(byte[] parentBytes, IntList parentLines) {
		int size = parentLines.size();
		// lines are 1 based here
		Line[] parentLineArray = new Line[size - 1];
		for (int i = 1; i < size; i++) {
			int start = parentLines.get(i);
			int end;
			if (i < size - 1) {
				end = parentLines.get(i + 1);
			} else {
				end = parentBytes.length;
			}
			parentLineArray[i - 1] = new Line(parentBytes, start, end);
		}
		return parentLineArray;
	}
}
