package org.spearce.jgit.diff.impl.wicket;

import org.spearce.jgit.blame.Difference;
import org.spearce.jgit.blame.IDiff;
import org.spearce.jgit.blame.IDifference;

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
}
