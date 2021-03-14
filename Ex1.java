import java.io.File;  
import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;  
public class Ex1 {

	public static void main(String[] args) throws IOException {
	
		
		Scanner s;
		String name = "";
		ArrayList<String> status = new ArrayList<>(); // The status of the node variable 
		ArrayList<String> parents = new ArrayList<>(); // The parents of the node variable
		ArrayList<Node> variables = new ArrayList<>(); // The variables them self
		FileWriter myWriter = new FileWriter("output.txt");

		
		try {
			s = new Scanner(new File("input.txt"));
			
			String line = s.next();
			
			while(s.hasNext()) { // If we haven't finish to read then continue to the next line 
				
				status.clear();
				parents.clear();
				
				if(!line.equals("Var") && !line.equals("Queries")) {
					line = s.next();
				}	
					
				if(line.equals("Var")) {// Starting reading the variable parameters
					line = s.next();
					name = line; // The name of the variable 
					
					
					
					s.next();
					line = s.next();
					String[] tmpV = line.split(","); // Create status list for the variable
					for(int index = 0; index < tmpV.length; index++) {
						status.add(tmpV[index]);
					}
					
					
					
					s.next();
					line = s.next();
					String[] tmpP = line.split(",");
					if(!line.equals("none")) { // Create parents list for the variable
						for(int index = 0; index < tmpP.length; index++) {
							parents.add(tmpP[index]);
						}	
					}
					
					Node tmp = new Node(name, parents,status);// Creating the new node
					variables.add(tmp);
					
					
					
					
					
					
					s.next();
					line = s.next();
					CPT table = new CPT(name, line, parents,status); // Creating CPT object such that in the end it'll attached to the variable
					boolean newVar = false;
					
					while(s.hasNext() && !newVar) {
						
						line = s.next();
						if(!line.contains("Var") && !line.contains("Queries")) {
							table.addSatistics(name, line, parents,status);// For a variable with multiple statistics in the CPT
						}
						
						else {
							newVar = true;
						}
					}
					
					tmp.setCPT(table);
				}
				
				else if (line.contains("Queries")) {// Starting reading the queries
					while(s.hasNext()) {
						line = s.next();
						short algoNumber = Short.parseShort(line.substring(line.length()-1));
						line = line.substring(0, line.length()-2);
						
						Algorithm algo = new Algorithm(line,algoNumber,variables);
						float[] results = algo.algorithm();// Compute the query
						int add = (int)results[1], mul = (int)results[2];
						String result = String.format("%.5f", results[0]);
						System.out.println(result +","+add+","+mul);
						if(s.hasNext()) // If there is a another query add a new line
							myWriter.write(result+","+add+","+mul+"\n");
					   
						else
							myWriter.write(result+","+add+","+mul);

					}
				}
			}
			myWriter.close();
			s.close();

		}
		
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
