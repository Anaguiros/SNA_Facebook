package dataSerialisation;


import java.util.Vector;


public class Arc {
	
	protected String person1 ;
	protected String person2 ;
	protected int weight ;
	protected int date ; // timestamp
	protected Vector<String> topics;
	
	/* Links between two persons */
	public Arc(String i, String j, int weight, int k,
			Vector<String> topics) {
		super();
		this.person1 = i;
		this.person2 = j;
		this.weight = weight;
		this.date = k;
		this.topics = topics;
	}
	
	/* Not a link  :  */
	public Arc(String i, int weight, int k,
			Vector<String> topics) {
		super();
		this.person1 = i;
		this.person2 = null;
		this.weight = weight;
		this.date = k;
		this.topics = topics;
	}

	// constructor for Jackson mapping
	public Arc(){} ;
	
	
	/* Getters and Setters */

	public String getPerson1() {
		return person1;
	}

	public void setPerson1(String person1) {
		this.person1 = person1;
	}

	public String getPerson2() {
		return person2;
	}

	public void setPerson2(String person2) {
		this.person2 = person2;
	}

	public int getPoids() {
		return weight;
	}

	public void setPoids(int poids) {
		this.weight = poids;
	}

	public int getDate() {
		return date;
	}

	public void setDate(int date) {
		this.date = date;
	}

	public Vector<String> getTopics() {
		return topics;
	}

	public void setTopics(Vector<String> topics) {
		this.topics = topics;
	}

	
	
}
