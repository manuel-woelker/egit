package org.spearce.jgit.blame;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import junit.framework.TestCase;

public class ScoreListTest extends TestCase {

	public void testEmpty() throws Exception {
		ScoreList<Integer, String> scoreList = new ScoreList<Integer, String>(3);
		assertEquals(Collections.EMPTY_LIST, scoreList.getEntries());
	}

	public void testSimple() throws Exception {
		ScoreList<Integer, String> scoreList = new ScoreList<Integer, String>(3);
		scoreList.add(Integer.valueOf(1), "one");
		assertEquals(Arrays.asList("one"), scoreList.getEntries());
	}

	public void testSimple2() throws Exception {
		ScoreList<Integer, String> scoreList = new ScoreList<Integer, String>(3);
		scoreList.add(Integer.valueOf(1), "one");
		scoreList.add(Integer.valueOf(2), "two");
		assertEquals(new HashSet(Arrays.asList("one", "two")), new HashSet(
				scoreList.getEntries()));
	}

	public void testMulti() throws Exception {
		ScoreList<Integer, String> scoreList = new ScoreList<Integer, String>(3);
		scoreList.add(Integer.valueOf(1), "one");
		scoreList.add(Integer.valueOf(2), "two");
		scoreList.add(Integer.valueOf(3), "three");
		scoreList.add(Integer.valueOf(4), "four");
		scoreList.add(Integer.valueOf(5), "five");
		assertEquals(new HashSet(Arrays.asList("three", "four", "five")),
				new HashSet(scoreList.getEntries()));
	}

	public void testMulti2() throws Exception {
		ScoreList<Integer, String> scoreList = new ScoreList<Integer, String>(3);
		scoreList.add(Integer.valueOf(5), "five");
		scoreList.add(Integer.valueOf(3), "three");
		scoreList.add(Integer.valueOf(4), "four");
		scoreList.add(Integer.valueOf(1), "one");
		scoreList.add(Integer.valueOf(2), "two");
		assertEquals(new HashSet(Arrays.asList("three", "four", "five")),
				new HashSet(scoreList.getEntries()));
	}

	public void testMulti3() throws Exception {
		ScoreList<Integer, String> scoreList = new ScoreList<Integer, String>(3);
		scoreList.add(Integer.valueOf(5), "five");
		scoreList.add(Integer.valueOf(3), "three");
		scoreList.add(Integer.valueOf(1), "one");
		scoreList.add(Integer.valueOf(4), "four");
		scoreList.add(Integer.valueOf(2), "two");
		assertEquals(new HashSet(Arrays.asList("three", "four", "five")),
				new HashSet(scoreList.getEntries()));
	}

	public void testFCFS() throws Exception {
		ScoreList<Integer, String> scoreList = new ScoreList<Integer, String>(1);
		scoreList.add(Integer.valueOf(2), "a");
		scoreList.add(Integer.valueOf(2), "b");
		assertEquals(new HashSet(Arrays.asList("a")), new HashSet(scoreList
				.getEntries()));
	}
}