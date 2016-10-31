/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package board;

/**
 * trieda jedneho pola na hracej ploche, rozdeluje sa na hratelne a okrajove pole
 *
 */
public interface Field {

	public static enum Direction {	

		L, LU, U, RU, R, RD, D, LD	;
	}

        /**
         * prida pole vedla sucasneho pola podla urceneho smeru
         * @param dirs smer v ktorom sa nove pole prida
         * @param field pole ktore sa ma pridat
         */
	void addNextField(Field.Direction dirs, Field field);

        /**
         * vrati disk na danom poli
         * @return objekt disku
         */
	Disk getDisk();

        /**
         * vrati pole vedla daneho pola vzhladom na urceny smer
         * @param dirs smer
         * @return objekt pola
         */
	Field nextField(Field.Direction dirs);

        /**
         * vlozi disk na dane pole, podla pravidiel otoci relevantne disky
         * @param disk pridany disk
         * @return uspesnost akcie
         */
	boolean putDisk(Disk disk);
        
        /**
         * urci ci je mozne na dane pole polozit disk
         * @param disk disk ktory chcem vlozit
         * @return true ak je disk mozne vlozit, inak false
         */
        boolean canPutDisk(Disk disk);
        
        /**
         * umoznuje polozit disk na hraciu plochu bez kontroly a bez otacania ostatnych diskov, vyuziva sa pri undo a load
         * @param disk disk ktory chcem polozit
         */
        void placeDisk(Disk disk);
        
        /**
         * odstrani disk z daneho pola, pouziva sa pri undo
         */
        void removeDisk();

}