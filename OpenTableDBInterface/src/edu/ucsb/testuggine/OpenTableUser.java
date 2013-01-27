package edu.ucsb.testuggine;

/** Format: 
 * author_id is a field inside the review. We have nothing more.
 * */
public class OpenTableUser {
	String id;
	/**
	 * @param userName
	 * @param num_cities
	 * @param num_helpful_votes
	 * @param num_reviews
	 * @param num_type_reviews
	 * @param id
	 * @param location
	 */

	/**
	 * @param id
	 */
	public OpenTableUser(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "OpenTableUser [id=" + id + "]";
	}
	
	
}
