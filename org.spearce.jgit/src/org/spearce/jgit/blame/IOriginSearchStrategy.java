package org.spearce.jgit.blame;

interface IOriginSearchStrategy {

	/**
	 * Find origins for a specified origin source
	 * 
	 * @param source
	 * @return an array of origins leading to the source origin
	 */
	Origin[] findOrigins(Origin source);
}
