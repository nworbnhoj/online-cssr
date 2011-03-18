/*
 * Copyright 2007, 2011 John A Brown
 * www.nhoj.info           nworbnhoj
 *
 * This file is part of oCSSR.
 *
 * oCSSR is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * oCSSR is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * oCSSR.  If not, see <http://www.gnu.org/licenses/>.
 */

package HMM;

import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import java.io.*;


/**
 *
 * @author nworbnhoj
 */
public class HMM  {
    
    private BufferedReader inStream;
    private BufferedWriter outStream;
    private int numberObservations;
    
    private DirectedSparseGraph model;
    
    public class HMMControl {
        
    }
    
    
    /** Creates a new instance of HMM */
    public HMM(Reader in, Writer out) {
        super();
        setInput(in);
        setOutput(out);
        numberObservations = 0;     
        model = new DirectedSparseGraph();
    }
    
    
    /** Sets up an input Stream
     */
    public void setInput(Reader reader) {
        if (reader != null){
            inStream = new BufferedReader(reader);
        }
    }
    
    
    /** Sets up an output Stream
     */
    public void setOutput(Writer writer) {
        if (writer != null){
            outStream = new BufferedWriter(writer);
        }
    }
    
    
    public boolean isMoreObs(){
        
        boolean ready = false;
        try {
            ready = inStream.ready();
        } catch (java.io.IOException e) {
            System.err.println("Caught IOException: " +  e.getMessage());
        }
        return ready;
    }
    
    public Integer getCount (){
        return numberObservations;
    }
    
    
    public char getObs(){
        char obs = '\uffff';
        try {
            obs = (char)inStream.read();
            numberObservations++;
        } catch (java.io.IOException e) {
            System.err.println("Caught IOException: " +  e.getMessage());
        }
        return obs;
    }
    
    
    
    
    
    
    
}
