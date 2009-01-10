package org.spearce.jgit.blame;

import java.util.List;

import org.spearce.jgit.lib.Repository;

/**
 * @author Manuel Woelker <manuel.woelker+github@gmail.com>
 *
 */
public class BlameEngine {
	/**
	 * @param repository git repository
	 * @param path file path
	 * @return list of blame entries linking line numbers to originating commit
	 */
	public List<BlameEntry> blame(Repository repository, String path) {
		Scoreboard scoreboard = new Scoreboard(/* TODO: implement */ null,null);
		return scoreboard.assingBlame();		
	}

}
