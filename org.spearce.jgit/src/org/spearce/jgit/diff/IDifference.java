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
 * Difference interface, represents a run of different lines between to objects
 * (think files).
 * 
 * This represents one entry in a diff, a complete diff between two files would
 * be a non-overlapping collection of IDifferences
 * 
 * 
 */
public interface IDifference {

	/**
	 * starting index (0-based) of difference in file A (i.e. 0-based line
	 * number in a file)
	 * 
	 * @return index of difference in file A
	 */
	int getStartA();

	/**
	 * length of A, 0 if difference was added in B
	 * 
	 * @return index of difference in file A
	 */
	int getLengthA();

	/**
	 * ending index (0-based) of difference in file A (i.e. 0-based line number
	 * in a file) note: if this is -1, that means the section in B was not
	 * present in A
	 * 
	 * @return index of difference in file A
	 */
	int getEndA();

	/**
	 * starting index (0-based) of difference in file B (i.e. 0-based line
	 * number in a file)
	 * 
	 * @return index of difference in file B
	 */
	int getStartB();

	/**
	 * ending index (0-based) of difference in file B (i.e. 0-based line number
	 * in a file) note: if this is -1, that means the section in A was not
	 * present in B
	 * 
	 * @return index of difference in file A
	 */
	int getEndB();

	/**
	 * length of B, 0 if difference was added in A
	 * 
	 * @return index of difference in file B
	 */
	int getLengthB();

}
