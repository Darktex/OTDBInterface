package edu.ucsb.testuggine;

import java.util.ArrayList;

import edu.princeton.cs.algs4.Date;


/** Format:
 * 
 * {"noise": "Energetic", 
 * "features": ["vibrant bar scene", "late-night find", "neighborhood gem", "romantic", "hot spot"], 
 * "service": 5,  
 * "title": "I love this place! The staff", 
 * "food": 5, 
 * "text": "I love this place! The staff is always attentive [...]", 
 * "restaurant_id": 33070, 
 * "ambiance": 5, 
 * "overall": 5, 
 * "date": "11/15/2012", 
 * "author_id": 20836401, 
 * "id": 36833858}
 */
public class OpenTableReview {
	String noise;
	ArrayList<String> features;
	Integer serviceRating;
	String title;
	Integer foodRating;
	String text;
	String restaurant_id;
	Integer ambienceRating;
	Integer overallRating;
	Date date;
	OpenTableUser author;
	String id;
	
	/** This version asks for a valid Date object
	 */
	public OpenTableReview(String noise, ArrayList<String> features,
			Integer service, String title, Integer food, String text,
			String restaurant_id, Integer ambience, Integer overall, Date date,
			String author_id, String id) {
		this.noise = noise;
		this.features = features;
		this.serviceRating = service;
		this.title = title;
		this.foodRating = food;
		this.text = text;
		this.restaurant_id = restaurant_id;
		this.ambienceRating = ambience;
		this.overallRating = overall;
		this.date = date;
		this.author = new OpenTableUser(author_id);
		this.id = id;
	}
	
	/** This version asks for a date in String format and converts it */
	public OpenTableReview(String noise, ArrayList<String> features,
			Integer service, String title, Integer food, String text,
			String restaurant_id, Integer ambience, Integer overall, String dateStr,
			String author_id, String id) {
		this.noise = noise;
		this.features = features;
		this.serviceRating = service;
		this.title = title;
		this.foodRating = food;
		this.text = text;
		this.restaurant_id = restaurant_id;
		this.ambienceRating = ambience;
		this.overallRating = overall;
		this.date = convertDate(dateStr);
		this.author = new OpenTableUser(author_id);
		this.id = id;
	}
	
	private static Date convertDate(String dateStr) {
		// format: "date": "08/18/2012"
		Integer month_start_pos = 0;
		Integer month_end_pos = dateStr.indexOf("/");
		
		Integer day_start_pos = month_end_pos + 1;
		Integer day_end_pos = dateStr.indexOf("/", day_start_pos);
		
		Integer year_start_pos = day_end_pos + 1; // Comma + space
		Integer year_end_pos = dateStr.length();
		
		String monthStr = dateStr.substring(month_start_pos, month_end_pos);
		Integer monthNum = Integer.valueOf(monthStr);
		Integer day = Integer.valueOf(dateStr.substring(day_start_pos, day_end_pos));
		Integer year = Integer.valueOf(dateStr.substring(year_start_pos, year_end_pos));
		
		return new Date(monthNum, day, year);
	}

	@Override
	public String toString() {
		return "OpenTableReview [noise=" + noise + ", features=" + features
				+ ", service=" + serviceRating + ", title=" + title + ", food="
				+ foodRating + ", text=" + text + ", restaurant_id=" + restaurant_id
				+ ", ambience=" + ambienceRating + ", overall=" + overallRating + ", date="
				+ date + ", author=" + author + ", id=" + id + "]";
	}

}