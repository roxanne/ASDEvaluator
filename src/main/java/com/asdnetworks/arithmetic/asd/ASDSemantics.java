/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asdnetworks.arithmetic.asd;

/**
 *
 * @author roxanne
 */


/**
   Interface to an object that implements the semantics used with
   an ASDParser.
   An ASDParser with non-null semantics invokes the semanticAction
   method when it advances into an ASDGrammarNode that has a non-null
   semanticAction field.  It invokes the semanticValue method when it
   completes a subphrase at a final node that has a non-null semanticValue
   field.
   @author James A. Mason
   @version 1.01 2001; 2001 Feb 7; Nov 23
 */
public interface ASDSemantics
{
   /**
      Interprets the parameter string in some application-specific manner.
      @param action the string to be interpreted
      @return a string indicating the result of the action;
       for an ASDParser invoking this method, the appropriate return values are
       ASDParser.FAIL - indicating that the current parse path should fail,
       ASDParser.QUIT - indicating that the current parse should terminate
       entirely, or
       any other string or null, which the ASDParser will interpret as
       successful interpretation of the action string.
    */
   public String semanticAction(String action);

   /**
      Evaluates the parameter string in some application-specific manner
      and returns an Object reference to the corresponding value.
      @param value the string to be interpreted
      @return the value computed for the given string
    */
   public Object semanticValue(String value);
}
