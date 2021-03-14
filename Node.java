import java.util.ArrayList;

public class Node { // Represent the node of the graph (=Bayesian network)

	public String name; // Name of the node
	public CPT cpt; // CPT object of the node
	public ArrayList<String> parents = new ArrayList<>(); // Parents name
	public ArrayList<String> status = new ArrayList<>(); // Status name

	
	public Node(String name, ArrayList<String> parents, ArrayList<String> status){ // Constructor
		this.name = name;
		this.parents.addAll(parents);
		this.status.addAll(status);
		
	}	
		
	public void setCPT(CPT cpt) { 
		this.cpt = cpt;
	}	
	
	
	public String getName() {
		return this.name;
	}
	
	public CPT getCPT() {
		return this.cpt;
	}
	
	public ArrayList<String> getParents(){
		return this.parents;
	}
	
	public ArrayList<String> getStatus(){
		return this.status;
	}
}
