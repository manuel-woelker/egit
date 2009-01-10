package org.spearce.jgit.blame;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spearce.jgit.lib.Commit;
import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.lib.ObjectLoader;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.lib.TreeEntry;

/**
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 * 
 */
public class CommitOrigin implements IOrigin {

	final Commit commit;

	/**
	 * @return the associated commit
	 */
	public Commit getCommit() {
		return commit;
	}

	private final String filename;

	/**
	 * @param commit
	 * @param filename
	 */
	public CommitOrigin(Commit commit, String filename) {
		super();
		this.commit = commit;
		this.filename = filename;
	}

	public ObjectId getObjectId() {
		try {
			TreeEntry blobEntry = commit.getTree().findBlobMember(filename);
			if (blobEntry == null) {
				return ObjectId.zeroId();
			}
			return blobEntry.getId();
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving data for origin "
					+ this);
		}
	}

	public Object[] getData() {
		try {
			Repository repository = commit.getTree().getRepository();
			TreeEntry blobEntry = commit.getTree().findBlobMember(filename);
			if (blobEntry == null) {
				// does not exist yet
				return new Object[0];
			}
			ObjectLoader objectLoader = repository.openBlob(blobEntry.getId());
			String string = new String(objectLoader.getBytes());
			Pattern pattern = Pattern.compile("^(.*)$",Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(string);
			ArrayList<String> resultList = new ArrayList<String>();
			while(matcher.find()) {
				resultList.add(matcher.group(1));
			}
			return resultList.toArray();
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving data for origin "
					+ this);
		}
	}

	public IOrigin[] getParents() {
		try {
			ArrayList<IOrigin> resultList = new ArrayList<IOrigin>();
			Repository repository = commit.getTree().getRepository();
			for (ObjectId objectId : commit.getParentIds()) {
				Commit parentCommit = repository.mapCommit(objectId);
				resultList.add(new CommitOrigin(parentCommit, filename));
			}
			return resultList.toArray(new IOrigin[0]);
		} catch (Exception e) {
			throw new RuntimeException("could not retrieve parents of commit "
					+ commit, e);
		}
	}

	@Override
	public String toString() {

		return filename + " --> " + commit;
	}

}