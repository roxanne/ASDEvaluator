
package com.asdnetworks.arithmetic.asd;
/*

Copyright 2000-2010 James A. Mason

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy
of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations
under the License.

 */


import java.io.*;
import java.lang.reflect.*;
import java.util.*;
/**
   Instances are decision network searchers that can load an ASDGrammar
   from a specified file and use it as a decision network.on an input
   string of decision tokens treated as an unordered set.
   @author James A. Mason
   @version 1.01 2010 Jul 26
 */
public class ASDDecider implements ASDSemantics
{
   /**
      Initializes an ASDDecider with no semantics or application.
    */
   public ASDDecider()
   {  semantics = null; application = null;
   }

   /**
      Initializes an instance with the ASDDecider itself to
      interpret semantic actions and semantic values in the grammar,
      and with application-specific messages to be sent to the
      specified application, if desired.
      @param app the target for application-specific messages
    */
   public ASDDecider(Object app)
   {  semantics = this;
      application = app;
   }

   /**
      Initializes an ASDDecider instance with a reference to a given
      application to which application-specific messages are to be sent,
      and a given instance of a class that implements the ASDSemantics
      interface.
      @param app the target for application-specific messages
      @param semanticsInstance instance of a class that implements
      ASDSemantics (null if none)
    */
   public ASDDecider(Object app, ASDSemantics semanticsInstance)
   {  semantics = semanticsInstance;
      application = app;
   }

   /**
      Returns the current Node at the top level in the decision structure.
    */
   public ASDPhraseNode currentNode() {  return state.currentNode; }

   /**
      Returns the decider's current list of expected phrase types for
      the top level of a search.
    */
   public ArrayList<String> expectedTypes() { return expectedTypes; }

   /**
      Returns the feature-value pairs for the current top level
      of the decision structure.
    */
   public HashMap<String, Object> features() { return state.features; }

   /**
      Returns the value of 'feature' with specified name.
      Same as the valueOf method.
      @param featureName the feature whose value is to be obtained
      @return the value of the feature; null if none
    */
   public Object get(String featureName)
   {  return state.features.get(featureName); }

   /**
      Returns the ASDGrammar (decision network) which is currently
      being used by the decider.
    */
   public ASDGrammar lexicon() { return ASDLexicon; }

   /**
      Returns the header node of the current decision structure.
    */
   public ASDPhraseNode decisionStructure()
      { return state.decisionStructure; }

   /**
      Returns the number of the current step in a search.
    */
   public int parseStepNumber() { return currentParseStepNumber; }

   /*
      Returns the string that causes the current advance of the decider to
      fail, forcing the decider to back up.
   public String noAdvance() { return NOADVANCE; }
   */

   /*
      Returns the string that causes the current search to fail
      altogether, causing the decider to quit the current parse
      unsuccessfully.
   public String quit() { return QUIT; }
    */

   /**
      Sets contents of a 'feature' with the specified name
      to the specified value.
      @param featureName the feature whose value is to be set
      @param value the value to be assigned to featureName
    */
   public void set(String featureName, Object value)
   {  state.features.put(featureName, value); }

   /**
      Returns the value of 'feature' with specified name.
      Same as the get method.
      @param featureName the feature whose value is to be obtained
      @return the value of the feature; null if none
    */
   public Object valueOf(String featureName)
   {  return state.features.get(featureName); }

   /**
      Initialize the ASDDecider instance for searching a given
      string of decision values as one of a list of expected
      decision types.
      @param aString the string to be searched
      @param expected an ArrayList of strings, each the name
      of one of the expected decision types.
    */
   public void initialize(String aString, ArrayList<String> expected)
   {  stringToBeParsed = aString;
      backstack = new Stack<ASDDeciderState>();
      expectedTypes = expected;
      currentParseStepNumber = 0;
      state = new ASDDeciderState();
      state.beginning = 0;
      state.features = new HashMap<String, Object>(10);
      state.currentChoices = null;  // choices not yet computed
      state.decisionStructure = segment(aString);
      state.decisionValues = createDecisionSet(aString);
      state.currentNode = state.decisionStructure;
      state.subphraseStack = new ASDDeciderStack();
      state.nextNodeSubphrase = null;
      state.advanceCase = ' ';
   } // end initialize

   /**
      Attempts to advance the search state one step.
      @return SUCCEED if successful, NOADVANCE if unsuccessful but search
      should continue after backup, QUIT if search should quit.
    */
   public String advance()
   {  if (state.currentChoices == null) // choices not yet computed
         state.currentChoices = choices(true, null);
      if (state.currentChoices.size() == 0)
         return NOADVANCE;  // no choices for advancing from this state
/* Debug output:
      System.out.println("Choices:");
      for (int j = 0; j < state.currentChoices.size(); ++j)
      {  ASDDeciderChoice choice = (ASDDeciderChoice)
            state.currentChoices.get(j);
         System.out.println("" + choice.advanceType
//            + ";" + choice.nextNode.toString()
            + ";" + choice.completedType);
      }
*/
      // Remove the next advance choice from the queue of
      // current choices:
      ASDDeciderChoice tryChoice
         = (ASDDeciderChoice) state.currentChoices.remove(0);
      state.advanceCase = tryChoice.advanceType;
         // the type of advance choice: INITIAL, FINAL, DUMMY, or NONDUMMY
      if (state.currentChoices.size() > 0) // other choices remain
      {
         if (state.currentNode.nextNode() != null)
            // current node in the decision structure is not the last
            // one; prepare for a possible permanent final advance
            // that may occur right after the current node:
            state.nextNodeSubphrase
               = state.currentNode.nextNode().subphrase();
         else
            state.nextNodeSubphrase = null;
         backstack.push((ASDDeciderState)state.clone());
      }
      if (state.advanceCase == FINAL) // a subphrase has ended
      {  String val = advanceFinal(tryChoice.completedType);
            // returns SUCCEED, NOADVANCE, or QUIT
         if (val == NOADVANCE || val == QUIT) return val;
      }
      else
      {  if (state.advanceCase == NONDUMMY)
            advanceNonDummy(tryChoice.nextNode);
         else if (state.advanceCase == DUMMY)
            advanceDummy(tryChoice.nextNode);
         else if (state.advanceCase == INITIAL)
            advanceInitial(tryChoice.nextNode);
         String action
            = tryChoice.nextNode.semanticAction();
         if (semantics != null && action != null && action.length() > 0)
         {  String resultOfAction = semantics.semanticAction(action);
            if (resultOfAction == NOADVANCE || resultOfAction == QUIT)
               return resultOfAction;
         }
      }
      ++currentParseStepNumber;
      return SUCCEED;
   } // end advance

   /**
      Carries out an advance of the Dummy kind, inserting a dummy node
      after the current node in the decision structure, with a pointer
      to the given grammar node.
      @param tryNode the node in the grammar to which the advance is to occur
    */
   void advanceDummy(ASDGrammarNode tryNode)
      // Create a new dummy ASDPhrase node and link it to the
      // next node in the decision structure:
   {  ASDPhraseNode dummy = new ASDPhraseNode();
      dummy.setWord(DUMMYWORD);
      dummy.setInstance(tryNode);
      dummy.setNextNode(state.currentNode.nextNode());
      // Other fields of the dummy node are null by default.

      // Complete the insertion of the dummy node, allowing for
      // possible backup later:
      if (backstack.empty())
         // No backups can occur to states before this state
         // in the search; so just insert the dummy node
         // after the current node
         state.currentNode.setNextNode(dummy);
      else
         // Later backup may be required; so copy the top level
         // of the decision structure up to the place where the
         // dummy node is to be inserted, and insert the
         // dummy node after the copy:
      {  ASDPhraseNode temp
            = (ASDPhraseNode)state.decisionStructure.clone();
         ASDPhraseNode prev;
         ASDPhraseNode oldNode = state.decisionStructure;
         state.decisionStructure = temp;
         while(oldNode != state.currentNode)
         {  oldNode = oldNode.nextNode();
            prev = temp;
            temp = (ASDPhraseNode)oldNode.clone();
            prev.setNextNode(temp);
         }
         temp.setNextNode(dummy);
      }
      // Advance to the dummy node and set currentChoices to
      // indicate that the choices have not yet been computed:
      state.currentNode = dummy;
      state.currentChoices = null;
   } // end advanceDummy

   /**
      Carries out an advance of the Final kind,
      replacing a completed subphrase at the top level
      of the decision structure by a single node whose
      "word" is the type of phrase that was completed.
      @return SUCCEED if successful, NOADVANCE if unsuccessful
      but the search should continue after backup,
      QUIT if the search should quit.
    */
   String advanceFinal(String phraseType)
   {  String val
         = state.currentNode.instance().semanticValue();
      // Evaluate the semantic value
      Object computedValue = null;
      String computedString = null;
      if (semantics != null && val != null && val.length() > 0)
         computedValue = semantics.semanticValue(val);
      else // no class for computing semantics
         computedValue = val;
      if (computedValue instanceof String)
         if (computedValue == NOADVANCE || computedValue == QUIT)
            return (String) computedValue;

      // Find the beginning of the completed subphrase:
      ASDPhraseNode prev = state.decisionStructure;
      for (int j = 1; j < state.beginning; ++j)
         prev = prev.nextNode();
      ASDPhraseNode first = prev.nextNode();
      // Create a new node to represent the entire completed
      // subphrase at the top level in the decision structure:
      ASDPhraseNode newNode = new ASDPhraseNode();
      newNode.setWord(phraseType);
      newNode.setValue(computedValue);
      // instance field is null by default
      newNode.setNextNode(state.currentNode.nextNode());

      // Copy the top level of the subphrase, set
      // the next link of the last node in the copy to null,
      // and hang the copied subphrase below the new node:
      ASDPhraseNode temp = (ASDPhraseNode)first.clone();
      newNode.setSubphrase(temp);
      ASDPhraseNode oldNode = first;
      while(oldNode != state.currentNode)
      {  oldNode = oldNode.nextNode();
         prev = temp;
         temp = (ASDPhraseNode)oldNode.clone();
         prev.setNextNode(temp);
      }
      temp.setNextNode(null);

      // Copy the nodes of the decision structure that precede
      // the completed subphrase, and set the next link of
      // the last of those nodes to point to the new node:
      temp = (ASDPhraseNode)state.decisionStructure.clone();
      oldNode = state.decisionStructure;
      state.decisionStructure = temp;
      while(oldNode.nextNode() != first)
      {  oldNode = oldNode.nextNode();
         prev = temp;
         temp = (ASDPhraseNode)oldNode.clone();
         prev.setNextNode(temp);
      }
      temp.setNextNode(newNode);

      // Set the current node to the one just before the
      // new node:
      state.currentNode = temp;

      // Restore characteristics of the resumed subphrase:
      ASDDeciderStackFrame popped
         = (ASDDeciderStackFrame)state.subphraseStack.pop();
      state.features = popped.features;
      state.beginning = popped.beginning;

      // Compute a new list of currentChoices for the resumed
      // currentNode:
      if (done())
         // If the search is completed, there should be no more
         // choices at this point; this prevents futile initial
         // advances if a subsequent advance is attempted to
         // find another parse.
         state.currentChoices = null;
      else
         // If the search has not been completed, compute a list
         // of choices, but without including dummy advances:
         state.currentChoices = choices(false, null);

      return SUCCEED;  // successful Final advance
   } // end advanceFinal

   /**
      Carries out an advance of the Initial kind,
      beginning a new subphrase at the top level of
      the decision structure at the given node in the grammar.
      @param tryNode the node in the grammar at which
      the new subphrase begins
    */
   void advanceInitial(ASDGrammarNode tryNode)
      // Advance to the next node in the decision structure
      // and to the corresponding node, tryNode, in the
      // grammar:
   {  state.currentNode = state.currentNode.nextNode();
      // Use the token from the grammar node as the "word"
      // for the node in the decision structure:
      state.currentNode.setWord(tryNode.word());
      state.currentNode.setInstance(tryNode);
      // Save the beginning position, the semantic features
      // table, and the flag which indicates uniqueness of
      // parsing for the suspended subphrase:
      state.subphraseStack.push(
         new ASDDeciderStackFrame(state.beginning,
            state.features) );
      // Compute the beginning position for the new subphrase:
      state.beginning = 0;
      for (ASDPhraseNode node = state.decisionStructure;
           node != state.currentNode; node = node.nextNode())
         ++state.beginning;
      // Create a new semantic features table for the new
      // subphrase:
      state.features = new HashMap<String, Object>(10);
      // Set currentChoices to indicate that the choices have
      // not been computed:
      state.currentChoices = null;
   } // end advanceInitial

   /**
      Carries out an advance of the Non-dummy kind,
      moving the current node pointer to the next
      node at the top level in the decision structure,
      and to the corresponding next node in the grammar.
      @param tryNode the node in the grammar to which the
      advance is to occur
    */
   void advanceNonDummy(ASDGrammarNode tryNode)
   {  state.currentNode = state.currentNode.nextNode();
      // Use the token from the grammar node as the "word"
      // for the node in the decision structure:
      state.currentNode.setWord(tryNode.word());
      state.currentNode.setInstance(tryNode);
      state.currentChoices = null;
   } // end advanceNonDummy

   /**
      Attempts to backtrack the search state to the most recent
      place where a local ambiguity occurred -- that is, where
      there was more than one choice for advancing.
      @return true if backtrack is successful, false if there
      was no step in the search to which to backtrack
    */
   public boolean backup()
   {  if (backstack.empty()) return false;
      state = (ASDDeciderState)backstack.pop();
      if (state.currentNode.nextNode() != null)
         // There is a next node in the decision structure.
         if (state.currentNode.nextNode().subphrase()
                != state.nextNodeSubphrase)
            // A unique, permanent subphrase search occurred
            // at the next node.
            if (state.advanceCase == DUMMY)
               // The permanent advance occurred after an
               // intermediate dummy node.  So compute a new
               // list of choices, including any REMAINING
               // dummy choices, for advancing from the
               // current node:
            {  ArrayList<ASDGrammarNode> dummyNodes
                  = new ArrayList<ASDGrammarNode>(10);
               ASDDeciderChoice choice;
               for (int j = 0; j < state.currentChoices.size();
                        ++j)
               {  choice = (ASDDeciderChoice)state.currentChoices.get(j);
                  if (choice.advanceType == DUMMY)
                     dummyNodes.add(choice.nextNode);
               }
               // dummyNodes is a list of the dummy nodes
               // among the remaining choices.
               state.currentChoices = choices(true, dummyNodes);
            }
            else
               // The permanent advance occurred immediately after
               // the current node.  So non-dummy choices for
               // advancing from the current node have already been
               // tried.  Keep only the remaining dummy alternatives
               // (if any) for advancing from it:
            {  ASDDeciderChoice choice;
               for (int j = state.currentChoices.size()-1;
                        j >= 0; --j)
               {  choice = (ASDDeciderChoice)state.currentChoices.get(j);
                  if (choice.advanceType != DUMMY)
                     state.currentChoices.remove(j);
               }
            }
      ++currentParseStepNumber;
      return true;
   } // end backup

   /**
      Returns a string showing the current utterance string with
      parentheses around each subphrase of more than one non-dummy word.
    */
   public String bracketPhrase()
   {  return bracketPhrase(state.decisionStructure.nextNode());
   }

   /**
      Returns a string with parentheses around each subphrase of
      p with length > 1.
      @param p an ASDPhraseNode in a decision structure
    */
   public String bracketPhrase(ASDPhraseNode p)
   {  if (p.nextNode() == null)
         if (p.subphrase() == null)
            return p.word();
         else
            return bracketPhrase(p.subphrase());
      else
      {  // find non-dummy word(s) in subphrase starting at p
         int nonDummyCount = 0;
         ASDPhraseNode nonDummyNode = null;
         for (ASDPhraseNode node = p; node != null; node = node.nextNode())
         {  if (!node.word().equals(DUMMYWORD))
               if (nonDummyCount == 0)
               {  nonDummyCount = 1;
                  nonDummyNode = node;
               }
               else // more than one non-dummy node
                  nonDummyCount = 2;
         }
         // if there is only one non-dummy word, don't surround
         // it with parentheses
         if (nonDummyCount == 1)
            if (nonDummyNode.subphrase() == null)
               return nonDummyNode.word();
            else
               return bracketPhrase(nonDummyNode.subphrase());
         else // surround the subphrase with parentheses
         {
            StringBuffer buffer = new StringBuffer(100);
            char ch = '(';
            ASDPhraseNode next = p;
            while (next != null)
            {  if (next.subphrase() == null)
               {  String nextWord = next.word();
                  if (!nextWord.equals(DUMMYWORD))
                  {  buffer.append(ch);
                     buffer.append(nextWord);
                  }
               }
               else
               {  buffer.append(ch);
                  buffer.append(bracketPhrase(next.subphrase()));
               }
               ch = ' ';
               next = next.nextNode();
            }
            buffer.append(')');
            return buffer.toString();
         }
      }
   } // end bracketPhrase(p)

   /**
      Computes queue of choices for advancing from current search state.
      Includes advances to dummy nodes if includeDummies is true.
      If dummies is a non-null ArrayList, it includes only advances to
      dummy nodes in that ArrayList.
    */
   ArrayList<ASDDeciderChoice> choices(boolean includeDummies,
                                     ArrayList<ASDGrammarNode> dummies)
   {  boolean initialsIn = false;
      ArrayList<ASDDeciderChoice> result;
      if (state.currentNode == state.decisionStructure)
         // at dummy header node
      {  result = initialsForTypes(state.decisionValues,
                     expectedTypes);
         return result;
      }

      result = new ArrayList<ASDDeciderChoice>(10);
      ASDGrammarNode grammarNode = state.currentNode.instance();
      if (grammarNode == null) // shouldn't happen
      {  System.out.println(
            "*** grammarNode unexpectedly null in ASDDecider choices");
         System.exit(0);
      }
      if (grammarNode.isFinal())
      {  ASDDeciderChoice choice = new ASDDeciderChoice();
         choice.advanceType = FINAL;
         choice.completedType = grammarNode.phraseType();
         result.add(choice);
         return result;
      }

      ASDPhraseNode next = state.currentNode.nextNode();
      ArrayList<String> types = grammarNode.successorTypes();
      ArrayList<ASDGrammarSuccessor> successors = grammarNode.successors();
      if (successors == null) // shouldn't happen
      {  System.out.println(
         "*** successors unexpectedly null for ASD grammar entry "
         + "for word\n'" + grammarNode.word() + "' and instance "
         + grammarNode.instance() );
         System.exit(0);
      }
      ASDGrammarSuccessor successor;
      ASDGrammarNode successorState;
      for (int j = 0; j < successors.size(); ++j)
      {  successor
            = (ASDGrammarSuccessor) successors.get(j);
         if (successor.getWord().equals(DUMMYWORD))
            // dummy successor
         {  if (includeDummies)
            {  successorState = ASDLexicon.lookupInstance(successor);
               boolean includeState = false;
               if (dummies == null) // include all dummy successors
                  includeState = true;
               else  // include dummy successors in the dummy vector
                  for (int k = 0;
                       !includeState && k < dummies.size(); ++k)
                     if (successorState ==
                           (ASDGrammarNode)(dummies.get(k)) )
                        includeState = true;
               if (includeState)
               {  ASDDeciderChoice choice = new ASDDeciderChoice();
                  choice.advanceType = DUMMY;
                  choice.nextNode = successorState;
                  result.add(choice);
               }
            }
         }
         else  // next node in grammar is not a dummy
         {  if (next != null)
              // there is a next node in the decision structure.
              // Include non-dummy successor notes in the grammar
              // that match words in the decisionValues set
            { if (state.decisionValues.contains(successor.getWord()) )
              {  successorState
                     = ASDLexicon.lookupInstance(successor);
                 ASDDeciderChoice choice = new ASDDeciderChoice();
                 choice.advanceType = NONDUMMY;
                 choice.nextNode = successorState;
                 result.add(choice);
              }
            }
         }
      } // end loop through successors

      return result;
   } // end choices

   /**
      Creates a decision set of tokens from the given String.
      @param aString a string to be searched
      @return the resulting set
    */
   public HashSet<String> createDecisionSet(String aString)
   {  HashSet<String> decisionValues = new HashSet<String>(aString.length());
      StringTokenizer tokenizer = new StringTokenizer(aString,
         SPACECHARS, true);
      while(tokenizer.hasMoreTokens())
      {  String token = tokenizer.nextToken().trim();
         if (token.length() > 0)
         {  // not whitespace
            decisionValues.add(token);
         }  // whitespace characters are ignored
      }
      return decisionValues;
   } // end createDecisionSet


   /**
      Tests whether or not a search has been completed successfully.
      @return true if successful, false if not.
    */
   public boolean done()
   {  boolean result = false;
      if (state.currentNode == state.decisionStructure
//            && state.decisionStructure.nextNode().nextNode() == null
         )
         // The search is at the header node.
         // See if the word in the node after the header node
         // is one of the expected phrase types:
      {  int n = expectedTypes.size();
         String word = state.decisionStructure.nextNode().word();
         for (int j = 0; !result && j < n; ++j)
            if (((String)expectedTypes.get(j)).equals(word))
               result = true;
      }
      return result;
   } // end done

   /**
      Finds all initial instances of tokens in the given set of
      decValues that could begin
      a path of one of a specified list of expected decision
      types
      @param decValues the set of tokens whose initial instances are sought
      @param expected a ArrayList of strings that name the
      expected decision types
      @return an ArrayList (possibly empty) of ASDDeciderChoice
      instances, each containing advanceType = INITIAL and
      an initial ASDGrammarNode.
    */
    ArrayList<ASDDeciderChoice> initialsForTypes(HashSet<String> decValues, ArrayList expected)
    {
       ArrayList<ASDDeciderChoice> result
          = new ArrayList<ASDDeciderChoice>(5);
       Iterator it = decValues.iterator();
       while (it.hasNext())
       {
          String token = (String) it.next();
          ArrayList<ASDGrammarNode> tokenEntry = ASDLexicon.lookupWord(token);
          if (tokenEntry != null)
          {
             // An entry for the word has been found in the lexicon
             // and returned as an ArrayList of ASDGrammarNode instances

             ASDGrammarNode wordInstance;
             ASDDeciderChoice choice;
             for (int index = 0; index < tokenEntry.size(); ++index)
             {  wordInstance = (ASDGrammarNode)tokenEntry.get(index);
                if (wordInstance.isInitial())
                {  ArrayList<String> beginsTypes = wordInstance.beginsTypes();
                   if (beginsTypes == null)
                      // instance can begin subphrases of unknown types
                   {  choice = new ASDDeciderChoice();
                      choice.advanceType = INITIAL;
                      choice.nextNode = wordInstance;
                      result.add(choice);
                   }
                   else
                      // instance can begin subphrases of specified types;
                      // see whether any of them match the given expected
                      // types:
                   {  boolean noMatch = true;
                      if (expected == null) // successors of unspecified types
                         noMatch = false;
                      else
                         for (int expectedIndex = 0;
                                noMatch && expectedIndex < expected.size();
                                ++expectedIndex)
                         {  String expectedType
                               = (String)(expected.get(expectedIndex));
                            for (int beginsIndex = 0;
                                 noMatch && beginsIndex < beginsTypes.size();
                                 ++beginsIndex)
                               if ( expectedType.equals(
                                    (String)
                                      (beginsTypes.get(beginsIndex))) )
                                  noMatch = false;  // match found
                         }
                      if (!noMatch)
                         // instance can begin a subphrase of one of the
                         // expected types.
                      {  choice = new ASDDeciderChoice();
                         choice.advanceType = INITIAL;
                         choice.nextNode = wordInstance;
                         result.add(choice);
                      }
                   }
                }
             }
          }
       }

       // return the ArrayList, possibly empty,
       // of initial ASDDeciderChoices found
       return result;
   } // end initialsForTypes

   /**
      Attempts to find next search of current decision structure.
      @return true if successful, false if unsuccessful
    */
   public boolean parse()
   {  String advanceResult; // SUCCEED, NOADVANCE, or QUIT
      while(true)
      {  advanceResult = advance();
         if (advanceResult == QUIT) return false;
         else if (advanceResult == SUCCEED)
         {  if (done()) return true;
         }
         else if (advanceResult == NOADVANCE)
         {  if (!backup())
               return false;
         }
         else  // this shouldn't occur
         {  System.out.println(
               "Invalid result of ASDDecider advance():"
               + advanceResult);
            System.exit(0);
         }
      }
   } // end parse

   /**
      Attempts to find next search of current decision structure
      in a specified maximum number of advance steps.
      @param maxSteps the maximum number of steps permitted
      @return positive number of steps required if successful,
      negative number of steps performed if unsuccessful
    */
   public int parse(int maxSteps)
   {  int steps = 0;
      String advanceResult; // SUCCEED, NOADVANCE, or QUIT
      while(steps < maxSteps)
      {  ++steps;
         advanceResult = advance();
         if (advanceResult == QUIT) return -steps;
         else if (advanceResult == SUCCEED)
         {  if (done()) return steps;
         }
         else if (advanceResult == NOADVANCE)
         {  if (!backup())
               return -steps;
         }
         else  // this shouldn't occur
         {  System.out.println(
               "Invalid result of ASDDecider advance(maxSteps):"
               + advanceResult);
            System.exit(0);
         }
      }
      return -steps;
   } // end parse(maxSteps)


   /**
      Copies the feature-value pairs from the current node value
      (which must be a Map) to the current top level of the phrase
      structure being searched.
    */
   public void raiseFeatures()
   {  if (!(currentNode().value() instanceof Map))
         return;  // current node value is not a Map.
      Set entries = ((Map)currentNode().value()).entrySet();
         // a set of Map.entry objects
      Iterator it = entries.iterator();
      Map.Entry entry;
      while(it.hasNext())
      {  entry = (Map.Entry)it.next();
         set((String)entry.getKey(), entry.getValue());
      }
   }

   /**
      Copies the feature-value pairs from the current node value
      (which must be a Map) to the current top level of the phrase
      structure being searched, provided they are compatible with feature
      values already assigned.  Return SUCCEED if they are, NOADVANCE if they
      are not compatible.  Return QUIT if the current node value is not a
      Map.
    */
   public String raiseFeaturesChecking()
   {  if (!(currentNode().value() instanceof Map))
         return QUIT;  // current node value is not a Map.
      Set entries = ((Map)currentNode().value()).entrySet();
         // a set of Map.entry objects
      Iterator it = entries.iterator();
      Map.Entry entry;
      while(it.hasNext())
      {  entry = (Map.Entry)it.next();
         String feature = (String)entry.getKey();
         Object existingValue = valueOf(feature);
         if (existingValue != null && !existingValue.equals(entry.getValue()))
            // value of feature differs from value to be raised
            return NOADVANCE;
         set((String)entry.getKey(), entry.getValue());
      }
      return SUCCEED;
   }

   /**
      Creates a decision structure, a chain of ASDPhraseNodes,
      each containing one token from the given String, with
      a dummy header node at the beginning of the chain.
      @param aString a string of tokens to be searched
      @return the first (dummy) ASDPhraseNode
    */
   ASDPhraseNode segment(String aString)
   {  ASDPhraseNode decisionStructure = new ASDPhraseNode();
      decisionStructure.setWord("nil");
         // the dummy header node
      ASDPhraseNode lastNode = decisionStructure;
      String lastToken = "";
      StringTokenizer tokenizer = new StringTokenizer(aString,
         SPACECHARS, true);
      while(tokenizer.hasMoreTokens())
      {  String token = tokenizer.nextToken().trim();
         if (token.length() > 0)
         {  // not whitespace
            ASDPhraseNode newNode = new ASDPhraseNode();
            newNode.setWord(token);
            lastNode.setNextNode(newNode);
            lastNode = newNode;

         }  // whitespace characters are ignored
         lastToken = token;
      }
      return decisionStructure;
   } // end segment

   /**
      Evaluates a string from a "semantic action" field in an
      ASDGrammar, by invoking the corresponding method of the
      application object.
      @param action the semantic action string to be evaluated;
       the name of a public method in the application class,
       one with no parameters that returns a String.
      @return the String returned by the method invoked in the
       application class; possibly the NOADVANCE or QUIT string.
    */
   public String semanticAction(String action)
   {  Method m = null;
      try
      {  m = application.getClass().getMethod(action, (Class<?>[])null);
      }
      catch(NoSuchMethodException e)
         {  System.out.println("Error invoking method " + action);
            return null;
         }
      if (m != null)
         try
         {  return (String)m.invoke(application, (Object[])null);
         }
         catch(InvocationTargetException e)
         {  System.out.println(
               "InvocationTargetException invoking method \"" + action + "\"");
            return null;
         }
         catch(IllegalAccessException e)
         {  System.out.println("IllegalAccessException invoking method " + action);
            return null;
         }
      else
         return null;
   } // end semanticAction

  /**
      Evaluates a string from a "semantic value" field in an
      ASDGrammar, as a long integer number, a quoted string, or
      by invoking the method of the application object which
      is named by the semantic value field.
      @param value the semantic value string to be evaluated,
       an integer, a string surrounded by double quotes, or
       the name of a public method in the application class,
       one with no parameters that returns a String.
      @return the Object returned by the method invoked in the
       application class; possibly the NOADVANCE or QUIT string.
    */
   public Object semanticValue(String value)
   {  boolean numericValue = true;
      long integerValue = 0;
      try  // try to interpret the value as an integer number
      {  integerValue = Long.parseLong(value);
      }
      catch(NumberFormatException e)
      {  numericValue = false;
      }
      if (numericValue)
         return new Long(integerValue);

      // See if value has quotes around it; if so, remove the
      // quotes and return the string between them:
      if (value.length() >= 2 && value.charAt(0) == '"'
          && value.charAt(value.length()-1) == '"')
         return value.substring(1, value.length()-1);

      Method m = null;
      try
      {  m = application.getClass().getMethod(value, (Class<?>[])null);
      }
      catch(NoSuchMethodException e)
      {  System.out.println("No such method found: " + value);
         return null;
      }
      if (m != null)
         try
         {  return m.invoke(application, (Object[])null);
         }
         catch(InvocationTargetException e)
         {  System.out.println("InvocationTargetException invoking method \"" + value + "\"");
            return null;
         }
         catch(IllegalAccessException e)
         {  System.out.println("IllegalAccessException invoking method " + value);
            return null;
         }
      else
         return null;
   } // end semanticValue

   /**
      Resets the string which is used to match anything at all
      in a given phrase or decision structure.
    */
   public void setANYTHING(String newValue)
   {  ANYTHING = newValue;
   }

   /**
      Resets the lexical token which is to be recognized as ending
      a quoted string.
    */
   public void setCLOSEQUOTE(String newValue)
   {  CLOSEQUOTE = newValue;
   }

   /**
      Sets the NOADVANCE (== NO) value which may be returned by the
      advance method to a new value preferred by the client.
    */
   public void setNOADVANCE(String newValue)
   {  NOADVANCE = newValue;
      NO = NOADVANCE;
   }

   /**
      Resets the string which is recognized by the ASDDecider as representing
      a "dummy" word in the grammar -- used in the labels on dummy nodes.
    */
   public void setDUMMYWORD(String newValue)
   {  DUMMYWORD = newValue;
   }

   /**
      Resets the string used to stand in for numeric values in
      a grammar lexicon.
    */
   public void setNUMBER(String newValue)
   {  NUMBER = newValue;
   }

   /**
      Resets the lexical token which is to be recognized as beginning
      a quoted string.
    */
   public void setOPENQUOTE(String newValue)
   {  OPENQUOTE = newValue;
   }

   /**
      Sets the QUIT value which may be returned by the advance method
      to a new value preferred by the client.
    */
   public void setQUIT(String newValue)
   {  QUIT = newValue;
   }

   /**
      Resets the string of characters which are to be recognized as
      delimiters for lexical tokens but not as lexical tokens themselves.
      For the initialize method to work properly, these should all be
      Unicode whitespace characters.
    */
   public void setSPACECHARS(String newValue)
   {  SPACECHARS = newValue;
   }

   /**
      Resets the string of characters which are to be recognized as
      individual lexical tokens by the segment method.
    */
   public void setSPECIALCHARS(String newValue)
   {  SPECIALCHARS = newValue;
   }

   /**
      Resets the string used to stand in for quoted strings
      in a grammar lexicon.
    */
   public void setSTRING(String newValue)
   {  STRING = newValue;
   }

   /**
      Sets the SUCCEED value which may be returned by the advance method
      to a new value preferred by the client.
    */
   public void setSUCCEED(String newValue)
   {  SUCCEED = newValue;
   }

   /**
      Resets the string used to stand in for words which are
      not found in a given grammar lexicon.
    */
   public void setUNKNOWN(String newValue)
   {  UNKNOWN = newValue;
   }

   /**
      Displays the current parse tree to the console, with a
      *-> pointer to the current node at the top level
    */
   public void showTree()
   {  state.decisionStructure.showTree(state.currentNode);
   }

   /**
      Attempts to find next search of current decision structure
      in a specified maximum number of advance steps;
      displays the decision structure to System.out after each step.
      @param maxSteps the maximum number of steps permitted
      @return positive number of steps required if successful,
      negative number of steps performed if unsuccessful
    */
   public void stepParse(int maxSteps)
   {  int steps = 0;
      String advanceResult; // SUCCEED, NOADVANCE, or QUIT
      BufferedReader keyboard = new BufferedReader(
         new InputStreamReader(System.in));
      System.out.println("\nInitial decision structure:");
      showTree();
      while(steps < maxSteps)
      {  ++steps;
         System.out.print("\nStep " + steps + ": ");
         advanceResult = advance();
         if (advanceResult == QUIT)
         {  System.out.println("parse quit with structure:");
            showTree();
            return;
         }
         else if (advanceResult == SUCCEED)
         {  System.out.println("parse advanced to:");
            showTree();
            if (done())
            {  System.out.println(
                  "\nSuccessful parse in " + steps + " steps, to:");
               showTree();
               String in = null;
               while(true)
               {  System.out.print(
                    "\nDo you want to try for an alternative parse (y or n)? ");
                  try
                  {  in = keyboard.readLine();
                  }
                  catch(IOException e)
                  {  System.out.println(
                       "IOException occurred in ASDDecider stepParse");
                     System.exit(0);
                  }
                  if (in.equalsIgnoreCase("y"))
                     break;
                  else if (in.equalsIgnoreCase("n"))
                     return;
               } // end while
            }
            else
            {  System.out.print(
                  "\nPress Enter key to advance the parse. ");
               try
               {  String in = keyboard.readLine();
               }
               catch(IOException e)
               {  System.out.println(
                    "IOException occurred in ASDDecider stepParse");
                  System.exit(0);
               }
            }
         }
         else if (advanceResult == NOADVANCE)
         {  if (backup())
            {  System.out.println("parse backed up to:");
               showTree();
            }
            else
            {  System.out.println(
                  "failed parse in " + steps + " steps, left with:");
               showTree();
               return;
            }
         }
         else  // this shouldn't occur
         {  System.out.println(
               "Invalid result of ASDDecider advance(maxSteps):"
               + advanceResult);
            return;
         }
      }
   } // end stepParse(maxSteps)

   /**
       Sets the ASDDecider instance to use a given decision network,
       in the form of an ASDGrammar.
       @param network the network to be used.
       @return true if network is not null, false if network is null.
     */
    public boolean useNetwork(ASDGrammar network)
    {  if (network == null) return false;
       ASDLexicon = network;
       if (!ASDLexicon.nodesWithIncomingEdgesMarked())
          ASDLexicon.markNodesWithIncomingEdges();
       return true;
    } // end useNetwork

   /**
      Sets the ASDDecider instance to use an ASDGrammar loaded from
      a specified file.
      @param fileName the name of the file containing the network.
      @return true if successful, false if the network cannot be
      loaded successfully from the file.
    */
   public boolean useNetwork(String fileName)
   {  try
      {  // Load the network without pixel coordinates, but do
         // mark all nodes with incoming edges:
         ASDLexicon = new ASDGrammar(fileName, false, true);
      }
      catch(IOException e)  // also catches ASDInputExceptions
      {  return false;  // network not successfully loaded
      }
      return true;      // network successfully loaded
   } // end useNetwork

   /**
      The string used to match anything at all in a given phrase
      or decision structure.  By default it is equal to "ANYTHING".
    */
   public String ANYTHING = "ANYTHING";

   /**
      The lexical token which is to be recognized as ending a
      quoted string.  The default is ".
    */
   public String CLOSEQUOTE = "\"";

   /**
      The string recognized as representing a dummy node/instance
      in a grammar.  By default it is equal to ASDGrammar.DUMMYWORD.
    */
   public String DUMMYWORD = ASDGrammar.DUMMYWORD;

   /**
      A possible value returned by the advance() method.
      It indicates that an attempt to advance the parse one step has
      failed, and backup should be performed if possible.
    */
   public String NOADVANCE = "B";

   /**
      Abbreviation for NOADVANCE
    */
   public String NO = NOADVANCE;

   /**
      The string used to stand in for numeric values in
      a grammar lexicon.  By default it is equal to "NUMBER".
    */
   public String NUMBER = "NUMBER";

   /**
      The lexical token which is to be recognized as beginning a
      quoted string.  The default is ".
    */
   public String OPENQUOTE = "\"";

   /**
      A possible value returned by the advance() method.
      It indicates that the parse cannot succeed and should be
      terminated with failure.
    */
   public String QUIT = "Q";

   /**
      Whitespace characters which are to be recognized as delimiters
      for lexical tokens but not as lexical tokens themselves.
      Note: Other Unicode whitespace characters are NOT recognized as
      delimiters but ARE trimmed from the beginnings and ends of
      lexical tokens, if present.
    */
   public String SPACECHARS = " \t\n\r";

   /**
      Characters which are to be recognized as individual lexical
      tokens by the segment method.  The default value is
      ",.;:?!$@#%&*()[]{}+=<>/~\\".
    */
   public String SPECIALCHARS = ",.;:?!$@#%&*()[]{}+=<>/~\\";

   /**
      The string used to stand in for quoted strings in
      a grammar lexicon.  By default it is equal to "STRING".
    */
   public String STRING = "STRING";

   /**
      A possible value returned by the advance() method.
      It indicates that an attempt to advance the parse one step has
      been successful.
    */
   public String SUCCEED = "S";

   /**
      The string used to stand in for lexical tokens which are not
      found in a given grammar lexicon.  By default it is equal
      to "UNKNOWN".
    */
   public static String UNKNOWN = "UNKNOWN";

   /* Constants to indicate types of advance steps: */
   private static final char INITIAL = 'I';
   private static final char FINAL = 'F';
   private static final char DUMMY = 'D';
   private static final char NONDUMMY = 'N';

   private String stringToBeParsed;
   private Stack<ASDDeciderState> backstack;
      // for saving ASDDeciderState instances for backtracking
   private ArrayList<String> expectedTypes;
      // a vector of strings that are phrase type names that
      // can be goals of a parse
   private ASDGrammar ASDLexicon;
      // the grammar/ASDLexicon to be used for parsing
   private int currentParseStepNumber;
      // the number of the current step in a parse
   private ASDDeciderState state;
      // the rest of the state of the current parse
   private ASDSemantics semantics;
      // the object which is to evaluate semantic action
      // and semantic value strings;
      // may be the ASDDecider instance itself.
  // The strict flag doesn't need to be in the parser.  It can
  // be in the pragmatics:

   private Object application;
      // the target for application-specific messages,
      // if semantics is the ASDDecider itself
} // end class ASDDecider

/**
   Instances record the state of a parse, including all
   of the information required to be put on the backup
   stack at points of local ambiguity during a parse,
   for recovery later if the parser backtracks.  Since this
   is intended to be like a C struct, the instance variables
   are made public for efficiency.  Since this is class is
   intended to be used only by the the ASDDecider class, it is
   not declared public.

   @author James A. Mason
   @version 1.03 2000 May 26; Jun 13; 2001 Feb 5; Aug 29
 */
class ASDDeciderState implements Cloneable
{
   public Object clone()
   {  ASDDeciderState result;
      try
      {  result = (ASDDeciderState) super.clone();
         result.features = (HashMap<String, Object>)features.clone();
         result.subphraseStack
            = (ASDDeciderStack)subphraseStack.clone();
      } catch (CloneNotSupportedException e)
      { // This shouldn't happen.
         result = null;
      }
      return result;
   } // end clone

   public ASDPhraseNode decisionStructure;
      // the first (dummy) node in the current decision structure
   public HashSet<String> decisionValues;
      // the set of values on which the decision network search is
      // to be performed
   public ASDPhraseNode currentNode;
      // the current node at top level in the decision structure
   public ArrayList<ASDDeciderChoice> currentChoices;
      // the choices for next advance of the search
   public int beginning;
      // the index of the first node in the current subphrase
      // (origin 1)
   public HashMap<String, Object> features;
      // the feature-value pairs for the current top-level
      // subphrase
   public ASDDeciderStack subphraseStack;
      // stacks the beginning indices for all currently
      // incomplete subphrases
   public ASDPhraseNode nextNodeSubphrase;
      // link to subphrase of next node after current one
      // (if any), to detect possible cases of permanent
      // final advance after the current node
   public char advanceCase;
      // the kind of advance about to be applied after the
      // current search state is put on the backup stack:
      // ASDDecider.INITIAL, .FINAL, .DUMMY, or .NONDUMMY
} // end class ASDDeciderState

/**
   Instances record choices for advancing the search state, as triples
      advanceType: INITIAL, DUMMY, NONDUMMY, FINAL
      nextNode: the next ASDGrammarNode for the decider to advance to,
         if advanceType is INITIAL, DUMMY, or NONDUMMY
      completedType: a String representing the phrase type of the
         completed subphrse, if advanceType is FINAL
 */
class ASDDeciderChoice
{  public char advanceType;
   public ASDGrammarNode nextNode;
   public String completedType;
} // end class ASDDeciderChoice

/**
   Instances are put on an ASDDecider's subphraseStack, to preserve
   information for a subphrase while its parsing is suspended to
   search other subphrases within it.
 */
class ASDDeciderStackFrame implements Cloneable
{  public ASDDeciderStackFrame(int begin, HashMap<String, Object> feat)
   {  beginning = begin;
      features = feat;
   }
   public Object clone()
   {  ASDDeciderStackFrame result;
      try
      {  result = (ASDDeciderStackFrame) super.clone();
         result.beginning = beginning;
         result.features = (HashMap<String, Object>)features.clone();
      } catch (CloneNotSupportedException e)
      { // This shouldn't happen.
         result = null;
      }
      return result;
   } // end clone

   public int beginning;
   public HashMap<String, Object> features;
} // end class ASDDeciderStackFrame

/**
   This class is used by the ASDDeciderStack class below.
 */
class ASDDeciderStackNode implements Cloneable
{  ASDDeciderStackNode(ASDDeciderStackFrame givenContent,
         ASDDeciderStackNode givenLink)
   {  content = givenContent;
      link = givenLink;
   }
   public Object clone()
   {  ASDDeciderStackNode result;
      try
      {  result = (ASDDeciderStackNode) super.clone();
         result.content = (ASDDeciderStackFrame) content.clone();
         if (link != null)
            result.link = (ASDDeciderStackNode) link.clone();
               //all the way down, recursively
         else
            result.link = null;
      } catch (CloneNotSupportedException e)
      { // This shouldn't happen.
         result = null;
      }
      return result;
   } // end clone

   ASDDeciderStackFrame content = null;
   ASDDeciderStackNode link = null;
}  // end class ASDDeciderStackNode

/**
   Instances are stacks which are deeply cloneable, in that
   all of the items in the stack are cloned when the stack is cloned.
 */
class ASDDeciderStack implements Cloneable
{  public Object clone()
   {  ASDDeciderStack result;
      try
      {  result = (ASDDeciderStack) super.clone();
         if (top != null)
            // Cloning the top node clones the others recursively.
            // See class StackNode.
            result.top = (ASDDeciderStackNode) top.clone();
//         else
//            result.top = null;
      } catch (CloneNotSupportedException e)
      { // This shouldn't happen.
         result = null;
      }
      return result;
   } // end clone

   boolean isEmpty()
   {  return top == null;
   }

   Object pop()
   {  Object result = null;
      if (top != null)
      {  result = top.content;
         top = (ASDDeciderStackNode)top.link;
      }
      return result;
   }

   void push(ASDDeciderStackFrame item)
   {  ASDDeciderStackNode newTop
         = new ASDDeciderStackNode(item, top);
      top = newTop;
   }

   ASDDeciderStackNode top = null;
} // end class ASDDeciderStack
