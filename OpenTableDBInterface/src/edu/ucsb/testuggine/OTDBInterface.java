package edu.ucsb.testuggine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.princeton.cs.algs4.Date;
import edu.princeton.cs.introcs.In;
import edu.princeton.cs.introcs.StdOut;

public class OTDBInterface {
	JSONParser parser;
	MySQLConnection db;

	/** Requires the path to the two files 
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws SQLException */
	public OTDBInterface(String restaurantsFilePath, String reviewsFilePath) throws FileNotFoundException, IOException, ParseException, SQLException {
		parser = new JSONParser();
		db = new MySQLConnection();
		In restaurantsIn = new In(restaurantsFilePath);
		while (!restaurantsIn.isEmpty()) {
			String line = restaurantsIn.readLine();
			Object reviewObj = parser.parse(line);
			JSONObject jObj = (JSONObject) reviewObj;
			OpenTableRestaurant r = parseRestaurant(jObj);
			writeToDB(r);
		}
		
		
		In reviewsIn = new In(reviewsFilePath);
		while (!reviewsIn.isEmpty()) {
			String line = reviewsIn.readLine();
			Object reviewObj = parser.parse(line);
			JSONObject jObj = (JSONObject) reviewObj;
			OpenTableReview rvw = parseReview(jObj);
			writeToDB(rvw);
		}
		
		

	}

	/** String format example: 
	  *{"website": "http://www.lacervecerianyc.com", 
	  *"city": "new-york", 
	  *"neighborhood": "East Village", 
	  *"name": "' La Cerveceria", 
	  *"short_name": "la-cerveceria", 
	  *"cuisine": "Peruvian", 
	  *"url": "http://www.opentable.com/la-cerveceria?scpref=110", 
	  *"price": "$31 to $50", 
	  *"email": "lacervecerianyc@gmail.com", 
	  *"phone": "(212) 777-6965", 
	  *"address": "65 2nd Avenue\nNew York, NY  10003", 
	  *"id": 51568}
	 **/
	private OpenTableRestaurant parseRestaurant(JSONObject jsonObject) {

		String website = (String) jsonObject.get("website");
		String name = (String) jsonObject.get("name");
		// short_name gets discarded, we don't really care about it
		String cuisine = (String) jsonObject.get("cuisine");
		String url = (String) jsonObject.get("url"); // Not their website
		String price = (String) jsonObject.get("price");
		String email = (String) jsonObject.get("email");
		String phone = (String) jsonObject.get("phone"); // TODO possibly use TA function to process it
		String addressStr = (String) jsonObject.get("address");
		String id = (String) jsonObject.get("id");	

		Address address = parseOpenTableAddress(addressStr);
		
		return new OpenTableRestaurant(name, cuisine, url, price, address, phone, email, website, id);
	}
	
	/** format: 
	 * 
	 * "address": "23808 Resort Parkway\nSan Antonio, TX  78261"
	 * @param addressStr
	 * @return
	 */
	private Address parseOpenTableAddress(String addressStr) {
		int street_end_pos = addressStr.indexOf("\n");
		int city_end_pos = addressStr.indexOf(", ", street_end_pos + 1);
		int region_end_pos = addressStr.indexOf(" ", city_end_pos + 1);
		
		String streetAndNum = addressStr.substring(0, street_end_pos);
		String city = addressStr.substring(street_end_pos + 1, city_end_pos);
		String region = addressStr.substring(city_end_pos + 1, region_end_pos);
		String zip = addressStr.substring(region_end_pos + 1);
		return new Address(streetAndNum, zip, city, region);
	}

	/** Format: 
	 * {"noise": "Moderate", 
	 * "features": 
	 * 		["vibrant bar scene", 
	 * 			"good for groups", 
	 * 			"neighborhood gem", 
	 * 			"special occasion", 
	 * 			"hot spot"
	 * 		], 
	 * "service": 5, 
	 * "title": "Due to working late I turned", 
	 * "food": 5, 
	 * "text": "Due to working late I turned up late so...", 
	 * "restaurant_id": 33070, 
	 * "ambiance": 5, 
	 * "overall": 5, 
	 * "date": "10/11/2012", 
	 * "author_id": 43371325, 
	 * "id": 35754942}
	 * */
	private OpenTableReview parseReview(JSONObject jsonObject) {
		
		JSONObject ratings = (JSONObject) jsonObject.get("ratings");
		Float globalRating = (float) ((Double) ratings.get("overall")).doubleValue();
		Float valueRating = (float) 0.0, atmosphereRating = (float) 0.0, serviceRating = (float) 0.0, foodRating = (float) 0.0;
		if (ratings.containsKey("value"))
		 valueRating = (float) ((Double) ratings.get("value")).doubleValue();
		if (ratings.containsKey("atmosphere")) 
			atmosphereRating = (float) ((Double) ratings.get("atmosphere")).doubleValue();
		if (ratings.containsKey("service")) 
			serviceRating = (float) ((Double) ratings.get("service")).doubleValue();
		if (ratings.containsKey("food")) 
			foodRating = (float) ((Double) ratings.get("food")).doubleValue();
		Integer helpfulCounter = 0;
		if (ratings.containsKey("helpful"))
				helpfulCounter = (Integer) ratings.get("helpful"); // TODO Maybe the name will differ?
		
		String title = (String) jsonObject.get("title");
		String text = (String) jsonObject.get("text");
		JSONObject authorJSON = (JSONObject) jsonObject.get("author");
		OpenTableUser author = parseAuthor(authorJSON);
		String id = String.valueOf((Long) jsonObject.get("id"));
		String dateStr = (String) jsonObject.get("date");
		
		String restaurant_id = String.valueOf((Long) jsonObject.get("offering_id"));
		
		
		return new OpenTableReview(id, author, globalRating, valueRating, atmosphereRating,
				serviceRating, foodRating, helpfulCounter, dateStr, title, text, restaurant_id);
	}


	/** Format: 
	 * {"username": "Estherleibel", 
	 * "num_cities": 2, 
	 * "num_helpful_votes": 1, 
	 * "num_reviews": 20, 
	 * "num_type_reviews": 16, 
	 * "id": "C3ECF97B6A4424B63DB907412344B520", 
	 * "location": "New York City, New York"}
	 * */
	private OpenTableUser parseAuthor(JSONObject jsonObject) {
		String id = (String) jsonObject.get("id");
		String userName = (String) jsonObject.get("username");
		
		Integer reviewsInCitiesCount = 0, helpfulCount = 0, totalReviewsCount = 0, restaurantReviewsCount = 0;
		
		if (jsonObject.containsKey("num_cities"))
			reviewsInCitiesCount = ((Long) jsonObject.get("num_cities")).intValue();
		if (jsonObject.containsKey("num_helpful_votes"))
			helpfulCount = ((Long) jsonObject.get("num_helpful_votes")).intValue();
		if (jsonObject.containsKey("num_reviews"))
			totalReviewsCount = ((Long) jsonObject.get("num_reviews")).intValue();
		if (jsonObject.containsKey("num_type_reviews"))
			restaurantReviewsCount = ((Long) jsonObject.get("num_type_reviews")).intValue();
		String location = (String) jsonObject.get("location");
		
		return new OpenTableUser(userName, reviewsInCitiesCount, helpfulCount, totalReviewsCount,
				restaurantReviewsCount, id, location);
		
	}

	void writeToDB(OpenTableRestaurant r) throws SQLException {
		String insertionQuery = "INSERT INTO `TripAdvisorRestaurant` " +
				"(`name`, `addressNum`, `addressStreet`, `addressCity`, " +
				"`addressRegion`, `addressZip`, `phoneNumber`, `url`, " +
				"`details`, `id`, `region_id`, `type`) " +
				"VALUES " +
				"(?, ?, ?, ?, " +
				"?, ?, ?, ?, " +
				"?, ?, ?, ?);";

		PreparedStatement prep = db.con.prepareStatement(insertionQuery);

		prep.setString(1, r.name); // Name CANNOT be null!
		safeInsert(prep, 2, r.address.number);
		safeInsert(prep, 3, r.address.street);
		safeInsert(prep, 4, r.address.city);
		safeInsert(prep, 5, r.address.region);
		safeInsert(prep, 6, r.address.zip);
		safeInsert(prep, 7, r.phoneNumber);
		safeInsert(prep, 8, r.url);
		safeInsert(prep, 9, r.details);
		safeInsert(prep, 10, r.id);
		safeInsert(prep, 11, r.region_id);
		safeInsert(prep, 12, r.type);

		if (!isAlreadyInDB(r)) {
			StdOut.println("----\n" + prep + "\n--------");
			prep.execute();
		} else
			StdOut.println("Restaurant " + r.name
					+ " already in the DB. Skipping...");
		prep.close();
	}

	void writeToDB(OpenTableReview rev) throws SQLException {

		String alreadyExistsCheckQuery = "SELECT * FROM  `TripAdvisorReview` WHERE  `id` =  ?";
		PreparedStatement checkStatement = db.con
				.prepareStatement(alreadyExistsCheckQuery);
		checkStatement.setString(1, rev.id);
		ResultSet alreadyExistsRes = checkStatement.executeQuery(); // if it's already there, don't insert
		String insertionQuery = "INSERT INTO `TripAdvisorReview` " +
				"(`id`, `author_id`, `restaurant_id`, `globalRating`, `valueRating`, `atmosphereRating`, " +
				"`serviceRating`, `foodRating`, `helpfulCounter`, `date`, `title`, `text`) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

		PreparedStatement prep = db.con.prepareStatement(insertionQuery);

		prep.setString(1, rev.id);

		writeToDb(rev.author);
		prep.setString(2, rev.author.id);
		prep.setString(3, rev.restaurant_id);
		
		prep.setFloat(4, rev.globalRating);
		prep.setFloat(5,  rev.valueRating);
		prep.setFloat(6,  rev.atmosphereRating);
		prep.setFloat(7,  rev.serviceRating);
		prep.setFloat(8,  rev.foodRating);
		prep.setInt(9, rev.helpfulCounter);
		safeInsert(prep, 10, mySQLformat(rev.date));
		safeInsert(prep, 11, rev.title);
		safeInsert(prep, 12, rev.text);

		if (!alreadyExistsRes.first()) {
			StdOut.println("----\n" + prep + "\n--------");
			prep.execute();
		}
		else 
			StdOut.println("Insert failed. Review already in db: \n\t" + rev);
		prep.close();
	}
	
	
	private String mySQLformat(Date d) {
		return d.year() + "-" + d.month() + "-" + d.day();
	}

	/** Format: 
	 * {"username": "Estherleibel", 
	 * "num_cities": 2, 
	 * "num_helpful_votes": 1, 
	 * "num_reviews": 20, 
	 * "num_type_reviews": 16, 
	 * "id": "C3ECF97B6A4424B63DB907412344B520", 
	 * "location": "New York City, New York"}
	 * */
	void writeToDb(OpenTableUser u) throws SQLException {

		String alreadyExistsCheckQuery = "SELECT * FROM  `TripAdvisorUser` WHERE  `id` =  ?";
		PreparedStatement checkStatement = db.con
				.prepareStatement(alreadyExistsCheckQuery);
		checkStatement.setString(1, u.id);
		ResultSet alreadyExistsRes = checkStatement.executeQuery(); // if it's already there, don't insert
		String insertionQuery = "INSERT INTO `TripAdvisorUser` (`username`, `num_cities`, " +
				"`num_helpful_votes`, `num_reviews`, `num_type_reviews`, `id`, `location`) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?);";
		PreparedStatement prep = db.con.prepareStatement(insertionQuery);
		prep.setString(1, u.userName); 
		prep.setInt(2, u.num_cities); // Name CANNOT be null!
		prep.setInt(3, u.num_helpful_votes);
		prep.setInt(4, u.num_reviews);
		prep.setInt(5, u.num_type_reviews);
		prep.setString(6, u.id); // Can't be null
		safeInsert(prep, 7, u.location);

		if (!alreadyExistsRes.first()) {
			prep.execute();
		}
		else 
			StdOut.println("User already in DB: \n\t" + u );
		prep.close();
	}

	private boolean isAlreadyInDB(OpenTableRestaurant r) throws SQLException {
		String alreadyExistsCheckQuery = "SELECT * FROM  `TripAdvisorRestaurant` WHERE  `id` =  ?";
		PreparedStatement checkStatement = db.con
				.prepareStatement(alreadyExistsCheckQuery);
		checkStatement.setString(1, r.id); // the ID of this restaurant
		ResultSet alreadyExistsRes = checkStatement.executeQuery();
		if (!alreadyExistsRes.first() ) return false;
		return true;
	}
	
	private static void safeInsert(PreparedStatement prep, int pos, String field)
			throws SQLException { // JDBC sends an empty string instead of a
		// NULL value.
		if (field.isEmpty())
			prep.setString(pos, null);
		else
			prep.setString(pos, field);
	}

}
