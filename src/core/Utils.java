package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
	
    public static List<String> readAllLines(String filePath) {
    	
    	try {
			FileReader fr = null;
			BufferedReader br = null;
			
			File file = getFile(filePath);
			
			if (file != null) 
			{
			    List<String> list = new ArrayList<>();
			    try {
			    	fr = new FileReader(file);
			    	br = new BufferedReader(fr);
			    	
			        String line;
			        while ((line = br.readLine()) != null) {
			        	if (!line.startsWith("#") && line.length() > 0) {
			        		list.add(line);
			        	}
			        }
			    }
			    finally {
			    	if (br != null) { br.close(); }
			    	if (fr != null) { fr.close(); }
			    }
			    return list;
			}
		} catch (IOException e) { e.printStackTrace(); }
    	System.out.println("Error! Can't Read File: " + filePath);
        return null;
    }
    
    public static String numericName(int num, int max) {
    	int maxLength = Integer.toString(max).length();
    	String numString = Integer.toString(num);
    	int numLength = numString.length();
    	
    	int size = maxLength - numLength;
    	while(size >= 0) {
    		numString = "0" + numString;
    		size--;
    	}
    	
    	return numString;
    }
    
    public static File getFile(String filePath) {
    	boolean exist = false;
    	
    	File file = new File(filePath);
    	
    	// Path in Input file name not specified 
    	if(!file.exists()) {
    		if(filePath.startsWith("null")) {
        		filePath = new String(main.options.jarPath + filePath.substring(4));
        		//System.out.println("Chenged D Path: " + filePath);
        		file = new File(filePath);
    		}
    	}
    	
    	//  Local path to file in NOT work Directory
    	if(!file.exists()) {
    		if(filePath.startsWith("./") || filePath.startsWith(".\\")) {
        		filePath = new String(main.options.jarPath + filePath.substring(1));
        		//System.out.println("Chenged C Path: " + filePath);
        		file = new File(filePath);
    		}
    	}
    	
    	if(!file.exists()) {
    		filePath = filePath.replace(File.separator, "/");
    		//System.out.println("Chenged A Path: " + filePath);
    		file = new File(filePath);
    	}
    	else {
    		exist = true;
    	}
    	
    	if(!file.exists()) {
    		filePath = filePath.replace("/", File.separator);
    		//System.out.println("Changed B Path: " + filePath);
    		file = new File(filePath);
    	}
    	else {
    		exist = true;
    	}
    	
    	if(exist) {
    		main.options.finalInPath = filePath;
    		return file;
    	}
    	
    	return null;
    }
    
	public static int[] listIntToArray(List<Integer> list) {
		int list_size = list.size();
		int[] result = new int[list_size];
		for (int i = 0; i < list_size; i++) {
			result[i] = list.get(i);
		}
		return result;
	}
	
	
    
    public static void printIntArray(int[] array) {
    	int size = array.length;
    	System.out.print("[ ");
		for (int i = 0; i < size; i++) {
			System.out.print(array[i] + " ");
		}
		System.out.print("]");
    }
    
    public static String getJarPath() {
    	return new File(System.getProperty("java.class.path")).getParent();
    }
    
	public static String removeExtension(String file) {
	    if(file != null && file.length() > 0) {
	        while(file.contains(".")) {
	            file = file.substring(0, file.lastIndexOf('.'));
	        }
	    }
	    return file;
	}
	
	public static String getExtension(String name) {
	    if(name != null && name.length() > 0 && name.contains(".")) {
	    	name = name.substring(name.lastIndexOf('.'), name.length());
	    }
	    return name;
	}


}
