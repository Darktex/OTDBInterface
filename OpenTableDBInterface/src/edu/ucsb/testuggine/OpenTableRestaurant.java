package edu.ucsb.testuggine;


/** Format:
 * 
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
*/
public class OpenTableRestaurant {
	String name;
	String cuisine;
	String url; // The URL to mine, not the restaurant's website URL
	String price; // Price range, good for kids etc
	Address address;
	String phoneNumber;
	String email;
	String website; // The restaurant's website
	String id;

	public OpenTableRestaurant(String name, String cuisine, String url,
			String price, Address address, String phoneNumber, String email,
			String website, String id) {
		this.name = name;
		this.cuisine = cuisine;
		this.url = url;
		this.price = price;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.website = website;
		this.id = id;
	}



	@Override
	public String toString() {
		return "OpenTableRestaurant [name=" + name + ", cuisine=" + cuisine
				+ ", url=" + url + ", price=" + price + ", address=" + address
				+ ", phoneNumber=" + phoneNumber + ", email=" + email
				+ ", website=" + website + ", id=" + id 
				+ "]";
	}
	


	
}
