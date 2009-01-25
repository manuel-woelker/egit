package org.spearce.jgit.blame;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.spearce.jgit.lib.Commit;
import org.spearce.jgit.lib.FileTreeEntry;
import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.lib.ObjectWriter;
import org.spearce.jgit.lib.PersonIdent;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.lib.RepositoryTestCase;
import org.spearce.jgit.lib.Tree;
import org.spearce.jgit.revwalk.RevWalk;

public class BlameEngineTest extends RepositoryTestCase {

	private static final String ENCODING = "UTF-8";

	public void testBlame() throws Exception {
		Repository repository = new Repository(new File("../.git"));
		System.out.println(repository.getFullBranch());
		BlameEngine blameEngine = new BlameEngine(repository);
//		List<BlameEntry> blame = blameEngine.blame("EGIT_INSTALL");
		 List<BlameEntry> blame = blameEngine.blame("SUBMITTING_PATCHES");
		// List<BlameEntry> blame = blameEngine.blame(repository,
		// "org.spearce.jgit/.classpath");
		// List<BlameEntry> blame = blameEngine.blame(repository, "TODO");
		for (BlameEntry blameEntry : blame) {
			for (int i = blameEntry.suspectStart; i < blameEntry.suspectStart
					+ blameEntry.originalRange.length; i++) {
				System.out.println(String.format("%.8s", blameEntry.suspect
						.getCommit().name()));
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
					.getBytes(ENCODING));
			final Tree tree = new Tree(repo);
			tree.addEntry(new FileTreeEntry(tree, id,
					"test".getBytes(ENCODING), false));
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
		BlameEngine blameEngine = new BlameEngine(repo);
		RevWalk revWalk = new RevWalk(repo);
		List<BlameEntry> blame = blameEngine.blame(revWalk
				.parseCommit(latestCommit.getCommitId()), "test");
		ObjectId[] expectedCommitIds = new ObjectId[] { commitIds.get(2),
				commitIds.get(0), commitIds.get(1) };
		Range[] expectedRanges = new Range[] { new Range(0, 3),
				new Range(3, 3), new Range(6, 3) };
		int[] expectedSuspectStarts = new int[] { 0, 0, 3 };
		for (i = 0; i < expectedCommitIds.length; i++) {
			BlameEntry blameEntry = blame.get(i);
			assertTrue(blameEntry.guilty);
			assertEquals(expectedCommitIds[i], blameEntry.suspect.getCommit()
					.copy());
			assertEquals(expectedRanges[i], blameEntry.originalRange);
			assertEquals("entry " + i, expectedSuspectStarts[i],
					blameEntry.suspectStart);
		}
	}

	public void testMerge() throws Exception {
		// Classic diamond merge ( dots just to keep formatting)
		// .... a
		// ... / \
		// . ba . ac
		// ... \ /
		// ... bac

		Repository repo = createNewEmptyRepo();
		String a = "a 1\na 2\na 3\n";
		String b = "b 1\nb 2\nb 3\n";
		String c = "c 1\nc 2\nc 3\n";

		String authors[] = new String[] { "a", "ba", "ac", "bac" };
		String versions[] = new String[] { a, b + a, a + c, b + a + c };
		// used to find commit ids
		String parentCommitIdAuthors[] = new String[] { "", "a", "a", "ba,ac" };
		ObjectWriter objectWriter = new ObjectWriter(repo);
		ArrayList<ObjectId> commitIds = new ArrayList<ObjectId>();
		HashMap<String, ObjectId> commitMap = new HashMap<String, ObjectId>();
		int i = 0;
		for (String version : versions) {
			final ObjectId id = new ObjectWriter(repo).writeBlob(version
					.getBytes(ENCODING));
			final Tree tree = new Tree(repo);
			tree.addEntry(new FileTreeEntry(tree, id,
					"test".getBytes(ENCODING), false));
			final ObjectId treeId = objectWriter.writeTree(tree);
			final Commit commit = new Commit(repo);
			commit.setAuthor(new PersonIdent(authors[i], "", 0, 0));
			commit.setCommitter(new PersonIdent(authors[i], "", 0, 0));
			commit.setMessage("test022\n");
			commit.setTreeId(treeId);
			ArrayList<ObjectId> parentIds = new ArrayList<ObjectId>();
			for (String author : parentCommitIdAuthors[i].split(",")) {
				if (author.length() > 0) {
					parentIds.add(commitMap.get(author));
				}
			}
			commit.setParentIds(parentIds.toArray(new ObjectId[0]));
			ObjectId commitId = objectWriter.writeCommit(commit);
			commitMap.put(authors[i], commitId);
			commitIds.add(commitId);
			i++;
		}
		Commit latestCommit = repo.mapCommit(commitMap.get("bac"));
		BlameEngine blameEngine = new BlameEngine(repo);
		RevWalk revWalk = new RevWalk(repo);
		List<BlameEntry> blame = blameEngine.blame(revWalk
				.parseCommit(latestCommit.getCommitId()), "test");
		blameEngine.prettyPrint(blame);
		ObjectId[] expectedCommitIds = new ObjectId[] { commitMap.get("ba"),
				commitMap.get("a"), commitMap.get("ac") };
		Range[] expectedRanges = new Range[] { new Range(0, 3),
				new Range(3, 3), new Range(6, 3) };
		int[] expectedSuspectStarts = new int[] { 0, 0, 3 };
		for (i = 0; i < expectedCommitIds.length; i++) {
			BlameEntry blameEntry = blame.get(i);
			assertTrue(blameEntry.guilty);
			assertEquals(expectedCommitIds[i], blameEntry.suspect.getCommit()
					.copy());
			assertEquals(expectedRanges[i], blameEntry.originalRange);
			assertEquals("entry " + i, expectedSuspectStarts[i],
					blameEntry.suspectStart);
		}
	}

	public void testMergeWithModifications() throws Exception {
		// Classic diamond merge ( dots just to keep formatting)
		// .... a
		// ... / \
		// . ba . ac
		// ... \ /
		// .. bxayc

		Repository repo = createNewEmptyRepo();
		String a = "a 1\na 2\na 3\n";
		String b = "b 1\nb 2\nb 3\n";
		String c = "c 1\nc 2\nc 3\n";
		String x = "x 1\nx 2\nx 3\n";
		String y = "y 1\ny 2\ny 3\n";

		String authors[] = new String[] { "a", "ba", "ac", "bxayc" };
		String versions[] = new String[] { a, b + a, a + c, b + x + a + y + c };
		// used to find commit ids
		String parentCommitIdAuthors[] = new String[] { "", "a", "a", "ba,ac" };
		ObjectWriter objectWriter = new ObjectWriter(repo);
		ArrayList<ObjectId> commitIds = new ArrayList<ObjectId>();
		HashMap<String, ObjectId> commitMap = new HashMap<String, ObjectId>();
		int i = 0;
		for (String version : versions) {
			final ObjectId id = new ObjectWriter(repo).writeBlob(version
					.getBytes(ENCODING));
			final Tree tree = new Tree(repo);
			tree.addEntry(new FileTreeEntry(tree, id,
					"test".getBytes(ENCODING), false));
			final ObjectId treeId = objectWriter.writeTree(tree);
			final Commit commit = new Commit(repo);
			commit.setAuthor(new PersonIdent(authors[i], "", 0, 0));
			commit.setCommitter(new PersonIdent(authors[i], "", 0, 0));
			commit.setMessage("test022\n");
			commit.setTreeId(treeId);
			ArrayList<ObjectId> parentIds = new ArrayList<ObjectId>();
			for (String author : parentCommitIdAuthors[i].split(",")) {
				if (author.length() > 0) {
					parentIds.add(commitMap.get(author));
				}
			}
			commit.setParentIds(parentIds.toArray(new ObjectId[0]));
			ObjectId commitId = objectWriter.writeCommit(commit);
			commitMap.put(authors[i], commitId);
			commitIds.add(commitId);
			i++;
		}
		Commit latestCommit = repo.mapCommit(commitMap.get("bxayc"));
		BlameEngine blameEngine = new BlameEngine(repo);
		RevWalk revWalk = new RevWalk(repo);
		List<BlameEntry> blame = blameEngine.blame(revWalk
				.parseCommit(latestCommit.getCommitId()), "test");
		blameEngine.prettyPrint(blame);
		ObjectId[] expectedCommitIds = new ObjectId[] { commitMap.get("ba"),
				commitMap.get("bxayc"), commitMap.get("a"),
				commitMap.get("bxayc"), commitMap.get("ac") };
		Range[] expectedRanges = new Range[] { new Range(0, 3),
				new Range(3, 3), new Range(6, 3), new Range(9, 3),
				new Range(12, 3) };
		int[] expectedSuspectStarts = new int[] { 0, 3, 0, 9, 3 };
		for (i = 0; i < expectedCommitIds.length; i++) {
			BlameEntry blameEntry = blame.get(i);
			assertTrue(blameEntry.guilty);
			assertEquals(expectedCommitIds[i], blameEntry.suspect.getCommit()
					.copy());
			assertEquals(expectedRanges[i], blameEntry.originalRange);
			assertEquals("entry " + i, expectedSuspectStarts[i],
					blameEntry.suspectStart);
		}
	}
}
