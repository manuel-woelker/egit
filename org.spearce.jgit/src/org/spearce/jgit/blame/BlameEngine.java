package org.spearce.jgit.blame;

import java.util.List;

import org.spearce.jgit.lib.Commit;
import org.spearce.jgit.lib.Constants;
import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.lib.Repository;

/**
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 * 
 */
public class BlameEngine {
	/**
	 * @param repository
	 *            git repository
	 * @param path
	 *            file path
	 * @return list of blame entries linking line numbers to originating commit
	 */
	public List<BlameEntry> blame(Repository repository, String path) {
		try {
			ObjectId headId = repository.resolve(Constants.HEAD);
			Commit lastCommit = repository.mapCommit(headId);
			CommitOrigin finalOrigin = new CommitOrigin(lastCommit, path);
			Scoreboard scoreboard = new Scoreboard(finalOrigin,
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
				System.out.println(String.format("%3d: [%.59s] %s", Integer
						.valueOf(lineno), blameEntry.suspect, data[i]));
				lineno++;
			}
		}
	}

}
