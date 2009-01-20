package org.spearce.jgit.log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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

public class OriginWalkTest extends RepositoryTestCase {

	private static final String ENCODING = "UTF-8";

	public void testSameName() throws Exception {
		Repository repo = createNewEmptyRepo();
		String a = "a 1\na 2\na 3\n";
		String filenames[] = new String[] { "first", "first" };
		String versions[] = new String[] { a, a + a };
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
			commit.setAuthor(new PersonIdent("" + i, "", 0, 0));
			commit.setCommitter(new PersonIdent("" + i, "", 0, 0));
			commit.setMessage("test " + i);
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
		OriginWalk originWalk = new OriginWalk(new Origin(repo, latestCommit,
				"first"), repo);
		ArrayList<Origin> actual = new ArrayList<Origin>();
		ArrayList<Origin[]> actualAncestors = new ArrayList<Origin[]>();
		while (originWalk.hasNext()) {
			actual.add(originWalk.next());
			actualAncestors.add(originWalk.getAncestorOrigins());
		}
		List<Origin> expected = new ArrayList<Origin>();
		for (ObjectId commitId : commitIds) {
			RevCommit commit = revWalk.parseCommit(commitId);
			expected.add(new Origin(repo, commit, "first"));
		}
		Collections.reverse(expected);
		LinkedList<Origin[]> expectedAncestors = new LinkedList<Origin[]>();
		for (Origin origin : expected) {
			expectedAncestors.add(new Origin[] { origin });
		}
		expectedAncestors.removeFirst();
		expectedAncestors.addLast(new Origin[0]);
		i = 0;
		for (Origin[] expectedAncestorOrigins : expectedAncestors) {
			assertTrue("Ancestors differ at position " + i, Arrays.equals(
					expectedAncestorOrigins, actualAncestors.get(i)));
			i++;
		}

		assertEquals(expected, actual);
	}

	public void testSameNameSkipSameVersion() throws Exception {
		Repository repo = createNewEmptyRepo();
		String a = "a 1\na 2\na 3\n";
		String filenames[] = new String[] { "first", "first", "first", "first" };
		String versions[] = new String[] { a, a, a + a, a + a };
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
			commit.setMessage("test " + i);
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
		OriginWalk originWalk = new OriginWalk(new Origin(repo, latestCommit,
				"first"), repo);
		ArrayList<Origin> actual = new ArrayList<Origin>();
		while (originWalk.hasNext()) {
			actual.add(originWalk.next());
		}
		List<Origin> expected = new ArrayList<Origin>();
		for (ObjectId commitId : commitIds) {
			RevCommit commit = revWalk.parseCommit(commitId);
			expected.add(new Origin(repo, commit, "first"));
		}
		expected.remove(3);
		expected.remove(1);
		Collections.reverse(expected);
		assertEquals(expected, actual);
	}
}
