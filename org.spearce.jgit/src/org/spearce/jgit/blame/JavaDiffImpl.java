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
package org.spearce.jgit.blame;

import java.util.List;

import org.incava.util.diff.Diff;
import org.incava.util.diff.Difference;

/**
 * Diff implementation backed by the incava diff library
 * 
 * @see "http://www.incava.org/projects/java/java-diff/"
 * 
 */
public class JavaDiffImpl implements IDiff {
	private static class JavaDifferenceImpl implements IDifference {
		final Difference difference;

		public JavaDifferenceImpl(Difference difference) {
			super();
			this.difference = difference;
		}

		public int getEndA() {
			return difference.getDeletedEnd();
		}

		public int getEndB() {
			return difference.getAddedEnd();
		}

		public int getStartA() {
			return difference.getDeletedStart();
		}

		public int getStartB() {
			return difference.getAddedStart();
		}

		@Override
		public String toString() {

			return String.format("Diff <%d,%d  %d,%d>", Integer
					.valueOf(getStartA()), Integer.valueOf(getEndA()), Integer
					.valueOf(getStartB()), Integer.valueOf(getEndB()));
		}

	}

	@SuppressWarnings("unchecked")
	public IDifference[] diff(Object[] a, Object[] b) {
		Diff diff = new Diff(a, b);
		List<Difference> differences = diff.diff();
		IDifference[] result = new IDifference[differences.size()];
		int i = 0;
		for (Difference difference : differences) {
			result[i] = new JavaDifferenceImpl(difference);
			i++;
		}
		return result;
	}

}
