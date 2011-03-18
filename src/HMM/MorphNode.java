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


/**
 *
 * @author nworbnhoj
 */
public class MorphNode extends Node<String, Character> {
    
    
    private Morph morph = new Morph();
    
    private enum NodeStatus {family, SuffiX, idle};
    
    private NodeStatus nodeStatus;
    
    private Integer stateId = -1;
    
    
    
    /**
     * Creates a new instance of MorphNode
     */
    public MorphNode(String str) {
        super(str);
        nodeStatus = NodeStatus.family;
    }
    
    
    
    // <editor-fold defaultstate="collapsed" desc=" Morph Status and State handling methods ">
    
    public boolean isFamily(){
        if (nodeStatus == NodeStatus.family) return true;
        else return false;
    }
    
    
    public void setIdle(){
        nodeStatus = NodeStatus.idle;
    }
    
    
    public boolean isIdle(){
        if (nodeStatus == NodeStatus.idle) return true;
        else return false;
    }
    
    
    public void setSuffiX(){
        nodeStatus = NodeStatus.SuffiX;
    }
    
    
    public boolean isSuffiX(){
        if (nodeStatus == NodeStatus.SuffiX) {
            return true;
        } else return false;
    }
    
    
    public Integer setStateId(Integer sid){
        Integer exStateId = stateId;
        stateId = sid;
        return exStateId;
    }
    
    
    public Integer getStateId(){
        return this.stateId;
    }
    
    // </editor-fold>
    
    
    
    public Morph getMorph(){
        return morph;
    }
    
    
    public String getParentId(){
        String myId = this.getId();
        if (myId == ""){
            return null;
        } else {
            return myId.substring(1);
        }
    }
    
    
    public Set<String> getChildren(){
        return getLinkedNodeIds();
    }
    
    
    public String getChildId(Character ch){
        return this.getLink(ch);
    }
    
    
    public String toString(){
        String aString = "";
        switch (nodeStatus){
            case family: {
                aString = "family node: ";
                aString += " morph=" + morph.toString();
            }  break;
            case SuffiX: {
                aString = "SuffiX node: state=" + this.stateId;
                aString += " morph=" + morph.toString();
            }  break;
            case idle: aString = "idle node"; break;
        }
        
        return aString;
    }
    
    
}

