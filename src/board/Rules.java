/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package board;

/**
 *rozhranie implementuje pravidla hry
 *
 */
public interface Rules {
    
    /**
     * vrati velkost hracej dosky
     * @return velkost
     */
    public int getSize();
    
    /**
     * vrati pocet diskov v hre
     * @return pocet diskov
     */
    public int numberDisks();
    
    /**
     * vytvori pole na hracej doske na danych suradniciach, urc jeho typ podla suradnic
     * @param row riadkova suradnica
     * @param col stlpcova suradnica
     * @param field pole
     * @return vytvorene pole
     */
    public Field createField(int row,int col, Field[][] field);
}
