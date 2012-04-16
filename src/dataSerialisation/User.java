package dataSerialisation;

import org.codehaus.jackson.annotate.JsonProperty;


public class User {

	protected String name ;
	@JsonProperty("uid")
	protected String id ;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "FbUser [name=" + name + ", id=" + id + "]";
	}
	
	
}
