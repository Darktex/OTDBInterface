package edu.ucsb.testuggine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	boolean verbose;

	/** Requires the path to the two files 
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws SQLException */
	public OTDBInterface(String restaurantsFilePath, String reviewsFilePath, boolean isVerbose) throws FileNotFoundException, IOException, ParseException, SQLException {
		verbose = isVerbose;
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
		String id = String.valueOf((Long) jsonObject.get("id")).trim();	

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
		int region_end_pos = addressStr.indexOf(" ", city_end_pos + 2);
		
		String streetAndNum = addressStr.substring(0, street_end_pos);
		String city = addressStr.substring(street_end_pos + 1, city_end_pos);
		String region = addressStr.substring(city_end_pos + 2, region_end_pos);
		String zip = addressStr.substring(region_end_pos + 2);
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
		
		String noise = (String) jsonObject.get("noise");
		
		ArrayList<String> features = new ArrayList<String>();
		JSONArray featuresArray = (JSONArray) jsonObject.get("features");
		if (featuresArray == null) featuresArray = new JSONArray();
		@SuppressWarnings("unchecked")
		Iterator<String> iterator = featuresArray.iterator();
		while (iterator.hasNext()) {
			features.add(iterator.next().trim());
		}
		
		Integer service = ((Long) jsonObject.get("service")).intValue();
		String title = (String) jsonObject.get("title");
		Integer food = ((Long) jsonObject.get("food")).intValue();
		String text = (String) jsonObject.get("text");
		String restaurant_id = String.valueOf((Long) jsonObject.get("restaurant_id")).trim();
		Integer ambience = ((Long) jsonObject.get("ambiance")).intValue();
		Integer overall = ((Long) jsonObject.get("overall")).intValue();
		String dateStr = (String) jsonObject.get("date");
		String author_id = String.valueOf((Long) jsonObject.get("author_id")).trim();
		String id = String.valueOf((Long) jsonObject.get("id")).trim();

		return new OpenTableReview(noise, features, service, title, food, 
				text, restaurant_id, ambience, overall, dateStr, author_id, id);
	}

	void writeToDB(OpenTableRestaurant r) throws SQLException {
		String insertionQuery = "INSERT INTO OpenTableRestaurant " +
				"(`name`, `cuisine`, `url`, `price`, `addressNum`, `addressStreet`, `addressCity`, " +
				"`addressRegion`, `addressZip`, `phoneNumber`, `email`, " +
				"`website`, `id`) " +
				"VALUES " +
				"(?, ?, ?, ?, " +
				"?, ?, ?, ?, " +
				"?, ?, ?, ?," +
				" ?);";

		PreparedStatement prep = db.con.prepareStatement(insertionQuery);

		prep.setString(1, r.name); // Name CANNOT be null!
		safeInsert(prep, 2, r.cuisine);
		safeInsert(prep, 3, r.url);
		safeInsert(prep, 4, r.price);
		safeInsert(prep, 5, r.address.number);
		safeInsert(prep, 6, r.address.street);
		safeInsert(prep, 7, r.address.city);
		safeInsert(prep, 8, r.address.region);
		safeInsert(prep, 9, r.address.zip);
		safeInsert(prep, 10, processPhoneNumber(r.phoneNumber));
		safeInsert(prep, 11, r.email);
		safeInsert(prep, 12, r.website);
		safeInsert(prep, 13, r.id);

		if (!isAlreadyInDB(r)) {
			StdOut.println("----\n" + prep + "\n--------");
			prep.execute();
		} else
			StdOut.println("Restaurant " + r.name
					+ " already in the DB. Skipping...");
		prep.close();
	}

	void writeToDB(OpenTableReview rev) throws SQLException {

		String alreadyExistsCheckQuery = "SELECT * FROM  `OpenTableReview` WHERE  `id` =  ?";
		PreparedStatement checkStatement = db.con
				.prepareStatement(alreadyExistsCheckQuery);
		checkStatement.setString(1, rev.id);
		ResultSet alreadyExistsRes = checkStatement.executeQuery(); // if it's already there, don't insert
		String insertionQuery = "INSERT INTO `OpenTableReview` " +
				"(`noise`, `features`, `serviceRating`, `title`, `foodRating`, `text`, " +
				"`restaurant_id`, `ambienceRating`, `overallRating`, `date`, `author_id`, `id`) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

		PreparedStatement prep = db.con.prepareStatement(insertionQuery);

		safeInsert(prep, 1, rev.noise);
		safeInsert(prep, 2, printFeatures(rev.features));
		prep.setInt(3, rev.serviceRating);
		safeInsert(prep, 4, rev.title);
		prep.setInt(5, rev.foodRating);
		safeInsert(prep, 6, rev.text);
		prep.setString(7, rev.restaurant_id);
		prep.setInt(8, rev.ambienceRating);
		prep.setInt(9, rev.overallRating);
		safeInsert(prep, 10, mySQLformat(rev.date));
		prep.setString(11, rev.author.id);
		prep.setString(12, rev.id);

		if (!alreadyExistsRes.first()) {
			if (verbose) 
				StdOut.println("----\n" + prep + "\n--------");
			prep.execute();
		}
		else {
			if (verbose)
			 StdOut.println("Insert failed. Review already in db: \n\t" + rev);
		}
		prep.close();
	}
	
	
	private String printFeatures(ArrayList<String> features) {
		String result = "";
		if (features.isEmpty()) return result;
		for (String element : features)
			result += element + ";\n";
		return result.substring(0, result.length() - 2);
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
		String insertionQuery = "INSERT INTO `OpenTableUser` (`id`, `num_reviews`, `num_helpful_votes`, " +
				"`first_review_date`, `last_review_date`, `num_featured`, `average`, `username`) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
		PreparedStatement prep = db.con.prepareStatement(insertionQuery);
		prep.setString(1, u.id);
		prep.setInt(2, 0);
		prep.setInt(3, 0);
		prep.setString(4, null);
		prep.setString(5, null);
		prep.setInt(6, 0);
		prep.setString(7, null); // CARE this is a float normally.
		prep.setString(8, null);
		
		if (!alreadyExistsRes.first()) {
			prep.execute();
		}
		else 
			StdOut.println("User already in DB: \n\t" + u );
		prep.close();
	}

	private boolean isAlreadyInDB(OpenTableRestaurant r) throws SQLException {
		String alreadyExistsCheckQuery = "SELECT * FROM  `OpenTableRestaurant` WHERE  `id` =  ?";
		PreparedStatement checkStatement = db.con
				.prepareStatement(alreadyExistsCheckQuery);
		checkStatement.setString(1, r.id); // the ID of this restaurant
		ResultSet alreadyExistsRes = checkStatement.executeQuery();
		if (!alreadyExistsRes.first() ) return false;
		return true;
	}
	
	private String processPhoneNumber(String phone) {
		String result = "";

		Pattern phoneNumberRegex = Pattern.compile("(\\+1 ?)?\\(?(\\d{3})\\)?\\D(\\d{3})\\D(\\d{4})");
		// optional countrycode, 3 digits, separator, 3 more digits, separator, last 4 digits. 
		// Each of the digits groups is put inside a capture group

		Matcher phoneMatcher = phoneNumberRegex.matcher(phone);

		while (phoneMatcher.find()) { // Until there are phone numbers
			String firstBlock = phoneMatcher.group(2);
			String secondBlock = phoneMatcher.group(3);
			String thirdBlock = phoneMatcher.group(4);
			String numStr = firstBlock + secondBlock + thirdBlock;
			result += numStr + "; ";
		}
		return result.substring(0, result.length() - 2); // Removing the last "; " sequence
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
