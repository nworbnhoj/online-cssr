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


import java.beans.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.ISOMLayout;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.SpringLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.contrib.CircleLayout;
import edu.uci.ics.jung.visualization.contrib.KKLayout;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.LayoutMutable;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.visualization.GraphLabelRenderer;
import edu.uci.ics.jung.graph.decorators.ConstantDirectionalEdgeValue;
import edu.uci.ics.jung.graph.decorators.EdgeStrokeFunction;

import edu.uci.ics.jung.graph.impl.SparseTree;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.decorators.UserDatumNumberEdgeValue;
import edu.uci.ics.jung.graph.decorators.UserDatumNumberVertexValue;
import edu.uci.ics.jung.graph.decorators.NumberEdgeValue;
import edu.uci.ics.jung.graph.decorators.NumberVertexValue;




import javax.swing.SwingWorker;
import HMM.*;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.ArchetypeEdge;
import edu.uci.ics.jung.graph.decorators.ToolTipFunctionAdapter;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.graph.decorators.EdgeStringer;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.VertexStrokeFunction;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;





/**
 *
 * @author  nworbnhoj
 */
public class HMMDisplay extends javax.swing.JPanel implements PropertyChangeListener {
    
    public cssrHMM model = null;
    
    ConstructHMM task = null;
    
    private SparseTree mtGraph = null;
    private Layout mtLayout = null;
    
    private SparseGraph hmmGraph = null;
    private LayoutMutable hmmLayout = null;
    
    private VisualizationViewer vv = null;
    private GraphZoomScrollPane gzsp = null;
    
    private javax.swing.JPanel hmmControls = null;
    
    
    
    enum Modes {learn, coast};
    
    
    /**
     *
     */
    private class ConstructHMM extends SwingWorker<cssrHMM, Integer> {
        
        Integer cssrSteps = 0;
        
        ConstructHMM(Integer steps){
            super();
            cssrSteps = steps;
        }
        
        @Override
        protected cssrHMM doInBackground() {
            model.CSSR(cssrSteps);
            publish(model.getCount());
            //     setProgress(progress);
            return model;
        }
        
        
        @Override
        public void done() {
            mtGraph = null;
            hmmGraph = null;
            
            if (morphTrieRadioButton.isSelected()){
                showMorphTrie();
            } else if (hmmRadioButton.isSelected()){
                showHMM();
            }
        }
        
        
        @Override
        protected void process(List<Integer> steps) {
            if (!steps.isEmpty()){
                Integer N = steps.get(steps.size()-1);
                count.setText(N.toString());
            }
        }
        
    }
    
    
    /**
     * Creates new form HMMDisplay
     */
    public HMMDisplay(Reader inStream, Writer outStream) {
        model = new cssrHMM(inStream, outStream);
        hmmGraph = new SparseGraph();
        mtGraph = new SparseTree(new SparseVertex());
        //currentStatus = model.getStatus();
        initGraphComponents();
        initComponents();
    }
    
    
    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            
        }
    }
    
    
    /**
     *
     * @author danyelf
     */
    
//    private static final class LayoutChooser implements ActionListener {
//        private final JComboBox jcb;
//        private final VisualizationViewer vv;
//
//        private LayoutChooser(JComboBox jcb, VisualizationViewer vv) {
//            super();
//            this.jcb = jcb;
//            this.vv = vv;
//        }
//
//        public void actionPerformed(ActionEvent arg0) {
//            Object[] constructorArgs =
//            { hmmVisual};
//
//            Class layoutC = (Class) jcb.getSelectedItem();
//            Class lay = layoutC;
//            try {
//                Constructor constructor = lay
//                        .getConstructor(new Class[] {Graph.class});
//                Object o = constructor.newInstance(constructorArgs);
//                Layout l = (Layout) o;
//                vv.stop();
//                vv.setGraphLayout(l, false);
//                vv.restart();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
    
    private void showHMM(){
        vv.stop();
        if (hmmGraph == null){
            hmmGraph = JUNGtranslator.translateHMMGraf(model.getHMMGraf());
            hmmLayout = new FRLayout(hmmGraph);
            // ((FRLayout) hmmLayout).setAttractionMultiplier(2.0);
        }
        vv.setGraphLayout(hmmLayout, true);
        vv.setToolTipFunction(new HMMTips());
        vv.restart();
        
        next.setText(model.getNextObservation().toString());
        history.setText(model.getRecentHistory());
        count.setText(model.getCount().toString());
    }
    
    private void showMorphTrie(){
        vv.stop();
        if (mtGraph == null){
            mtGraph = JUNGtranslator.translateMorphTrie(model.getMorphTrie());
            mtLayout = new TrieLayout(mtGraph);
        }
        vv.setGraphLayout(mtLayout, true);
        vv.setToolTipFunction(new morphTips());
        vv.restart();
        
        next.setText(model.getNextObservation().toString());
        history.setText(model.getRecentHistory());
        count.setText(model.getCount().toString());
    }
    
    
    /**
     * @return a list of layout classes
     */
    private static Class[] getCombos() {
        List layouts = new ArrayList();
        layouts.add(KKLayout.class);
        layouts.add(FRLayout.class);
        layouts.add(CircleLayout.class);
        layouts.add(SpringLayout.class);
        layouts.add(ISOMLayout.class);
        return (Class[]) layouts.toArray(new Class[0]);
    }
    
    
    
    
    
    public class HMMTips extends ToolTipFunctionAdapter {
        
        public String getToolTipText(Vertex v) {
            if (v.containsUserDatumKey("suffixString")){
                return v.getUserDatum("suffixString").toString();
            } else return "no tip";
        }
        
        public String getToolTipText(Edge e) {
            return "no tip";
        }
        
    }
    
    public class morphTips extends ToolTipFunctionAdapter {
        
        public String getToolTipText(Vertex v) {
            String tip = "";
            if (v.containsUserDatumKey("history")){
                tip += v.getUserDatum("history").toString();
            }
            if (v.containsUserDatumKey("size")){
                tip += "  N=" + v.getUserDatum("size").toString();
            }
            if (v.containsUserDatumKey("morph")){
                tip += "  " + v.getUserDatum("morph").toString();
            }
            return tip;
        }
        
        public String getToolTipText(Edge e) {
            if (e.containsUserDatumKey("detail")){
                return e.getUserDatum("detail").toString();
            } else return "-";
        }
        
    }
    
    
    public class VertexLabel implements VertexStringer{
        
        public String getLabel(ArchetypeVertex v){
            String label = "";
            if (v!= null){
                if (v.containsUserDatumKey("title")){
                    label = v.getUserDatum("title").toString();
                    if (label == null) label = "";
                }
            }
            return label;
        }
    }
    
    public class EdgeLabel implements EdgeStringer{
        
        public String getLabel(ArchetypeEdge e){
            String label = "";
            if (e!= null){
                if (e.getUserDatum("id") != null){
                    label = e.getUserDatum("id").toString();
                    if (label == null) label = "";
                }
                if (e.getUserDatum("probability") != null){
                    label += "|" + String.format("%1$.3f", e.getUserDatum("probability"));
                }
            }
            return label;
        }
    }
    
    private final class VertexStroke implements VertexStrokeFunction {
        
        public Stroke getStroke(Vertex v){
            if (hmmRadioButton.isSelected()){
                return new BasicStroke((float) 1.0);
            } else if (morphTrieRadioButton.isSelected()){
                return new BasicStroke((float) 1.0);
            }
            return new BasicStroke((float) 1.0);
        }
    }
    
    
    private final class EdgeWeightStrokeFunction implements EdgeStrokeFunction{
        
        public Stroke getStroke(Edge e) {
            if (hmmRadioButton.isSelected()){
                if (e.containsUserDatumKey("probability")){
                    double weight = new Double(e.getUserDatum("probability").toString());
                    if (weight > 0.9) return new BasicStroke(4);
                    if (weight > 0.7) return new BasicStroke(3);
                    if (weight > 0.3) return new BasicStroke(2);
                    if (weight > 0.1) return new BasicStroke(1);
                    if (weight > 0.01) return PluggableRenderer.DASHED;
                    return PluggableRenderer.DOTTED;
                }
            } else if (morphTrieRadioButton.isSelected()){
                Double significance = 0.0;
                if (e.containsUserDatumKey("significance")){
                    significance = new Double(e.getUserDatum("significance").toString());
                    Double sigTest = 0.001;
                    Integer weight = (int) (significance/sigTest);
                    if (weight > 100) return new BasicStroke(4);
                    if (weight > 30) return new BasicStroke(3);
                    if (weight > 10) return new BasicStroke(2);
                    if (weight > 3) return new BasicStroke(1);
                    if (weight > 1) return PluggableRenderer.DASHED;
                    return PluggableRenderer.DOTTED;
                }
            }
            return new BasicStroke(1);
        }
    }
    
    private Paint getStateColor(Integer stateId){
        if (stateId == null) return Color.RED;
        if (stateId == 0) return Color.RED;
 
        int red = (stateId*476)%256;
        int green = (stateId*356)%256;
        int blue = (stateId*792)%256;
        return new Color(red, green, blue);
    }
    
    private final class VertexColor implements VertexPaintFunction {
        
        public Paint getDrawPaint(Vertex v) {
            if (hmmRadioButton.isSelected()){
                return Color.BLACK;
            } else if (morphTrieRadioButton.isSelected()){
                if (v.containsUserDatumKey("morphStatus")){
                    String morphStatus = (String) v.getUserDatum("morphStatus");
                    if (morphStatus == "idle") return Color.GRAY;
                    if (morphStatus == "family") return Color.BLACK;
                    if (morphStatus == "SuffiX") {return Color.BLACK;
                    }
                }
            }
            return Color.BLACK;
        }
        
        public Paint getFillPaint(Vertex v) {
            if (hmmRadioButton.isSelected()){
                if (v.containsUserDatumKey("stateId")){
                    Integer stateId = new Integer(v.getUserDatum("stateId").toString());
                    return getStateColor(stateId);
                }
            } else if (morphTrieRadioButton.isSelected()){
                if ((v.containsUserDatumKey("morphStatus")) && (v.containsUserDatumKey("stateId"))){
                    String morphStatus = v.getUserDatum("morphStatus").toString();
                    if (morphStatus == "SuffiX"){
                        Integer stateId = new Integer(v.getUserDatum("stateId").toString());
                        return getStateColor(stateId);
                    }
                    if (morphStatus == "idle") return Color.WHITE;
                    if (morphStatus == "family") return Color.LIGHT_GRAY;
                }
                return Color.WHITE;
            }
            return Color.WHITE;
        }
    }
    
    private void initGraphComponents(){
        
        PluggableRenderer pr = new PluggableRenderer();
        
        pr.setVertexStringer(new VertexLabel());
        pr.setVertexStrokeFunction(new VertexStroke());
        pr.setVertexLabelCentering(true);
        pr.setVertexPaintFunction(new VertexColor());
        
        pr.setEdgeStringer(new EdgeLabel());
        pr.setEdgeStrokeFunction(new EdgeWeightStrokeFunction());
        pr.setEdgeLabelClosenessFunction(new ConstantDirectionalEdgeValue(0.3, 0.3));
        
//        pr.setEdgeStringer(new NumberEdgeValueStringer(edge_weight));
//        pr.setVertexStringer(new NumberVertexValueStringer(vertex_weight));
        
        
        //if the mtRadioButton default is Selected then use this code
//        SparseGraph visibleGraph = model.translateMorphTrie();
//        Layout layout = new TreeLayout((SparseTree) visibleGraph);
        //-----------------------------------------------------------
        
        // if the hmmRadioButton default is Selected  then use this code
        SparseGraph visibleGraph = JUNGtranslator.translateHMMGraf(model.getHMMGraf());
        Layout layout = new SpringLayout(visibleGraph);
        //-----------------------------------------------------------
        GraphLabelRenderer graphLabelRenderer = pr.getGraphLabelRenderer();
        graphLabelRenderer.setRotateEdgeLabels(false);
        
        
        vv = new VisualizationViewer(layout, pr);
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        vv.setGraphMouse(graphMouse);
        //  vv.setToolTipFunction(new detailTips());
        
        gzsp = new GraphZoomScrollPane(vv);
        
//        hmmVV.setPickSupport(new ShapePickSupport());
//
//        // HMM Visual Controls
//        hmmControls = new javax.swing.JPanel();
//
//        //mode box
//        JComboBox hmmModeBox = hmmGraphMouse.getModeComboBox();
//        hmmModeBox.addItemListener(((DefaultModalGraphMouse)hmmVV.getGraphMouse()).getModeListener());
//
//        //Zoom buttons
//        final ScalingControl hmmScaler = new CrossoverScalingControl();
//        javax.swing.JButton hmmZoomIn = new javax.swing.JButton();
//        hmmZoomIn.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                hmmScaler.scale(hmmVV, 1.1f, hmmVV.getCenter());
//            }
//        });
//        javax.swing.JButton hmmZoomOut = new javax.swing.JButton();
//        hmmZoomOut.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                hmmScaler.scale(hmmVV, 1/1.1f, hmmVV.getCenter());
//            }
//        });
//
//        // Layout Combo
////        Class[] combos = getCombos();
////        javax.swing.JComboBox hmmGraphLayout = new javax.swing.JComboBox(combos);
////        // use a renderer to shorten the layout name presentation
////        hmmGraphLayout.setRenderer(new DefaultListCellRenderer() {
////            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
////                String valueString = value.toString();
////                valueString = valueString.substring(valueString.lastIndexOf('.')+1);
////                return super.getListCellRendererComponent(list, valueString, index, isSelected,
////                        cellHasFocus);
////            }
////        });
////        hmmGraphLayout.addActionListener(new LayoutChooser(hmmGraphLayout, hmmVV));
////        hmmGraphLayout.setSelectedItem(KKLayout.class);
//
//        hmmControls.add(hmmZoomIn);
//        hmmControls.add(hmmZoomOut);
////        hmmControls.add(hmmGraphLayout);
//        hmmControls.add(hmmModeBox);
        
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        displayGroup = new javax.swing.ButtonGroup();
        probGroup = new javax.swing.ButtonGroup();
        HMMOptions = new javax.swing.JDialog();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        MorphTrieOptions = new javax.swing.JDialog();
        jLabel1 = new javax.swing.JLabel();
        morphPanel = new javax.swing.JPanel();
        showMorphAsProb = new javax.swing.JRadioButton();
        showMorphAsPerc = new javax.swing.JRadioButton();
        showMorphAsTally = new javax.swing.JRadioButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jButton3 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        showSig = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        showStates = new javax.swing.JCheckBox();
        CSSROptions = new javax.swing.JDialog();
        jButton1 = new javax.swing.JButton();
        Lmax = new javax.swing.JTextField();
        Significance = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        GraphPanel = new javax.swing.JPanel();

        CSSRControl = new javax.swing.JPanel();
        go = new javax.swing.JButton();
        stepOption = new javax.swing.JComboBox();
        Stop = new javax.swing.JButton();
        history = new javax.swing.JTextField();
        next = new javax.swing.JTextField();
        count = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        DisplayControl = new javax.swing.JPanel();
        hmmRadioButton = new javax.swing.JRadioButton();
        morphTrieRadioButton = new javax.swing.JRadioButton();
        hmmOptions = new javax.swing.JButton();
        morphTrieOptions = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree(new MorphJTrie());

        jCheckBox1.setText("jCheckBox1");
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jCheckBox2.setText("jCheckBox2");
        jCheckBox2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox2.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel5.setText("HMM Options");

        javax.swing.GroupLayout HMMOptionsLayout = new javax.swing.GroupLayout(HMMOptions.getContentPane());
        HMMOptions.getContentPane().setLayout(HMMOptionsLayout);
        HMMOptionsLayout.setHorizontalGroup(
            HMMOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HMMOptionsLayout.createSequentialGroup()
                .addGroup(HMMOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(HMMOptionsLayout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(jCheckBox2))
                    .addGroup(HMMOptionsLayout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jCheckBox1))
                    .addGroup(HMMOptionsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel5)))
                .addContainerGap(268, Short.MAX_VALUE))
        );
        HMMOptionsLayout.setVerticalGroup(
            HMMOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, HMMOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                .addComponent(jCheckBox1)
                .addGap(32, 32, 32)
                .addComponent(jCheckBox2)
                .addGap(131, 131, 131))
        );
        MorphTrieOptions.setTitle("Morph Trie Options");
        MorphTrieOptions.setName("MorphTrieOptions");
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLabel1.setText("Morph Trie Options");

        morphPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Morphs"));
        probGroup.add(showMorphAsProb);
        showMorphAsProb.setText("Probability");
        showMorphAsProb.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showMorphAsProb.setMargin(new java.awt.Insets(0, 0, 0, 0));

        probGroup.add(showMorphAsPerc);
        showMorphAsPerc.setText("Percentage");
        showMorphAsPerc.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showMorphAsPerc.setMargin(new java.awt.Insets(0, 0, 0, 0));

        probGroup.add(showMorphAsTally);
        showMorphAsTally.setText("Tally");
        showMorphAsTally.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showMorphAsTally.setMargin(new java.awt.Insets(0, 0, 0, 0));

        probGroup.add(jRadioButton1);
        jRadioButton1.setText("none");
        jRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout morphPanelLayout = new javax.swing.GroupLayout(morphPanel);
        morphPanel.setLayout(morphPanelLayout);
        morphPanelLayout.setHorizontalGroup(
            morphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(morphPanelLayout.createSequentialGroup()
                .addGroup(morphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(showMorphAsProb)
                    .addComponent(showMorphAsPerc)
                    .addComponent(showMorphAsTally)
                    .addComponent(jRadioButton1))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        morphPanelLayout.setVerticalGroup(
            morphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(morphPanelLayout.createSequentialGroup()
                .addComponent(showMorphAsProb)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showMorphAsPerc)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showMorphAsTally)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton1)
                .addContainerGap(137, Short.MAX_VALUE))
        );

        jButton3.setText("OK");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Edges"));
        showSig.setText("show significance");
        showSig.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showSig.setMargin(new java.awt.Insets(0, 0, 0, 0));
        showSig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSigActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(showSig)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(showSig)
                .addContainerGap(85, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Vertices"));
        showStates.setText("show states");
        showStates.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showStates.setMargin(new java.awt.Insets(0, 0, 0, 0));
        showStates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showStatesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(showStates)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(showStates)
                .addContainerGap(85, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout MorphTrieOptionsLayout = new javax.swing.GroupLayout(MorphTrieOptions.getContentPane());
        MorphTrieOptions.getContentPane().setLayout(MorphTrieOptionsLayout);
        MorphTrieOptionsLayout.setHorizontalGroup(
            MorphTrieOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MorphTrieOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(MorphTrieOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(MorphTrieOptionsLayout.createSequentialGroup()
                        .addGroup(MorphTrieOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(MorphTrieOptionsLayout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MorphTrieOptionsLayout.createSequentialGroup()
                                .addComponent(jButton3)
                                .addGap(27, 27, 27)))
                        .addComponent(morphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        MorphTrieOptionsLayout.setVerticalGroup(
            MorphTrieOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MorphTrieOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(MorphTrieOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(morphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(MorphTrieOptionsLayout.createSequentialGroup()
                        .addGroup(MorphTrieOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(34, 34, 34)
                        .addComponent(jButton3)
                        .addContainerGap(58, Short.MAX_VALUE))))
        );
        CSSROptions.setTitle("CSSR Options");
        jButton1.setText("Source / Sink");
        jButton1.setPreferredSize(new java.awt.Dimension(95, 15));

        Lmax.setText("4");
        Lmax.setToolTipText("The maximum length of history included in the analysis (1-1000)");
        Lmax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LmaxActionPerformed(evt);
            }
        });
        Lmax.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                LmaxPropertyChange(evt);
            }
        });

        Significance.setText("0.001");
        Significance.setToolTipText("The significance of the difference between morphs before a split is made (0-1)");
        Significance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SignificanceActionPerformed(evt);
            }
        });
        Significance.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                SignificancePropertyChange(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel2.setText("CSSR Options");

        jLabel3.setText("History Length");

        jLabel4.setText("Significance");

        jButton4.setText("OK");

        javax.swing.GroupLayout CSSROptionsLayout = new javax.swing.GroupLayout(CSSROptions.getContentPane());
        CSSROptions.getContentPane().setLayout(CSSROptionsLayout);
        CSSROptionsLayout.setHorizontalGroup(
            CSSROptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CSSROptionsLayout.createSequentialGroup()
                .addContainerGap(135, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(105, 105, 105))
            .addGroup(CSSROptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(294, Short.MAX_VALUE))
            .addGroup(CSSROptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CSSROptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(36, 36, 36)
                .addGroup(CSSROptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(Significance, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Lmax, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
                .addContainerGap(239, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CSSROptionsLayout.createSequentialGroup()
                .addContainerGap(265, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addGap(88, 88, 88))
        );
        CSSROptionsLayout.setVerticalGroup(
            CSSROptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CSSROptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(CSSROptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(Lmax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(CSSROptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Significance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(133, 133, 133)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addComponent(jButton4)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        GraphPanel.setLayout(new java.awt.BorderLayout());

        GraphPanel.add(gzsp, BorderLayout.CENTER);
        GraphPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        GraphPanel.setToolTipText("Use the mouse wheel to zoom.");

        CSSRControl.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "CSSR Control", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11)));
        go.setText("Go");
        go.setToolTipText("Go starts the CSSR process.");
        go.setMaximumSize(new java.awt.Dimension(45, 15));
        go.setPreferredSize(new java.awt.Dimension(45, 18));
        go.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goActionPerformed(evt);
            }
        });

        stepOption.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "to split", "1 step", "10 steps", "100 steps", "1000 steps", "10^4 steps", "10^5 steps", "10^6 steps", "10^7 steps", "to end" }));
        stepOption.setToolTipText("Selects the amount of CSSR processing to be done.");
        stepOption.setPreferredSize(new java.awt.Dimension(45, 18));

        Stop.setText("Stop");
        Stop.setToolTipText("Stops the CSSR process.");
        Stop.setPreferredSize(new java.awt.Dimension(45, 18));
        Stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StopActionPerformed(evt);
            }
        });

        history.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        history.setText("HISTORY");
        history.setPreferredSize(new java.awt.Dimension(45, 18));

        next.setMinimumSize(new java.awt.Dimension(6, 15));
        next.setPreferredSize(new java.awt.Dimension(45, 18));

        count.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        count.setText("count");
        count.setPreferredSize(new java.awt.Dimension(45, 18));

        jButton2.setText("Options");
        jButton2.setPreferredSize(new java.awt.Dimension(45, 18));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout CSSRControlLayout = new javax.swing.GroupLayout(CSSRControl);
        CSSRControl.setLayout(CSSRControlLayout);
        CSSRControlLayout.setHorizontalGroup(
            CSSRControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CSSRControlLayout.createSequentialGroup()
                .addGroup(CSSRControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(CSSRControlLayout.createSequentialGroup()
                        .addComponent(go, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stepOption, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(CSSRControlLayout.createSequentialGroup()
                        .addComponent(history, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(next, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(CSSRControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(count, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Stop, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE))
                .addContainerGap(79, Short.MAX_VALUE))
        );
        CSSRControlLayout.setVerticalGroup(
            CSSRControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CSSRControlLayout.createSequentialGroup()
                .addGroup(CSSRControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Stop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(go, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stepOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(CSSRControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(count, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(next, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(history, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        DisplayControl.setBorder(javax.swing.BorderFactory.createTitledBorder("Display"));
        displayGroup.add(hmmRadioButton);
        hmmRadioButton.setSelected(true);
        hmmRadioButton.setText("HMM");
        hmmRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        hmmRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hmmRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hmmRadioButtonActionPerformed(evt);
            }
        });

        displayGroup.add(morphTrieRadioButton);
        morphTrieRadioButton.setText("Morph Trie");
        morphTrieRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        morphTrieRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        morphTrieRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                morphTrieRadioButtonActionPerformed(evt);
            }
        });

        hmmOptions.setText("Options");
        hmmOptions.setPreferredSize(new java.awt.Dimension(69, 18));

        morphTrieOptions.setText("Options");
        morphTrieOptions.setPreferredSize(new java.awt.Dimension(69, 18));

        javax.swing.GroupLayout DisplayControlLayout = new javax.swing.GroupLayout(DisplayControl);
        DisplayControl.setLayout(DisplayControlLayout);
        DisplayControlLayout.setHorizontalGroup(
            DisplayControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DisplayControlLayout.createSequentialGroup()
                .addGroup(DisplayControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hmmRadioButton)
                    .addComponent(morphTrieRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(DisplayControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(hmmOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(morphTrieOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        DisplayControlLayout.setVerticalGroup(
            DisplayControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DisplayControlLayout.createSequentialGroup()
                .addGroup(DisplayControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hmmRadioButton)
                    .addComponent(hmmOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(DisplayControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(morphTrieRadioButton)
                    .addComponent(morphTrieOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        DisplayControlLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {hmmOptions, hmmRadioButton, morphTrieOptions, morphTrieRadioButton});

        jScrollPane1.setViewportView(jTree1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(CSSRControl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DisplayControl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(GraphPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(CSSRControl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(DisplayControl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(GraphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed
    
    private void showStatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showStatesActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_showStatesActionPerformed
    
    private void showSigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSigActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_showSigActionPerformed
    
    private void LmaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LmaxActionPerformed
        String textLmax = Lmax.getText();
        Integer intLmax = new Integer(textLmax);
        intLmax = model.setLmax(intLmax);
        Lmax.setText(intLmax.toString());
    }//GEN-LAST:event_LmaxActionPerformed
    
    private void SignificanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SignificanceActionPerformed
        String textSig = Significance.getText();
        Double dblSig = new Double(textSig);
        dblSig = model.setSignificance(dblSig);
        Significance.setText(dblSig.toString());
    }//GEN-LAST:event_SignificanceActionPerformed
    
    private void LmaxPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_LmaxPropertyChange
        
    }//GEN-LAST:event_LmaxPropertyChange
    
    private void SignificancePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_SignificancePropertyChange
        
    }//GEN-LAST:event_SignificancePropertyChange
    
    private void hmmRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hmmRadioButtonActionPerformed
        showHMM();
    }//GEN-LAST:event_hmmRadioButtonActionPerformed
    
    private void morphTrieRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_morphTrieRadioButtonActionPerformed
        showMorphTrie();
    }//GEN-LAST:event_morphTrieRadioButtonActionPerformed
    
    private void goActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goActionPerformed
        Integer cssrSteps = 0;
        switch (stepOption.getSelectedIndex()){
            case 0: cssrSteps = 0; break;
            case 1: cssrSteps = 1; break;
            case 2: cssrSteps = 10; break;
            case 3: cssrSteps = 100; break;
            case 4: cssrSteps = 1000; break;
            
            case 5: cssrSteps = 10000; break;
            case 6: cssrSteps = 100000; break;
        }
        
        task = new ConstructHMM(cssrSteps);
        // task.addPropertyChangeListener(this);
        task.execute();
        try {
            model = task.get();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (ExecutionException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_goActionPerformed
    
    private void StopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StopActionPerformed
        try {
            model = task.get(2, TimeUnit.SECONDS);
        } catch (ExecutionException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (TimeoutException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_StopActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CSSRControl;
    private javax.swing.JDialog CSSROptions;
    private javax.swing.JPanel DisplayControl;
    private javax.swing.JPanel GraphPanel;
    private javax.swing.JDialog HMMOptions;
    private javax.swing.JTextField Lmax;
    private javax.swing.JDialog MorphTrieOptions;
    private javax.swing.JTextField Significance;
    private javax.swing.JButton Stop;
    private javax.swing.JTextField count;
    private javax.swing.ButtonGroup displayGroup;
    private javax.swing.JButton go;
    private javax.swing.JTextField history;
    private javax.swing.JButton hmmOptions;
    private javax.swing.JRadioButton hmmRadioButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    private javax.swing.JPanel morphPanel;
    private javax.swing.JButton morphTrieOptions;
    private javax.swing.JRadioButton morphTrieRadioButton;
    private javax.swing.JTextField next;
    private javax.swing.ButtonGroup probGroup;
    private javax.swing.JRadioButton showMorphAsPerc;
    private javax.swing.JRadioButton showMorphAsProb;
    private javax.swing.JRadioButton showMorphAsTally;
    private javax.swing.JCheckBox showSig;
    private javax.swing.JCheckBox showStates;
    private javax.swing.JComboBox stepOption;
    // End of variables declaration//GEN-END:variables
    
    
    
    protected NumberEdgeValue edge_weight = new UserDatumNumberEdgeValue("weight");
    protected NumberVertexValue vertex_weight = new UserDatumNumberVertexValue("tally");
    
}
