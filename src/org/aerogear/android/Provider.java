/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aerogear.android;

/**
 *
 * @author summers
 */
public interface Provider<T> {
    
    /**
     * 
     * Constructs and returns an object of type T
     * 
     * @param in a variable number of parameters to pass to the constructor
     * @return 
     */
    public T get(Object... in );
    
}
