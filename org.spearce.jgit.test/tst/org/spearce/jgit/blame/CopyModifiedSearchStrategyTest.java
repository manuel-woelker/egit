package org.spearce.jgit.blame;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.spearce.jgit.revwalk.RevCommit;
import org.spearce.jgit.revwalk.RevWalk;

public class CopyModifiedSearchStrategyTest extends RepositoryTestCase {
	private static final String ENCODING = "UTF-8";

	public void testPerfectMatch() throws Exception {
		Repository repo = createNewEmptyRepo();
		String a = "a 1\na 2\na 3\n";
		String filenames[] = new String[] { "first", "second" };
		String versions[] = new String[] { a, a };
		ObjectId lastCommitId = null;
		ObjectWriter objectWriter = new ObjectWriter(repo);
		ArrayList<ObjectId> commitIds = new ArrayList<ObjectId>();
		HashMap<String, ObjectId> commitMap = new HashMap<String, ObjectId>();
		int i = 0;
		for (String version : versions) {
			final ObjectId id = new ObjectWriter(repo).writeBlob(version
					.getBytes(ENCODING));
			final Tree tree = new Tree(repo);
			tree.addEntry(new FileTreeEntry(tree, id, filenames[i]
					.getBytes(ENCODING), false));
			final ObjectId treeId = objectWriter.writeTree(tree);
			final Commit commit = new Commit(repo);
			commit.setAuthor(new PersonIdent("", "", 0, 0));
			commit.setCommitter(new PersonIdent("", "", 0, 0));
			commit.setMessage("test");
			commit.setTreeId(treeId);
			if (lastCommitId != null) {
				commit.setParentIds(new ObjectId[] { lastCommitId });
			}
			ObjectId commitId = objectWriter.writeCommit(commit);
			commitMap.put(filenames[i], commitId);
			lastCommitId = commitId;
			commitIds.add(commitId);
			i++;
		}
		RevWalk revWalk = new RevWalk(repo);
		RevCommit latestCommit = revWalk.parseCommit(lastCommitId);
		CopyModifiedSearchStrategy strategy = new CopyModifiedSearchStrategy();
		Origin[] origins = strategy.findOrigins(new Origin(repo, latestCommit,
				"second"));
		List<Origin> actual = Arrays.asList(origins);
		RevCommit firstCommit = revWalk.parseCommit(commitMap.get("first"));
		List<Origin> expected = Arrays.asList(new Origin(repo, firstCommit,
				"first"));
		assertEquals(expected, actual);
	}

	public void testSimilar() throws Exception {
		Repository repo = createNewEmptyRepo();
		String a = "a 1\na 2\na 3\n";
		String b = "b 1\n";
		String filenames[] = new String[] { "first", "second" };
		String versions[] = new String[] { a, a + b };
		ObjectId lastCommitId = null;
		ObjectWriter objectWriter = new ObjectWriter(repo);
		ArrayList<ObjectId> commitIds = new ArrayList<ObjectId>();
		HashMap<String, ObjectId> commitMap = new HashMap<String, ObjectId>();
		int i = 0;
		for (String version : versions) {
			final ObjectId id = new ObjectWriter(repo).writeBlob(version
					.getBytes(ENCODING));
			final Tree tree = new Tree(repo);
			tree.addEntry(new FileTreeEntry(tree, id, filenames[i]
					.getBytes(ENCODING), false));
			final ObjectId treeId = objectWriter.writeTree(tree);
			final Commit commit = new Commit(repo);
			commit.setAuthor(new PersonIdent("", "", 0, 0));
			commit.setCommitter(new PersonIdent("", "", 0, 0));
			commit.setMessage("test");
			commit.setTreeId(treeId);
			if (lastCommitId != null) {
				commit.setParentIds(new ObjectId[] { lastCommitId });
			}
			ObjectId commitId = objectWriter.writeCommit(commit);
			commitMap.put(filenames[i], commitId);
			lastCommitId = commitId;
			commitIds.add(commitId);
			i++;
		}
		RevWalk revWalk = new RevWalk(repo);
		RevCommit latestCommit = revWalk.parseCommit(lastCommitId);
		CopyModifiedSearchStrategy strategy = new CopyModifiedSearchStrategy();
		Origin[] origins = strategy.findOrigins(new Origin(repo, latestCommit,
				"second"));
		List<Origin> actual = Arrays.asList(origins);
		RevCommit firstCommit = revWalk.parseCommit(commitMap.get("first"));
		List<Origin> expected = Arrays.asList(new Origin(repo, firstCommit,
				"first"));
		assertEquals(expected, actual);
	}

	public void testTwoSimilar() throws Exception {
		Repository repo = createNewEmptyRepo();
		String a = "a 1\na 2\na 3\n";
		String b = "b 1\n";
		String c = "c 1\n";
		String x = "x 1\n";
		String filenames[][] = new String[][] {
				new String[] { "firstb", "firstc" }, new String[] { "second" } };
		String versions[][] = new String[][] { new String[] { a + b, a + c },
				new String[] { a + x } };
		ObjectId lastCommitId = null;
		ObjectWriter objectWriter = new ObjectWriter(repo);
		ArrayList<ObjectId> commitIds = new ArrayList<ObjectId>();
		HashMap<String, ObjectId> commitMap = new HashMap<String, ObjectId>();
		int i = 0;
		for (String[] version : versions) {
			final Tree tree = new Tree(repo);
			int j = 0;
			for (String file : version) {
				final ObjectId id = new ObjectWriter(repo).writeBlob(file
						.getBytes(ENCODING));
				tree.addEntry(new FileTreeEntry(tree, id, filenames[i][j]
						.getBytes(ENCODING), false));
				j++;
			}
			final ObjectId treeId = objectWriter.writeTree(tree);
			final Commit commit = new Commit(repo);
			commit.setAuthor(new PersonIdent("", "", 0, 0));
			commit.setCommitter(new PersonIdent("", "", 0, 0));
			commit.setMessage("test");
			commit.setTreeId(treeId);
			if (lastCommitId != null) {
				commit.setParentIds(new ObjectId[] { lastCommitId });
			}
			ObjectId commitId = objectWriter.writeCommit(commit);
			commitMap.put(filenames[i][0], commitId);
			lastCommitId = commitId;
			commitIds.add(commitId);
			i++;
		}
		RevWalk revWalk = new RevWalk(repo);
		RevCommit latestCommit = revWalk.parseCommit(lastCommitId);
		CopyModifiedSearchStrategy strategy = new CopyModifiedSearchStrategy();
		Origin[] origins = strategy.findOrigins(new Origin(repo, latestCommit,
				"second"));
		List<Origin> actual = Arrays.asList(origins);
		RevCommit firstCommit = revWalk.parseCommit(commitMap.get("firstb"));
		List<Origin> expected = Arrays.asList(new Origin(repo, firstCommit,
				"firstb"), new Origin(repo, firstCommit, "firstc"));
		assertEquals(expected, actual);
	}
}
