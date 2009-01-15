package org.spearce.jgit.blame;

import java.util.ArrayList;
import java.util.Collection;

import org.spearce.jgit.diff.IDiff;
import org.spearce.jgit.diff.IDifference;
import org.spearce.jgit.diff.impl.wicket.WicketDiffImpl;
import org.spearce.jgit.lib.ObjectLoader;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.revwalk.RevCommit;
import org.spearce.jgit.revwalk.RevWalk;
import org.spearce.jgit.treewalk.TreeWalk;
import org.spearce.jgit.util.IntList;
import org.spearce.jgit.util.RawParseUtils;

class CopyModifiedSearchStrategy implements IOriginSearchStrategy {
	final static double MAX_SCORE = 100000.0;

	double maxScore = MAX_SCORE;

	private Repository repository;

	public Origin[] findOrigins(Origin source) {
		RevCommit commit = source.commit;
		repository = source.repository;
		try {
			ArrayList<Origin> resultList = new ArrayList<Origin>();
			for (RevCommit parent : commit.getParents()) {
				RevWalk revWalk = new RevWalk(repository);
				revWalk.parse(parent);
				resultList.addAll(findOrigins(source, parent));
			}
			return resultList.toArray(new Origin[0]);
		} catch (Exception e) {
			throw new RuntimeException(
					"could not retrieve scapegoats for commit " + commit, e);
		}
	}

	private Collection<Origin> findOrigins(Origin source, RevCommit parent) {
		IDiff diff = new WicketDiffImpl();
		ArrayList<Origin> resultList = new ArrayList<Origin>();
		try {
			// Tree tree = repository.mapTree(parent);
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.reset(parent.getTree());
			treeWalk.setRecursive(true);
			int highestScore = -1;
			String highestPath = null;
			while (treeWalk.next()) {
				if (!treeWalk.isSubtree()) {
					// file
					ObjectLoader openBlob = repository.openBlob(treeWalk
							.getObjectId(0));
					byte[] parentBytes = openBlob.getBytes();
					byte[] targetBytes = source.getBytes();
					IntList parentLines = RawParseUtils.lineMap(parentBytes, 0,
							parentBytes.length);
					IntList targetLines = RawParseUtils.lineMap(targetBytes, 0,
							targetBytes.length);
					IDifference[] differences = diff.diff(parentBytes,
							parentLines, targetBytes, targetLines);
					int maxLines = Math.max(targetLines.size(), parentLines
							.size()) - 1;
					int totalLines = targetLines.size() + parentLines.size()
							- 2;
					int changedlines = 0;
					for (IDifference difference : differences) {
						changedlines += difference.getLengthA()
								+ difference.getLengthB();
					}
					int commonLines = (totalLines - changedlines) / 2;
					int score = (int) (commonLines * maxScore / maxLines);
					if (score > highestScore) {
						highestScore = score;
						highestPath = treeWalk.getPathString();
					}
				}
			}
			if (highestScore > -1) {
				resultList.add(new Origin(repository, parent, highestPath));
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return resultList;
	}
}
