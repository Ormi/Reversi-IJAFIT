/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package board;

public class BorderField implements Field {

	public BorderField() {
	}

	public void addNextField(Field.Direction dirs, Field field) {
		return;
	}

	public Field nextField(Field.Direction dirs) {
		return null;
	}

	public boolean putDisk(Disk disk) {
		return false;
	}

	public Disk getDisk() {

		return null;
	}
        
        public boolean canPutDisk(Disk disk) {
            return false;
        }
        
        public void placeDisk(Disk disk) {
            
        }
        
        public void removeDisk(){
            
        }


}