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

import java.util.*;
import java.util.Set.*;
import java.util.HashSet;
import java.io.*;




/**
 * cssrHMM uses a contonuous version of CSSR to for a Hidden Markov Model.
 * The process differs from the three phases publishes by Shalizi
 * 1. Morphs: the frequency counts are tallied for each history of Lmax in the raw data. O(N). Phase I
 * 2. Homogenity: the histories are sorted and partitioned into contiguous Suffix Groups with homogenous morphs. O(kL.log(kL)). Phase II
 * 3. Suffixes: the morph is caculated for each Suffix Group. O(kL). Phase II.
 * 4. Determinist: the subsequent Suffix Group is established for each Suffix Group. O(kl). Phase III.
 * 5. States: Suffix Groups which are both homogenous and deterministic are combined and assigned states. O(?). Phase II.
 *
 * @author nworbnhoj
 */
public class cssrHMM extends HMM {
    
    
    // <editor-fold defaultstate="collapsed" desc=" Variable declarations ">
    private HMMGraf hmmGraf = new HMMGraf();
    private MorphTrie morphTrie = new MorphTrie();
    
    private static List<Character> alphabet = new ArrayList();  // all of the characters observed from the data source
    private int Lmax = 4;                                       // maximum history Length
    private Character nextObservation = '\u0000';               // holds the next observation
    private String recentHistory = "";                          // holds the previous Lmax observations
    
    enum TestType {KS, CHIS};                                       // the significance test algorythms available
    private static TestType testtype = TestType.CHIS;           // the selected significance test algorythm
    private double sigTest = 0.01;                                 // the significance set fr the null hypothesis
    
    // this holds a record of HMM states which have been disrupted and the transitions than need to be remapped.
    private  Map<Integer, Set<Character>> unmappedHMMTransitions = new HashMap<Integer, Set<Character>>();
    
    // </editor-fold>
    
    
    // <editor-fold defaultstate="collapsed" desc=" Initialisation methods ">
    
    /** Creates a new instance of cssrHMM.
     * The default significance test type of CHIS is set with significance = 0.001.
     * The morph trie is initialised with a root node.
     * The first observation is read from the data source.
     */
    public cssrHMM(Reader in, Writer out) {
        super(in,out);
        
        // Initialise cssrHMM class variables
        // recentHistory = new History(1);
        testtype = TestType.CHIS;
        sigTest = 0.001;
        
        // Initialise the root of the MorphTrie into the initial State of the HMM
        Integer initialStateId = 0;
        String rootMorphId = "";
        HMMNode initialState = hmmGraf.getNode(initialStateId);
        MorphNode rootMorphNode = morphTrie.getNode(rootMorphId);
        this.setState(rootMorphNode, initialStateId);
        
        //Initialise the first observation and recentHistory
        this.makeNextObservation();
        recentHistory = "";
    }
    
    /** Sets the longest history considered Lmax. The value set is
     * returned.
     * @param l the value of Lmax (1 < Lmax < 1000)
     */
    public Integer setLmax(Integer l){
        if ((l>0) && (l<1000)){
            Lmax = l;
        }
        return Lmax;
    }
    
    /** Sets the value of the significance to be used in the significance test.
     * Returns the value of the significance set.
     * @param s significance (0 < s < 1)
     */
    public Double setSignificance(Double s){
        if ((s>0) && (s<1)){
            sigTest = s;
        }
        return sigTest;
    }
    
    // </editor-fold>
    
    
    // <editor-fold defaultstate="collapsed" desc=" Data sequence processing methods ">
    
    /**
     * CSSR reads a number of characters from the data stream, updates the
     * morph trie and if a non-homogeneous morphNode is identified then updates
     * the HMM.
     *
     * @param steps determines how many characters are read from the data source
     * and processed. If steps == 0 then CSSR continues to read characters until
     * either a change is made to the HMM or the end of the data source is reached.
     */
    public void CSSR(Integer steps){
        char nextObs = makeNextObservation();
        
        boolean done = false, untilSplit = false;
        if (steps == 0) untilSplit = true;
        
        while ((nextObs != '\uffff') && !done){
            steps--;
            
            MorphNode problemChildNode = updateMorphs(nextObs);
            
            // if there were any problemChilds that are not homogeneous with their parents
            if (problemChildNode != null){
                if (problemChildNode.isFamily()){
                    isolateChild(problemChildNode);
                } else if (problemChildNode.isSuffiX()){
                    isolateSuffiX(problemChildNode);
                }
                this.mapHMMTransitions();
                if (untilSplit) done = true;
            }
            
            if (steps == 0) done = true;
            
            if (!done) nextObs = makeNextObservation();
        }
    }
    
    
    /** Reads one character from the data source, adds the previous character read
     * to the end of recent history and returns the new character.
     */
    private char makeNextObservation(){
        
        // add the new observation to recent history and trim to length Lmax
        recentHistory = recentHistory + nextObservation.toString();
        if (recentHistory.length() > Lmax){
            recentHistory = recentHistory.substring(1);
        }
        
        // update the alphabet to include all characters seen in the data stream
        nextObservation = getObs();
        if (!alphabet.contains(nextObservation)){
            alphabet.add(nextObservation);
        }
        
        return nextObservation;
    }
    
    /** Returns the character most recently read from the data source.
     */
    public Character getNextObservation(){
        return nextObservation;
    }
    
    
    /** Returns the most recent history up to maxHistoryLength
     */
    public String getRecentHistory(){
        return recentHistory;
    }
    
    /** updateMorphs takes a single character representing the next opbservation and
     * updates the morphs in the morphTrie.  This involves starting at the root of
     * the morphTrie and traversing the path represented by the recent history.  At every
     * morphNode the tally for the next Observation is incremented and the homogeniety
     * is checked against the parent (family nodes) or State (SuffiX nodes). updateMorphs
     * returns a non-homogeneous morphNode from the deepest point in the morph trie
     * if there are one or more (or null if there are none).
     * @param nextObs The next observation from the data source to be used to update the morphs.
     */
    private MorphNode updateMorphs(Character nextObs){
        Morph childMorph = null, parentMorph = null;
        MorphNode parentNode = null, problemChildNode = null;
        MorphNode childNode = morphTrie.getNode("");
        Integer tally = 0;
        
        // the morph for every suffix of recentHistory needs to be updated
        Integer remainingHistory = recentHistory.length()+1;
        do {
            childMorph = childNode.getMorph();
            
            // tally the observation
            tally = childMorph.tally(nextObs, 1);
            
            // check that the child is homogeneous with the parent/state
            if (childNode.isSuffiX()){
                HMMNode baseState = hmmGraf.getNode(childNode.getStateId());
                if (tally == 1){    //this is a first observation so the transition must be added to the HMM
                    MorphNode mappedNode = morphTrie.mapHistory(childNode.getId() + (new Character(nextObs)).toString());
                    HMMNode nextState = hmmGraf.getNode(mappedNode.getStateId());
                    hmmGraf.addLink(baseState.getId(), nextObs, nextState);
                }
                if (!isHomogeneous(childMorph, baseState.getMorph())){
                    problemChildNode = childNode;
                }
            } else if (childNode.isFamily()) {
                if (!isHomogeneous(childMorph, parentNode.getMorph())){
                    problemChildNode = childNode;
                }
            }
            
            // move to the child for the next loop
            remainingHistory--;
            if (remainingHistory > 0){
                parentNode = childNode;
                Character nextChar = recentHistory.charAt(remainingHistory-1);
                childNode = morphTrie.getChild(parentNode, nextChar);
                if (childNode == null){
                    childNode = new MorphNode(nextChar.toString() + parentNode.getId());
                    morphTrie.addLink(parentNode.getId(), nextChar, childNode);
                }
            }
        } while (remainingHistory > 0);
        
        return problemChildNode;
    }
// </editor-fold>
    
    
    // <editor-fold defaultstate="collapsed" desc=" Determinism methods ">
    
    /** isolateSuffix ensures that the supplied SuffiX node is in a state
     * of its own and that the state from which is was extracted is homogeneous
     * @param node is the morphNode to be extracted from the composite State
     */
    private void isolateSuffiX(MorphNode node){
        if (node == null) return;
        if (!node.isSuffiX()) return;
        HMMNode state = hmmGraf.getNode(node.getStateId());
        if (state.size() > 1){  // place the SuffiX node in a new state and check the homogeniety of the disrupted state
            this.setState(node, null);
            MorphNode outcast = null;
            do{     // extracts any other non-homogeneous morphNodes from the state
                outcast = this.getOutcast(state);
                this.setState(outcast, null);
            } while (outcast != null);
            this.mapHMMTransitions();
        }
        return;
    }
    
    
    /**
     * isolateChild takes a childNode that has been recognised as being non-homogenous
     * with its parent node and makes the changes necessary to correct the
     * morph-trie and the HMM.
     * This is a recursive procedure which works its way down the morph-trie setting
     * parents to idle, siblings to SuffiX  and assigning the siblings to homogeneous
     * states.  At each step down the morph-trie a second recursive procedure is set
     * in motion which which works across and down the morph-trie setting parents to idle,
     * whole sibling groups to SuffiX and applying a state template from the previous
     * step to the SuffiX nodes. Each of these recursions stop when they have worked
     * their way down to a SuffiX node and begin to unwind.
     *
     * @param childNode is the morphNode which is not homogeneous with its parent to
     * set of the recusions.
     */
    private void isolateChild(MorphNode childNode){
        if (childNode == null) return;
        
        // if the recusion has worked its way down to a SuffiX node, isolate it, set it to idle and unwind the recursion
        if (childNode.isSuffiX()){
            this.isolateSuffiX(childNode);
            this.setState(childNode, -1);
        }
        
        MorphNode parentNode = morphTrie.getNode(childNode.getParentId());
        if (parentNode == null) return;
        
        // set the siblings to SuffiX and assign homogenous states then initiate the
        // recursion to apply state templates back along the transition line across and down.
        if (childNode.isFamily()){
            HashMap<Integer, Set<Character>> stateTemplate = null;
            stateTemplate = this.splitFamily(parentNode, childNode);
            this.propogateTemplate(parentNode, stateTemplate);
        }
        
        // Recursive call stepping one node down the morphTrie
        this.isolateChild(parentNode);
        
        // Set the parent to be idle after all is done as the recursion unwinds
        this.setState(parentNode, -1);
    }
    
        
    /** splitFamily takes a sibling group of family nodes and sets them all to be
     * Suffix nodes.  Each SuffiX node is assigned to one of at least two homogenous
     * states.
     *
     * @param parentNode is the parent node if the siblings to be split in the morph-trie
     * @param childNode is the morphNode which has been identified to be non-homogeneous
     * with its parent.
     * @return A template of the states assigned to the siblings is returned for use in 
     * the propgateTransitions method. The template is a Map of StateId's to a set of Characters 
     * representing the link out from the parent to the siblings.
     */
    private HashMap<Integer, Set<Character>> splitFamily(MorphNode parentNode, MorphNode childNode){
        if ((childNode == null) || (parentNode == null)) return null;
        
        // set-up some variables etc required below
        MorphNode siblingNode = null;
        Morph siblingMorph = null;
        HMMNode aState = null;
        Set<Integer> siblingStates = new HashSet<Integer>();
        Morph parentMorph = parentNode.getMorph();
        HashMap<Integer, Set<Character>> stateTemplate = new HashMap<Integer, Set<Character>>();
        
        // iterate thru each of the siblings to set them as SuffiX (if not already idle) and
        // assign each to a homogeneous state.
        for (String siblingNodeId : parentNode.getChildren()){
            siblingNode = morphTrie.getNode(siblingNodeId);
            // if this is the non-homogenous child Node then ensure that it is assigned a different state.
            if ((siblingNode == childNode) && (!childNode.isIdle())){
                this.setState(childNode, null);
                siblingStates.add(childNode.getStateId());
            } else  {
                Integer stateId = -1;
                boolean unassigned = true;
                // check to see if this sibling is homogeneous with an existing state.
                for (Iterator<Integer> stateIter = siblingStates.iterator() ; (unassigned && stateIter.hasNext());){
                    stateId = stateIter.next();
                    aState = hmmGraf.getNode(stateId);
                    if (this.isHomogeneous(siblingNode.getMorph(), aState.getMorph())){
                        // this is in-effect the backup null hypothesis
                        this.setState(siblingNode, stateId);
                        unassigned = false;
                    }
                }
                if (unassigned){        // not homogeneous with existing sibling states so create a new state
                    this.setState(siblingNode, null);
                    siblingStates.add(siblingNode.getStateId());
                }
            }
            
            // this segment of code progressively builds the stateTemplate to be returned
            // it has no bearing on the primary activity of splitFamily.
            Integer stateId = siblingNode.getStateId();
            Character ch = siblingNode.getId().charAt(0);
            if (stateTemplate.containsKey(stateId)){
                stateTemplate.get(stateId).add(ch);
            } else {
                HashSet<Character> group = new HashSet<Character>();
                group.add(ch);
                stateTemplate.put(stateId, group);
            }
        }
        
        return stateTemplate;
    }
    
    
    /** propogateTemplate ensures the determinism of transitions in the morphTrie by applying a template
     * of state assignments in one sibling group to the state assignments in the corresponding sibling
     * group with the most recent (last) character removed.  This is a recursive procedure which requires
     * the parent and template of the destination sibling group and applies it to the corresponding 
     * originating sibling group.  The recursion is repeated until the sibling group contains all idle
     * nodes.
     * @ param parentNode is the parent of the sibling group from which the stateTemplate was constructed.
     * @ param stateTemplate maps between stateId's and those siblings assigned to those states
     */
    private void propogateTemplate(MorphNode parentNode, HashMap<Integer, Set<Character>> stateTemplate){
        if ((parentNode == null) || (stateTemplate == null)) return;
        if (parentNode.getChildren().size() == 0) return;
        if (parentNode.getId().length() < 1) return;
        
        // set up some variables for use below
        String suffix = parentNode.getId();                         // the id of the Node which will transition to parentNode
        Character lastChar = suffix.charAt(suffix.length()-1);     // the character which will cause the transition from targetParentNode to ParentNode
        String targetParentId = suffix.substring(0, suffix.length()-1);
        MorphNode targetParentNode = morphTrie.getNode(targetParentId); // the parent node of the siblings to which the template is to be applied
        MorphNode siblingNode = null;                               // the sibling Node to which the template is currently being applied
        Morph siblingMorph = null;                                  // the morph of the sibling node
        Integer siblingStateId = -1;                                // the id of the state assigned to the sibling node
        HMMNode siblingState = null;                                //  the state assigned to the sibling node
        Set<Integer> usedStates = new HashSet<Integer>();           // the states already used in previosly processed groups
        Map<Integer, Integer> reassignedStates = new HashMap<Integer, Integer>();
        // reassignedStates hold those stateId's which the template has required to be changed and what they were changed to
        
        
        // set the targetParent node to be an idle node
        this.setState(targetParentNode, -1);
        
        // iterate thru the stateId's in the State Template'
        for (Integer destinationStateId : stateTemplate.keySet()){    // for every group in the template
            Set<Character> group = stateTemplate.get(destinationStateId);   // a group of siblings in the template assigned to the same state
            for (Character ch : group){          // for every transition in the group
                siblingNode = morphTrie.getChild(targetParentNode, ch);
                siblingStateId = siblingNode.getStateId();
                if ((siblingNode.isFamily()) || (usedStates.contains(siblingStateId))){     // if the state of the sibling Node must be changed
                    if (reassignedStates.containsKey(siblingStateId)){                      // if this stateId has been changed for a previous sibling Node in the group
                        this.setState(siblingNode, reassignedStates.get(siblingStateId));   // make the same change to this one
                    } else {                                                                // else this stateId has not been seen before in this group
                        HMMNode newState = hmmGraf.newState();                              // create a new state
                        newState.addLink(lastChar, destinationStateId);                     // add the outbound state transition because we know it now
                        this.setState(siblingNode, newState.getId());                       // assign the sibling node to this new state
                        reassignedStates.put(siblingStateId, siblingNode.getStateId());     // add a new mapping into reassignedStates
                    }
                } else if (!siblingNode.isIdle()) {                                         // ignore Idle nodes
                    reassignedStates.put(siblingStateId, siblingNode.getStateId());         // for SuffiX nodes record that this stateId has been used in this group
                }                
            }
            // clear the groupStates info because a different state is now mandated by the destination template
            usedStates.addAll(reassignedStates.values());
            reassignedStates.clear();
        }
        

        //stateTemplate = morphTrie.getStateTemplate(targetParentNode);
        // make a recursive call propogateStateTemplate.
        this.propogateTemplate(targetParentNode, stateTemplate);
        
    }
    
    
    
    
    /** setState completes a number of related actions in the morphtrie and HMMgraph to
     * change the state assignment of a morphNode. If no destination stateId is 
     * supplied then a new state is created in the HMMgraph. The morphNode is moved
     * from its existing state (possibly -1) to the supplied state in the morph-trie.
     * The morphNode is altered to record the new stateId and set to SuffiX or idle 
     * as required. A record is made of the disrupted state transitions for later
     * resolution. Finally, the oldState is deleted if the reassignment leaves it
     * empty.
     * @param stateId The new stateId to which the morph should be assigned.
     */
    public void setState(MorphNode morphNode, Integer stateId){
        
        // create a new State if none is supplied
        HMMNode state = null;
        if (stateId == null){
            state = hmmGraf.newState();
            stateId = state.getId();
        } else {
            state = hmmGraf.getNode(stateId);
        }
        Integer oldStateId = morphNode.getStateId();
        
        // make changes to the HMM states
        Set<Character> newTransitions = hmmGraf.reassign(morphNode, stateId);
        
        // make changes to the morph node
        morphNode.setStateId(stateId);
        if (stateId < 0) morphNode.setIdle();
        else morphNode.setSuffiX();
        
        if (stateId >=0){       // make a record of the disrupted HMM state transitions
            // unmappedHMMTransitions holds the Id's of States with missing transitions.
            Set<Character> requiredTransitions = new HashSet<Character>();
            if (unmappedHMMTransitions.containsKey(stateId)){
                requiredTransitions.addAll(unmappedHMMTransitions.get(stateId));
            }
            requiredTransitions.addAll(morphNode.getMorph().getVocab());
            requiredTransitions.removeAll(state.getLinkIds());
            unmappedHMMTransitions.put(stateId, requiredTransitions);
        } else {                // delete the old state if it is now empty.
            if (unmappedHMMTransitions.containsKey(oldStateId)){
                HMMNode oldState = hmmGraf.getNode(oldStateId);
                if (oldState.isEmpty()) {
                    unmappedHMMTransitions.remove(oldStateId);
                    hmmGraf.removeNode(oldStateId);
                }
            }
        }
    }
    
    
    /** mapTransitions steps  thru the previously recorded disrupted transitions in the 
     * HMM graph and remaps them.
     */
    public void mapHMMTransitions(){
        for (Integer baseStateId : hmmGraf.getNodeIds()){
            HMMNode baseState = hmmGraf.getNode(baseStateId);
            for (String morphNodeId : baseState.getMemberIds()){
                MorphNode morphNode = morphTrie.getNode(morphNodeId);
                Morph morph = morphNode.getMorph();
                for (Character ch : morph.getVocab()){
                    baseState.removeLink(ch);
                    String history = morphNode.getId() + ch.toString();
                    MorphNode mappedNode = morphTrie.mapHistory(history);
                    Integer targetStateId = mappedNode.getStateId();
                    baseState.addLink(ch, targetStateId);
                }
            }
        }
    }
    
//    public void mapHMMTransitions(){
//        Set<Character> stateTransitions = null;
//        for (Integer baseStateId : unmappedHMMTransitions.keySet()){
//            HMMNode baseState = hmmGraf.getNode(baseStateId);
//            if (baseState.isEmpty()){
//                hmmGraf.removeNode(baseStateId);
//            } else {
//                MorphNode morphNode = baseState.getRandomMorphNode();
//
//                stateTransitions = unmappedHMMTransitions.get(baseStateId);
//                for(Character ch : stateTransitions){
//                    baseState.removeLink(ch);
//                    String history = morphNode.getId() + ch.toString();
//                    MorphNode mappedNode = morphTrie.mapHistory(history);
//                    Integer targetStateId = mappedNode.getStateId();
//                    baseState.addLink(ch, targetStateId);
//                }
//            }
//        }
//    }
    
// </editor-fold>
    
    
    // <editor-fold defaultstate="collapsed" desc=" Homogeneity testing methods ">
    
    /** isHomogeneous test the homogenity of the two morphs provided based on the
     * current value of SigTest and testtype set for the morph-trie. In addition, the
     * value of the probability of the difference between the two morphs is
     * stored in the first morph associated wih obs
     * @ param morph1 The first morph to be compared. Stores the resulting probability.
     * @ param morph2 The second morph to be compared.
     * @ param obs The character associated with the transition between the two morphs.
     */
    private boolean isHomogeneous(Morph morph1, Morph morph2){
        return (significance(morph1, morph2, testtype) > sigTest);
    }
    
    /** significance tests the homogenity of the two morphs provided based on the
     * current value of SigTest and testtype set for the morph-trie.
     * @ param morph1 The first morph to be compared.
     * @ param morph2 The second morph to be compared.
     */
    public static Double significance(Morph morph1, Morph morph2, TestType test){
        Double prob = 0.0;
        switch (test){
            case KS:{
                //            prob = Stats.KSsig(morph1.getTallies(), morph2.getTallies());
            }
            case CHIS:{
                List<Integer> temp1 = new ArrayList<Integer>();
                List<Integer> temp2 = new ArrayList<Integer>();
                Integer value = 0, index = 0;
                for(Character ch : alphabet){
                    if (morph1.getVocab().contains(ch)){
                        value = morph1.getTally(ch);
                    } else {
                        value = 0;
                    }
                    temp1.add(index, value);
                    
                    if (morph2.getVocab().contains(ch)){
                        value = morph2.getTally(ch);
                    } else {
                        value = 0;
                    }
                    temp2.add(index, value);
                    
                    index++;
                }
                prob = Stats.chstwo(temp1, temp2, 0);
                
            }
            default: {}
        }
        return prob;
    }
    
    
    /** Steps through the morphNodes assigned to a State and returns the first
     * found that is not homogeneous or null otherwise.
     * @param state is the state to be checked for homogeneity.
     * @returns a nonHomogeneous MorphNode if any exist.
     */
    public MorphNode getOutcast(HMMNode state){
        if (state.size() <= 1) return null;
        
        Morph stateMorph = state.getMorph();
        for(MorphNode morphNode : state.getMembers()){
            if (!this.isHomogeneous(morphNode.getMorph(), stateMorph)){
                return morphNode;
            }
        }
        return null;
    }
    
    
// </editor-fold>
    
    public MorphTrie getMorphTrie(){
        return morphTrie;
    }
    
    public HMMGraf getHMMGraf(){
        return hmmGraf;
    }
    
    public static TestType getTestType(){
        return testtype;
    }
    
}

