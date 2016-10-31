/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package board;

/**
 * Trieda reprezentujuca hraciu dosku
 *
 */

public class Board {

	protected int size;
	protected Field[][] field;
        protected Rules rules;

	public Board(Rules rules) {
            
                this.rules=rules;
		this.size=rules.getSize();
		this.field=new Field[size+2][size+2];

		for (int row=0 ; row<=size+1 ; row++) {
			for (int col=0 ; col<=size+1 ; col++) {
				/*if (row==0 || row==size+1 || col==0 || col==size+1){
					this.field[row][col]=new BorderField();
				} else {
					this.field[row][col]=new BoardField(row,col);
				}*/
                            this.field[row][col]=rules.createField(row, col,field);
			}
		}
       

	}

        
        /**
         * vrati objekt hracieho pola na danych suradniciach
         * @param row riadok pola
         * @param col stlpec pola
         * @return pole
         */
	public Field getField(int row, int col) {

		return field[row][col];

	}

        /**
         * vrati velkost hracej plochy
         * @return velkost
         */
	public int getSize() {

		return size;
	}
        /**
         * vrati pravidla podla ktorych bola implementovana hracia doska
         * @return pravidla
         */
        public Rules getRules() {
            
            return this.rules;
        }
        
        /**
         * vrati, ci su vsetky disky danej farby zmrazene
         * @param isWhite farba
         * @return true ak su zmrazene, inak false
         */
    public boolean all_frozen_board(boolean isWhite) {
        
        for (int col=1;col<=getSize();col++) {
            for (int row=1;row<=this.getSize();row++) {
                if (getField(row, col).getDisk()!=null) {
                    if (getField(row, col).getDisk().isWhite()==isWhite) {
                        if (getField(row, col).getDisk().isFrozen()==false) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
        
    /**
     * vrati pocet diskov danej farby na hracej ploche
     * @param isWhite farba
     * @return pocet diskov
     */
    public int number_disks_board(boolean isWhite) {
        
        int counter=0;
        
        for (int col=1;col<=getSize();col++) {
            for (int row=1;row<=getSize();row++) {
                if (getField(row, col).getDisk()!=null) {
                    if (getField(row, col).getDisk().isWhite()==isWhite) {
                        counter++;
                    }
                }
            }
        }
        return counter;
    }

}