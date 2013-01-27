package edu.ucsb.testuggine;


/** Format:
 * 
 *{"city": "new-york", 
 *"neighborhood": "Southampton", 
 *"name": "75 Main", 
 *"short_name": "75-main", 
 *"cuisine": "Contemporary American", 
 *"url": "http://www.opentable.com/75-main?scpref=110", 
 *"price": "$31 to $50", 
 *"address": "75 Main Street\nSouthampton, NY  11968", 
 *"id": 56296}
*/
public class OpenTableRestaurant {
	String name;
	String cuisine;
	String url; // The URL to mine, not the restaurant's website URL
	String price; // Price range, good for kids etc
	Address address;
	String phoneNumber; // Not scraped
	String email;
	String website; // The restaurant's website
	String id;
	String type;

	public OpenTableRestaurant(String name, String cuisine, String url,
			String price, Address address, String phoneNumber, String email,
			String website, String id, String type) {
		this.name = name;
		this.cuisine = cuisine;
		this.url = url;
		this.price = price;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.website = website;
		this.id = id;
		this.type = type;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		result = prime * result + ((website == null) ? 0 : website.hashCode());
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
		OpenTableRestaurant other = (OpenTableRestaurant) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (phoneNumber == null) {
			if (other.phoneNumber != null)
				return false;
		} else if (!phoneNumber.equals(other.phoneNumber))
			return false;
		if (website == null) {
			if (other.website != null)
				return false;
		} else if (!website.equals(other.website))
			return false;
		return true;
	}



	@Override
	public String toString() {
		return "OpenTableRestaurant [name=" + name + ", cuisine=" + cuisine
				+ ", url=" + url + ", price=" + price + ", address=" + address
				+ ", phoneNumber=" + phoneNumber + ", email=" + email
				+ ", website=" + website + ", id=" + id + ", type=" + type
				+ "]";
	}
	


	
}
