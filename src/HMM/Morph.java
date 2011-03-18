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

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;

/**
 * A Morph is a conditional distribution of the next observable given a history.
 *
 *
 * @author nworbnhoj
 */
public class Morph {
    
    
    
     /** The Morph does not store probabilities but frequency counts
     * which can be used to calculate the probability  */
    private HashMap<Character, Integer> tallies = new HashMap<Character, Integer>();

    
    /**
     * Creates a new instance of Morph
     */
    public Morph() {
    }
    
    
    
    /**
     * this method alters the tally for obs and updates the Morph
     */
    public int tally(char ch, int tally){
       if (tallies.containsKey(ch)){
            tally += tallies.get(ch);
        }
        tallies.put(ch, tally);
        return tally;
    }
  
    
    /**
     * returns the total of all of the tallies in the Morph
     */
    public int sampleSize(){
        int tally = 0;
        for(Character ch : tallies.keySet()){
            tally += tallies.get(ch);
        }
        return tally;
    }
    
    
    public double getProb(char ch){
        if (tallies.containsKey(ch)){
            Integer tally = tallies.get(ch);
            Integer size = this.sampleSize();
            if (size == 0){
                return 0;
            } else {
                return ((double)tally/size);
            }
        } else return 0;
    }
    
      
    public Set<Character> getVocab(){
        Set<Character> vocab = new HashSet<Character>();
        vocab.addAll(tallies.keySet());
        return vocab;
    }
    
    
    public int vocabSize(){
        return tallies.size();
    }
    
    
    public Iterator iteratorVocab(){
        return tallies.keySet().iterator();
    }
    
    
    public int getTally(char ch){
        if (tallies.containsKey(ch)){
            return tallies.get(ch);
        } else return 0;
    }
   
    
    public String toString(){
        String aString = "";
        int ss = sampleSize();
        for (Character ch : tallies.keySet()){
            aString += String.format("%1$s(%2$.3f) ", ch, (double)tallies.get(ch)/ss);
        }
        return aString;
    }
    
    private class SetSizeComparator {
        public int compare(Set s1, Set s2) {
            return s2.size() - s1.size();
        }
    }
    
    
    
    
    
    
}
