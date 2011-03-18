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


import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author nworbnhoj
 */
public class HMMGraf extends Graf<HMMNode, Integer, Character> {
    
    private Integer topId = -1;
    
    /** Creates a new instance of HMMGraf */
    public HMMGraf() {
        HMMNode root = this.newState();
        this.addLink(-1, null, root);    // the initial state in the HMM
    }

    
    public HMMNode newState(){
        HMMNode node = new HMMNode(this.getNextId());
        this.addLink(-1, null, node);
        return node;
    }
    
    private Integer getNextId(){
        topId++;
        return topId;
    }
    
    
    public Set<Character> reassign(MorphNode morphNode, Integer newStateId){
        Set<Character> missingTransitions = null;
        
        // remove the morphNode from the old state
        Integer oldStateId = morphNode.getStateId();
        if (this.containsId(oldStateId)){
            HMMNode oldStateNode = this.getNode(oldStateId);
            oldStateNode.remove(morphNode);
            
            // remove any surplus transitions
            Morph stateMorph = oldStateNode.getMorph();
            Set<Character> morphVocab = stateMorph.getVocab();
            Set<Character> surplusTransitions = oldStateNode.getLinkIds();
            surplusTransitions.removeAll(morphVocab);
            for(Character ch : surplusTransitions){
                oldStateNode.removeLink(ch);
            }
        }
        
        
        // add the morphNode to the new state.
        if (this.containsId(newStateId)){
            HMMNode newStateNode = this.getNode(newStateId);
            newStateNode.add(morphNode);
            
            // discover any new transitions to return
            Morph stateMorph = newStateNode.getMorph();
            Set<Character> transitionVocab = newStateNode.getLinkIds();
            missingTransitions = stateMorph.getVocab();
            missingTransitions.removeAll(transitionVocab);            
        } 
        
        return missingTransitions;
    }
    
    
 
}
