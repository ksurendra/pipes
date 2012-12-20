/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.pipes.JSON;

import com.google.gson.JsonObject;
import com.tinkerpop.pipes.AbstractPipe;
import edu.mayo.pipes.history.History;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *  Delim2JSONPipe takes in it's constructor a String[] describing each of the columns
 * in a delimited file (i.e. the metadata) and it converts these columns to JSON,
 * finally, it tacks the JSON on the end of the input string and sends it along.
 * for now, it does not do any complex parsing of the input data inside the columns, 
 * but this could be added.  If and when they are added, the plan would be to do an 
 * encoding on the strings like:
 * [value1:pipe, value2:comma, value3:semicolon:equal]
 * This would mean value1 has an array of values that is pipe delimited,
 * value2 has an array of values that is comma delimited,
 * and value3 is a hash that is semicolon delimited with an equal sign separating the values.
 * 
 * @Input = a parsed list of strings (not tab separated, you may want to do that first with SplitPipe)
 * @Output - the same set of strings, with the JSON equivalent in the last column.
 * 
 * @author m102417
 */
public class Delim2JSONPipe extends AbstractPipe<History, History>{
    String[] meta = null;
    //delimiters...not in use yet
    public static final String comma = "comma";
    public static final String pipe = "pipe";
    public static final String semicolon = "semicolon";
    public static final String colon = "colon";
    public static final String equal = "equal";

    
    public Delim2JSONPipe(String[] encoding, String delim){
        getMeta(encoding);
    }
    
    private String[] getMeta(String[] e){
        meta = new String[e.length];
        for(int i=0; i<e.length; i++){
            String[] split = e[i].split(":");
            meta[i] = split[0];
        }
        return meta;
    }
    
    
    @Override
    protected History processNextStart() throws NoSuchElementException {
        History history = this.starts.next();
        
        history.add(computeJSON(history));
        return  history;
    }
    
    private String computeJSON(List<String> c){
        JsonObject f = new JsonObject();
        for(int i=0; i<c.size(); i++){
            String s = c.get(i);
            if(canInt(s)){
                f.addProperty(meta[i], toInt(s));
            }else if(canDouble(s)){
                f.addProperty(meta[i], toDouble(s));
            }else {
                f.addProperty(meta[i], s);
            }
        }
        return f.toString();
    }
    
    private boolean canInt(String s){
        try{
            Integer.parseInt(s);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }
    
    private int toInt(String s){
        int p = Integer.parseInt(s);
        return p;
    }
    
    private boolean canDouble(String s){
        try{
            Double.parseDouble(s);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }
    
    private double toDouble(String s){
        double p = Double.parseDouble(s);
        return p;
    }
    
    
}