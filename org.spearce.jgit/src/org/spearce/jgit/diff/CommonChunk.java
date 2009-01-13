/*
 * Copyright (C) 2008, Manuel Woelker <manuel.woelker@gmail.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Git Development Community nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.spearce.jgit.diff;

/**
 * 
 */
public class CommonChunk {
	final int astart;

	final int bstart;

	final int length;

	/**
	 * @param astart
	 * @param bstart
	 * @param length
	 */
	public CommonChunk(int astart, int bstart, int length) {
		super();
		this.astart = astart;
		this.bstart = bstart;
		this.length = length;
	}

	@Override
	public String toString() {
		return String.format("Common <%d:%d  %d>", Integer.valueOf(astart),
				Integer.valueOf(bstart), Integer.valueOf(length));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + astart;
		result = prime * result + bstart;
		result = prime * result + length;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommonChunk other = (CommonChunk) obj;
		if (astart != other.astart)
			return false;
		if (bstart != other.bstart)
			return false;
		if (length != other.length)
			return false;
		return true;
	}

	/**
	 * @return start of chunk in B
	 */
	public int getAstart() {
		return astart;
	}

	/**
	 * @return start of chunk in B
	 */
	public int getBstart() {
		return bstart;
	}

	/**
	 * @return length of common chunk
	 */
	public int getLength() {
		return length;
	}

}