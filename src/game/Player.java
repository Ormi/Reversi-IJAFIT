/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;
import board.*;

/**
 *trieda implementujuca hraca
 *
 */
public class Player {
    
    protected boolean isWhite;
    protected int disks;
    
    public Player(boolean isWhite) {
        
        this.isWhite=isWhite;
        this.disks=0;
    }
    
    /**
     * vrati farbu hraca
     * @return true ak biely, false ak cierny
     */
    public boolean isWhite() {
        
        if (isWhite==true) return true;
        else if (isWhite==false) return false;
        
        return false;
    }
    
    /**
     * vrati ci hracovi dosli disky (unused)
     * @return true ak hracovi dosli disky
     */
    public boolean emptyPool() {
        
        if (disks==0) return true;
        else return false;
    }
    
    /**
     * inicializuje hraciu plochu pre daneho hraca, vlozi uvodne disky
     * @param board hracia plocha
     */
    public void init(Board board) {
        
        int size=board.getSize();
        Disk disk1;
        Disk disk2;
        Disk disk3;
        Disk disk4;
     
        this.disks=(size*size)/2-2;
        
        if (this.isWhite==true) {  
            
         disk1=new Disk(true,true); 
         disk2=new Disk(true,true);
         board.getField(size/2, size/2).putDisk(disk1);
         board.getField(size/2+1, size/2+1).putDisk(disk2);
        } else {
            
         disk3=new Disk(false,true); 
         disk4=new Disk(false,true);
         board.getField(size/2, size/2+1).putDisk(disk3);
         board.getField(size/2+1, size/2).putDisk(disk4);
        }
        
    }
    
    /**
     * urci ci moze dany hrac polozit disk na dane pole
     * @param field pole na ktore chce hrac ulozit disk
     * @return true ak je mozne disk polozit, inak false
     */
    public boolean canPutDisk(Field field) {
        
        if (this.emptyPool()==true) {
            return false;
        }
        
        if (isWhite==true) {
            
           Disk disk=new Disk(true,false);
           return field.canPutDisk(disk);
        } else {
           Disk disk=new Disk(false,false);
           return field.canPutDisk(disk); 
            
        }
        
    }
    
    /**
     * ulozi disk daneho hraca na dane pole
     * @param field pole na ktore chce hrac umiestnit disk
     * @return uspesnost akcie
     */
    public boolean putDisk(Field field) {
        
        if (this.canPutDisk(field)==false){
            return false;
        } else {
            this.disks=this.disks-1;
            if (isWhite==true) {
            
                Disk disk=new Disk(true,false);
                return field.putDisk(disk);
            } else {
                Disk disk=new Disk(false,false);
                return field.putDisk(disk); 
            }
        }
    }
    
    public String toString() {
        
        String helpstr;
        
        if (isWhite==true) helpstr="white";
        else helpstr="black";
        
        return helpstr;
    }
    
}
