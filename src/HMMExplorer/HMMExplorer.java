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

import java.io.*;
import javax.swing.JFileChooser;

/**
 *
 * @author nworbnhoj
 */
public class HMMExplorer extends javax.swing.JFrame {
    
    /**
     * Creates new form HMMExplorer
     */
    public HMMExplorer() {
        initComponents();
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jFileChooser1 = new javax.swing.JFileChooser();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        HMM = new javax.swing.JMenu();
        New = new javax.swing.JMenuItem();
        Open = new javax.swing.JMenuItem();
        Save = new javax.swing.JMenuItem();
        SaveAs = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        Exit = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        Help = new javax.swing.JMenu();
        ReadMe = new javax.swing.JMenuItem();
        About = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("Dynamic CSSR");

        HMM.setText("HMM");
        New.setText("New");
        New.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewActionPerformed(evt);
            }
        });

        HMM.add(New);

        Open.setText("Open");
        Open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenActionPerformed(evt);
            }
        });

        HMM.add(Open);

        Save.setText("Save");
        Save.setEnabled(false);
        HMM.add(Save);

        SaveAs.setText("Save As");
        HMM.add(SaveAs);

        HMM.add(jSeparator1);

        Exit.setMnemonic('x');
        Exit.setText("Exit");
        HMM.add(Exit);

        jMenuBar1.add(HMM);

        jMenu4.setText("Control");
        jMenuBar1.add(jMenu4);

        Help.setText("Help");
        ReadMe.setText("Read me");
        Help.add(ReadMe);

        About.setText("About");
        Help.add(About);

        jMenuBar1.add(Help);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void NewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewActionPerformed
         HMMStreams hmmStreams = new HMMStreams(HMMExplorer.this, true);
        hmmStreams.setVisible(true);
        jTabbedPane1.add("new HMM", new HMMDisplay(hmmStreams.getSource(), hmmStreams.getDestination()));
    }//GEN-LAST:event_NewActionPerformed
    
    private void OpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenActionPerformed
        int returnVal = jFileChooser1.showOpenDialog(HMMExplorer.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser1.getSelectedFile();
           
 // this needs to be coded to read in a whole HMM from file           
//            ObjectReader inputStream = null;
//            try {
//                inputStream = new BufferedReader(new FileReader(file));
//                jTabbedPane1.add(file.getName(), new HMMDisplay(inputStream));
//            } catch (java.io.IOException e) {
//                System.err.println("Caught "
//                        + "java.io.IOException: "
//                        +   e.getMessage());
//            } finally {
//                if (inputStream != null) {
//                    //           inputStream.close();
//                }
//            }
        } else {
            //   Open command cancelled by user.
        }
    }//GEN-LAST:event_OpenActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new HMMExplorer().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem About;
    private javax.swing.JMenuItem Exit;
    private javax.swing.JMenu HMM;
    private javax.swing.JMenu Help;
    private javax.swing.JMenuItem New;
    private javax.swing.JMenuItem Open;
    private javax.swing.JMenuItem ReadMe;
    private javax.swing.JMenuItem Save;
    private javax.swing.JMenuItem SaveAs;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
    
}
