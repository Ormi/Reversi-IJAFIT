/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;
import board.*;


public class ReversiRules implements Rules {
    
    protected int size;
    
    public ReversiRules(int size) {
    
        this.size=size;
    }
    
    public int getSize() {
        
        return this.size;
    }
    
    public int numberDisks() {
        
        return (size*size)/2;
    }
    
    public Field createField(int row,int col, Field[][] field) {
        
        Field newField;
        
        if ((row==0) || (col==0) || (row==size+1) || (col==size+1)) {
        
        newField=new BorderField();
        } else {
            newField=new BoardField(row,col,field);
        }
        
        return newField;
    }
    
}
