package org.spearce.jgit.transport;

import junit.framework.TestCase;

public class URIishTest extends TestCase {

	public void testUnixFile() throws Exception {
		final String str = "/home/m y";
		URIish u = new URIish(str);
		assertNull(u.getScheme());
		assertFalse(u.isRemote());
		assertEquals(str, u.getPath());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testWindowsFile() throws Exception {
		final String str = "D:/m y";
		URIish u = new URIish(str);
		assertNull(u.getScheme());
		assertFalse(u.isRemote());
		assertEquals(str, u.getPath());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testFileProtoUnix() throws Exception {
		final String str = "file:///home/m y";
		URIish u = new URIish(str);
		assertEquals("file", u.getScheme());
		assertFalse(u.isRemote());
		assertEquals("/home/m y", u.getPath());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testFileProtoWindows() throws Exception {
		final String str = "file:///D:/m y";
		URIish u = new URIish(str);
		assertEquals("file", u.getScheme());
		assertFalse(u.isRemote());
		assertEquals("D:/m y", u.getPath());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testGitProtoUnix() throws Exception {
		final String str = "git://example.com/home/m y";
		URIish u = new URIish(str);
		assertEquals("git", u.getScheme());
		assertTrue(u.isRemote());
		assertEquals("example.com", u.getHost());
		assertEquals("/home/m y", u.getPath());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testGitProtoUnixPort() throws Exception {
		final String str = "git://example.com:333/home/m y";
		URIish u = new URIish(str);
		assertEquals("git", u.getScheme());
		assertTrue(u.isRemote());
		assertEquals("example.com", u.getHost());
		assertEquals("/home/m y", u.getPath());
		assertEquals(333, u.getPort());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testGitProtoWindowsPort() throws Exception {
		final String str = "git://example.com:338/D:/m y";
		URIish u = new URIish(str);
		assertEquals("git", u.getScheme());
		assertTrue(u.isRemote());
		assertEquals("D:/m y", u.getPath());
		assertEquals(338, u.getPort());
		assertEquals("example.com", u.getHost());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testGitProtoWindows() throws Exception {
		final String str = "git://example.com/D:/m y";
		URIish u = new URIish(str);
		assertEquals("git", u.getScheme());
		assertTrue(u.isRemote());
		assertEquals("D:/m y", u.getPath());
		assertEquals("example.com", u.getHost());
		assertEquals(-1, u.getPort());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testScpStyleWithoutUser() throws Exception {
		final String str = "example.com:some/p ath";
		URIish u = new URIish(str);
		assertNull(u.getScheme());
		assertTrue(u.isRemote());
		assertEquals("some/p ath", u.getPath());
		assertEquals("example.com", u.getHost());
		assertEquals(-1, u.getPort());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testScpStyleWithUser() throws Exception {
		final String str = "user@example.com:some/p ath";
		URIish u = new URIish(str);
		assertNull(u.getScheme());
		assertTrue(u.isRemote());
		assertEquals("some/p ath", u.getPath());
		assertEquals("user", u.getUser());
		assertEquals("example.com", u.getHost());
		assertEquals(-1, u.getPort());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testGitSshProto() throws Exception {
		final String str = "git+ssh://example.com/some/p ath";
		URIish u = new URIish(str);
		assertEquals("git+ssh", u.getScheme());
		assertTrue(u.isRemote());
		assertEquals("/some/p ath", u.getPath());
		assertEquals("example.com", u.getHost());
		assertEquals(-1, u.getPort());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testSshGitProto() throws Exception {
		final String str = "ssh+git://example.com/some/p ath";
		URIish u = new URIish(str);
		assertEquals("ssh+git", u.getScheme());
		assertTrue(u.isRemote());
		assertEquals("/some/p ath", u.getPath());
		assertEquals("example.com", u.getHost());
		assertEquals(-1, u.getPort());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testSshProto() throws Exception {
		final String str = "ssh://example.com/some/p ath";
		URIish u = new URIish(str);
		assertEquals("ssh", u.getScheme());
		assertTrue(u.isRemote());
		assertEquals("/some/p ath", u.getPath());
		assertEquals("example.com", u.getHost());
		assertEquals(-1, u.getPort());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testSshProtoWithUserAndPort() throws Exception {
		final String str = "ssh://user@example.com:33/some/p ath";
		URIish u = new URIish(str);
		assertEquals("ssh", u.getScheme());
		assertTrue(u.isRemote());
		assertEquals("/some/p ath", u.getPath());
		assertEquals("example.com", u.getHost());
		assertEquals("user", u.getUser());
		assertNull(u.getPass());
		assertEquals(33, u.getPort());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}

	public void testSshProtoWithUserPassAndPort() throws Exception {
		final String str = "ssh://user:pass@example.com:33/some/p ath";
		URIish u = new URIish(str);
		assertEquals("ssh", u.getScheme());
		assertTrue(u.isRemote());
		assertEquals("/some/p ath", u.getPath());
		assertEquals("example.com", u.getHost());
		assertEquals("user", u.getUser());
		assertEquals("pass", u.getPass());
		assertEquals(33, u.getPort());
		assertEquals(str, u.toString());
		assertEquals(u, new URIish(str));
	}
}