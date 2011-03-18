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
import java.util.HashSet;
        


/**
 *
 * @author nworbnhoj
 */


    // NI is a generic for the NodeId and LI is a generic for the LinkId.
    public class Node<NI, LI> {
    
    NI nodeId;
    Map<LI, NI> links;
    
    
    /**
     * Creates a new instance of Node
     */
    public Node(NI ni) {
        nodeId = ni;
        links = new HashMap<LI, NI>();
   }
 
    
    
    // <editor-fold defaultstate="collapsed" desc=" id methods ">
    
    public NI getId(){
        return nodeId;
    }
    
    public void setId(NI ni){
        nodeId = ni;    }
    
    // </editor-fold>
    
    
    // <editor-fold defaultstate="collapsed" desc=" Link methods ">
    
    public boolean addLink(LI lid, NI nid){
        if (!links.containsKey(lid)){
            links.put(lid, nid);
            return true;
        } else {
           return false;
        }
    }
    
    
    public NI getLink(LI lid){
        return links.get(lid);
    }
    
    
    public Set<NI> getLinkedNodeIds(){
        Set<NI> linkIds = new HashSet<NI>();
        linkIds.addAll(links.values());
        return linkIds;
    }
    
    
    public Set<LI> getLinkIds(){
        Set<LI> linkIds = new HashSet<LI>();
        linkIds.addAll(links.keySet());
        return linkIds;
    }
    
    
    public boolean hasLinks(){
        return (!links.isEmpty());
    }
         
    
    public boolean removeLink(LI linkLabel){
        if (links.containsKey(linkLabel)){
            links.remove(linkLabel);
            return true;
        } else {
            return false;
        }
    }
    
    
    public int removeLinks(){
        int count = links.size();
        links.clear();
        return count;
    }
   
    
    public NI getConnectedId(LI li){
        return links.get(li);
    }

    
// </editor-fold>
    
    }