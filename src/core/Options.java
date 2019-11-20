package core;

public class Options {
		
		private String[] langArray = { "-en", "-pl" };
		
		int lang = 0;
		
		// Show some Stuff
		boolean sss = false;
		
		// Show all Sh!t
		boolean sas = false;
		
		boolean saveHalfSorted = false;
		
		boolean pritHelp = false;
		boolean showEULA = false;
		
		// Name stuff
		String jarPath;
		
		String inPath;
		String inName;
		String inExtension;
		
		String finalInPath;
		
		String outPath;
		
		public Options() { }
		
		public void printHelp() {
			System.out.println(
					"\nKeys:\n" +
					"  -in  <path>   Input File path. Core parameter. If not specified or empty, program will exit.\n" + // 
					"                Be careful with spaces in file path, if so cower path with \"\" .\n" + // cower with
					"                Examples:\n" +
					"                  -in ./file.txt\n" +
					"                  -in ./input/file.tab\n" +
					"                  -in /home/user/gtsarm_tool/file (Linux/Unix Systems Only)\n" +
					"                  -in \"C:\\WSIZ\\2 year\\SI\\labs\\ftp\\LAB01\\file.data\" (Windows Only)\n\n" +
					
					"  -out <path>   Output File path. If not specified or empty program will save ouput file like:\n" +
					"                 <path_to_original_file>/<original_file_name> + _result_ + <lang> + <original_file_extension>\n\n" +
					
					"  -eula         Printing in console End User License Agreement (EULA).\n" +
					"  -help         Printing in console this Help Information and exiting. No Tasks will be done.\n\n" +
					
					"  -pl           Will Set the default output file language to Polish (default language is English).\n" +
					"  -shs          Save half sorted results in output file.\n" +
					"  -sss          Shows useful information what program doing.\n" + // 
					"  -sas          Shows all Sh!t\n"
					);
		}
		
		public String getCurrentLanguage() {
			return langArray[lang].substring(1);
		}
	}

