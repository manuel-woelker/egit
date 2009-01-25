package org.spearce.jgit.log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.spearce.jgit.diff.IDiff;
import org.spearce.jgit.diff.IDifference;
import org.spearce.jgit.diff.impl.wicket.WicketDiffImpl;
import org.spearce.jgit.errors.CorruptObjectException;
import org.spearce.jgit.errors.IncorrectObjectTypeException;
import org.spearce.jgit.errors.MissingObjectException;
import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.lib.ObjectLoader;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.revwalk.RevCommit;
import org.spearce.jgit.revwalk.RevWalk;
import org.spearce.jgit.treewalk.TreeWalk;
import org.spearce.jgit.util.IntList;
import org.spearce.jgit.util.RawParseUtils;

/**
 * Origin search strategy looking for a copied and modified file
 * 
 */
public class CopyModifiedSearchStrategy implements IOriginSearchStrategy {
	final static double MAX_SCORE = 1000;

	double maxScore = MAX_SCORE;

	double thresholdScore = MAX_SCORE / 5;

	int maxCandidates = 2;

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
		ScoreList<Integer, String> scoreList = new ScoreList<Integer, String>(
				maxCandidates);
		ArrayList<Origin> resultList = new ArrayList<Origin>();
		try {
			// Tree tree = repository.mapTree(parent);
			TreeWalk treeWalk = createTreeWalk(source, parent);
			while (treeWalk.next()) {
				if (!treeWalk.isSubtree()) {
					// file
					ObjectId objectId = treeWalk.getObjectId(0);
					if (objectId == null || objectId.equals(ObjectId.zeroId())) {
						// nothing to see here
						continue;
					}
					ObjectLoader openBlob = repository.openBlob(objectId);
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
					String pathString = treeWalk
					.getPathString();
					if (score > thresholdScore) {
						scoreList.add(Integer.valueOf(score), pathString);
					}
				}
			}
			for (String path : scoreList.getEntries()) {
				resultList.add(new Origin(repository, parent, path));
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return resultList;
	}

	/**
	 * @param source
	 * @param parent
	 * @return tree walk for the origin search
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws CorruptObjectException
	 * @throws IOException
	 */
	protected TreeWalk createTreeWalk(Origin source, RevCommit parent)
			throws MissingObjectException, IncorrectObjectTypeException,
			CorruptObjectException, IOException {
		TreeWalk treeWalk = new TreeWalk(repository);
		treeWalk.reset(parent.getTree());
		treeWalk.setRecursive(true);
		return treeWalk;
	}
}
