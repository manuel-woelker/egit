package org.spearce.jgit.blame;

import java.io.IOException;

import org.spearce.jgit.errors.CorruptObjectException;
import org.spearce.jgit.errors.IncorrectObjectTypeException;
import org.spearce.jgit.errors.MissingObjectException;
import org.spearce.jgit.revwalk.RevCommit;
import org.spearce.jgit.treewalk.TreeWalk;
import org.spearce.jgit.treewalk.filter.TreeFilter;

class RenameModifiedSearchStrategy extends CopyModifiedSearchStrategy implements
		IOriginSearchStrategy {

	@Override
	protected TreeWalk createTreeWalk(Origin source, RevCommit parent)
			throws MissingObjectException, IncorrectObjectTypeException,
			CorruptObjectException, IOException {
		TreeWalk treeWalk = super.createTreeWalk(source, parent);
		treeWalk.addTree(source.getCommit().getTree());
		treeWalk.setFilter(TreeFilter.ANY_DIFF);
		return treeWalk;
	}

}
