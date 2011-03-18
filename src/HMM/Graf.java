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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/** Graf is a generic representation of a Graph with directed vertices.
 *
 * @author nworbnhoj
 */
public class Graf<N extends Node<NI, LI>, NI, LI> {
    
    
    private Map<NI, N> nodes;
    
    
    /** Creates a new instance of Graf */
    public Graf() {
        nodes = new HashMap<NI, N>();
    }
    
    
    public N getTarget(NI baseId, LI linkId){
        N baseNode = this.getNode(baseId);
        NI targetId = baseNode.getConnectedId(linkId);
        return this.getNode(targetId);
    }
    
    
    public boolean addLink(NI baseId, LI linkId, N targetNode){
        NI targetNodeId = targetNode.getId();
        
        // add the target  node if it does not already exist
        if (!nodes.containsKey(targetNodeId)){
            nodes.put(targetNodeId, targetNode);
        }
        
        // add the link from the base node
        if ((linkId != null) && nodes.containsKey(baseId)){
            N baseNode = this.getNode(baseId);
            baseNode.addLink(linkId, targetNodeId);
        }
        return true;
    }
    
    
    public N getNode(NI nid){
        if (nodes.containsKey(nid)){
            return nodes.get(nid);
        } else {
            return null;
        }
    }
    
    
    public void removeNode(Integer stateId){
        nodes.remove(stateId);
    }
    
    
    public boolean containsId(NI nid){
        return nodes.containsKey(nid);
    }
    
    
    public boolean isEmpty(){
        return nodes.isEmpty();
    }
    
    
    public int size(){
        return nodes.size();
    }
    
    
    public Set<NI> getNodeIds(){
        return nodes.keySet();
    }
    
}
