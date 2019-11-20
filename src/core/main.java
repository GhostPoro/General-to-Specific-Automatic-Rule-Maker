package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main {
	
	private static enum DATATYPE { OPTION, RESULT };
	
	static Options options;
	
	private static final String introduction =	"Welcome in General-To-Specific Automatic Rules Maker.\n" +
												"Author: Oleksii Lysienkov.\n" +
												"Using this application you agree and accept EULA.\n" +
												"For more information use -eula key.";
	
	private static final String EULA =	"\nTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,\n" + 
										"INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR\n" + 
										"PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM,\n" + 
										"DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" + 
										"OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.";
	
	// EULA
	/* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
	 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
	 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM,
	 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
	 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */

	public static void main(String[] args) {
		
		// Printing introduction
		print(introduction);
		
		options = new Options();
		parseInputParams(args);
		
		// Printing EULA
		if(options.showEULA) { print(EULA); }
		
		if(options.pritHelp) {
			options.printHelp();
			return;
		}

		if(options.inName == null) {
			print("\nNo Input File Specified. Exiting...");
			return;
		}
		
		if(options.sas || options.sss) {
			print("");
			print("Program Path: " + options.jarPath);
			print("Input File Path: " + options.inPath);
			print("Input File Name: " + options.inName);
			print("Input File ext: " + options.inExtension);
			print("");
		}

		List<String> inputData = Utils.readAllLines(new String(options.inPath + File.separatorChar + options.inName + options.inExtension));
		
		if(inputData == null) {
			print("\nFatal Error! Cant Read Data from Input File. Exiting...");
			return;
		}
		
		List<DATATYPE> datatypes = new ArrayList<DATATYPE>();
		List<Integer> optionColumns = new ArrayList<Integer>();
		List<Integer> resultColumns = new ArrayList<Integer>();
		
		List<String> columnNames = new ArrayList<String>();
		List<String> fillers = new ArrayList<String>();
		List<String> rawStringData = new ArrayList<String>();
		
		for (int md = 0; md < inputData.size(); md++) {
			String line = inputData.get(md);
			
			// Removing all spaces, tabulation, etc... 
			line.replaceAll("\\s{2,}", " ");
			
			if (line.contains("<")) {
				String[] lineParts = line.split(" ");
				for (int i = 1; i < lineParts.length - 1; i++) {
					String symbol = lineParts[i].trim();
					if (symbol.equals("s") || symbol.equals("o")) {
						datatypes.add(DATATYPE.OPTION);
						optionColumns.add(i - 1);
					}
					else if (symbol.equals("d") || symbol.equals("r")) {
						datatypes.add(DATATYPE.RESULT);
						resultColumns.add(i - 1);
					}
				}
			}
			else if(line.contains("[")) {
				md = processColumnNames(inputData, columnNames, md) - 1;
			}
			else {
				String[] lineParts = line.split(" ");
				for (int i = 0; i < lineParts.length; i++) {
					String part = lineParts[i].trim();
					if (part.length() > 0) {
						if(options.sas) { print("Adding Raw Unprocessed Data: -->" + part + "<-- (with no spaces)"); }
						rawStringData.add(part);
					}
				}
			}
		}
		
		if(options.sas || options.sss) { 
			print("\nColumns Names:");
			for(String name : columnNames) {
				System.out.print(name + " ");
			}
			System.out.print("\n\n");
		}
		
		List<int[]> combinations =  calculateColumnCombinations(optionColumns);
		
		if(options.sas || options.sss) {
			
			int combSize = combinations.size();

			print("\nCombinations:");
			
			for (int s = 0; s < combSize; s++) {
				
				int[] combination = combinations.get(s);
				int combLength = combination.length;
				System.out.print("Line: " + s + " - ");
				for (int l = 0; l < combLength; l++) {
					System.out.print(combination[l] + " ");
				}
				System.out.print("- \n");
			}
		}
		
		int data_size = rawStringData.size();
		int columns_size = columnNames.size();
		
		int rows = data_size / columns_size;
		
		if (data_size % columnNames.size() == 0) {
			if(options.sas || options.sss) { print("\nOK! Rows and Columns Found their Synergy..."); }
		}
		else {
			print("\nFatal Error! Rows and Columns have inproper correlation (possible Input data corruption and/or bad input file formatting). Exiting...");
			return;
		}
		
		int[][] table = new int[columns_size][rows];
		
		int i = 0;
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < columns_size; x++) {
				table[x][y] = -1;
				String title = rawStringData.get(i++);
				int fillers_size = fillers.size();
				boolean inTable = false;
				
				if(fillers_size > 0) {
					for (int n = 0; n < fillers_size && !inTable; n++) {
						if(fillers.get(n).equals(title)) {
							table[x][y] = n;
							inTable = true;
						}
					}
					if(!inTable) {
						fillers.add(title);
						table[x][y] = fillers_size;
						inTable = true;
					}
				}
				else {
					fillers.add(title);
					table[x][y] = 0;
				}
			}
		}
		
		if(options.sas || options.sss) {
			print("");
			System.out.print("Legend: ");
			for(String fillerName : fillers) {
				System.out.print(fillerName + " ");
			}
			print("");
	
			for (int y = 0; y < rows; y++) {
				for (int x = 0; x < columns_size; x++) {
					System.out.print(table[x][y] + " ");
				}
				System.out.print("\n");
			}
		}
		
		
		Map<Integer, List<Conclusion>> results = processGTS( datatypes, table, columnNames, fillers, optionColumns, resultColumns, combinations);
		if(results == null) {
			print("Error!!! Results equals null. Exiting...");
			return;
		}
		
		List<Conclusion> halfSortedConclusions = new ArrayList<Conclusion>();
		
		// Store only Valid Results From Map
		print("");
		for(int key : results.keySet()) {
			List<Conclusion> conclusions = results.get(key);
			if(options.sas || options.sss) { print("Number of Conclusions for Key -" + fillers.get(key) + "- : " + conclusions.size()); }
			for(Conclusion conc : conclusions) {
				if(conc.isValid()) {
					halfSortedConclusions.add(conc);
				}
			}
		}
		print("");
		
		// Delete duplicates
		for (int org = 0; org < halfSortedConclusions.size(); org++) {
			
			Conclusion orig = halfSortedConclusions.get(org);
			
			double orig_score = orig.getScore();
			List<Integer> orig_cr = orig.getCoveredRows();
			int orig_cr_size = orig_cr.size();
			
			for (int psd = 0; psd < halfSortedConclusions.size(); psd++) {
				if(org != psd) {
					Conclusion psdup = halfSortedConclusions.get(psd);
					if(orig_score == psdup.getScore()) {
						
						List<Integer> psdup_cr = psdup.getCoveredRows();
						int psdup_cr_size = psdup_cr.size();
						if(orig_cr_size == psdup_cr_size) {
							boolean same = true;
							for (int cmrp = 0; cmrp < orig_cr_size && same; cmrp++) {
								if(orig_cr.get(cmrp) != psdup_cr.get(cmrp)) {
									same = false;
								}
							}
							
							if(same) {
								halfSortedConclusions.remove(psd);
							}
						}
						
					}
				}
				
			}
		}
		
		if(options.sas || options.sss) {
			print("\nHalf Sorted Conclusions:");
			for(Conclusion conc : halfSortedConclusions) {
				System.out.print("Result: " + fillers.get(conc.getResult()) + " Score: " + conc.getScore() + " Options: [ ");
				int counter = 0;
				for (int param : conc.getParam()) {
					if(counter++ % 2 == 0) {
						System.out.print((param + 1) + " ");
					}
					else {
						System.out.print(fillers.get(param) + " ");
					}
				}
				System.out.print("]");
				
				if(true) {
					System.out.print(" Covered Results: [ ");
					for (int cr : conc.getCoveredRows()) {
						System.out.print((cr + 1) + " ");
					}
					System.out.print("]");
				}
				print("");
			}
			print("");
		}
		
		if(options.saveHalfSorted) {
			exportInFile(halfSortedConclusions, fillers, columnNames, columnNames.get(resultColumns.get(0)));
			print("Half Sorted Result Saved");
			return;
		}
		
		
		List<Conclusion> sortedConclusions = finalizeGTS(halfSortedConclusions, table, resultColumns.get(0), columnNames.size(), rows);
		
		if(options.sas || options.sss) {
			print("\nSorted Conclusions:");
			for(Conclusion conc : sortedConclusions) {
				System.out.print("Result: " + fillers.get(conc.getResult()) + " Score: " + conc.getScore() + " Options: [ ");
				int counter = 0;
				for (int param : conc.getParam()) {
					if(counter++ % 2 == 0) {
						System.out.print((param + 1) + " ");
					}
					else {
						System.out.print(fillers.get(param) + " ");
					}
				}
				System.out.print("]");
				
				if(true) {
					System.out.print(" Covered Results: [ ");
					for (int cr : conc.getCoveredRows()) {
						System.out.print((cr + 1) + " ");
					}
					System.out.print("]");
				}
				print("");
			}
		}
		
		exportInFile(sortedConclusions, fillers, columnNames, columnNames.get(resultColumns.get(0)));
		
		print("Job Done! :)");
	}
	
	private static List<Conclusion> finalizeGTS(List<Conclusion> unsortedList, int[][] data, int resLocation, int columnSize, int listSize) {
		
		List<Conclusion> copyUnsortedList = new ArrayList<Conclusion>();
		
		for (int i = 0; i < unsortedList.size(); i++) {
			copyUnsortedList.add(unsortedList.get(i));
		}
				
		
		List<Conclusion> finalRes = new ArrayList<Conclusion>();
		List<Integer> existingRows = new ArrayList<Integer>();
		List<Integer> deletedRows = new ArrayList<Integer>();

		// Filling dummy ArrayList with numerical row for later usage (deleting and comparing)
		for (int i = 0; i < listSize; i++) {
			existingRows.add(i);
		}
		
		boolean found = true;
		
		while(existingRows.size() > 0 && found) {
			found = false;
			
			List<Conclusion> localTempBestConclusions = new ArrayList<Conclusion>();
			List<Integer> removeListFromTempBest = new ArrayList<Integer>();
			
			double maxScore = -10_000_000_000d;
			int maxCovarege = 0;
			int minOptionCounter = 999;
			int minRowPos = 999;
			
			// Delete all what not fit
			if(existingRows.size() < listSize) {
				
				for(int i = unsortedList.size() - 1; i > -1; i--) {
					boolean delete = false;
					Conclusion localConclusion = unsortedList.get(i);
					List<Integer> affectedRows = localConclusion.getCoveredRows();
					for(int affectedRow : affectedRows) {
						for(int deletedRow : deletedRows) {
							if(affectedRow == deletedRow) {
								delete = true;
							}
						}
					}
					
					if(delete) {
						unsortedList.remove(i);
					}
				}
			}
			
			// Find Best score
			for(Conclusion conc : unsortedList) {
				double localScore = conc.getScore();
				if(maxScore < localScore) {
					maxScore = localScore;
				}
			}
			
			// Find Best by score (in range)
			for(Conclusion conc : unsortedList) {
				if(maxScore <= conc.getScore()) {
					localTempBestConclusions.add(conc);
				}
			}
			
			// Find Best by coverage (in range)
			for (int i = 0; i < localTempBestConclusions.size(); i++) {
				
				Conclusion localConclusion = localTempBestConclusions.get(i);
				int localCovarege = localConclusion.getCoveredRows().size();
				
				if(maxCovarege < localCovarege) {
					maxCovarege = localCovarege;
					i = -1;
				}
				
				if(maxCovarege > localCovarege) {
					removeListFromTempBest.add(i);
				}
			}
			
			for (int i = removeListFromTempBest.size() - 1; i > -1; i--) {
				int rid = removeListFromTempBest.get(i);
				if(localTempBestConclusions.size() > rid) { localTempBestConclusions.remove(rid); }
			}
			removeListFromTempBest.clear();
			
			// Find Best by lowest options (in range)
			for (int i = 0; i < localTempBestConclusions.size(); i++) {
				Conclusion localConclusion = localTempBestConclusions.get(i);
				int localOptionNum = localConclusion.getParam().length;
				
				if(minOptionCounter > localOptionNum) {
					minOptionCounter = localOptionNum;
					i = -1;
				}
				
				if(minOptionCounter < localOptionNum) {
					removeListFromTempBest.add(i);
				}
			}
			
			for (int i = removeListFromTempBest.size() - 1; i > -1; i--) {
				int rid = removeListFromTempBest.get(i);
				if(localTempBestConclusions.size() > rid) { localTempBestConclusions.remove(rid); }
			}
			removeListFromTempBest.clear();
			
			
			// Find Best by position (in range)
			for (int i = 0; i < localTempBestConclusions.size(); i++) {
				
				Conclusion localConclusion = localTempBestConclusions.get(i);
				int localRowPos = localConclusion.getParam()[0];
								
				if(minRowPos > localRowPos) {
					minRowPos = localRowPos;
					i = -1;
				}
				
				if(minRowPos < localRowPos) {
					removeListFromTempBest.add(i);
				}
			}
			
			for (int i = removeListFromTempBest.size() - 1; i > -1; i--) {				
				int rid = removeListFromTempBest.get(i);
				if(localTempBestConclusions.size() > rid) { localTempBestConclusions.remove(rid); }
			}
			removeListFromTempBest.clear();
			
			
			// Add Best to Final Result and delete rows from allowed range
			// switch "found" key
			for(Conclusion localConclusion : localTempBestConclusions) {
				for(int resField : localConclusion.getCoveredRows()) {
					for (int i = existingRows.size() - 1; i > -1; i--) {
						if(existingRows.get(i) == resField) {
							existingRows.remove(i);
							deletedRows.add(resField);
						}
					}
				}
				
				finalRes.add(localConclusion);
				found = true;
			}
			localTempBestConclusions.clear();
		}
		
		
		if(existingRows.size() > 0) {
			for (int i = 0; i < existingRows.size();i++) {
				int row = existingRows.get(i);
				boolean foundd = false;
				for(Conclusion copyUnsorted : copyUnsortedList) {
					if(!foundd) {
						List<Integer> inRows = copyUnsorted.getCoveredRows();
						if(inRows.size() == 1) {
							int inRow = inRows.get(0);
							if(inRow == row) {
								finalRes.add(copyUnsorted);
								existingRows.remove(i);
								foundd = true;
								i = -1;
							}
						}
					}
				}
			}
			
		}
		
		if(existingRows.size() > 0) {
			System.out.print("WARNING!!! Exist unprocessed Rows: ");
			for (int cr : existingRows) {
				System.out.print((cr + 1) + " ");
			}
			print("\nCreating custom rules for them...\n");
			
			for (int cr : existingRows) {
				Conclusion customConc = new Conclusion(-1, 1, true);
				for (int x = 0; x < columnSize; x++) {
					if(x == resLocation) {
						customConc.setResult(data[x][cr]);
					}
					else {
						customConc.addParam(x, data[x][cr]);
					}
				}
				finalRes.add(customConc.addCoveredRow(cr)); 
			}
		}		
		return finalRes;
	}
	
	
	private static Map<Integer, List<Conclusion>> processGTS(List<DATATYPE> datatypes, int[][] data, List<String> columnNames, List<String> fillers, List<Integer> optionColumns, List<Integer> resultColumns, List<int[]> combinations) {
		int resultLocation = resultColumns.get(0);
		if(resultLocation == -1) {
			return null;
		}
		
		int columns_size = columnNames.size();
		int rows_size = data[0].length;
		int processDataColumnLocation = 0;
		
		while(processDataColumnLocation == resultLocation && processDataColumnLocation < columns_size) {
			processDataColumnLocation++;
		}
		
		if(processDataColumnLocation >= columns_size) {
			return null;
		}
		
		Map<Integer, List<Conclusion>> optionMap = new HashMap<Integer, List<Conclusion>>();
		List<Integer> knownResults = new ArrayList<Integer>();
		
		for (int y = 0; y < rows_size; y++) {
			
			// Get Result from Table
			int localRes = data[resultLocation][y];
			
			List<Conclusion> variants = optionMap.get(localRes);
			if(variants == null) {
				variants = new ArrayList<Conclusion>();
				optionMap.put(localRes, variants);
				knownResults.add(localRes);
			}
			
			// Create Map for Result
			for(int[] combination : combinations) {
				
				Conclusion loacalConc = new Conclusion(localRes, false);
				
				int optionCounter = 0;
				int resultsCounter = 0;
				
				for(int column : combination) {
					int localOption = data[column][y];
					loacalConc.addParam(column,localOption);
				}
				
				List<Integer> coveredResults = new ArrayList<Integer>();
				
				for (int lr = 0; lr < rows_size; lr++) {
					boolean fine = true;
					
					int paramShift = 1;
					for(int column : combination) {
						int localOption = data[column][lr];
						int[] params = loacalConc.getParam();
						if(localOption != params[paramShift]) {
							fine = false;
						}
						paramShift += 2;
					}
					
					// combination validation
					if(fine) {
						optionCounter++;
						if(localRes == data[resultLocation][lr]) {
							resultsCounter++;
							coveredResults.add(lr);
						}
					}
				}
				
				// Calculate score for conclusion
				
				// G = (Ep + Eb) / E
				// A = Ep / (Ep + Eb)
				// IF A == 1 (a/a) -->
				// H = G + sqrt(A)
				
				double G = ((double) optionCounter)  / ((double) rows_size);
				double A = ((double) resultsCounter) / ((double) optionCounter);
				double score = G + Math.sqrt(A);
				
				loacalConc.setScore(score);
				
				if(resultsCounter == optionCounter) {
					loacalConc.setValid();
				}
				
				loacalConc.setCoveredRows(coveredResults);
				variants.add(loacalConc);
			}
		}
		return optionMap;
	}
	
	private static void exportInFile(List<Conclusion> conclusions, List<String> fillers, List<String> columnNames, String resultName) {
		String foldersPath = null;
		boolean created = false;
		boolean exist = false;
		
		if(options.outPath == null) {
			foldersPath = new File(options.finalInPath).getParent();
			File tempFile = new File(foldersPath);
			exist = tempFile.exists();
			created = tempFile.mkdirs();
			options.outPath = new String(foldersPath + File.separatorChar + options.inName + "_" + options.getCurrentLanguage() + "_result" + options.inExtension);
		}
		else {
			File tempFile = new File(new File(options.outPath).getParent());
			exist = tempFile.exists();
			created = tempFile.mkdirs();
		}
		
		print("Output File: " + options.outPath);
		
		if(created || exist) {
			PrintWriter out;
			try {
				out = new PrintWriter(options.outPath.trim());
				
				int counter = 1;
				for (Conclusion conc : conclusions) {
					
					boolean firsttime = true;
					
					int[] optionss = conc.getParam();
					int optionsSize = optionss.length;
					int optionCounter = 0;
					
					if(options.lang == 1) { // pl
						out.println("Regula " + counter);
						while (optionCounter < optionsSize){
							if(firsttime) {
								out.println("JEZELI  " + columnNames.get(optionss[optionCounter++]) + "\t\tJEST  " + fillers.get(optionss[optionCounter++]));
								firsttime = false;
							}
							else {
								out.println("ORAZ    " + columnNames.get(optionss[optionCounter++]) + "\t\tJEST  " + fillers.get(optionss[optionCounter++]));
							}
							
						}
						out.println("TO      " + resultName + "\t\tJEST  " + fillers.get(conc.getResult()) + "\t\t\t" + conc.printCoveredRows());
					}
					else {
						out.println("Rule " + counter);

						
						while (optionCounter < optionsSize){
							if(firsttime) {
								out.println("IF    " + columnNames.get(optionss[optionCounter++]) + "\t\tIS  " + fillers.get(optionss[optionCounter++]));
								firsttime = false;
							}
							else {
								out.println("AND   " + columnNames.get(optionss[optionCounter++]) + "\t\tIS  " + fillers.get(optionss[optionCounter++]));
							}
							
						}
						out.println("THEN  " + resultName + "\t\tIS  " + fillers.get(conc.getResult()) + "\t\t\t" + conc.printCoveredRows());
					}
					counter++;
					out.println();
					out.println();
				}
				
				out.close();
			} catch (FileNotFoundException e) { e.printStackTrace(); }
		}
		else {
			print("Error! Can't create folder: " + foldersPath);
		}


	}
	
	private static List<int[]> calculateColumnCombinations(List<Integer> optionColumns) {
		List<int[]> result = new ArrayList<int[]>();
		
		List<int[]> additionalResults = new ArrayList<int[]>();
		
		int combiationSize = 1;
		
		int optionsSize = optionColumns.size();
		
		while(combiationSize < optionsSize) {
			
			int shift = 0;
			while(shift + combiationSize - 1 < optionsSize) {
				List<Integer> combinations = new ArrayList<Integer>();
				for (int i = shift; i < shift + combiationSize; i++) {
					combinations.add(optionColumns.get(i));
				}
				shift++;
				result.add(Utils.listIntToArray(combinations));
				
				combinations.clear();
				combinations = null;
			}
			
			combiationSize++;
		}
		
		List<Integer> masterCombinations = new ArrayList<Integer>();
		for (int i = 0; i < optionsSize; i++) {
			masterCombinations.add(optionColumns.get(i));
		}
		
		
		int[] masterArray = Utils.listIntToArray(masterCombinations);
		
		masterCombinations.clear();
		masterCombinations = null;
		
		int resultSize = result.size();
		
		for (int comb = 0; comb < resultSize; comb++) {
			
			int[] delData = result.get(comb);
			
			if(options.sas) {
				print("\nDeleting: ");
				for(int unit : delData) {
					System.out.print(unit + " ");
				}
				print("");
			}
			
			
			int[] tempArray = deleteArraysObjects(masterArray, delData);
			additionalResults.add(tempArray);
			
			for (int adcomb = 0; adcomb < resultSize; adcomb++) {
				int[] arrData = result.get(adcomb);
				
				int arrDataSize = arrData.length;
				int delDataSize = delData.length;
				
				if(arrDataSize > 2 && arrDataSize > delDataSize) {
					int[] tempArrayIn = deleteArraysObjects(arrData, delData);
					
					if(options.sas) {
						print("Original Array: ");
						for(int unit : arrData) {
							System.out.print(unit + " ");
						}
	
						print("\nNew Array: ");
						for(int unit : tempArrayIn) {
							System.out.print(unit + " ");
						}
						print("\n");
					}
					additionalResults.add(tempArrayIn);
				}
			}
		}

		
		for(int[] ar : additionalResults) {
			boolean inside = false;
			boolean insert = true;
			for (int i = 0; i < result.size() && !inside; i++) {
				inside = contain(result.get(i), ar, 0);
				if(inside && result.get(i).length == ar.length) {
					insert = false;
				}
			}
			if(insert) { result.add(ar); }
		}
		
		additionalResults.clear();
		additionalResults = null;
		
		result.add(masterArray);
		
		return result;
	}
	
	private static int[] deleteArraysObjects(int[] array, int[] delletigItems) {
		
		int arraySize = array.length;
		int delSize = delletigItems.length;
		
		if(arraySize < 2 || delSize < 1) {
			return array;
		}
		
		boolean found = false;
		
		int deleteItem = -1;
		
		for (int a = 0; a < arraySize && !found; a++) {
			if(array[a] == delletigItems[0]) {
				deleteItem = a;
				found = true;
			}
		}
		
		int smallerDelSize = delSize - 1;
		int[] smallerDelletigItems = new int[smallerDelSize];
		
		if(delSize > 1) {
			for(int o = 1, s = 0; s < smallerDelSize; o++, s++) {
				smallerDelletigItems[s] = delletigItems[o];
			}
		}
		
		if(found) {
			int smallerSize = arraySize - 1;
			int[] smallerArray = new int[smallerSize];
			
			for(int o = 0, s = 0; s < smallerSize; o++, s++) {
				if(o == deleteItem) {
					o++;
				}
				smallerArray[s] = array[o];
			}
			return deleteArraysObjects(smallerArray, smallerDelletigItems);
		}
		
		return deleteArraysObjects(array, smallerDelletigItems);
	}
	
	private static boolean contain(int[] container, int[] checkers, int position) {
		
		int containerSize = container.length;
		int checkersSize = checkers.length;
		
		if(containerSize < checkersSize) {
			return false;
		}
		
		boolean found = false;
		boolean recurResult = false;
		for (int i = 0; i < containerSize && !found; i++) {
			if(container[i] == checkers[position]) {
				found = true;
			}
		}
		
		position++;
		if(position < checkersSize) {
			recurResult = contain(container, checkers, position);
		}
		else {
			recurResult = true;
		}
		return (found && recurResult);
	}
	
	private static void parseInputParams(String[] params) {
		if(params != null) {
			for (int i = 0; i < params.length; i++) {
				String param = params[i].trim();
						if(param.equals("-in"))		{ setNamesByInput(params[i + 1]);		}
				else	if(param.equals("-out"))	{ setOutputNameByInput(params[i + 1]);	}
				
				else	if(param.equals("-eula"))	{ options.showEULA = true;				}
				else	if(param.equals("-help"))	{ options.pritHelp = true;				}
				else	if(param.equals("-shs"))	{ options.saveHalfSorted = true;		}
				else	if(param.equals("-sss"))	{ options.sss = true;					}
				else	if(param.equals("-sas"))	{ options.sas = true;					}
				else	if(param.equals("-pl"))		{ setLangByInput(param);				}
			}
		}
	}
	
	private static void setNamesByInput(String path) {
		File inFile = new File(path);
		options.jarPath = Utils.getJarPath();
		options.inPath = inFile.getParent();
		String fullFileName = inFile.getName();
		options.inName = Utils.removeExtension(fullFileName);
		options.inExtension = Utils.getExtension(fullFileName);
	}
	
	private static void setOutputNameByInput(String path) {
		if(!path.contains("/") && !path.contains("\\")) {
			path = new String(main.options.jarPath + File.separatorChar + path);
		}
		
		if(path.startsWith("./") || path.startsWith(".\\")) {
			path = new String(main.options.jarPath + path.substring(1));
		}
		
		if(path.endsWith("/") || path.endsWith("\\")) {
			path = new String(path + options.inName + "_" + options.getCurrentLanguage() + "_result" + options.inExtension);
		}
		
		options.outPath = path;
	}
	
	private static void setLangByInput(String lang) {
		if(lang.equals("-pl")) {
			options.lang = 1;
		}
	}
	

	
	private static int processColumnNames(List<String> data, List<String> columnNames, int arrayPosition) {
		
		int md = arrayPosition;
		boolean end = false;
		for (; md < data.size() && !end; md++) {
			
			String line = data.get(md);
			
			line.replaceAll("\\s{2,}", " ");
			
			String[] lineParts = line.split(" ");
			for (int i = 0; i < lineParts.length && !end; i++) {
				String part = lineParts[i].trim();
				if (part.contains("]")) {
					end = true;
				}
				else if (!part.contains("[") && part.length() > 0) {
					columnNames.add(part);
				}
			}
		}
		
		if(options.sas || options.sss) { print("\nReturning Data Array index shift position after parsing Column Names: " + md + "\n"); }
		return md;
	}
	
	private static String print(String text) {
		System.out.println(text);
		return text;
	}
}
