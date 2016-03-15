/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asdnetworks.arithmetic.eval;

import javax.swing.JFrame;
import com.asdnetworks.arithmetic.asd.ASDPhraseNode;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
/**
 *
 * @author rox
 */
public class EvaluatorUI extends JFrame{
    



 
     /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(EvaluatorUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EvaluatorUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EvaluatorUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EvaluatorUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
          private Evaluator evaluator;
            public void run() {
                  
                new EvaluatorUI(evaluator).setVisible(true);
                
            }
        });
    }
   

// public ASDTesterGui(ASDTesterLogic tester)



EvaluatorUI (Evaluator aThis)
      {  evaluator = aThis;
         grammarFileNameField = new JTextField(40);
	 grammarFileNameField.addActionListener(
	 new GrammarFileNameFieldListener(this));
       
        
        JFrame.setDefaultLookAndFeelDecorated(true);

         /* Also referred to as utteranceField */
         expressionField = new JTextField(40);
         expressionField.addActionListener(
            new ExpressionFieldListener(this));
         /* results field */
         valueField = new JTextField(40);
         JPanel pane = new JPanel();
         pane.setLayout(
            new BoxLayout(pane, BoxLayout.Y_AXIS));
         JTextArea description = new JTextArea("\n"
            + "   This application evaluates arithmetic expressions like"
            + "   2x -[3.5x37 - (0.64 / 4 + 2.)] + .001\n"
            + "   To use it, just enter a desired expression in the"
            + " expression line below, press the Enter key,\n "
            + "   and use the Action menu or the menu in the pane"
            + " below to parse and evaluate it.\n", 5, 50
            );
         description.setMaximumSize(new Dimension(800, 50));
         description.setFont(new Font("Times Roman", Font.BOLD, 14));
         pane.add(description);
	 pane.add(
	   new LabeledTextField("Grammar file: ", grammarFileNameField));
         pane.add(
            new LabeledTextField("Expression:   ", expressionField));
         pane.add(
            new LabeledTextField("Value:        ", valueField));
         outputPane = new JTextArea();
         outputPane.setMinimumSize(new Dimension(DEFAULT_WIDTH,
            DEFAULT_HEIGHT));
         outputPane.setFont(Evaluator.PLAINFONT);
         OutputPaneMenu menu = new OutputPaneMenu(outputPane, evaluator);
         MouseListener popupListener = new PopupListener(menu);
         outputPane.addMouseListener(popupListener);

         pane.add(new JScrollPane(outputPane));
         getContentPane().add(pane, BorderLayout.CENTER);
         addWindowListener(new WindowCloser(this));
            // listens for window closing events (see below)
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

         JMenuBar menuBar = new JMenuBar();
         setJMenuBar(menuBar);
         ActionMenu aMenu = new ActionMenu(this);
         aMenu.setMnemonic(KeyEvent.VK_A);
         menuBar.add(aMenu);
         HelpMenu hMenu = new HelpMenu(this);
         hMenu.setMnemonic(KeyEvent.VK_H);
         menuBar.add(hMenu);



          //1. ICON 1   OPEN  (init for now) & Create toolbar
          JToolBar toolbar = new JToolBar();
          //toolbar.setSize(800,30);
          add(toolbar, BorderLayout.NORTH);
          // the open icon will be for future use with a DIALOG window
          // 1. ICON 1 initialize parse     uses OPEN  ICON    with listener
          // because the grammar file is opened automatically
          // pretend we init the parse
          JButton newGrammarFileButton = null;
          newGrammarFileButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("open.gif")));

//newGrammarFileButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("/com/asdnetworks/arithmetic/resources/open.gif")));
          //JButton newGrammarFileButton = new JButton(new ImageIcon("images/open.gif"));// uses open.gif  icon
          newGrammarFileButton.setToolTipText("Use grammar file text field");
          toolbar.add(newGrammarFileButton);
          toolbar.addSeparator();
          newGrammarFileButton.addActionListener(new ActionListener() {

              public void actionPerformed(ActionEvent event) {
                  //initializeParse();        // 1. ICON 0 TODO  OPEN FILE  with listener     *****
                  evaluator.initializeParse(true);

              }
          });


           /////////////////////////////////////////////////////////////////////////////////////////////////
          //2. ICON 2    INIT

          JButton initializeParseButton = null;

          initializeParseButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("new.gif")));
          //  JButton initializeParseButton = new JButton(new ImageIcon("images/new.gif")); // uses new.gif  icon
          initializeParseButton.setToolTipText("Initialize new parse");
          toolbar.add(initializeParseButton);

          initializeParseButton.addActionListener(new ActionListener() {

              public void actionPerformed(ActionEvent event) {
                  //initializeParse();      2. ICON uses OPEN  ICON  1 INIT PARSE    *****
                  evaluator.initializeParse(true);
              }
          });

           /////////////////////////////////////////////////////////////////////////////////////////////////
          // 3. ICON 3  advance parse
          JButton completeParseButton = null;

          completeParseButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("allRemaining.png")));


          //JButton completeParseButton = new JButton(new ImageIcon("images/allRemaining.png"));//uses parse.png icon
          completeParseButton.setToolTipText("Advance Parse");
          toolbar.add(completeParseButton);

          completeParseButton.addActionListener(new ActionListener() {

              public void actionPerformed(ActionEvent event) {
                 // evaluator.completeParse();
                   evaluator.advance();
              }
          });



          /////////////////////////////////////////////////////////////////////////////////////////////////
          // 4. ICON 4 
          //  Show Phrase Tree Structure with listener
          JButton showPhraseButton = null;
          //JButton showTreeButton = new JButton(new ImageIcon("images.sideparse.png"));    // uses  icon
          //(new ImageIcon(this.getClass().getClassLoader().getResource("images/open.gif")));
          showPhraseButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("blue-side-tree.png")));

          //  JButton showPhraseButton = new JButton(new ImageIcon("images/blue-side-tree.png"));    //uses icon
          showPhraseButton.setToolTipText("Show parse tree phrase structure");
          toolbar.add(showPhraseButton);

          showPhraseButton.addActionListener(new ActionListener() {

              public void actionPerformed(ActionEvent event) {
                  //showBracketedPhrase();
                  evaluator.showPhraseStructure();
              }
          });


           /////////////////////////////////////////////////////////////////////////////////////////////////
          // 5.  Complete and Evaluate Expression
          JButton showTreeButton = null;
          //JButton showTreeButton = new JButton(new ImageIcon("images.sideparse.png"));    // uses  icon
          //(new ImageIcon(this.getClass().getClassLoader().getResource("images/open.gif")));
          showTreeButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("sideparse.png")));

          //  JButton showTreeButton = new JButton(new ImageIcon("images/sideparse.png")); // uses  icon
          showTreeButton.setToolTipText("Evaluate Arithmetic Expression ");
          toolbar.add(showTreeButton);
          toolbar.addSeparator();
          showTreeButton.addActionListener(new ActionListener() {

              public void actionPerformed(ActionEvent event) {
                  // showPhraseStructure();
                  //evaluator.showSemanticValue();
                  evaluator.completeParse();
              }
          });

          /////////////////////////////////////////////////////////////////////////////////////////////////
          // 6.  Select All output text area //   

          JButton copyButton = null;
          // JButton selectButton = new JButton(new ImageIcon("images/copy.gif"));    // uses  icon
          //(new ImageIcon(this.getClass().getClassLoader().getResource("images/open.gif")));
          copyButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("copy.gif")));
          //JButton copyButton = new JButton(new ImageIcon("images/copy.gif")); // uses COPY icon
          copyButton.setToolTipText("Select all ");
          toolbar.add(copyButton);

          copyButton.addActionListener(new ActionListener() {

              public void actionPerformed(ActionEvent event) {
                  outputPane.requestFocus();
                  outputPane.selectAll();
              }
          });
         ////////////////////////////////////////////////////////////////////////////////////////////////
        // 7 Erase / Clear  Select output text area


          JButton cutEraseButton = null;

          cutEraseButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("cut.gif")));
          //  JButton cutEraseButton = new JButton(new ImageIcon("images/cut.gif"));    // need ERASE icon
          cutEraseButton.setToolTipText("Clear/Delete output text area");
          toolbar.add(cutEraseButton);
          toolbar.addSeparator();
          cutEraseButton.addActionListener(new ActionListener() {

              public void actionPerformed(ActionEvent event) {
                  outputPane.setText("");
              }
          });

          // 8. Help   showAboutInfo   with listener
          JButton helpButton = null;
          helpButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("help.png")));
          // JButton helpButton = new JButton(new ImageIcon("images/help.png"));  // uses  icon
          helpButton.setToolTipText("Show App info: this can take a little time");
          toolbar.add(helpButton);

          helpButton.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent event) {
                    
                       //showAboutInfo();
                       com.asdnetworks.arithmetic.eval.BareBonesBrowserLaunch.openURL
                       ("http://www.yorku.ca/jmason/arithmeticExpressions.htm");
                      // ("http://www.yorku.ca/jmason/arithmeticExpressions.htm");
                   
                     
                    
              }
          });

           /////////////////////////////////////////////////////////////////////////////////////////////////
           // 9 EXIT

          JButton exitButton = null;
          // JButton exitButton = new JButton(new ImageIcon("images/delete.gif"));    // uses  icon
          //(new ImageIcon(this.getClass().getClassLoader().getResource("images/open.gif")));
          exitButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("delete.gif")));

          // JButton exitButton = new JButton(new ImageIcon("images/delete.gif"));    // uses  icon
          exitButton.setToolTipText("Close application ");
          toolbar.add(exitButton);
          //toolbar.addSeparator();
          exitButton.addActionListener(new ActionListener() {

              public void actionPerformed(ActionEvent event) {
                  System.exit(0);
              }
          });




      } // end EvaluatorWindow(Evaluator givenTester)
    
      void clearValueField() { valueField.setText(""); }
      void clearExpressionField() { expressionField.setText(""); }
      void clearGrammarFileNameField() { grammarFileNameField.setText(""); }

      JTextField getValueField() { return valueField; }
      JTextField getGrammarFileNameField() { return grammarFileNameField; }
      JTextArea getOutputPane() { return outputPane; }
      Evaluator getEvaluator() { return evaluator; }
      JTextField getExpressionField() { return expressionField; }

      void displaySemanticValue()
      {
      }

      void grammarFileNameFieldChanged(String in)
      {  clearValueField();
      if (!evaluator.useGrammar(in))
            // grammar file was not loaded
            // Note: the grammarFileNameField is intentionally NOT
            // reset to empty here, so the user can edit the incorrect
            // file name if desired.
            return;
         outputPane.append(
            "\nNew grammar file has been loaded.\n");
         String utterance = evaluator.getUtterance();
         if (utterance != null && utterance.length() > 0)
            evaluator.initializeParse(true); // also clears the value field
      } //end grammarFileNameChanged


      void expressionFieldChanged()
      {  evaluator.setUtterance(expressionField.getText().trim());
      }

      void setGrammarFile(String fileName)
      {  grammarFileNameField.setText(fileName);
      }

      void setValueField(String value)
      {  valueField.setText(value);
      }

      /**
         Displays the tree rooted at the given head node,
         with node currentNode indicated by an asterisk and an arrow.
         @param head the header node of the phrase structure
         @param currentNode the current node at the top level
         in the phrase structure
       */
      void showTree(ASDPhraseNode head, ASDPhraseNode currentNode)
      {  showTreeMark(head, "", currentNode);
         outputPane.append("\n");
      } // end showTree

      /**
         Displays the portion of the tree starting at the
         given node and indented with the given indentString as
         prefix for each line that does not represent a top-
         level node.  Top-level nodes are prefixed with three
         blanks or, in the case of the given aNode, an asterisk
         and an arrow whose purpose is to indicate the node
         which is the current node during a parse.
         @param indentString prefix for indenting of the
         current subtree
         @param aNode the node to be marked with an arrow
       */
      private void showTreeMark(ASDPhraseNode givenNode, String indentString,
                               ASDPhraseNode markNode)
      {  outputPane.append("\n");
         if (givenNode == markNode)
            outputPane.append("*->");
         else
            outputPane.append("   ");
         outputPane.append(indentString + givenNode.word() + " ");
         if (givenNode.instance() != null)
            outputPane.append(givenNode.instance().instance());
         else
            outputPane.append("nil");
         if (givenNode.subphrase() != null)
            showTreeMark(givenNode.subphrase(),indentString + "   ",
               markNode);
         if (givenNode.nextNode() != null)
            showTreeMark(givenNode.nextNode(), indentString, markNode);
      } // end showTreeMark

      static final int DEFAULT_WIDTH = 800;  // window width
      static final int DEFAULT_HEIGHT = 900; // window height
      private Evaluator evaluator;
      private JTextField grammarFileNameField;
      private JTextField valueField;
      private JTextField expressionField;
      private JTextArea outputPane;
   } // end class EvaluatorWindow


  class LabeledTextField extends JPanel
   {  LabeledTextField(String labelText, JTextField textField)
      {  setMaximumSize(new Dimension(800,10));
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         JLabel label = new JLabel(labelText);
         label.setFont(Evaluator.BOLDFONT);
         textField.setFont(Evaluator.PLAINFONT);
         this.add(label);
         this.add(textField);
      }
   } // end class LabeledTextField

   /**
      An instance defines what should happen when a window
      closes.
    */
   class WindowCloser extends WindowAdapter
   {  WindowCloser(EvaluatorUI w)
      {  window = w;
      }

      public void windowClosing(WindowEvent e)
      {
         System.exit(0);        // stop the program
      }

      EvaluatorUI window;
   } // end class WindowCloser

   class GrammarFileNameFieldListener implements ActionListener
   {
      GrammarFileNameFieldListener(EvaluatorUI w)
      {  window = w;
      }

      public void actionPerformed(ActionEvent e)
       {// window.grammarFileNameFieldChanged();
      }

      private EvaluatorUI window;
   } // end class GrammarFileNameFieldListener

    class ExpressionFieldListener implements ActionListener
   {
      ExpressionFieldListener(EvaluatorUI w)
      {  window = w;
      }

      public void actionPerformed(ActionEvent e)
      {  window.expressionFieldChanged();
      }

      private EvaluatorUI window;
   } // end class ExpressionFieldListener

 class OutputPaneMenu extends JPopupMenu implements ActionListener
   {  OutputPaneMenu(JTextArea p, Evaluator t)
      {  pane = p;
         evaluator = t;
         setInvoker(pane);
JMenuItem openMenuItem = new JMenuItem("Open",
KeyEvent.VK_O);
openMenuItem.setAccelerator(KeyStroke.getKeyStroke(
KeyEvent.VK_O, ActionEvent.ALT_MASK));
openMenuItem.addActionListener(this);
add(openMenuItem);
addSeparator();


         JMenuItem initializeItem = new JMenuItem("Initialize parse");
         initializeItem.addActionListener(this);
         add(initializeItem);
         addSeparator();
         JMenuItem advanceItem = new JMenuItem("Advance one step");
         advanceItem.addActionListener(this);
         add(advanceItem);
         JMenuItem completeParseItem = new JMenuItem("Complete parse");
         completeParseItem.addActionListener(this);
         add(completeParseItem);
         addSeparator();
         JMenuItem showTreeItem = new JMenuItem("Show expression structure tree");
         showTreeItem.addActionListener(this);
         add(showTreeItem);
         addSeparator();
         JMenuItem selectAllItem = new JMenuItem("Select all");
         selectAllItem.addActionListener(this);
         add(selectAllItem);
         JMenuItem copyItem = new JMenuItem("Copy selection");
         copyItem.addActionListener(this);
         add(copyItem);
         addSeparator();
         JMenuItem clearItem = new JMenuItem("Erase output pane");
         clearItem.addActionListener(this);
         add(clearItem);
      } // end OutputPaneMenu(JTextArea p, Evaluator t)

      public void actionPerformed(ActionEvent e)
      {  if (evaluator == null) return;
         String command = e.getActionCommand();
         if (command.equals("Initialize parse"))
            evaluator.initializeParse(true); // also clears the value field
         else if (command.equals("Advance one step"))
            evaluator.advance();
         else if (command.equals("Complete parse"))
            evaluator.completeParse();
         else if (command.equals("Show expression structure tree"))
            evaluator.showPhraseStructure();
         else if (command.equals("Select all"))
         {  pane.requestFocus();
            pane.selectAll();
         }
         else if (command.equals("Copy selection"))
            pane.copy();
         else if (command.equals("Erase output pane"))
            pane.setText("");
      } // end actionPerformed

      Evaluator evaluator;
      JTextArea pane; // the pane to which the menu is attached.
   } // end class OutputPaneMenu

   class ActionMenu extends JMenu implements ActionListener
   {  ActionMenu(EvaluatorUI w)
      {  super("Action");
         evaluatorwindow = w;
         evaluator = evaluatorwindow.getEvaluator();
         outputPane = evaluatorwindow.getOutputPane();
         JMenuItem openMenuItem = new JMenuItem("Open grammar file",
         KeyEvent.VK_O);
         openMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_O, ActionEvent.ALT_MASK));
         openMenuItem.addActionListener(this);
         add(openMenuItem);
         addSeparator();
         JMenuItem initializeMenuItem = new JMenuItem("Initialize parse",
            KeyEvent.VK_I);
         initializeMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_I, ActionEvent.ALT_MASK));
         add(initializeMenuItem);
         initializeMenuItem.addActionListener(this);
         JMenuItem advanceMenuItem = new JMenuItem("Advance one Step",
            KeyEvent.VK_S);
         advanceMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_S, ActionEvent.ALT_MASK));
         add(advanceMenuItem);
         advanceMenuItem.addActionListener(this);
         JMenuItem completeParseMenuItem = new JMenuItem("Complete Parse",
            KeyEvent.VK_P);
         completeParseMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_P, ActionEvent.ALT_MASK));
         add(completeParseMenuItem);
         completeParseMenuItem.addActionListener(this);
         addSeparator();
         JMenuItem showTreeMenuItem = new JMenuItem(
            "Show expression structure tree", KeyEvent.VK_T);
         showTreeMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_T, ActionEvent.ALT_MASK));
         showTreeMenuItem.addActionListener(this);
         add(showTreeMenuItem);
         addSeparator();
         JMenuItem copyAllMenuItem = new JMenuItem(
            "Select All of output pane", KeyEvent.VK_A);
         copyAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_A, ActionEvent.CTRL_MASK));
         copyAllMenuItem.addActionListener(this);
         add(copyAllMenuItem);
         JMenuItem copySelectionMenuItem = new JMenuItem("Copy Selection",
            KeyEvent.VK_C);
         copySelectionMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_C, ActionEvent.CTRL_MASK));
         copySelectionMenuItem.addActionListener(this);
         add(copySelectionMenuItem);
         addSeparator();
         JMenuItem eraseMenuItem = new JMenuItem("Erase output pane",
            KeyEvent.VK_E);
         eraseMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_E, ActionEvent.CTRL_MASK));
         eraseMenuItem.addActionListener(this);
         add(eraseMenuItem);
         JMenuItem exitMenuItem = new JMenuItem("Exit",KeyEvent.VK_X);
         exitMenuItem.addActionListener(this);
         add(exitMenuItem);

      }
     /**
        Listens for menu item events.
         */
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("Initialize parse")) {
                evaluator.initializeParse(true); // also clears the value field
            } else if (command.equals("Advance one Step")) {
                evaluator.advance();
            } else if (command.equals("Complete Parse")) {
                evaluator.completeParse();
            } else if (command.equals("Show expression structure tree")) {
                evaluator.showPhraseStructure();
            } else if (command.equals("Select All of output pane")) {
                outputPane.requestFocus();
                outputPane.selectAll();
            } else if (command.equals("Copy Selection")) {
                outputPane.copy();
            } else if (command.equals("Erase output pane")) {
                outputPane.setText("");
            } else {
                if (command.equals("Open")) //evaluator.loadNewFile();
                {
                    evaluator.initializeParse(true);
                } else {
                    if (command.equals("Exit")) {
                        evaluator.exitEvaluator();
                    }

                }
            }
            }
                        
               // }
           // }

        




      Evaluator evaluator;
      EvaluatorUI evaluatorwindow;//////////////////////////////////////////
      JTextArea outputPane;
   } // end class ActionMenu

   class HelpMenu extends JMenu implements ActionListener
   {  HelpMenu(EvaluatorUI w)
      {  super("Help");
         evaluatorwindow = w;
         evaluator = evaluatorwindow.getEvaluator();
         JMenuItem aboutMenuItem = new JMenuItem("About Evaluator",
            KeyEvent.VK_A);
         add(aboutMenuItem);
         aboutMenuItem.addActionListener(this);
      }

      /**
         Listens for menu item events.
       */
//      public void actionPerformed(ActionEvent e)
//      {  String command = e.getActionCommand();
//         if (command.equals("About Evaluator"))
//            try {
//                evaluator.showAboutInfo();
//            } catch (URISyntaxException ex) {
//                Logger.getLogger(Evaluator.class.getName()).log(Level.SEVERE, null, ex);
//            }
//      }
   
    public void actionPerformed(ActionEvent e)
      {  String command = e.getActionCommand();
         if (command.equals("About Evaluator"))
            try {
              try {
                  evaluator.showAboutInfo();
              } catch (FileNotFoundException ex) {
                  Logger.getLogger(HelpMenu.class.getName()).log(Level.SEVERE, null, ex);
              } catch (IOException ex) {
                  Logger.getLogger(HelpMenu.class.getName()).log(Level.SEVERE, null, ex);
              }
            } catch (URISyntaxException ex) {
                Logger.getLogger(Evaluator.class.getName()).log(Level.SEVERE, null, ex);
            }
      }

      Evaluator evaluator;
      EvaluatorUI evaluatorwindow;
    
   private static ResourceBundle resources;
  //  private final static String EXIT_AFTER_PAINT = "-exit";
  //  private static boolean exitAfterFirstPaint;

    static {
       try {
           resources = ResourceBundle.getBundle("Evaluator",
                   Locale.getDefault());
            //String propertyValue = resources.getString("key")
       } catch (MissingResourceException mre) {
            System.err.println("resources/Evaluator.properties not found");
            System.exit(1);
       }
   }
     
 
}// end EvaluatorUI
class PopupListener extends MouseAdapter
{  PopupListener(JPopupMenu m)
   {  menu = m;
   }

   public void mousePressed(MouseEvent e)
   {  maybeShowPopup(e);
   }

   public void mouseReleased(MouseEvent e)
   {  maybeShowPopup(e);
   }

   private void maybeShowPopup(MouseEvent e)
   {  if (e.isPopupTrigger())
        menu.show(e.getComponent(), e.getX(), e.getY());
   }

   private JPopupMenu menu;
} // end class PopupListener


/*
 * <b>Bare Bones Browser Launch for Java</b><br>
 * Utility class to open a web page from a Swing application
 * in the user's default browser.<br>
 * Supports: Mac OS X, GNU/Linux, Unix, Windows XP/Vista/7<br>
 * Example Usage:<code><br> &nbsp; &nbsp;
 *    String url = "http://www.google.com/";<br> &nbsp; &nbsp;
 *    BareBonesBrowserLaunch.openURL(url);<br></code>
 * Latest Version: <a href="http://www.centerkey.com/java/browser/">www.centerkey.com/java/browser</a><br>
 * Author: Dem Pilafian<br>
 * Public Domain Software -- Free to Use as You Like
 * @version 3.1, June 6, 2010
 */
class BareBonesBrowserLaunch {

   static final String[] browsers = { "google-chrome", "firefox", "opera",
      "epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla" };
   static final String errMsg = "Error attempting to launch web browser";

   /**
    * Opens the specified web page in the user's default browser
    * @param url A web address (URL) of a web page (ex: "http://www.google.com/")
    */
   public static void openURL(String url) {
      try {  //attempt to use Desktop library from JDK 1.6+
         Class<?> d = Class.forName("java.awt.Desktop");
         d.getDeclaredMethod("browse", new Class[] {java.net.URI.class}).invoke(
            d.getDeclaredMethod("getDesktop").invoke(null),
            new Object[] {java.net.URI.create(url)});
         //above code mimicks:  java.awt.Desktop.getDesktop().browse()
         }
      catch (Exception ignore) {  //library not available or failed
         String osName = System.getProperty("os.name");
         try {
            if (osName.startsWith("Mac OS")) {
               Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
                  "openURL", new Class[] {String.class}).invoke(null,
                  new Object[] {url});
               }
            else if (osName.startsWith("Windows"))
               Runtime.getRuntime().exec(
                  "rundll32 url.dll,FileProtocolHandler " + url);
            else { //assume Unix or Linux
               String browser = null;
               for (String b : browsers)
                  if (browser == null && Runtime.getRuntime().exec(new String[]
                        {"which", b}).getInputStream().read() != -1)
                     Runtime.getRuntime().exec(new String[] {browser = b, url});
               if (browser == null)
                  throw new Exception(Arrays.toString(browsers));
               }
            }
         catch (Exception e) {
            JOptionPane.showMessageDialog(null, errMsg + "\n" + e.toString());
            }
         }
      }

   }

class DesktopClassToLaunch {

   public static void openUrl(String a) throws URISyntaxException {
      try {
        URI uri = new
        URI ("http://www.yorku.ca/jmason/arithmeticExpressions.htm");
        Desktop desktop = null;

        if (Desktop.isDesktopSupported()) {
        desktop = Desktop.getDesktop();
        }
        if (desktop != null)
        desktop.browse(uri);
        } catch (IOException ioe) {

        ioe.printStackTrace();
        } catch (URISyntaxException use) {
        use.printStackTrace();
        }
    }
}

