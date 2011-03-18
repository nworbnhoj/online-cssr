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
public class HMMNode extends Node<Integer, Character> {
    
    private Set<MorphNode> morphNodes = new HashSet<MorphNode>();
    
    
    /**
     * Creates a new instance of HMMNode
     */
    public HMMNode(Integer ni) {
        super(ni);
    }
    
    @Override
    public void setId(Integer id){
        this.setId(id);
    }
    
    public MorphNode getRandomMorphNode(){
        MorphNode[] mna = new MorphNode[1];
        return (morphNodes.toArray(mna))[0];
    }
    
    public boolean add(MorphNode morphNode){
        return  morphNodes.add(morphNode);
    }
    
    public boolean remove(MorphNode morphNode){
        return  morphNodes.remove(morphNode);
    }
    
    public boolean isEmpty(){
        return morphNodes.isEmpty();
    }
    
    public Integer size(){
        return morphNodes.size();
    }
    
    public Set<MorphNode> getMembers(){
        Set<MorphNode> members = new HashSet<MorphNode>();
        members.addAll(morphNodes);
        return members;
    }
    
    public Set<String> getMemberIds(){
        Set<String> memberIds = new HashSet<String>();
        for(MorphNode morphNode : morphNodes){
            memberIds.add(morphNode.getId());
        }
        return memberIds;
    }
    
    
    public Morph getMorph(){
        Morph stateMorph = new Morph();
        Morph memberMorph = null;
        Integer tally = 0;
        
        for(MorphNode morphNode : morphNodes){
            memberMorph = morphNode.getMorph();
            for (Character ch : memberMorph.getVocab()){
                stateMorph.tally(ch, memberMorph.getTally(ch));
            }
        }
        return stateMorph;
    }
    
    
    
}
