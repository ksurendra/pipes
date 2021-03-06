package edu.mayo.pipes.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.WordUtils;

public class StringUtils {

	/**
	 * Alternative to the java.lang.String.split() method.  This implementation
	 * will account for missing data between delimiters, which are empty Strings.
	 * 
	 * @param s         The string to be split.
	 * @param delimiter The delimiter to be used for splitting the given string.
	 * @return A string array containing the fields from the original string.
	 */
	public static String[] safeSplit(String s, String delimiter) {
		List<String> list = new ArrayList<String>();

		int fromIdx = 0;
		int delimiterIdx = 0;
		
		do {
			// get index of next delimiter
			delimiterIdx = s.indexOf(delimiter, fromIdx);
			
			// add the next token
			if (delimiterIdx != -1) {
				list.add(s.substring(fromIdx, delimiterIdx));
			} else {
				// last token
				list.add(s.substring(fromIdx));				
			}
			
			// update the from index so that the String.indexOf search
			// ignores the token that was just added
			fromIdx = delimiterIdx + 1;
			
		} while (delimiterIdx != -1);
		
		return list.toArray(new String[0]);
	}
	
	/** Split a string by delimiter and return a list of the split strings.  
	 *  Useful for handling ##BioR lines with commas and quotes and escaped quotes.
	 *  Handles quotes, escaped quotes, delimiters within quotes.
	 *  All strings are trimmed to remove extra non-display characters to the left and right.
	 *  Ex: 
	 *    A,B,C   --> [A, B, C] (size=3)
	 *    "A,B,C" --> ["A,B,C"] (size=1)
	 *    A;B,"C" --> [A, B, "C"] (size=3)
	 *    A,"a , string, with commas;" --> [A, "a , string, with commas;"] (size=2)
	 * @param strToSplit
	 * @return
	 */
	public static List<String> split(String strToSplit, List<String> delimiters) {
		List<String> strList = new ArrayList<String>();
		if( strToSplit == null || strToSplit.length() == 0 )
			return strList;
		StringBuilder str = new StringBuilder();
		
		boolean isMidQuote = false;
		for(int i=0; i < strToSplit.length(); i++) {
			char chr = strToSplit.charAt(i);
			// If quote
			if( chr == '\"' ) {
				// if last chr was backslash, then add this quote to str
				if( i > 0 && strToSplit.charAt(i-1) == '\\' ) {
					str.append(chr);
				// if first quote, then add, and now we are mid-quote
				} else if( ! isMidQuote ) { 
					str.append(chr);
					isMidQuote = true;
				// If midQuote and we encounter a quote, then this is the end quote, so:
				//   add to string,
				//   and add string to list,
				//   and reset string
				//   and no longer midquote
				} else if( isMidQuote ) {
					str.append(chr);
					isMidQuote = false;
				}
			// If any other character, but mid quote, then just add it
			} else if( isMidQuote ) {
				str.append(chr);
			// Else, if one of the delimiters, then:
			//   Do NOT add chr to str
			//   Add the string to list
			//   Reset str
			} else if( delimiters.contains(chr+"") ){
				addToListAndClear(str, strList);
			// Else add to str
			} else {
				str.append(chr);
			}
		}
		// If there are any characters left in str, then add the string to the list
		addToListAndClear(str, strList);
		
		return strList;
	}

	/** Used by split():
	 * Trim the strB, but only add it to the list if its length is > 0 */
	private static void addToListAndClear(StringBuilder strB, List<String> strList) {
		String s = strB.toString().trim();
		if( s.length() > 0 )
			strList.add(s);
		strB.delete(0, strB.length());
	}
	
	
	
	
	/**
	 * Wraps the given string to the specified width.  Lines that start with the
	 * tag <NOWRAP> will not be wrapped.
	 * 
	 * @param str 
	 * 			the string to wrap
	 * @param max_width 
	 * 			the maximum width
	 * @return the wrapped string
	 * @throws IOException
	 */
	public static String wrap(String str, int max_width) throws IOException {
		StringWriter sWtr = new StringWriter();
		PrintWriter pWtr = new PrintWriter(sWtr);
		
		StringReader sRdr = new StringReader(str);
		BufferedReader bRdr = new BufferedReader(sRdr);
		String line = bRdr.readLine();
		while (line != null) {
			if (line.trim().startsWith("<NOWRAP>")) {
				line = line.substring("<NOWRAP>".length());
			} else {
				line = WordUtils.wrap(line, max_width);				
			}
			pWtr.println(line);
			
			line = bRdr.readLine();
		}		
		
		sWtr.close();		
		String wrappedStr = sWtr.toString();

		// chomp trailing line feed
		if (wrappedStr.length() > 0) {
			wrappedStr = wrappedStr.substring(0, wrappedStr.length() - 1);
		}
		
		return wrappedStr;		
	}
	
	/**
	 * Does a "block" indent on the given string such that each line in the string
	 * is indented over the specified number of indents.
	 *  
	 * @param str
	 * 			the string to be indented
	 * @param numIndents
	 * 			number of indents to use
	 * @return the indented string 		
	 * @throws IOException
	 */
	public static String indent(String str, int numIndents) throws IOException {

		String indent = "";
		for (int i=0; i < numIndents; i++) {
			indent += "\t";
		}
		
		StringWriter sWtr = new StringWriter();
		PrintWriter pWtr = new PrintWriter(sWtr);
		
		StringReader sRdr = new StringReader(str);
		BufferedReader bRdr = new BufferedReader(sRdr);
		String line = bRdr.readLine();
		while (line != null) {
			
			pWtr.print(indent);
			pWtr.println(line);
			
			line = bRdr.readLine();
		}		
		
		sWtr.close();		
		String indentedStr = sWtr.toString();

		// chomp trailing line feed
		if (indentedStr.length() > 0) {
			indentedStr = indentedStr.substring(0, indentedStr.length() - 1);
		}
		
		return indentedStr;
	}

	/** Strips any outside quotes if there are any, else just returns the original string */
    public static String stripOutsideQuotes(String s) {
    	boolean isQuoted = s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"");
    	return  isQuoted  ?  s.substring(1,s.length()-1)  :  s;
    }

}