package org.spearce.jgit.blame;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.spearce.jgit.lib.Commit;
import org.spearce.jgit.lib.FileTreeEntry;
import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.lib.ObjectWriter;
import org.spearce.jgit.lib.PersonIdent;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.lib.RepositoryTestCase;
import org.spearce.jgit.lib.Tree;

public class BlameEngineTest extends RepositoryTestCase {

	public void disabledTestBlame() throws Exception {
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

	public void testSimple() throws Exception {
		Repository repo = createNewEmptyRepo();
		String a = "a 1\na 2\na 3\n";
		String b = a + "b 1\nb 2\nb 3";
		String c = "c 1\nc 2\nc 3\n" + b;
		String authors[] = new String[] { "a", "b", "c" };
		String versions[] = new String[] { a, b, c };
		ObjectId lastCommitId = null;
		ObjectWriter objectWriter = new ObjectWriter(repo);
		ArrayList<ObjectId> commitIds = new ArrayList<ObjectId>();
		int i = 0;
		for (String version : versions) {
			final ObjectId id = new ObjectWriter(repo).writeBlob(version
					.getBytes("UTF-8"));
			final Tree tree = new Tree(repo);
			tree.addEntry(new FileTreeEntry(tree, id, "test".getBytes("UTF-8"),
					false));
			final ObjectId treeId = objectWriter.writeTree(tree);
			final Commit commit = new Commit(repo);
			commit.setAuthor(new PersonIdent(authors[i], "", 0, 0));
			commit.setCommitter(new PersonIdent(authors[i], "", 0, 0));
			commit.setMessage("test022\n");
			commit.setTreeId(treeId);
			if (lastCommitId != null) {
				commit.setParentIds(new ObjectId[] { lastCommitId });
			}
			ObjectId commitId = objectWriter.writeCommit(commit);
			lastCommitId = commitId;
			commitIds.add(commitId);
			i++;
		}
		Commit latestCommit = repo.mapCommit(lastCommitId);
		BlameEngine blameEngine = new BlameEngine();
		List<BlameEntry> blame = blameEngine.blame(latestCommit, "test");
		ObjectId[] expectedCommitIds = new ObjectId[] { commitIds.get(2),
				commitIds.get(0), commitIds.get(1) };
		Range[] expectedRanges = new Range[] { new Range(0, 3),
				new Range(3, 3), new Range(6, 3) };
		int[] expectedSuspectStarts = new int[] { 0, 0, 3 };
		for (i = 0; i < expectedCommitIds.length; i++) {
			BlameEntry blameEntry = blame.get(i);
			assertTrue(blameEntry.guilty);
			assertEquals(expectedCommitIds[i], blameEntry.suspect.commit
					.getCommitId());
			assertEquals(expectedRanges[i], blameEntry.originalRange);
			assertEquals("entry " + i, expectedSuspectStarts[i],
					blameEntry.suspectStart);
		}
	}
}
