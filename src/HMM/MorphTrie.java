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
import java.util.Set;
import java.util.HashSet;


/**
 *
 * @author nworbnhoj
 */
public class MorphTrie extends Graf<MorphNode, String, Character> {
    
    
    /** Creates a new instance of MorphTrie */
    public MorphTrie() {
        MorphNode root = new MorphNode("");
        root.setSuffiX();
        this.addLink("", null, root);    // the root of the morph-trie
    }
  
    
    public MorphNode getChild(MorphNode parent, Character linkId){
        String parentId = parent.getId();
        MorphNode node = this.getTarget(parentId, linkId);
        return node;
    }
    
    
    public MorphNode mapHistory(String history){
        Integer index = history.length();
        
        boolean mapped = false;
        char ch;
        //start at the root
        MorphNode node = this.getNode("");
        do {
            if (node.isSuffiX()) {
                mapped = true;
            } else {
                ch = history.charAt(index - 1);
                node = this.getNode(node.getChildId(ch));                
            }
            index--;
        } while (!mapped && (index >= 0) && (node != null));
        
        if (mapped){
            return node;
        } else {
            return null;
        }
    }
    
    
    /** When a group of sibling is being split up into different states then
     * at a minimum they must be split as the destination group of siblings is
     * split. This method returns that pattern of mappings (a template) as a
     * list of sets of transitions */
    public HashMap<Integer, Set<Character>> getStateTemplate(MorphNode parentNode){
        HashMap<Integer, Set<Character>> template = new HashMap<Integer, Set<Character>>();
        Integer childStateId = 0;
        for (Character ch : parentNode.getLinkIds()){
            childStateId =  this.getChild(parentNode, ch).getStateId();
            if (!template.containsKey(childStateId)){
                template.put(childStateId, new HashSet<Character>());
            }
            template.get(childStateId).add(ch);
        }
        return template;
    }
 
    
}
