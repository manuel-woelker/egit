package org.spearce.jgit.blame;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.spearce.jgit.lib.Repository;

public class BlameEngineTest extends TestCase {

	public void testBlame() throws Exception {
		Repository repository = new Repository(new File("../.git"));
		System.out.println(repository.getFullBranch());
		BlameEngine blameEngine = new BlameEngine();
		// List<BlameEntry> blame = blameEngine.blame(repository,
		// "EGIT_INSTALL");
		List<BlameEntry> blame = blameEngine.blame(repository,
				"SUBMITTING_PATCHES");
		// List<BlameEntry> blame = blameEngine.blame(repository,
		// "org.spearce.jgit/.classpath");
		// List<BlameEntry> blame = blameEngine.blame(repository, "TODO");
		for (BlameEntry blameEntry : blame) {
			for (int i = blameEntry.suspectStart; i < blameEntry.suspectStart
					+ blameEntry.originalRange.length; i++) {
				System.out.println(String.format("%.8s", blameEntry.suspect
						.getCommit().getCommitId().name()));
			}
		}
		blameEngine.prettyPrint(blame);
	}
}
