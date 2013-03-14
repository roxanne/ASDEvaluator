package com.asdnetworks.arithmetic.eval;

import com.asdnetworks.arithmetic.asd.ASDParser;    // JFrame, JPanel
import com.asdnetworks.arithmetic.asd.ASDPhraseNode;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;



/**
   An instance of Evaluator permits a user to evaluate arithmetic
   expressions.  It provides a graphical user interface that permits
   initialization of a parse, stepping the parse, and displaying
   the phrase structure and semantic value as the parse proceeds.
<BR><BR>
   Command-line usage:
   <BR>In an MS-Windows command-line window:
   <BR><tt><b> java -cp asddigraphs.jar;. arithmetic/Evaluator</b></tt>
   <BR>Or under UNIX:
   <BR><tt><b> java -cp asddigraphs.jar:. arithmetic/Evaluator</b></tt>
   <BR>OR if asddigraphs.jar and asdx.jar have been put in the system
   classpath:
   <BR><tt><b> java arithmetic/Evaluator</b></tt>
   <BR>
   <BR>An optional command-line parameter can be used to specify the maximum
   <BR>number of parsing advance steps before pausing.
   <BR>Example:  java arithmetic/Evaluator 50000
   <BR>(The default maximum number of advance steps between pauses is 10000.)

   @authors James A. Mason, Roxanne M. Parent
   @version 2.00  2005 Aug, March 2013
 */
public class Evaluator
{  public static void main(String[] args)
   {  int maxSteps = 0;
      if (args.length == 0)
         maxSteps = MAXSTEPS;
      else
         maxSteps = Integer.parseInt(args[0]);

      
      
      
       try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Evaluator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Evaluator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Evaluator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Evaluator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
     

      Evaluator thisEvaluator = new Evaluator(maxSteps);
      
   }

   Evaluator(int maxSteps)
   {  window = new EvaluatorUI(this);
      window.setTitle(
         "Arithmetic Expression Evaluator - version " + VERSION);
      window.setVisible(true);
      maximumSteps = maxSteps;
      parser = new ASDParser(this); // This Evaluator instance is
         // the object in which the parser is to invoke functions
         // that correspond to semanticAction and semanticValue
         // fields in the grammar being used.
      useGrammar(GRAMMARFILE);
      window.setGrammarFile(GRAMMARFILE);
      expectedTypes = new ArrayList();
      expectedTypes.add(EXPECTEDTYPE);

   }

   void advance()
   {  if (!grammarLoaded)
      {  JOptionPane.showMessageDialog(window,
            "No grammar file is currently loaded.");
         window.clearGrammarFileNameField();
         return;
      }
      if (utterance == null || utterance.equals("")) return;
      window.clearValueField();  // attempting to compute a new value
      String advanceResult = parser.advance();
      if (advanceResult.equals(parser.QUIT))
      {  window.getOutputPane().append(
            "\nParse quit after " + stepsThisTry + " new and "
            + steps + " total advance steps,\n"
            + "and " + backupStepsThisTry + " new and "
            + backupSteps + " total backup steps, leaving structure:\n");
         showPhraseStructure();
         showSemanticValue();
         // Re-initialize the parse without clearing the value field:
         initializeParse(false);
      }
      else if (advanceResult.equals(parser.SUCCEED))
      {  steps++;
         stepsThisTry++;
         stepsSincePause++;
         window.getOutputPane().append(
            "\n" + steps + " - parse advanced to:\n");
         showPhraseStructure();
         if (parser.done())
         {  showExpressionValue();
            showExpressionValueField();
            window.getOutputPane().append(
               "\nSuccessful parse after " + stepsThisTry + " new and "
               + steps  + " total advance steps,\n"
               + "and " + backupStepsThisTry + " new and "
               + backupSteps + " total backup steps.\n");
            // prepare for an attempt at an alternative parse
            stepsThisTry = 0;
            backupStepsThisTry = 0;
            stepsSincePause = 0;
         }
         else
            showSemanticValue();
      }
      else if (advanceResult.equals(parser.NOADVANCE))
      {  if (parser.backup())
         {  ++backupSteps; ++backupStepsThisTry;
            window.getOutputPane().append("\nparse backed up to:\n");
            showPhraseStructure();
            showSemanticValue();
         }
         else
         {  window.getOutputPane().append(
               "\nParse failed after " + stepsThisTry + " new and "
            + steps + " total advance steps,\n"
            + "and " + backupStepsThisTry + " new and "
            + backupSteps + " total backup steps, leaving structure:\n");
            showPhraseStructure();
            showSemanticValue();
            // prepare for an attempt at an alternative parse
            stepsThisTry = 0;
            backupStepsThisTry = 0;
            stepsSincePause = 0;
         }
      }
      else  // this shouldn't occur
         window.getOutputPane().append(
            "\nInvalid result of ASDParser advance(maxSteps):"
            + advanceResult + "\n");
   } // end advance

   boolean completeParse()
   {  if (!grammarLoaded)
      {  JOptionPane.showMessageDialog(window,
            "No grammar file is currently loaded.");
         window.clearGrammarFileNameField();
         return false;
      }
      if (utterance == null || utterance.equals(""))
         return false;
      window.clearValueField();  // attempting to compute a new value
      String advanceResult; // SUCCEED, NOADVANCE, or QUIT
      while(stepsSincePause < maximumSteps)
      {  advanceResult = parser.advance();
         if (advanceResult.equals(parser.QUIT))
         {  window.getOutputPane().append(
               "\nParse quit after " + stepsThisTry + " new and "
               + steps + " total advance steps,\n"
               + "and " + backupStepsThisTry + " new and "
               + backupSteps
               + " total backup steps, leaving structure:\n");
            showPhraseStructure();
            showSemanticValue();
            // Re-initialize the parse without clearing the value field:
            initializeParse(false);
            return false;
         }
         else if (advanceResult.equals(parser.SUCCEED))
         {  ++steps;
            ++stepsThisTry;
            ++stepsSincePause;
            if (parser.done())
            {  window.getOutputPane().append(
                  "\nSuccessful parse in " + stepsThisTry + " new and "
                  + steps  + " total advance steps,\n"
                  + "and " + backupStepsThisTry + " new and "
                  + backupSteps + " total backup steps.\n");
               window.displaySemanticValue();
               showPhraseStructure();
               showExpressionValue();
               showExpressionValueField();
               // prepare for an attempt at an alternative parse
               stepsThisTry = 0;
               backupStepsThisTry = 0;
               stepsSincePause = 0;
               return true;
            }
         }
         else if (advanceResult.equals(parser.NOADVANCE))
         {  if (parser.backup())
            {  ++backupSteps; ++backupStepsThisTry;
            }
            else
            {  window.getOutputPane().append(
                  "\nParse failed after " + stepsThisTry + " new and "
                  + steps + " total advance steps,\n"
                  + "and " + backupStepsThisTry + " new and "
                  + backupSteps + " total backup steps, leaving structure:\n");
               showPhraseStructure();
               showSemanticValue();
               // prepare for an attempt at an alternative parse
               stepsThisTry = 0;
               backupStepsThisTry = 0;
               stepsSincePause = 0;
               return false;
            }
         }
         else  // this shouldn't happen
         {  window.getOutputPane().append(
               "Invalid result of ASDParser advance(maxSteps):"
               + steps);
            return false;
         }
      }
      window.getOutputPane().append(
         "\nAttempt to Complete parse paused after advance step "
         + steps + ",\n" + maximumSteps
         + " advance steps since start or last pause."
         +"\nIt can be resumed by menu selection.\n");
      stepsSincePause = 0;
      return false;
   } // end completeParse

   ASDParser getParser() { return parser; }

   String getUtterance() { return utterance; }

   boolean initializeParse(boolean shouldClearValueField)
   {  if (!grammarLoaded)
      {  JOptionPane.showMessageDialog(window,
            "No grammar file is currently loaded.");
         window.clearGrammarFileNameField();
         return false;
      }
      if (utterance == null || utterance.length() == 0)
      {  JOptionPane.showMessageDialog(window,
            "The expression to be parsed must not be empty.");
         return false;
      }
      if (expectedTypes == null || expectedTypes.size() == 0)
      {  JOptionPane.showMessageDialog(window,
            "The list of expected phrase types must not be empty.");
         return false;
      }
      utterance = scanInputString(utterance);
      parser.initialize(utterance, expectedTypes);
      phraseInitialized = true;
      steps = 0;
      stepsThisTry = 0;
      stepsSincePause = 0;
      backupSteps = 0;
      backupStepsThisTry = 0;
      if (shouldClearValueField)
         window.clearValueField();
      window.getOutputPane().append("\nInitialized expression structure:\n");
      showPhraseStructure();
      return true;
   } // end initializeParse

   String scanInputString(String expression)
   {  String result = "";
      StringTokenizer tokenizer = new StringTokenizer(expression,
         parser.SPACECHARS + parser.SPECIALCHARS + "-x", true);
      // Punctuation marks must be separated from words before
      // the EnglishWord.processApostrophe method is applied.
      while(tokenizer.hasMoreTokens())
      {  String token = tokenizer.nextToken().trim();
         if (token.length() == 1) // punctuation or one-letter word
         {  if ("(".equals(token))
               result += " LPAREN";
            else if (")".equals(token))
               result += " RPAREN";
            else
               result += " " + token;
         }
         else if (token.length() > 1)
            result += " " + token;
         // whitespace tokens are ignored
      }
      return result;
   } // end scanInputString

   void setUtterance(String newUtterance)
   {  if (!grammarLoaded)
      {  JOptionPane.showMessageDialog(window,
            "No grammar file is currently loaded.");
         window.clearGrammarFileNameField();
         window.clearExpressionField();
         return;
      }
      utterance = newUtterance;
      if (utterance == null || utterance.length() == 0)
      {  JOptionPane.showMessageDialog(window,
            "The expression to be parsed must not be empty.");
         return;
      }
      initializeParse(true);  // also clears the value field
   } // end setUtterance

   void setUtteranceNull() { utterance = null; }

  // void showAboutInfo()
  // {  // responds to About Evaluator choice in Help menu
     // JOptionPane.showMessageDialog(window,
      //   "Evaluator version " + VERSION +
      //   "\nAuthor: James A. Mason" +
       //  "\nEmail: jmason@yorku.ca" +
       //  "\nhttp://www.yorku.ca/jmason/");
  // }

   void showAboutInfo()
      throws FileNotFoundException, IOException, URISyntaxException
   {  // responds to About CardWorld choice in Help menu
     // CardWorldAboutWindow w = new CardWorldAboutWindow(
        // "CardWorld2Documentation.txt");
      
       DesktopClassToLaunch.openUrl
       ("http://www.yorku.ca/jmason/CardWorld/CardWorld2Documentation.html");
      
   //}
   }

  
   
   
   void showExpressionValue()
   {  window.getOutputPane().append("\n");
      window.getOutputPane().append("Value of the expression is "
         + parser.phraseStructure().nextNode().value() + "\n");
   }

   void showExpressionValueField()
   {  window.setValueField(
         parser.phraseStructure().nextNode().value() + "");
   }

   void showPhraseStructure()
   {  if (!phraseInitialized)
         JOptionPane.showMessageDialog(window,
            "No expression has been entered.");
      else
      {  ASDPhraseNode head = parser.phraseStructure();
         ASDPhraseNode currentNode = parser.currentNode();
         window.showTree(head, currentNode);
      }
   } // end showPhraseStructure

   void showSemanticValue()
   {  window.getOutputPane().append("\n");
      window.getOutputPane().append("Value of current node is: "
         + parser.currentNode().value() + "\n");
   }

   void showSemanticValueField()
   {  window.setValueField(parser.currentNode().value() + "");
   }

   boolean useGrammar(String fileName)
   {  if (parser.useGrammar(fileName))
      {  grammarLoaded = true;
         return true;
      }
      else
      {  JOptionPane.showMessageDialog(window,
            "Grammar file with that name could not be loaded.");
         grammarLoaded = false;
         return false;
      }
   } // end useGrammar

   public String currentWord()
   {  return parser.currentNode().word();
   }

   public Object get(String featureName)
   {  return parser.get(featureName);
   }

   public Object nodeValue()
   {  return parser.currentNode().value();
   }

   public void set(String featureName, Object featureValue)
   {  parser.set(featureName, featureValue);
   }

/* Functions to compute semantic actions and values of grammar nodes
    in expression.grm:
  */

   public Object expression_$$_1_v()
   {  return get("previous");
   }

   public Object expression_$$_2_v()
   {  return get("previous");
   }

   public Object expression_$$_3_v()
   {  String integerPart = (String) get("integerPart");
      return new Double(integerPart);
   }

   public String expression_DECIMALPOINT_2()
   {  set("integerPart", "0");
      return null;
   }

   public String expression_DIGITSTRING_1()
   {  set("integerPart", nodeValue());
      return null;
   }

   public Object expression_DIGITSTRING_2_v()
   {  String integerPart = (String) get("integerPart");
      String fractionPart = (String) nodeValue();
      return new Double(integerPart + "." + fractionPart);
   }

   public String expression_divided_by_1()
   {  set("operator", currentWord());
      return null;
   }

   public String expression_EXPRESSION()
   {  set("value", nodeValue());
      return null;
   }

   public String expression_FACTOR_1()
   {  Double previous = (Double) get("previous");
      String operator = (String) get("operator");
      Double current = (Double) nodeValue();
      if (previous == null) // first factor
         previous = current;
      else
      {  double resultDouble = 0;
         if ("*".equals(operator))
            resultDouble = previous.doubleValue() * current.doubleValue();
         else if ("/".equals(operator))
         {  if (current.doubleValue() == 0)
            {  window.getOutputPane().append(
                  "\n*** Attempt to divide by 0 ***\n");
               window.setValueField("*** Attempt to divide by 0 ***");
               return parser.QUIT;
            }
            resultDouble = previous.doubleValue() / current.doubleValue();
         }
         else // shouldn't happen
            System.out.println("Invalid operator " + operator);
         previous = new Double(resultDouble);
      }
      set("previous", previous);
      return null;
   }

   public Object expression_FACTOR_2_v()
   {  Double factor = (Double) nodeValue();
      return new Double(- factor.doubleValue());
   }

   public Object expression_NUMBER_1_v()
   {  return currentWord();
   }

   public String expression_minus_1()
   {  set("operator", currentWord());
      return null;
   }

   public String expression_plus_1()
   {  set("operator", currentWord());
      return null;
   }

   public Object expression_RIGHT_BRACKET_v()
   {  return get("value");
   }

   public String expression_TERM_1()
   {  Double previous = (Double) get("previous");
      String operator = (String) get("operator");
      Double current = (Double) nodeValue();
      if (previous == null) // first term
         previous = current;
      else
      {  double resultDouble = 0;
         if ("+".equals(operator))
            resultDouble = previous.doubleValue() + current.doubleValue();
         else if ("-".equals(operator))
            resultDouble = previous.doubleValue() - current.doubleValue();
         else // shouldn't happen
            System.out.println("Invalid operator " + operator);
         previous = new Double(resultDouble);
      }
      set("previous", previous);
      return null;
   }

   public String expression_times_1()
   {  set("operator", "*");
      return null;
   }
   
   

  // static final String GRAMMARFILE = "expression.grm";
   static final String GRAMMARFILE = "http://www.asd-networks.com/grammars/expression.grm";
   static final String EXPECTEDTYPE = "EXPRESSION";
   static final int MAXSTEPS = 10000;
   static final String VERSION = "1.2";
   static final Font PLAINFONT
      = new Font("Monospaced", Font.PLAIN, 14);
   static final Font BOLDFONT
      = new Font("Monospaced", Font.BOLD, 14);
   private EvaluatorUI window;
   private ASDParser parser;
   private boolean grammarLoaded = false;
   private boolean phraseInitialized = false;
   private ArrayList expectedTypes;
   private int steps = 0;  // total advance steps since phrase initialization
   private int stepsSincePause = 0;  // steps since last pause
   private int stepsThisTry = 0;  // advance steps since phrase
                                  // initialization or last successful parse
   private int backupSteps = 0;  // total backup steps since phrase init.
   private int backupStepsThisTry = 0; // since init. or last succ. parse
   private int maximumSteps = MAXSTEPS;
   private String utterance = null;

    void exitEvaluator() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
 
    

} // end class Evaluator
