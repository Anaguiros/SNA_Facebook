package dataSerialisation;

import java.util.List;



public class Data {
	
	private List<Arc> arcs ;
	
	private List<User> nodes ;
	
	public Data(List<Arc> arcs, List<User> nodes) {
		super();
		this.arcs = arcs;
		this.nodes = nodes;
	}
	
	// constructor for Jackson mapping
	public Data(){} ;
	
	public List<Arc> getArcs() { return arcs; }
	public void setArcs(List<Arc> arcs) {this.arcs = arcs;}

	public List<User> getNodes() {return nodes;}
	public void setNodes(List<User> nodes) {this.nodes = nodes;}
}
