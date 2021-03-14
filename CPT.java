import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;


public class CPT {

	Map<String, Float> table;// The table that preserve the satistic data
	int size; // The size of the table
	
	public CPT() {// Default constructor
		table = new HashMap<String,Float>();
		this.size = 0;
	}
	
	public CPT (String name, String values, ArrayList<String> parents, ArrayList<String> status) { // Constructor for 
		table = new HashMap<String,Float>();
		this.size = 0;
		String[] data = values.split(",");// Split the data for easy management
		String missingStatus = status.get(status.size()-1);
		float complement = 1;
		
		int indexParents = 0;
		String input = "";		
		if(values.charAt(0) == '=') { // If there is a parents for the variable or not	
			for(int index = 0 ; index < data.length; index++) { 
				if(index%2 == 0) { // For status
					input = "P("+name+data[index]+")";
				}
				
				else { // For value
					float satistic = Float.parseFloat(data[index]);
					complement -= satistic;
					table.put(input, satistic);
					size++;
					input = "";
					
				}
			}
			
			input = "P("+name+"="+missingStatus+")";
			complement = (float)Math.round(complement * 1000000) / 1000000;
			table.put(input, complement);
			size++;

			
		}
		
		else { // If there exist parents for the variable
			input="";
			String parInfo = ""; 
			for(int index = 0 ; index < data.length; index++) { 
				

				if(indexParents < parents.size()) {// Create string of info about the parents
					parInfo += parents.get(indexParents);
					indexParents++;
					
					if(index+1 != parents.size())
						parInfo = parInfo + "=" + data[index]+",";
					
					else
						parInfo = parInfo + "=" + data[index];
					
				}
				
				else if(data[index].charAt(0) == '=') { // Finish the string for a key such that (current variable| parents variable)
					input = "P(" + name + data[index] +"|" + parInfo + ")" ;
				}
				
				
				else { // Insert the {key, value into the table}
					float satistic = Float.parseFloat(data[index]);
					complement -= satistic;
					table.put(input, satistic);
					size++;
					input = "";
				}
			}
			
			input = "P("+name+"="+missingStatus+"|"+parInfo+")";
			complement = (float)Math.round(complement * 1000000) / 1000000;
			table.put(input, complement); // Insert the complement option into the table
			size++;
		}
	}
	
	
	public void addSatistics(String name, String values, ArrayList<String> parents, ArrayList<String> status){ // The same as the constructor above this function is for variables with parents for each line
		
		
		String[] data = values.split(",");
		int indexParents = 0;
		String input="";
		String parInfo = ""; 
		
		
		
		String missingStatus = status.get(status.size()-1);
		float complement = 1;		
		
		for(int index = 0 ; index < data.length; index++) { 
			

			if(indexParents < parents.size()) {
				parInfo += parents.get(indexParents);
				indexParents++;
				
				if(index+1 != parents.size())
					parInfo = parInfo + "=" + data[index]+",";
				
				else
					parInfo = parInfo + "=" + data[index];

			}
			
			else if(data[index].charAt(0) == '=') {
				input = "P(" + name + data[index] +"|" + parInfo + ")" ;
			}
			
			
			else {
				float satistic = Float.parseFloat(data[index]);
				complement -= satistic;
				table.put(input, satistic);
				size++;
				input = "";
			}
		}
		
		input = "P("+name+"="+missingStatus+"|"+parInfo+")";
		complement = (float)Math.round(complement * 1000000) / 1000000;
		table.put(input, complement);
		size++;
	}
	
	public Map<String, Float> getTable() {// Returning the table itself
		return this.table;
	}
	
	public void addToFactor(String key, float value) { // When create factor table for algorithm 2 and 3 we use this function to insert the {key: value}  
		table.put(key, value);
		size++;
	}
	
	public int size() {// The number of values on this tables
		return size;
	}
}
