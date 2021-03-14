import java.util.ArrayList;
import java.util.Map.Entry;


public class Algorithm {

	private String querie; // The query that we got for computing 
	private int algoNumber; // The algorithm we need to operate 
	private ArrayList<Node> variables = new ArrayList<>(); // The variable list
	private float[] results = new float[3];
	private float summing; // For the fectorizing
	private ArrayList<ArrayList<String>> cartesian = new ArrayList<>();// For the first algorithm to take any option for calculate
	
	public Algorithm(String querie, int algoNumber, ArrayList<Node> variables){
		this.querie = querie;
		this.algoNumber = algoNumber;
		this.variables.addAll(variables);
		this.summing = 0;
	}
	
	public void setAlgoNumber(int algoNumber) {
		this.algoNumber = algoNumber;
	}
	
	public void setQuerie(String querie) {
		this.querie = querie;
	}
	
	public float[] algorithm() {// The preliminary procedure before using the algorithm it self
		
		int index1 = querie.indexOf('|');
		int index2 = querie.indexOf(')');

		if(index1 == -1) {// Without conditions 
			String calculate = querie.replace("P(", "");
			calculate = calculate.replace(")","");
			int esign = calculate.indexOf("=");
			String name = calculate.substring(0, esign); 
			Node node = null;
			for(Node tmp : variables) {
				if(name.equals(tmp.getName())) {
					node = tmp;
				}	
			}
			
			search(calculate,node);
		}

		else {// With conditions
			String calculate = querie.substring(2, index1);
			String[] given = querie.substring(index1+1,index2).split(",");
	
			int index3 = calculate.indexOf('=');

			Node nodeCheck = null;
			String varCheck = calculate.substring(0, index3);
		
			for(Node v : variables) {
				if(v.getName().equals(varCheck)) {
					nodeCheck = v;
				}
			}
	

			int numberMissingVariables = variables.size()-1-given.length; // If we had a query that all his variable define we will not compute it 
																		  // and will search it through the table of the variable we need to calculate
			switch(algoNumber) {
				case 1:
		
					if(numberMissingVariables == 0) {
						results[0] = search(calculate, given, nodeCheck); // If all the variables are define
						return results;

					}
				
					else {
						algorithm1(calculate, given, nodeCheck);
						results[0] = this.summing; // The results of calculate the query it self without the factor and calculate the complements 
						ArrayList<String> remaning = new ArrayList<>();
						remaning.addAll(nodeCheck.getStatus());
						String needRemove = calculate.substring(index3+1, calculate.length());
						remaning.remove(needRemove);
				
				
						for(String stat : remaning) {// Calculate the complements 
							calculate = calculate.substring(0,index3+1);
							calculate += stat;
							cartesian.clear();
							algorithm1(calculate, given, nodeCheck);
							results[1]++;

						}
				
						float alpha = 1/this.summing;// The factor that we gone multiply by the final results
						this.results[0] *= alpha; 
						results[0] = (float)Math.round(results[0] * 100000) / 100000;
						break;
					}
				
				
				
				
				case 2:
				
					if(numberMissingVariables == 0) {// As the same for the algorithm1 above
						results[0] = search(calculate, given, nodeCheck);
						return results;
						
					}
				
					else {
						algorithm2(calculate, given, nodeCheck);
					}
					break;
			
				case 3:
			
					if(numberMissingVariables == 0) { // As the same for the algorithm1 above
						results[0] = search(calculate, given, nodeCheck);
						return results;
					}
				
					else {
						algorithm3(calculate, given, nodeCheck);
					}
			
					break;
				
				default:
					System.out.println("the number of the chosen algorithm it is not correct");
		
			}
		}
		
		return results;
		
	}
	
	private void search(String calculate, Node current) {// if the probability is not with condition I'm not sure if the node have a parents
		
		for(Entry<String,Float> entry : current.getCPT().table.entrySet()) {
			String key = entry.getKey();
			if(key.contains(calculate)) { 
				if(summing != 0) {
					results[1]++;
				}
				
				summing += entry.getValue();
			}	
		}
		
		results[0] = summing/(results[1]+1);
	}
	
	private float search(String calculate, String[] given, Node current) {// If there is a condition on the query
		
		boolean theSame = false;
		float output = 0;
		
		for(Entry<String,Float> entry : current.getCPT().table.entrySet()) {// For any entry of the table check if it is contained the variables with the same define status
			String[] key = entry.getKey().replace("P(","").replace(")","").replace("|",",").split(",");
			boolean different = false;
			boolean found = false;
			
			if(key[0].equals(calculate) && !theSame) { // If the  
				for(int EIndex = 1; EIndex < key.length; EIndex++) {
					found = false;
					for(int index = 0; index < given.length && !different; index++) {
						if(key[EIndex].equals(given[index])) { // If one of the status is different and we pass on all the status in the list then the entry and the query is not the same
							found = true;
						}
						
						else if(!found && index+1 == given.length) {
							different = true;
						}
					}
				}
				
				if(!different) {// If the query and the entry are equal than return the value
					theSame = true;
					output = entry.getValue();
				}
			}
		}
		
		return output;
	}
	
	
	private float parserCalculate(String[] compute, ArrayList<Node> missingVariable) {// The function are parse the string and then will search the entry such that are the same
		
		String[] parse = new String[compute.length];
		for(int index = 0; index < compute.length; index++) {// Parse the what we need to compute
			parse[index] = compute[index].replace(")", "").replace("P(", "");
		}
		
		String[] middle = new String[variables.size()];
		float output = 0, cal = 0;
		for(int index = 0; index < parse.length; index++) {
			middle = parse[index].split(",");// Split what we need to find in the table
			for(int j = 0; j < middle.length; j++) {
				int esign1 = middle[j].indexOf("=");
				String name = middle[j].substring(0, esign1);
				for(Node wantSatistic : variables) { // Passing on the variables to see witch variable we need to compute such that we can get the table of the node we need
													 //to compute the multiplication of the probability
				
					if(wantSatistic.getName().equals(name)) {// If we find the node
						
						if(wantSatistic.getParents().size()==0) {// If the node dose not have parents
							float test = 0;
							if(cal == 0) {// If we didn't start to compute so we don't need to count it as multiplication operation
								test = wantSatistic.getCPT().getTable().get("P("+middle[j]+")");
								cal = test;
							}	
							else {
								test = wantSatistic.getCPT().getTable().get("P("+middle[j]+")");// maybe move here result.setMul
								cal *= test;
								results[2]++;
							}
						}
						else {// If the node have parents
							String[] parInfo = new String [wantSatistic.getParents().size()];
							int parIndex = 0;
							for(String tmp2 : wantSatistic.getParents()) {// Create a new information about the parents of the node
								for(int k = 0; k < middle.length; k++) {
									int esign2 = middle[k].indexOf("=");
									String nameP = middle[k].substring(0, esign2);
									if(nameP.equals(tmp2)) {
										parInfo[parIndex] = middle[k];
										parIndex++;
									}	
								}
							}
							
							float test = 0;// Multiplication of probabilities of the variables
							if(cal == 0) {
								test = search(middle[j], parInfo, wantSatistic);
								cal = test;
							}
							
							else {
								test= search(middle[j], parInfo, wantSatistic);
								cal *= test;
								results[2]++;
							}
						}
					}
				}
			}
			
			if(output != 0) {// Sum the multiplications 
				results[1]++;
			}
			
			output += cal;

			cal = 0;
		}
		
		return output;
	}
	

	
	
	
	private void algorithm1(String calculate, String[] given, Node calNode) {// need to add the option that there is no condition at all
		
		ArrayList<Node> missingVariable = new ArrayList<>();
			
		for(Node check : variables) { // Found all the non given variables such that we know what to calculate
			if(!calNode.getName().equals(check.getName())) {
				boolean found = false;
				
				for(int index = 0; index < given.length && !found; index++) {
						
					if(given[index].contains(check.getName())){
						found = true;
					}
						
					else if(!given[index].contains(check.getName()) && index+1 == given.length) {
						missingVariable.add(check);
					}
						
				}
			}
		}
			
		int missingStatus = 1;
		for(Node miss : missingVariable) {// count the status of the missing variables
			missingStatus *= miss.getStatus().size(); // Calculate the options for calculate probabilities
		}
			
			
			
		String[] compute = new String[missingStatus];// Here we going to store strings of probabilities what we need to compute 
			
			
		if(missingVariable.size() == 1) {// If missing only one variable we need only one variable to add 
			for(int index = 0; index < missingStatus; index++) {
				compute[index] = "P("+ calculate+",";
					
				for(int j = 0; j < given.length; j++) {
					compute[index] += given[j]+",";
				}
					
				Node tmp = missingVariable.get(0);// Calculate all the option for the missing variable
				compute[index] += tmp.getName() + "=" + tmp.getStatus().get(index)+")";
			}
		}
			
		else if(missingVariable.size() > 1) {// If missing two or more variables so there need to be 2 variable such that compute 
			for(int index = 0; index < missingStatus; index++) {// Creates the string such that it would have a base for what we need to calculate
				compute[index] = "P("+ calculate+",";
					
				for(int j = 0; j < given.length; j++) {
					compute[index] += given[j]+",";
				}
					
			}
				
			for(Node node : missingVariable) {
				cartesian.add(node.status);
			}
				
			String[] product = new String[missingStatus];
			for(int i = 0; i < missingStatus; i++) { // Create Cartesian product for all the options of the missing variables 
				int j = 1, index = 0;
				boolean first = true;
			    for(ArrayList<String> option : cartesian) {
				    	
			    	if(first) {// If it is the first we must to add colum
			    		product[i] = missingVariable.get(index).getName() + "=" 
			    				     + (option.get((i/j)%option.size()) + ",");
			    		first = false;
			    		index++;
			    	}
				    	
			    	else {// If it is the second variable or further then we need to examine if we need to add column or not
			    		product[i] += missingVariable.get(index).getName() + "=" 
			    					  + (option.get((i/j)%option.size()));
			    		index++;	
			    		if(missingVariable.size() != index)
				   			product[i] += ",";	
			    	}
				    	
				   	j *= option.size();
			    }
				    
				product[i] += ")";
			}
				
			for(int index = 0; index < missingStatus; index++) {
				compute[index] += product[index];
			}
		}	
					
		float output = parserCalculate(compute, missingVariable);
		summing += output;		
	}		
	
	
	
	
	

	private CPT createFactorTable(String strFactor) {// For the non define variables we create a factor table so the second and the third algorithm will calculate 
		strFactor = strFactor.replace("P(", "");
		strFactor = strFactor.replace(")", "");
		strFactor = strFactor.replace("|", ",");
		String[] middleMan = strFactor.split(",");
		ArrayList<String> defineVar = new ArrayList<>(); 
		for(int index = 0; index < middleMan.length; index++) {
			if(middleMan[index].contains("=")) {
				defineVar.add(middleMan[index]);
			}
		}
		
		int esign = middleMan[0].indexOf("=");
		String varName; 
		if(esign == -1) {
			varName = middleMan[0]; 
		}
		
		else {
			varName = middleMan[0].substring(0,esign);
		}
		
		Node needFactor = null;
		for(Node searchForFactor : variables){// Witch variable we need to build for him the table
			if(searchForFactor.getName().equals(varName))
				needFactor = searchForFactor;
		}
		
		CPT output = new CPT();
		
		for(Entry<String,Float> entry : needFactor.getCPT().getTable().entrySet()) {// Create a new CPT object according the non define variables that contains the key and values
			boolean needToAdd = true;
			String key = entry.getKey();
			for(String check : defineVar) {
				if(!key.contains(check)) {
					needToAdd = false;
				}
			}
			
			if(needToAdd) {
				key = key.replace("|", ",");
				output.addToFactor(key, entry.getValue());
		
			}
		}	
		
		return output;
		
	}
	
	
	
	private CPT join(CPT factor1, CPT factor2, String mutiplyBy) {// Join the factor table
		CPT output = new CPT();
		String[] joinBy = mutiplyBy.split(",");
		boolean[] joinFound = new boolean[joinBy.length];
		String outputKey = "P(";
		float outputMul = 0;
		String parInfo = "";
		
		

		for(Entry<String,Float> externalEntry : factor1.getTable().entrySet()) {// Passing on each entry to see if there is a overlapping on each variable that he is not define 
			parInfo = "";
			String[] externalSplit = externalEntry.getKey().replace(")", "").replace("P(", "").split(",");
			ArrayList<String> keyExist = new ArrayList<>();
			for(int index = 0 ; index < externalSplit.length; index++) {// Create string that represent the parent information of the node
				int esign = externalSplit[index].indexOf("=");
				if(!externalSplit[index].substring(0,esign).equals(mutiplyBy) && index+1 != externalSplit.length) {
					parInfo+=externalSplit[index]+",";
					int esignT = externalSplit[index].indexOf('=');
					String tmp = externalSplit[index].substring(0,esignT);
					keyExist.add(tmp);
				}
				
				else if(!externalSplit[index].substring(0,esign).equals(mutiplyBy)) {
					parInfo+=externalSplit[index];
					int esignT = externalSplit[index].indexOf('=');
					String tmp = externalSplit[index].substring(0,esignT);
					keyExist.add(tmp);
					}
			}
			
			
			if(parInfo.length() > 0) {
				outputKey = "P("+parInfo+",";
			}
			
			else {
				outputKey = "P(";
			}	
			
			
			for(Entry<String,Float> innerEntry : factor2.getTable().entrySet()) {
				String[] innerSplit = innerEntry.getKey().replace(")", "").replace("P(", "").split(",");

				
				
				for(int index = 0; index < joinBy.length; index++) {// For each undefined node
					for(int Eindex = 0; Eindex < externalSplit.length; Eindex++) {// We find the string name of the node we start to search in the last line 
						int esign1 = externalSplit[Eindex].indexOf("=");
						String Exvar = externalSplit[Eindex].substring(0,esign1);
						if(Exvar.equals(joinBy[index])){// If the name are equals continue search it in the inner table loop
							for(int Iindex = 0; Iindex < innerSplit.length; Iindex++) {
								int esign2 = innerSplit[Iindex].indexOf("=");
								String Invar = innerSplit[Iindex].substring(0,esign2);
								if(!keyExist.contains(Invar)) {
									outputKey += innerSplit[Iindex]+",";
								}	
								if(externalSplit[Eindex].equals(innerSplit[Iindex])){ // If the the external entry and the inner entry node name are the same such that 																											
									joinFound[index] = true;
								
								}	
							}
						}
						
						boolean foundAll = true;
						for(int checkJoin = 0; checkJoin < joinFound.length; checkJoin++) {
							if(!joinFound[checkJoin]) {
								foundAll = false;
							}
							
							if(!foundAll) {
							
								if(parInfo.endsWith(",")){
									parInfo = parInfo.substring(0,parInfo.length()-1);
								}
							
								if(parInfo.length() > 0) {
									outputKey = "P("+parInfo+",";
								}
					
								else {
									outputKey = "P(";
								}
							}
						}	
					}
				}
				
				boolean needToAddToFactor = true;
				for(int checkJoin = 0; checkJoin < joinFound.length; checkJoin++) {// If there is a join of two or more variables then we need to make sure
					if(!joinFound[checkJoin]) {
						needToAddToFactor = false;
					}
				}
					
				for(int checkJoin = 0; checkJoin < joinFound.length; checkJoin++) {// Initlize the array
					joinFound[checkJoin] = false;
				}


						
				if(needToAddToFactor) { // If we find variable that it's not defined overlapping the we want to multiply the values of the entries
					outputKey = outputKey.substring(0, outputKey.length()-1)+")";
					outputMul = externalEntry.getValue() * innerEntry.getValue();
					results[2]++;
					output.addToFactor(outputKey, outputMul);
				
					if(parInfo.length() > 0) {
						outputKey = "P("+parInfo+",";
					}	
					
					else {
						outputKey = "P(";
					}	
					
					needToAddToFactor = false;
				}
					
				else {
					if(parInfo.endsWith(",")){
						parInfo = parInfo.substring(0,parInfo.length()-1);
					}
						
					if(parInfo.length() > 0) {
						outputKey = "P("+parInfo+",";
					}
				
					else {
						outputKey = "P(";
					}
				}	
			}
		}
		
		return output;
	}
	
	
	
	private CPT eliminate(String sumVar,CPT factor) {// We sum a values according to the variable name sumVar in the factor the the function got
		
		CPT output = new CPT();
		String outputKey = "P(";
		float outputAdding = 0;
		ArrayList<String> probList = new ArrayList<>();
		
		int whereToFindName = sumVar.indexOf(":");
		sumVar = sumVar.substring(whereToFindName+1);
		Node needToSum = null;
		for(Node check : variables) {// Find the sumVar node
			if(check.getName().equals(sumVar)) {
				needToSum = check;
			}
		}
		
		for(Entry<String,Float> entry : factor.getTable().entrySet()){// Create strings such that contains the defied variables to ease the procedure of summing the non-defined
			String[] key = entry.getKey().replace("P(", "").replace(")","").split(",");
			for(int index = 0; index < key.length; index++) {
				int esign = key[index].indexOf("=");
				if(!key[index].substring(0,esign).equals(sumVar)) {
					outputKey += key[index]+",";
				}
			}
			outputKey = outputKey.substring(0, outputKey.length()-1)+")";
			if(!probList.contains(outputKey))
				probList.add(outputKey);
			
			outputKey = "P(";
			
		}	
		
		for(String search : probList) {// Search for each entry if it contains the same values as the defined variables 
			String[] validate  = search.replace("P(", "").replace(")","").split(",");
			for(Entry<String,Float> entry : factor.getTable().entrySet()){
				boolean toSum = true;
				String[] splitKey = entry.getKey().replace("P(", "").replace(")","").split(",");
				for(int Eindex = 0; Eindex < validate.length && toSum; Eindex++) {
					boolean found = false;
					for(int Iindex = 0; Iindex < splitKey.length && !found; Iindex++) {
						if(validate[Eindex].equals(splitKey[Iindex])) {
							found = true;
						}
						
						if(Iindex+1 == splitKey.length && !found) {
							toSum = false;
						}
					}
				}
				
				if(toSum) {// If we find two entry with the same define variables then we suming it into a new table 
					outputKey = search;
					if(outputAdding != 0) {
						results[1]++;
					}
						
					outputAdding += entry.getValue();
					output.addToFactor(outputKey, outputAdding);
				}	
			}
			outputAdding = 0;
			
		}
		
		return output;
		
	}
	
	
	private ArrayList<String> createParentList(Node current){// Create an ancestor List of all the defined variables such that if there exist a variable that is not 
															 //defined and he his not an ancestor of the defined variables then we can neutralized him
		
		ArrayList<String> output = new ArrayList<>();
		
		for(String parent : current.getParents()) {
			if(!output.contains(parent)) {
				output.add(parent);
		
			}
		}
		
		for(String parent : current.getParents()) {
			for(Node maybeAdd : variables) {
				if(parent.equals(maybeAdd.getName())) {
					output.addAll(createParentList(maybeAdd));
				}
			}
		}
		
		return output;
	}

	private ArrayList<String> clear(ArrayList<String> factorList,ArrayList<String> parentsList , Node calNode, String[] given){ //
	
		for(int index = factorList.size()-1; index > 1; index--) {
			int csign = factorList.get(index).indexOf("|");
			String checking = factorList.get(index).substring(0,csign).replace("P(","");
			int esign = checking.indexOf("=");
			boolean deleate = false;
			if(esign == -1) {
				if(!parentsList.contains(checking) && !checking.equals(calNode.getName())) {
					deleate = true;
				}
				
				for(int innerIndex = 0; innerIndex < given.length && !deleate; innerIndex++) {
					int givenEsign = given[innerIndex].indexOf("="); 
					String givenVar = given[innerIndex].substring(0,givenEsign);
					if(givenVar.equals(checking)) {
						deleate = true;
					}
				}
			}
			
			if(deleate) 
				factorList.remove(index);
			
		}
		
		return factorList;
	}
	
	
	
	
	
	
	
	
	private String intersection(String[] left, String[] right) {// Intersection of the factors string to figure it out witch variable according to need to join
		
		String output = "";
		
		for(int EIndex = 0; EIndex < left.length; EIndex++) {
			
			int esign1 = left[EIndex].indexOf("=");
			if(esign1 != -1) {
				left[EIndex] = left[EIndex].substring(0, esign1);
			}
			
			for(int IIndex = 0; IIndex < right.length; IIndex++) {
				int esign2 = right[IIndex].indexOf("=");
				if(esign2 != -1) {
					right[IIndex] = right[IIndex].substring(0, esign2);
				}
				
				if(right[IIndex].equals(left[EIndex])) {
					output +=right[IIndex]+",";
				}
			}
		}
		
		output = output.substring(0, output.length()-1);
		return output;
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	private ArrayList<CPT> orderingFactorsByABC(ArrayList<String> factorList, ArrayList<CPT> factorTables){
		
		ArrayList<CPT> output = new ArrayList<>();
		output.addAll(factorTables);
		
		ArrayList<Integer> summingPosition = new ArrayList<>();	
		ArrayList<String> levelFactor = new ArrayList<>();
		ArrayList<ArrayList<String>> levels = new ArrayList<>();

		
		
		for(int index = 0; index < factorList.size(); index++) {
			if(factorList.get(index).contains(":")) {
				summingPosition.add(index);
			}
		}
		
		int left = 0, right = 0, pointer = 0;
		
		for(int index = 1 ; index < summingPosition.size(); index++) {
			left = right;
			right = summingPosition.get(index);
			if(right-left!=1) {
				for(int innerIndex = left+1; innerIndex < right; innerIndex++) {
					levelFactor.add(factorList.get(innerIndex));
				}
				
				levels.add(pointer,(ArrayList<String>)levelFactor.clone());
				levelFactor.clear();
				pointer++;
			}
			
		}
		
		for(int index = right+1 ; index < factorList.size(); index++) {
			levelFactor.add(factorList.get(index));
		}
		
		levels.add(pointer,(ArrayList<String>)levelFactor.clone());
		pointer = 0;
		
		


		int tablePointer = 0;
		tablePointer = 0;
	
		for(ArrayList<String> currentLevel : levels) {
			if(currentLevel.size() > 1) {
				for(int Eindex = 0; Eindex < currentLevel.size(); Eindex++) {
					for(int Iindex = 1; Iindex < currentLevel.size() - Eindex ; Iindex++) {
						if(currentLevel.get(Iindex-1).compareTo(currentLevel.get(Iindex))>0) {
							String tmpNum = currentLevel.get(Iindex-1);
							currentLevel.set(Iindex-1, currentLevel.get(Iindex));
							currentLevel.set(Iindex, tmpNum);
							CPT tmpTable = factorTables.get(tablePointer+Iindex-1);
							output.set(tablePointer+Iindex-1, output.get(tablePointer+Iindex));
							output.set(tablePointer+Iindex, tmpTable);
						}
					}
				}
			}
			tablePointer += currentLevel.size();
		}
		
		return output;
	}
	
	
	
	
	
	
	
	
	private void algorithm2(String calculate, String[] given, Node calNode) {
		
		
		
		ArrayList<String> factorList = new ArrayList<>();
		ArrayList<String> missingnames = new ArrayList<>();
		ArrayList<Node> missingVariable = new ArrayList<>();
		
		missingVariable.add(calNode);
		missingnames.add(calNode.getName());
		
		for(Node check : variables) { // Found all the non given variables such that we know what to calculate and create for them CPT factor
			
			boolean found = false;
				
			for(int index = 0; index < given.length && !found; index++) {
					
				int esign = given[index].indexOf("=");
				String test = given[index];
				if(esign != -1) {
					test = test.substring(0,esign);
				}
				
				if(calNode.getName().equals(check.getName())) {
					found = true;
				}
				
				if(test.equals(check.getName())){
					found = true;
				}
				
				
						
				else if(!test.equals(check.getName()) && !found && index+1==given.length) {
					missingVariable.add(check);
					missingnames.add(check.getName());
				}		
			}
			
		}
		
	
		
		
		for(Node var : variables) {// Create the list of strings that represent the factors such that from them we create the factor tables
			String input = "";
			input += "P(" + var.getName();
	
			for(int index = 0; index < given.length; index++) {
				int esign = given[index].indexOf("=");
				String stat = given[index].substring(esign);
				if(given[index].substring(0, esign).equals(var.getName()))
					input += stat;
			
				
			}
			
			
			if(var.getParents().size() == 0) {
				input += ")";
				factorList.add(input);	
			}
				
			else {
				input += "|";				
				for(int index = 0; index < var.getParents().size(); index++) {
					
					boolean addComma = false, addClose = false;
					
					if(index+1 != var.getParents().size()) {
						input += var.getParents().get(index);
						
						
						for(int j = 0; j < given.length; j++) {
							int esign = given[j].indexOf("=");
							String stat = given[j].substring(esign);
							if(given[j].substring(0, esign).equals(var.getParents().get(index))) {	
								input += stat+",";
								addComma = !addComma;		
							}
						}
						
						if(!addComma)
							input += ",";
					}
					
					
					else  {
						input += var.getParents().get(index);
						if(var.getParents().get(index).equals(calNode.getName())){
							int esign = calculate.indexOf("=");
							String stat = calculate.substring(esign);
							input += stat+")";
							addClose = !addClose;
						}
						
						else {
							for(int j = 0; j < given.length; j++) {
								int esign = given[j].indexOf("=");
								String stat = given[j].substring(esign);
								if(given[j].substring(0, esign).equals(var.getParents().get(index))) {
									input += stat+")";
									addClose = !addClose;
								}	
							}
						}
						
						if(!addClose)
							input += ")";
					
						
						factorList.add(input);
					}
				}
			}
		}
		
		
		ArrayList<String> parentsList = new ArrayList<>();
		ArrayList<String> tmpList = new ArrayList<>();

		parentsList = createParentList(calNode);
		
		for(int index = 0; index < given.length; index++) {// Create list of all the ancestors of the defined variables in the query
			int esign = given[index].indexOf("=");
			String nodeName = given[index].substring(0,esign);
			for(Node check : variables) {
				if(check.getName().equals(nodeName)){
					tmpList = createParentList(check);
					for(String search : tmpList) {
						if(!parentsList.contains(search)) {
							parentsList.add(search);
						}
					}
				}
			}
		}
		
		factorList = clear(factorList, parentsList, calNode,given);// Remove all the factors string that is not being calculate 
		
		
		
		String parse = "";
		factorList.add(0, "Summing:" + calNode.getName());
		
		for(int index = 0 ; index < factorList.size(); index++) {// Adding the string Summing: <node name> such that when we find out the this string then we use eliminate function
			parse = factorList.get(index).replace("P(", "").replace(")", "");
			int csign = parse.indexOf("|");
			int esign = parse.indexOf("=");
			
			
			if(csign == -1) {
				if(esign == -1 && missingnames.contains(parse) && !parse.equals(calNode.getName())) {
					factorList.add(index, "Summing:" + parse);
					missingnames.remove(parse);
					index++;
				}
				
			}
			
			else {
				String[] cond = parse.substring(csign+1).split(",");
				parse = parse.substring(0, csign);
				esign = parse.indexOf("=");
				
				if(esign != -1) {
					parse = parse.substring(0,esign);
				}	
				
				
				if(missingnames.contains(parse) && !parse.equals(calNode.getName())) {
					factorList.add(index, "Summing:" + parse);
					missingnames.remove(parse);
					index++;
				
				}	
				
			}
		
		}
		
	
		ArrayList<CPT> factorTables = new ArrayList<>();
		
		for(int index = 0; index < factorList.size(); index++) {// Create the tables factors and insert it into a list
			factorList.set(index,  factorList.get(index).replace('|', ','));
			String factor = factorList.get(index);
			if(!factor.contains(":")) {
				CPT tmp = createFactorTable(factor);
				if(!(tmp.size <= 1) || calNode.getName().equals(factor)) {
					factorTables.add(createFactorTable(factor));
				}
					
				else {
					factorList.remove(index);
					index--;
				}
			}	
		}
		
		factorTables =  orderingFactorsByABC(factorList, factorTables);
		
		String joinNodeName = ""; // Passing from the end to the start of the list of string factors and according to the string factors we join/eliminate the factors table 
		int listLength = factorTables.size()-1;
		for(int backwardIndex = factorList.size()-1; backwardIndex > 1; backwardIndex--) {
			String joinOrEliminate = factorList.get(backwardIndex-1);
			if(!joinOrEliminate.contains(":")) {
				CPT left = factorTables.get(listLength-1);
				CPT right = factorTables.get(listLength);
				factorTables.remove(listLength);
				factorTables.remove(listLength-1);
				
				Object[] leftValues =  left.getTable().keySet().toArray();
				Object[] rightValues = right.getTable().keySet().toArray();

				String[] leftString =  ((String) leftValues[0]).replace("P(","").replace(")","").replace("|",",").split(",");
				String[] rightString = ((String) rightValues[0]).replace("P(","").replace(")","").replace("|",",").split(",");
				joinNodeName = intersection(leftString, rightString);

				
				
				listLength--;					
				
				factorTables.add(join(left,right,joinNodeName));
				

			}
			
			else {
			
				
				
				CPT suming = factorTables.get(listLength);
				factorTables.remove(listLength);
				String tmp = factorList.get(backwardIndex-1).replace(":", "");
				factorList.remove(backwardIndex-1);
				factorTables.add(eliminate(joinOrEliminate,suming));
				

			}
			
		}
		
		String checkStatus = querie.replace("P(","") ;
		int csign = checkStatus.indexOf("|");
		checkStatus = checkStatus.substring(0, csign);
		
		CPT finalTable = factorTables.get(0);
		boolean first = true;
		for(Entry<String,Float> entry : finalTable.getTable().entrySet()){// Create the factor and calculate the results
			summing += entry.getValue();
		
			if(!first) {
				results[1]++;
			}
			first = false;
			if(entry.getKey().contains(checkStatus)) {
				String[] vertificate = entry.getKey().replace("P(", "").replace(")","").split(",");
				for(int index = 0; index < vertificate.length;index++) {
					if(checkStatus.equals(vertificate[index])) {
						results[0] = entry.getValue();// If the entry and the query has the same define variables then it is the results
					}
				}
			}
		}

		float alpha = 1/this.summing;// Create the factor for the result
		this.results[0] *= alpha; 
		results[0] = (float)Math.round(results[0] * 100000) / 100000;
	}

	
	/**
	 * 
	 * For every variable that we need to sum we separate all the sum variables into separate lists. 
	 * For example {Summing:B,P(B),Summing:E,P(E),Summing:A,P(A|B,E),P(J|A),P(M|A)}
	 * We separate them into lists of list {{Summing:B,P(B)},{Summing:E,P(E),{Summing:A,P(A|B,E),P(J|A),P(M|A)}}
	 * So for every place there is Summing:<variable name> we separate until the next summing string into a new list
	 * And for any separate list in the length of at least of a 3 we order the factor tables such that in the end of the 
	 * List there is the smallest factor tables and when we doing join we done it on the smallest factor tables that this list had
	 * @param factorList - String list of factors such that determine the order of tables
	 * @param factorTables - CPT list of factors that we in this function order them such that give us the minimal multiplication operation
	 * @return The best order of factors tables that reduce the number of multiplications 
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<CPT> orderingFactorsBySize(ArrayList<String> factorList, ArrayList<CPT> factorTables){
		
		ArrayList<CPT> output = new ArrayList<>();
		output.addAll(factorTables);
		
		ArrayList<Integer> summingPosition = new ArrayList<>();	
		ArrayList<String> levelFactor = new ArrayList<>();
		ArrayList<ArrayList<String>> levels = new ArrayList<>();

		
		
		for(int index = 0; index < factorList.size(); index++) {
			if(factorList.get(index).contains(":")) {
				summingPosition.add(index);
			}
		}
		
		int left = 0, right = 0, pointer = 0;
		
		for(int index = 1 ; index < summingPosition.size(); index++) {
			left = right;
			right = summingPosition.get(index);
			if(right-left!=1) {
				for(int innerIndex = left+1; innerIndex < right; innerIndex++) {
					levelFactor.add(factorList.get(innerIndex));
				}
				
				levels.add(pointer,(ArrayList<String>)levelFactor.clone());
				levelFactor.clear();
				pointer++;
			}
			
		}
		
		for(int index = right+1 ; index < factorList.size(); index++) {
			levelFactor.add(factorList.get(index));
		}
		
		levels.add(pointer,(ArrayList<String>)levelFactor.clone());
		pointer = 0;
		
		
		ArrayList<Integer> tmp = new ArrayList<>();
		ArrayList<ArrayList<Integer>> sizeLevel = new ArrayList<>();

		int tablePointer = 0;
		for(int index = 0; index < levels.size(); index++) {
			for(int j = 0; j < levels.get(index).size(); j++) {
				tmp.add(factorTables.get(tablePointer).size());
				tablePointer++;
			}
			sizeLevel.add(index,(ArrayList<Integer>)tmp.clone());
			tmp.clear();
		}
		tablePointer = 0;
		for(ArrayList<Integer> currentLevel : sizeLevel) {
			if(currentLevel.size() > 2) {
				for(int Eindex = 0; Eindex < currentLevel.size(); Eindex++) {
					for(int Iindex = 1; Iindex < currentLevel.size() - Eindex ; Iindex++) {
						if(currentLevel.get(Iindex-1) < currentLevel.get(Iindex)) {
							int tmpNum = currentLevel.get(Iindex-1);
							currentLevel.set(Iindex-1, currentLevel.get(Iindex));
							currentLevel.set(Iindex, tmpNum);
							CPT tmpTable = factorTables.get(tablePointer+Iindex-1);
							output.set(tablePointer+Iindex-1, output.get(tablePointer+Iindex));
							output.set(tablePointer+Iindex, tmpTable);
						}
					}
				}
			}
			tablePointer += currentLevel.size();
		}
		
		return output;
	}
	
	
	private void algorithm3(String calculate, String[] given, Node calNode) {// Same as algorithm 2 except we order the factor tables that reduce the number of mutiplication
		
		ArrayList<String> factorList = new ArrayList<>();
		ArrayList<String> missingnames = new ArrayList<>();
		ArrayList<Node> missingVariable = new ArrayList<>();
		
		missingVariable.add(calNode);
		missingnames.add(calNode.getName());
		
		for(Node check : variables) { // found all the non given variables such that we know what to calculate
			
			boolean found = false;
				
			for(int index = 0; index < given.length && !found; index++) {
					
				int esign = given[index].indexOf("=");
				String test = given[index];
				if(esign != -1) {
					test = test.substring(0,esign);
				}
				
				if(calNode.getName().equals(check.getName())) {
					found = true;
				}
				
				if(test.equals(check.getName())){
					found = true;
				}
				
				
						
				else if(!test.equals(check.getName()) && !found && index+1==given.length) {
					missingVariable.add(check);
					missingnames.add(check.getName());
				}		
			}
			
		}
		
	
		
		
		for(Node var : variables) {
			String input = "";
			input += "P(" + var.getName();
	
			for(int index = 0; index < given.length; index++) {
				int esign = given[index].indexOf("=");
				String stat = given[index].substring(esign);
				if(given[index].substring(0, esign).equals(var.getName()))
					input += stat;
			
				
			}
			
			
			if(var.getParents().size() == 0) {
				input += ")";
				factorList.add(input);	
			}
				
			else {
				input += "|";				
				for(int index = 0; index < var.getParents().size(); index++) {
					
					boolean addComma = false, addClose = false;
					
					if(index+1 != var.getParents().size()) {
						input += var.getParents().get(index);
						
						
						for(int j = 0; j < given.length; j++) {
							int esign = given[j].indexOf("=");
							String stat = given[j].substring(esign);
							if(given[j].substring(0, esign).equals(var.getParents().get(index))) {	
								input += stat+",";
								addComma = !addComma;		
							}
						}
						
						if(!addComma)
							input += ",";
					}
					
					
					else  {
						input += var.getParents().get(index);
						if(var.getParents().get(index).equals(calNode.getName())){
							int esign = calculate.indexOf("=");
							String stat = calculate.substring(esign);
							input += stat+")";
							addClose = !addClose;
						}
						
						else {
							for(int j = 0; j < given.length; j++) {
								int esign = given[j].indexOf("=");
								String stat = given[j].substring(esign);
								if(given[j].substring(0, esign).equals(var.getParents().get(index))) {
									input += stat+")";
									addClose = !addClose;
								}	
							}
						}
						
						if(!addClose)
							input += ")";
					
						
						factorList.add(input);
					}
				}
			}
		}
		
		
		ArrayList<String> parentsList = new ArrayList<>();
		ArrayList<String> tmpList = new ArrayList<>();

		parentsList = createParentList(calNode);
		
		for(int index = 0; index < given.length; index++) {
			int esign = given[index].indexOf("=");
			String nodeName = given[index].substring(0,esign);
			for(Node check : variables) {
				if(check.getName().equals(nodeName)){
					tmpList = createParentList(check);
					for(String search : tmpList) {
						if(!parentsList.contains(search)) {
							parentsList.add(search);
						}
					}
				}
			}
		}
		
		factorList = clear(factorList, parentsList, calNode,given);
		
		
		
		
		
		String parse = "";
		factorList.add(0, "Summing:" + calNode.getName());
		
		for(int index = 0 ; index < factorList.size(); index++) {
			parse = factorList.get(index).replace("P(", "").replace(")", "");
			int csign = parse.indexOf("|");
			int esign = parse.indexOf("=");
			
			
			if(csign == -1) {
				if(esign == -1 && missingnames.contains(parse) && !parse.equals(calNode.getName())) {
					factorList.add(index, "Summing:" + parse);
					missingnames.remove(parse);
					index++;
				}
				
			}
			
			else {
				String[] cond = parse.substring(csign+1).split(",");
				parse = parse.substring(0, csign);
				esign = parse.indexOf("=");
				
				if(esign != -1) {
					parse = parse.substring(0,esign);
				}	
				
				
				if(missingnames.contains(parse) && !parse.equals(calNode.getName())) {
					factorList.add(index, "Summing:" + parse);
					missingnames.remove(parse);
					index++;
				
				}	
				
			}
		
		}
		
		
		
	
		ArrayList<CPT> factorTables = new ArrayList<>();
		
		for(int index = 0; index < factorList.size(); index++) {
			factorList.set(index,  factorList.get(index).replace('|', ','));
			String factor = factorList.get(index);
			if(!factor.contains(":")) {
				CPT tmp = createFactorTable(factor);
				if(!(tmp.size <= 1) || calNode.getName().equals(factor)) {
					factorTables.add(createFactorTable(factor));
				}
					
				else {
					factorList.remove(index);
					index--;
				}
			}	
		}
		
		ArrayList<CPT> factorTable =  orderingFactorsBySize(factorList, factorTables);
		
		
		
		String joinNodeName = ""; 
		int listLength = factorTable.size()-1;
		for(int backwardIndex = factorList.size()-1; backwardIndex > 1; backwardIndex--) {
			String joinOrEliminate = factorList.get(backwardIndex-1);
			if(!joinOrEliminate.contains(":")) {
				CPT left = factorTable.get(listLength-1);
				CPT right = factorTable.get(listLength);
				factorTable.remove(listLength);
				factorTable.remove(listLength-1);
				
				Object[] leftValues =  left.getTable().keySet().toArray();
				Object[] rightValues = right.getTable().keySet().toArray();

				String[] leftString =  ((String) leftValues[0]).replace("P(","").replace(")","").replace("|",",").split(",");
				String[] rightString = ((String) rightValues[0]).replace("P(","").replace(")","").replace("|",",").split(",");
				joinNodeName = intersection(leftString, rightString);

				
				
				listLength--;					
				
				factorTable.add(join(left,right,joinNodeName));
				

			}
			
			else {
				CPT suming = factorTable.get(listLength);
				factorTable.remove(listLength);
				String tmp = factorList.get(backwardIndex-1).replace(":", "");
				factorList.remove(backwardIndex-1);
				factorTable.add(eliminate(joinOrEliminate,suming));
				

			}
			
		}
		
		String checkStatus = querie.replace("P(","") ;
		int csign = checkStatus.indexOf("|");
		checkStatus = checkStatus.substring(0, csign);
		
		CPT finalTable = factorTable.get(0);
		boolean first = true;
		for(Entry<String,Float> entry : finalTable.getTable().entrySet()){
			summing += entry.getValue();
		
			if(!first) {
				results[1]++;
			}
			first = false;
			if(entry.getKey().contains(checkStatus)) {
				String[] vertificate = entry.getKey().replace("P(", "").replace(")","").split(",");
				for(int index = 0; index < vertificate.length;index++) {
					if(checkStatus.equals(vertificate[index])) {
						results[0] = entry.getValue();
					}
				}
			}
		}

		float alpha = 1/this.summing;
		this.results[0] *= alpha; 
		results[0] = (float)Math.round(results[0] * 100000) / 100000;	
		
	}
}
