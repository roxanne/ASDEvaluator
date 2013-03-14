
package com.asdnetworks.arithmetic.asd;

import java.util.ArrayList;

/**
 *
 * @author roxanne
 */


/**
   Instances are entries for an ASDGrammar, consisting of
   a word and an ArrayList of instances for that word.
   Visibility is restricted to the asd package.
   @author James A. Mason
   @version 1.02 2001 Oct 10; 2010 Aug
 */
class ASDWordEntry implements Comparable
{
   ASDWordEntry(String wd, ArrayList<ASDGrammarNode> inst)
   {
      word = wd;
      instances = inst;
   }

   public int compareTo(Object other)
   {  ASDWordEntry otherEntry = (ASDWordEntry)other;
      String myWord = word.toLowerCase();
      String otherWord = otherEntry.getWord().toLowerCase();
      return myWord.compareTo(otherWord);
   }

   String getWord() { return word; }

   ArrayList<ASDGrammarNode> getInstances() { return instances; }

   private String word;
   private ArrayList<ASDGrammarNode> instances;
} // end class ASDWordEntry
