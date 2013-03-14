/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asdnetworks.arithmetic.asd;

import java.io.IOException;

    
        
/**
   An ASDInputException is thrown if a syntactic
   error is encountered during input of an ASDGrammar
   from a file.
 */
public class ASDInputException extends IOException
{
   public ASDInputException() {}
   public ASDInputException(String message)
   { super(message);
   }
}
    
    
    
    


