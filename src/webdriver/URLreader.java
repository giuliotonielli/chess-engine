package webdriver;

import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import main.Move;

public class URLreader {
	public String URL;
	public int[] indexes;
	public String doc;
	
	public URLreader(String URL) {
		this.URL = URL;
		this.doc = getDoc();
		this.indexes = new int[3];
	}
	
	public String[] getMoves(int moveNumber) {
		String[] output = new String[3];
		String toSearch;
		int adder;
		
		if (moveNumber % 2 == 0) {
			toSearch = moveNumber/2 + "... ";
			adder = 5;
		}
		else {
			toSearch = ((int)moveNumber/2+1) + ". ";
			adder = 3;
		}
		
        int index = doc.indexOf(toSearch);
        int i = 0, j;
        
        while (i < 3) {
        	if (index == -1)
        		return new String[1];
        	
        	for (j = adder; j < 15; j++) {
				if (doc.charAt(index+j) == '<') 
	        		break;
			}
        	
        	output[i] = doc.substring(index+adder, index+j);
        	this.indexes[i] = index;
        	
            index = doc.indexOf(toSearch, index + 1);
            i++;
        }
        
		return output;
	}

	public String nextGivenIndex(int randomNum) {
		String doc = this.doc;
		int index = this.indexes[randomNum] - 2;
		int i = 0;
		
		while (!Character.toString(doc.charAt(index - i)).equals("/")) 
			i++;
		
		return "https://www.365chess.com" + doc.substring(index-i, index).replace("amp;","");
	}
	
	private String getDoc() {
		Document document = null;
		
		try {document = Jsoup.connect(this.URL).get();}
        catch (Exception ex) {return null;}
		
		return String.valueOf(document.select("div.wide_sidebar"));
	}

	
	public String getNextUrlGivenPlayersMove(Move m, HashMap<Character, Integer> charToInt, int moveNumber) {
		String doc = getDoc();
		String[] moves = new String[5];
		int indexes[] = new int[5], adder, added = 0;
		String toSearch;
		
		if (doc == null)
			return null;
		
		if (doc.length() == 0)
			return null;
		
		if (moveNumber % 2 == 0) {
			toSearch = moveNumber/2 + "...";
			adder = 5;
		}
		else {
			toSearch = ((int)moveNumber/2 + 1) + ". ";
			adder = 3;
		}
		
		int index = doc.indexOf(toSearch);
	    int i = 0, j;
	    char x;
	    
	    
		while (i < 20) {
			for (j = adder; j < 15; j++) {
				if (doc.charAt(index+j) == '<') 
	        		break;
			}
			
			while (charToInt.get(doc.charAt(index+j-2)) == null)
				index--;
			
        	if (8-Character.getNumericValue(doc.charAt(index+j-1)) == m.d & charToInt.get(doc.charAt(index+j-2)) == m.c) {

        		switch (Math.abs((int)m.value)) {
        		case 1: x = 'P'; break;
        		case 3: x = 'N'; break;
        		case 5: x = 'R'; break;
        		case 9: x = 'Q'; break;
        		case 100: x = 'K'; break;
        		default: x = 'B'; break;
        		}
        			
        		if (x == 'P' || doc.charAt(index+adder) == x) {
        			moves[added] = doc.substring(index+adder, index+j);
        			indexes[added] = index;
        			added++;
        		}
       		}
        	
            index = doc.indexOf(toSearch, index + 1);
            i++;
        }
		
		if (added == 1) {
			index = indexes[0];
			i = 0;
			
			while (!Character.toString(doc.charAt(index - i)).equals("/")) 
				i++;
			
			return "https://www.365chess.com" + doc.substring(index-i, index-2).replace("amp;","");
    	}
		
		else if (added > 1) {
			
		}
		
		else {
			
		}
		
		return "";
	}
}
