package org.spearce.jgit.blame;

import java.util.ArrayList;
import java.util.Collection;

class ScoreList<SCORE extends Comparable, VALUE> {

	final Entry[] entries;

	private static class Entry {
		Comparable<Object> score;

		Object value;
	}

	public ScoreList(int nMaxEntries) {
		entries = new Entry[nMaxEntries];
	}

	@SuppressWarnings("unchecked")
	public void add(SCORE score, VALUE value) {
		Comparable<Object> lowestScore = score;
		int lowestEntry = -1;
		for (int i = 0; i < entries.length; i++) {
			Entry entry = entries[i];
			if (entry == null) {
				lowestEntry = i;
				break;
			}
			if (lowestScore.compareTo(entry.score) > 0) {
				lowestScore = entry.score;
				lowestEntry = i;
			}
		}
		if (lowestEntry >= 0) {
			Entry entry = new Entry();
			entry.score = score;
			entry.value = value;
			entries[lowestEntry] = entry;
		}
	}

	@SuppressWarnings("unchecked")
	Collection<VALUE> getEntries() {
		ArrayList<VALUE> result = new ArrayList<VALUE>();
		for (int i = 0; i < entries.length; i++) {
			Entry entry = entries[i];
			if (entry != null) {
				result.add((VALUE) entry.value);
			}
		}
		return result;
	}
}
