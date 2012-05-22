package org.greenstone.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ScriptReadWrite {

    public ScriptReadWrite() {
    }

    public ArrayList<String> readInFile(File file) {
	try {
	    ArrayList<String> fileLines = new ArrayList<String>();
	    String oneLine = null;
	    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
	    while ((oneLine = bufferedReader.readLine()) != null) {
		fileLines.add(oneLine);
	    }
	    bufferedReader.close();
	    return fileLines;
	} catch (Exception e) {
	    System.err.println("exception:" + e);
	    return null;
	}
    }

    public void writeOutFile(File file, ArrayList<String> fileLines_in) {
	try {
	    PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
	    for (int i = 0; i < fileLines_in.size(); i++) {
		printWriter.println(fileLines_in.get(i));
	    }
	    printWriter.flush();
	    printWriter.close();
	} catch (Exception e) {
	    System.err.println("exception:" + e);
	}
    }

    public ArrayList<String> queryReplace(ArrayList<String> fileLines_ex, String param,
				  String newValue) {
	// only replace existing, don't append if param does not exist
	return replaceOrAddLine(fileLines_ex, param, newValue, false); 
    }
    
    // if the parameter exists, then a replace is performed, else the parameter-value
    // is appended
    public ArrayList<String> replaceOrAddLine(ArrayList<String> fileLines_ex, String param, String newValue, 
				      boolean replaceElseAdd) 
    {	
	String oneLine = null;
	String newLine = null;
	String name = null;
	// int oneLine_length = 0;
	param = param.trim();
	// int param_length = param.length();

	for (int i = 0; i < fileLines_ex.size(); i++) {
	    oneLine = fileLines_ex.get(i).trim();
	    // oneLine_length = oneLine.length();
	    StringTokenizer st = new StringTokenizer(oneLine, "=");
	    if (st.hasMoreTokens()) {
		name = st.nextToken();
		if (param.equals(name)) {
		    newLine = param + "=" + newValue;
		    fileLines_ex.set(i, newLine);
		    replaceElseAdd = false; // replaced, no longer need to add in any case
		    break;
		}
	    }
	}
	// If we've made no replacement and need to append the new item
	if(replaceElseAdd) {
	    fileLines_ex.add(param+"="+newValue);
	}
	return fileLines_ex;
    }

    public String existValue(ArrayList fileLines, String querytext) {
	String retValue = null;
	String oneline = null;
	String name = null;
	// String value=null;
	// int oneline_length = 0;
	querytext = querytext.trim();
	// int querytext_length = querytext.length();
	for (int i = 0; i < fileLines.size(); i++) {
	    oneline = ((String) fileLines.get(i)).trim();
	    // oneline_length = oneline.length();
	    StringTokenizer st = new StringTokenizer(oneline, "=");
	    if (st.hasMoreTokens()) {
		name = st.nextToken();
		if (querytext.equals(name)) {
		    if (st.hasMoreTokens()) {
			retValue = st.nextToken();
			retValue = retValue.trim();
			if (retValue.equals("")) {
			    retValue = null;
			}
		    } else {
			retValue = null;
		    }
		    break;
		}
	    }
	}
	return retValue;
    }
}
