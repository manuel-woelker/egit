package org.spearce.jgit.log;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.spearce.jgit.errors.IncorrectObjectTypeException;
import org.spearce.jgit.errors.MissingObjectException;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.lib.Tree;
import org.spearce.jgit.lib.TreeEntry;
import org.spearce.jgit.revwalk.RevCommit;
import org.spearce.jgit.revwalk.RevSort;
import org.spearce.jgit.revwalk.RevWalk;

/**
 * Class for walking origins
 * 
 */
public class OriginWalk implements Iterable<Origin>, Iterator<Origin> {

	private static final Origin[] NO_ORIGINS = new Origin[0];

	final Origin initalOrigin;

	final Repository repository;

	HashMap<RevCommit, HashSet<Origin>> commitMap = new HashMap<RevCommit, HashSet<Origin>>();

	// Origins which have not been visited yet
	LinkedList<Origin> pendingOrigins = new LinkedList<Origin>();

	private RevWalk revWalk;

	private IOriginSearchStrategy[] originSearchStrategies = new IOriginSearchStrategy[] { new SameNameOriginSearchStrategy() };

	private Origin[] parentOrigins = NO_ORIGINS;

	private Origin currentOrigin;

	private Origin[] ancestorOrigins;

	/**
	 * Standard constructor
	 * 
	 * @param initalOrigin
	 * @param repository
	 * @throws IOException
	 * @throws IncorrectObjectTypeException
	 * @throws MissingObjectException
	 */
	public OriginWalk(Origin initalOrigin, Repository repository)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		super();
		this.initalOrigin = initalOrigin;
		this.repository = repository;
		revWalk = new RevWalk(repository);
		revWalk.sort(RevSort.TOPO);
		revWalk.markStart(initalOrigin.getCommit());
		RevCommit lastSameCommit = findLastSameCommit(initalOrigin);
		Origin firstOrigin = new Origin(repository, lastSameCommit,
				initalOrigin.filename);
		pendingOrigins.add(firstOrigin);
		addOrigin(initalOrigin);
	}

	void addOrigin(Origin origin) {
		RevCommit commit = origin.getCommit();
		HashSet<Origin> originSet = commitMap.get(commit);
		if (originSet == null) {
			originSet = new HashSet<Origin>();
			commitMap.put(commit, originSet);
		}
		originSet.add(origin);
	}

	/**
	 * Copy constructor
	 * 
	 * @param other
	 * @throws IOException
	 * @throws IncorrectObjectTypeException
	 * @throws MissingObjectException
	 */
	public OriginWalk(OriginWalk other) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		this(other.initalOrigin, other.repository);
	}

	public Iterator<Origin> iterator() {
		try {
			return new OriginWalk(this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasNext() {
		return !pendingOrigins.isEmpty();
	}

	public Origin next() {
		try {
			currentOrigin = pendingOrigins.remove();
			parentOrigins = NO_ORIGINS;
			for (IOriginSearchStrategy strategy : originSearchStrategies) {
				parentOrigins = strategy.findOrigins(currentOrigin);
				if (parentOrigins.length != 0) {
					break;
				}
			}
			ancestorOrigins = new Origin[parentOrigins.length];
			for (int i = 0; i < ancestorOrigins.length; i++) {
				Origin parentOrigin = parentOrigins[i];
				RevCommit ancestorCommit = findLastSameCommit(parentOrigin);
				Origin ancestorOrigin = new Origin(repository, ancestorCommit,
						parentOrigin.filename);
				ancestorOrigins[i] = ancestorOrigin;
				pendingOrigins.add(ancestorOrigin);
			}
			return currentOrigin;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private RevCommit findLastSameCommit(Origin origin)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		RevWalk rw = new RevWalk(repository);
		rw.markStart(origin.commit);
		rw.sort(RevSort.TOPO);
		RevCommit lastFoundCommit = origin.commit;
		while (true) {
			RevCommit newLastFoundCommit = null;
			for (RevCommit parent : lastFoundCommit.getParents()) {
				parent = rw.parseCommit(parent);
				Tree tree = repository.mapTree(parent.getTree());
				TreeEntry blobMember = tree.findBlobMember(origin.filename);
				if (blobMember != null
						&& blobMember.getId().equals(origin.getObjectId())) {
					newLastFoundCommit = parent;
					break;
				}
			}
			if (newLastFoundCommit != null) {
				lastFoundCommit = newLastFoundCommit;
			} else {
				break;
			}
		}
		return lastFoundCommit;
	}

	/**
	 * @return parent origins for the current origin
	 */
	public Origin[] getParentOrigins() {
		return parentOrigins;
	}

	/**
	 * Non-trivial ancestor origins, non-trivial means that these ancestor
	 * origins differ from the current origin
	 * 
	 * @return non-trivial ancestors of current origin
	 */
	public Origin[] getAncestorOrigins() {
		return ancestorOrigins;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}
