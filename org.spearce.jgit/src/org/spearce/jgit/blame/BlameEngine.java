package org.spearce.jgit.blame;

import java.util.List;

import org.spearce.jgit.lib.Constants;
import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.revwalk.RevCommit;
import org.spearce.jgit.revwalk.RevSort;
import org.spearce.jgit.revwalk.RevWalk;

/**
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 * 
 */
public class BlameEngine {
	private Repository repository;

	private final RevWalk revWalk;

	/**
	 * @param repository
	 */
	public BlameEngine(final Repository repository) {
		this(new RevWalk(repository));
	}

	/**
	 * @param revWalk
	 */
	public BlameEngine(RevWalk revWalk) {
		this.revWalk = revWalk;
		this.repository = revWalk.getRepository();
	}

	/**
	 * @param path
	 *            file path
	 * @return list of blame entries linking line numbers to originating commit
	 */
	public List<BlameEntry> blame(String path) {
		try {
			ObjectId headId = repository.resolve(Constants.HEAD);
			RevCommit lastCommit = revWalk.parseCommit(headId);
			return blame(lastCommit, path);
		} catch (Exception e) {
			throw new RuntimeException("Internal error in BlameEngine", e);
		}
	}

	/**
	 * @param commit
	 *            commit from which to start the blame process
	 * @param path
	 *            file path
	 * @return list of blame entries linking line numbers to originating commit
	 */
	public List<BlameEntry> blame(RevCommit commit, String path) {
		try {
			Origin finalOrigin = new Origin(repository, commit, path);
			revWalk.sort(RevSort.TOPO);
			revWalk.reset();
			revWalk.markStart(commit);
			Scoreboard scoreboard = new Scoreboard(revWalk, finalOrigin,
					new JavaDiffImpl());
			return scoreboard.assingBlame();
		} catch (Exception e) {
			throw new RuntimeException("Internal error in BlameEngine", e);
		}
	}

	/**
	 * print it pretty
	 * 
	 * @param blameEntries
	 *            entries to print
	 */
	public void prettyPrint(List<BlameEntry> blameEntries) {
		int lineno = 1;
		for (BlameEntry blameEntry : blameEntries) {
			Object[] data = blameEntry.suspect.getData();

			for (int i = blameEntry.suspectStart; i < blameEntry.suspectStart
					+ blameEntry.originalRange.length; i++) {
				System.out.println(String.format("%3d: [%.120s] %s", Integer
						.valueOf(lineno), blameEntry.suspect, data[i]));
				lineno++;
			}
		}
	}

}
