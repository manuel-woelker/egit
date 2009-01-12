package org.spearce.jgit.blame;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.lib.ObjectLoader;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.lib.TreeEntry;
import org.spearce.jgit.revwalk.RevCommit;
import org.spearce.jgit.revwalk.RevTree;

/**
 * Origin object representing the origin of a part of the file, usually there
 * should be one origin for each commit and path
 * 
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 * 
 */
public class Origin {

	final RevCommit commit;

	/**
	 * @return the associated commit
	 */
	public RevCommit getCommit() {
		return commit;
	}

	final String filename;

	private final Repository repository;

	/**
	 * creates a new Commit origin for a given commit and path
	 * 
	 * @param repository
	 *            git repository for this origin
	 * @param commit
	 *            the commit object for this origin
	 * @param filename
	 *            the path of the file in this commit
	 */
	public Origin(Repository repository, RevCommit commit, String filename) {
		super();
		this.repository = repository;
		this.commit = commit;
		this.filename = filename;
	}

	/**
	 * get the ObjectId of the file, used for identifying identical versions
	 * 
	 * @return object id
	 */
	public ObjectId getObjectId() {
		try {
			RevTree revTree = commit.getTree();
			TreeEntry blobEntry = repository.mapTree(revTree).findBlobMember(
					filename);
			if (blobEntry == null) {
				return ObjectId.zeroId();
			}
			return blobEntry.getId();
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving data for origin "
					+ this, e);
		}
	}

	/**
	 * get the file contents at this commit
	 * 
	 * @return an array of strings containing the file lines
	 */
	public Object[] getData() {
		try {
			RevTree revTree = commit.getTree();
			TreeEntry blobEntry = repository.mapTree(revTree).findBlobMember(
					filename);
			if (blobEntry == null) {
				// does not exist yet
				return new Object[0];
			}
			ObjectLoader objectLoader = repository.openBlob(blobEntry.getId());
			String string = new String(objectLoader.getBytes());
			Pattern pattern = Pattern.compile("^(.*)$", Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(string);
			ArrayList<String> resultList = new ArrayList<String>();
			while (matcher.find()) {
				resultList.add(matcher.group(1));
			}
			return resultList.toArray();
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving data for origin "
					+ this);
		}
	}

	@Override
	public String toString() {
		return filename + " --> " + commit;
	}

}