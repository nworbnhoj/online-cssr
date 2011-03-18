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

package HMMExplorer;

import HMM.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Vector;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


/**
 *
 * @author nworbnhoj
 */
public class MorphJTrie extends MorphTrie implements TreeModel {
    
    private List<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();
    
    /** Creates a new instance of MorphJTrie */
    public MorphJTrie() {
    }
    
    
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.add(l);
    }
    
    public Object getChild(Object parent, int index) {
        MorphNode parentMorphNode = (MorphNode) parent;
        List<Character> linkIds = new ArrayList<Character>();
        linkIds.addAll(parentMorphNode.getLinkIds());
        Collections.sort(linkIds);
        Character linkId = linkIds.get(index);
        String childMorphNodeId = parentMorphNode.getChildId(linkId);
        return this.getNode(childMorphNodeId);
    }
    
    public int getChildCount(Object parent) {
        MorphNode node = (MorphNode) parent;
        return node.getChildren().size();
    }
    
    
    public int getIndexOfChild(Object parent, Object child) {
        if ((parent == null) || (child == null)) return -1;
        
        MorphNode parentMorphNode = (MorphNode) parent;
        MorphNode childMorphNode = (MorphNode) child;
        List<Character> linkIds = new ArrayList<Character>();
        linkIds.addAll(parentMorphNode.getLinkIds());
        Collections.sort(linkIds);
        Integer index = 0;
        MorphNode siblingMorphNode;
        String siblingMorphNodeId = "";
        for (Character linkId : linkIds){
            siblingMorphNodeId = parentMorphNode.getChildId(linkId);
            siblingMorphNode = this.getNode(siblingMorphNodeId);
            if (siblingMorphNode == childMorphNode){
                return index;
            } else {
                index++;
            }
        }
        return -1;
    }
    
    
    public Object getRoot() {
        return this.getNode("");
    }
    
    
    public boolean isLeaf(Object node) {
        MorphNode morphNode = (MorphNode) node;
        if (morphNode.getChildren().size() == 0) return true;
        else return false;
    }
    
    
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.remove(l);
    }
    
    
    public void valueForPathChanged(TreePath path, Object newValue) {
        System.out.println("*** valueForPathChanged : "
                           + path + " --> " + newValue);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
