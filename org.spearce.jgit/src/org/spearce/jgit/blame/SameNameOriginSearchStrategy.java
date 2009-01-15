package org.spearce.jgit.blame;

import java.util.ArrayList;

import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.lib.Tree;
import org.spearce.jgit.revwalk.RevCommit;

class SameNameOriginSearchStrategy implements IOriginSearchStrategy {

	public Origin[] findOrigins(Origin source) {
		RevCommit commit = source.commit;
		Repository repository = source.getRepository();
		try {
			ArrayList<Origin> resultList = new ArrayList<Origin>();
			for (RevCommit parent : commit.getParents()) {
				Tree tree = repository.mapTree(parent);
				if (tree.existsBlob(source.filename)) {
					resultList.add(new Origin(repository, parent,
							source.filename));
				}
			}
			return resultList.toArray(new Origin[0]);
		} catch (Exception e) {
			throw new RuntimeException(
					"could not retrieve scapegoats for commit " + commit, e);
		}
	}
}
