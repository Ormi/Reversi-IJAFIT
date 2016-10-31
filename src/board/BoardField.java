/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package board;

import java.util.Arrays;
import java.util.Objects;

public class BoardField implements Field {

	protected int row;
	protected int col;
	protected Disk disk;
        protected Field[][] this_field; 

	public BoardField(int row, int col,Field[][] field) {
		this.row=row;
		this.col=col;
                this.this_field=field;
	}

	public void addNextField(Field.Direction dirs, Field field) {

		if (dirs == Field.Direction.L) {
                        this_field[row][col-1]=field;
		} else if (dirs == Field.Direction.LU) {
                        this_field[row-1][col-1]=field;
		} else if (dirs == Field.Direction.U) {
                        this_field[row-1][col]=field;
		} else if (dirs == Field.Direction.RU) {
                        this_field[row-1][col+1]=field;
		} else if (dirs == Field.Direction.R) {
                        this_field[row][col+1]=field;
		} else if (dirs == Field.Direction.RD) {
                        this_field[row+1][col+1]=field;
		} else if (dirs == Field.Direction.D) {
                        this_field[row+1][col]=field;
		} else if (dirs == Field.Direction.LD) {
                        this_field[row+1][col-1]=field;
		}
	}
        

	public Field nextField(Field.Direction dirs) {

		if (dirs == Field.Direction.L) {
                        return this_field[row][col-1];
		} else if (dirs == Field.Direction.LU) {
                        return this_field[row-1][col-1];
		} else if (dirs == Field.Direction.U) {
                        return this_field[row-1][col];
		} else if (dirs == Field.Direction.RU) {
                        return this_field[row-1][col+1];
		} else if (dirs == Field.Direction.R) {
                        return this_field[row][col+1];
		} else if (dirs == Field.Direction.RD) {
                        return this_field[row+1][col+1];
		} else if (dirs == Field.Direction.D) {
                        return this_field[row+1][col];
		} else if (dirs == Field.Direction.LD) {
                        return this_field[row+1][col-1];
		}
		return null;
	}

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.row;
        hash = 79 * hash + this.col;
        hash = 79 * hash + Objects.hashCode(this.disk);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BoardField other = (BoardField) obj;
        if (this.row != other.row) {
            return false;
        }
        if (this.col != other.col) {
            return false;
        }
        if (!Objects.equals(this.disk, other.disk)) {
            return false;
        }
        return true;
    }


    
    public boolean canPutDisk(Disk disk) {
        
        boolean isWhite=disk.isWhite();
        Field currentFieldDirection;
        Field currentFieldLine;
        
        if (this.getDisk()!=null) { //disk is already here...
            return false;
        }
        
        for (Direction d : Direction.values()) { //check all directions
            
            currentFieldDirection=this.nextField(d);//get field in current direction

            if (currentFieldDirection.getDisk()!=null) { // if disk on field exists

                if (currentFieldDirection.getDisk().isWhite()!=isWhite) { //if it has opposite color...
             
                    currentFieldLine=currentFieldDirection;
                    while ((currentFieldLine.getDisk()!=null) && (currentFieldLine.getDisk().isWhite()!=isWhite)) {
                    //check straight line until end or same color is found
                        currentFieldLine=currentFieldLine.nextField(d);
                        
                    }
                    if (currentFieldLine.getDisk()!=null) {
                        if (currentFieldLine.getDisk().isWhite()==isWhite && currentFieldLine.getDisk().isFrozen() == false) {//if its same, disk can be placed (true)
                            return true;
                        }
                    }
                }
            }
        }
        //if all directions have been checked with no success, false
        return false;
    }
    


    public boolean putDisk(Disk disk) {
        
        boolean isWhite=disk.isWhite();
        Field currentFieldDirection;
        Field currentFieldLine;

        
        if (this.getDisk()!=null) { //this is hopefully unnessecary
            return false;
        }
        
        /*if ((this.row==4 || this.row==5) && (this.col==4) || (this.col==5)) {//initialization...
            
            this.disk=disk;
            return true;   
        }*/
        
        if (disk.isInit()==true) {
            
            this.disk= new Disk(disk.isWhite(),disk.isInit());
            return true;
        }
        
       // System.out.println(this.row+" "+this.col+"...is me");
        
        for (Direction d : Direction.values()) { //check all directions
            
            currentFieldDirection=this.nextField(d);//get field in current direction
            
            if (currentFieldDirection.getDisk()!=null) { // if disk on field exists
                
                if (currentFieldDirection.getDisk().isWhite()!=isWhite) { //if it has opposite color...
                    
                    currentFieldLine=currentFieldDirection;
                    
                    while ((currentFieldLine.getDisk()!=null) && (currentFieldLine.getDisk().isWhite()!=isWhite)) {
                    //check straight line until end or same color is found
                        currentFieldLine=currentFieldLine.nextField(d);
                    }
                    if (currentFieldLine.getDisk()!=null) {
                        if (currentFieldLine.getDisk().isWhite()==isWhite && currentFieldLine.getDisk().isFrozen() == false) {//if its same, disk can be placed (true)
                            
                            while (currentFieldDirection.getDisk().isWhite()!=isWhite){
                                currentFieldDirection.getDisk().turn();//turn disk...
                                currentFieldDirection=currentFieldDirection.nextField(d);//change field until same color
                            }
                        }
                    }
                }
            }
        }
        this.disk= new Disk(disk.isWhite(),disk.isInit());
        return true;
    }

    
	public Disk getDisk() {

		if (disk == null) {
			return null;
		} else {
			return disk;
		}

	}
        
        public void placeDisk(Disk disk) {
            this.disk= new Disk(disk.isWhite(),disk.isInit());
        }
        
        public void removeDisk() {
            this.disk=null;
        }

}