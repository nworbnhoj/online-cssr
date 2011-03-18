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

import java.util.*;

import edu.uci.ics.jung.graph.impl.SparseTree;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.graph.impl.SimpleDirectedSparseVertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.utils.UserDataContainer.*;
import edu.uci.ics.jung.utils.*;
import HMM.*;



/**
 *
 * @author nworbnhoj
 */
public class JUNGtranslator {
    
      private static SparseTree  tree ;
    
    /** Creates a new instance of JUNGtranslator */
    public JUNGtranslator() {
    }
 
     
    public static SparseTree translateMorphTrie(MorphTrie morphTrie){
        rebuildTree("", null, morphTrie);
        return tree;
    }
    
    public static SparseGraph translateHMMGraf(HMMGraf hmmGraf){
        
        SparseGraph graph = new SparseGraph();
        
        // bridge temporarily records the relationship between stateId's and vertexes while the graph is constructed
        Map<Integer, SparseVertex> bridge = new HashMap<Integer, SparseVertex>();
        
        // set up the vertices first - populating the bridge as we go
        SparseVertex  toVertex = null, fromVertex = null;
        for(Integer stateId : hmmGraf.getNodeIds()){
            HMMNode state = hmmGraf.getNode(stateId);
            fromVertex = getHMMState(state, graph, bridge);
            
            // now setup the edges
            for (Character linkId : state.getLinkIds()){
                HMMNode destState = hmmGraf.getTarget(stateId, linkId);
                toVertex = getHMMState(destState, graph, bridge);
                
                DirectedSparseEdge edge = new DirectedSparseEdge(fromVertex, toVertex);
                edge.addUserDatum("probability", state.getMorph().getProb(linkId), new UserDataContainer.CopyAction.Clone());
                edge.addUserDatum("id", linkId.toString(), new UserDataContainer.CopyAction.Clone());
//                edge.addUserDatum("detail", detail, new UserDataContainer.CopyAction.Clone());
//                edge.addUserDatum("size", 12, new UserDataContainer.CopyAction.Clone());
                
                graph.addEdge(edge);
            }
        }
        return graph;
    }
    
    private static SparseVertex getHMMState(HMMNode state, SparseGraph graph, Map<Integer, SparseVertex> bridge){
        SparseVertex vertex = null;
        Integer stateId = state.getId();
        // if the state has already been created as a vertex
        if (bridge.containsKey(stateId)){
            vertex = bridge.get(stateId);
        } else {           // create a new vertex
            vertex = new SparseVertex();
            bridge.put(stateId, vertex);
            graph.addVertex(vertex);
            
            vertex.addUserDatum("stateId", stateId, new UserDataContainer.CopyAction.Clone());
            
            String suffixString = "";
            for (String suffix : state.getMemberIds()){
                suffixString += suffix + " ";
            }
            vertex.addUserDatum("suffixString", suffixString, new UserDataContainer.CopyAction.Clone());
//            vertex.addUserDatum("size", 12, new UserDataContainer.CopyAction.Clone());
        }
        return vertex;
    }
    
    public static void rebuildTree(String parentNodeId, SimpleDirectedSparseVertex parentVertex, MorphTrie morphTrie){
        
        if (parentNodeId == null ? "" == null : parentNodeId.equals("")){
            parentVertex = new SimpleDirectedSparseVertex();
            parentVertex = embelishVertex("", parentVertex, morphTrie);
            tree = new SparseTree(parentVertex);
        }
        
        MorphNode parentNode = morphTrie.getNode(parentNodeId);
        
        if (parentNode.hasLinks()){
            SimpleDirectedSparseVertex childVertex = null;
            String childNodeId = "";
            for (Character ch : parentNode.getLinkIds()){
                childNodeId = parentNode.getChildId(ch);
                
                //create a new vertex for the child
                childVertex = new SimpleDirectedSparseVertex();
                childVertex = embelishVertex(childNodeId, childVertex, morphTrie);
                tree.addVertex(childVertex);
                
                // create a new edge from the parent to the child vertice
                DirectedSparseEdge edge = new DirectedSparseEdge(parentVertex, childVertex);
                MorphNode fromNode = morphTrie.getNode(parentNodeId);
                Morph fromMorph = fromNode.getMorph();
                MorphNode toNode = morphTrie.getNode(childNodeId);
                Morph toMorph = toNode.getMorph();
                edge.addUserDatum("id", ch, new UserDataContainer.CopyAction.Clone());
                edge.addUserDatum("significance", cssrHMM.significance(fromMorph, toMorph, cssrHMM.getTestType()), new UserDataContainer.CopyAction.Clone());
                
                //      String probability = String.format("%1$5.3f", fromMorph.getProb(toNode.getId()));
                //      edge.addUserDatum("probability", probability, new UserDataContainer.CopyAction.Clone());
                tree.addEdge(edge);
                
                // recursively build the tree out from the child vertex
                rebuildTree(childNodeId, childVertex, morphTrie);        //recursive call
            }
        }
    }
    
    
    public static SimpleDirectedSparseVertex embelishVertex(String nodeId, SimpleDirectedSparseVertex vertex, MorphTrie morphTrie){
        MorphNode node = morphTrie.getNode(nodeId);
        Morph morph = node.getMorph();
        vertex.addUserDatum("morph", morph.toString(), new UserDataContainer.CopyAction.Clone());
        vertex.addUserDatum("history", node.getId(), new UserDataContainer.CopyAction.Clone());
        vertex.addUserDatum("size", morph.sampleSize(), new UserDataContainer.CopyAction.Clone());
        vertex.addUserDatum("stateId", node.getStateId(), new UserDataContainer.CopyAction.Clone());
        String morphStatus = "state";
        if (node.isIdle()) {
            morphStatus = "idle";
        }
        if (node.isSuffiX()) {
            morphStatus = "SuffiX";
        }
        if (node.isFamily()) {
            morphStatus = "family";
        }
        vertex.addUserDatum("morphStatus", morphStatus, new UserDataContainer.CopyAction.Clone());
        vertex.addUserDatum("sortby", node.getId(), new UserDataContainer.CopyAction.Clone());
        return vertex;
    }
    
// </editor-fold>
    
    
}
