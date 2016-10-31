/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;
import board.*;
import java.io.*;

/**
 *trieda implementujuca hru
 * 
 */
public class Game {
    
    protected Board board;
    public Player whitePlayer=null;
    protected Player blackPlayer=null;
    protected boolean currentIsWhite=true;
    protected Board[] board_history;
    protected int move_counter;
    
    public Game(Board board) {
        
        this.board=board;
        this.board_history=new Board[this.board.getSize()*this.board.getSize()*2];
        this.move_counter=0;
    }
    
    /**
     * prida hraca do danej hry, musi byt 1 biely a 1 cierny
     * @param player hrac, ktoreho chceme pridat
     * @return uspesnost akcie
     */
    public boolean addPlayer(Player player) {
        
        boolean isWhite=player.isWhite();
        
        if (isWhite==true) {
            
            if (whitePlayer!=null) {
                return false;
            }
            
            whitePlayer=player;
            whitePlayer.init(board);
            
            return true;
            
        } else {
            
            if (blackPlayer!=null) {
                return false;
            }
            
            blackPlayer=player;
            blackPlayer.init(board);
            
            return true;
        }
    }
    
    /**
     * vrati hraca ktory je momentalne na tahu
     * @return hrac ktory je na tahu
     */
    public Player currentPlayer() {
        
        if (currentIsWhite==true) {
            return whitePlayer;
        } else {
            return blackPlayer;
        }
    }

    /**
     * Vracia hodnotu aktualneho hraca na tahu pre MP, rezia servera
     * Kazdy hrac ma vlastny GUI, currentPlayer() nepouzitelne
     * @param MPPlayer integer hodnota 0-biley hrac, 1-cierny hrac
     * @return hrac na tahu
     * xormos00
     */
    public Player MPPlayer(int MPPlayer) {
        
        if (MPPlayer == 0) {
            return whitePlayer;
        } else {
            return blackPlayer;
        }
    }
    
    /**
     * zmeni hraca ktory je na tahu
     * @return hrac, ktory je od teraz na tahu
     */
    public Player nextPlayer() {
        //System.out.println("eee "+board.getSize());
        this.board_history[move_counter]=new Board(this.board.getRules());
        
        for (int row=1;row<=this.board.getSize();row++) {
           for (int col=1;col<=this.board.getSize();col++) {
               if (this.board.getField(row, col).getDisk()!=null) {
                    this.board_history[move_counter].getField(row, col).placeDisk(this.board.getField(row, col).getDisk());
               }
           }
        }
        move_counter=move_counter+1;
        
        if (currentIsWhite==true) {
            currentIsWhite=false;
        } else if (currentIsWhite==false) {
            currentIsWhite=true;
        }
        
        if (currentIsWhite==true) {
            return whitePlayer;
        } else {
            return blackPlayer;
        }    
        
    }
    
    /**
     * vrati objekt hracej dosky pouzivanej v danej hre
     * @return hracia doska
     */
    public Board getBoard() {
        
        return this.board;
    }
    
    /**
     * vrati herne rozmiestnenie pred poslednym tahom, pouzite pri undo
     */
    public void last_move() {
               
        if (move_counter>1) {
            for (int row=1;row<=this.board.getSize();row++) {
                for (int col=1;col<=this.board.getSize();col++) {
                    if (this.board_history[move_counter-2].getField(row, col).getDisk()!=null) {
                        this.board.getField(row, col).placeDisk(this.board_history[move_counter-2].getField(row, col).getDisk());
                    } else {
                        this.board.getField(row, col).removeDisk();
                    }
                }
            }
            
        this.board_history[move_counter-1]=null;
        move_counter=move_counter-1;
        }
        
        if (move_counter>1) {
            if (currentIsWhite==true) {
                currentIsWhite=false;
            } else if (currentIsWhite==false) {
                currentIsWhite=true;
            }
        }
        
        if (move_counter==1) {
            currentIsWhite=false;
        }
        
    }
    
    /**
     * vrati poradie tahu od zaciatku hry
     * @return poradie tahu
     */
    public int get_counter() {
        return move_counter;
    }
    
    /**
     * vrati herne rozmiestnenie ktore bolo na danom tahu
     * @param i tah, ktoreho rozmiestnenie chceme
     * @return hracia doska
     */
    public Board get_board(int i) {
        return board_history[i];
    }
    
    /**
     * ulozi do historie aktualny tah, vyuzite pri loade
     * @param move textova reprezentacia tahu
     * @param current_move poradie daneho tahu
     * @param last true ak je dany tah posledny, inak false
     */
    public void put_history(String move,int current_move,boolean last) {
        
        Disk white_disk=new Disk(true,false);
        Disk black_disk=new Disk(false,false);
        
        this.board_history[current_move]=new Board(this.board.getRules());
        //System.out.println("alles gute.."+move.substring(0, 1)+"..");
        for (int col=1;col<=this.board.getSize();col++) {
           for (int row=1;row<=this.board.getSize();row++) {
               if (move.substring(0, 1).equals("W")){
                   this.board_history[current_move].getField(row, col).placeDisk(white_disk);
               } else if (move.substring(0, 1).equals("B")){
                   this.board_history[current_move].getField(row, col).placeDisk(black_disk);
               } else if (move.substring(0, 1).equals("N")){
                   ;
               } else {
                   System.out.println("zly format suboru4");
               }
               move=move.substring(1);
           }
        }
        if (!move.equals("\n")) {
            System.out.println("zly format suboru5");
        }  
        /*System.out.println("here it cums "+board.getSize());
        
                                for (int col=1;col<=board.getSize();col++) {
                            for (int row=1;row<=board.getSize();row++) {
                                if (board_history[current_move].getField(row,col).getDisk()==null) {
                                    System.out.print("N");
                                } else if (board_history[current_move].getField(row,col).getDisk().isWhite()==true) {
                                    System.out.print("W");
                                } else if (board_history[current_move].getField(row,col).getDisk().isWhite()==false) {
                                    System.out.print("B");
                                }
                                if (row==board.getSize()) {
                                    System.out.println(" ");
                                }
                            }
                        }*/
                                
                                //System.out.println("ende");
        
        move_counter=current_move+1;
        
        if (last==true) {
            //System.out.println("i am last");
            for (int row=1;row<=this.board.getSize();row++) {
                for (int col=1;col<=this.board.getSize();col++) {
                    if (this.board_history[move_counter-1].getField(row, col).getDisk()!=null) {
                        this.board.getField(row, col).placeDisk(this.board_history[move_counter-1].getField(row, col).getDisk());
                    }
                }
            } 
            //System.out.println("done");
               
            if (current_move%2 == 0) {
                currentIsWhite=false;
            } else currentIsWhite=true;
            
                /*                for (int row=1;row<=this.board.getSize();row++) {
                            for (int col=1;col<=this.board.getSize();col++) {
                                if (getBoard().getField(row,col).getDisk()==null) {
                                    System.out.print("N");
                                } else if (getBoard().getField(row,col).getDisk().isWhite()==true) {
                                    System.out.print("W");
                                } else if (getBoard().getField(row,col).getDisk().isWhite()==false) {
                                    System.out.print("B");
                                }
                                if (col==this.board.getSize()) {
                                    System.out.println(" ");
                                }
                            }
                        }
                      System.out.println("super done");   */       
            
        }
    }
    
    /**
     * urci ci existuju tahy pre sucasneho hraca
     * @return true ak existuje aspon 1 tah, inak false
     */
    public boolean moves_available() { //true ak existue move, inak false
        for (int col=1;col<=this.board.getSize();col++) {
           for (int row=1;row<=this.board.getSize();row++) {
               Field testField = getBoard().getField(row, col);
               if (currentPlayer().canPutDisk(testField) == true) {
                   return true;
               }
           }
        }
        return false;
    }
    
    /**
     * urci ci existuju tahy pre daneho MP hraca
     * @param MPPlayer hrac
     * @return true ak existuje aspon 1 tah, inak false
     */
    public boolean MP_moves_available(int MPPlayer) { //true ak existue move, inak false
        for (int col=1;col<=this.board.getSize();col++) {
           for (int row=1;row<=this.board.getSize();row++) {
               Field testField = getBoard().getField(row, col);
               if (MPPlayer(MPPlayer).canPutDisk(testField) == true) {
                   return true;
               }
           }
        }
        return false;
    }    
    
    /**
     * urci ci moze hociktory tah dat hrac, teda ci sa hra nezasekla
     * @return true ak nikto nemoze dat tahy
     */
     public boolean game_stalemate() { //true ak nikto nemoze dat tahy, inak false
        for (int col=1;col<=this.board.getSize();col++) {
           for (int row=1;row<=this.board.getSize();row++) {
               Field testField = getBoard().getField(row, col);
               if (currentPlayer().canPutDisk(testField) == true) {
                   return true;
               }
           }
        }
        return false;
    }
    
    /**
     * urobenie tahu pre AI 1
     * @return true ak AI dal tah, inak false
     */
    public boolean ai_1() {
        for (int col=1;col<=this.board.getSize();col++) {
           for (int row=1;row<=this.board.getSize();row++) {
               Field aiField = getBoard().getField(row, col);
               if (currentPlayer().canPutDisk(aiField) == true) {
                   currentPlayer().putDisk(aiField);
                   return true;
               }
           }
        }
        return false;
    }
    
    /**
     * urobenie tahu pre AI 2
     * @return true ak AI dal tah, inak false
     */
    public boolean ai_2() {
        for (int col=this.board.getSize();col>=1;col--) {
           for (int row=this.board.getSize();row>=1;row--) {
               Field aiField = getBoard().getField(row, col);
               if (currentPlayer().canPutDisk(aiField) == true) {
                   currentPlayer().putDisk(aiField);
                   return true;
               }
           }
        }
        return false;
    }
    
    /**
     * zisti ci su vsetky disky  na hracej ploche danej farby
     * @param isWhite farba diskov
     * @return true ak vsetky disky su danej farby, inak false
     */
    public boolean all_same_color(boolean isWhite) {
        for (int col=1;col<=this.board.getSize();col++) {
            for (int row=1;row<=this.board.getSize();row++) {
                if (this.getBoard().getField(row, col).getDisk()!=null) {
                    if (this.getBoard().getField(row, col).getDisk().isWhite()!=isWhite) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * vrati pocet diskov danej farby
     * @param isWhite farba
     * @return pocet diskov
     */
    public int number_disks(boolean isWhite) {
        
        int counter=0;
        
        for (int col=1;col<=this.board.getSize();col++) {
            for (int row=1;row<=this.board.getSize();row++) {
                if (this.getBoard().getField(row, col).getDisk()!=null) {
                    if (this.getBoard().getField(row, col).getDisk().isWhite()==isWhite) {
                        counter++;
                    }
                }
            }
        }
        return counter;
    }
    
    /**
     * zisti ci su vsetky disky danej farby zmrazene
     * @param isWhite farba diskov
     * @return true ak su vsetky zmrazene, inak false
     */
    public boolean all_frozen(boolean isWhite) {
        
        for (int col=1;col<=this.board.getSize();col++) {
            for (int row=1;row<=this.board.getSize();row++) {
                if (this.getBoard().getField(row, col).getDisk()!=null) {
                    if (this.getBoard().getField(row, col).getDisk().isWhite()==isWhite) {
                        if (this.getBoard().getField(row, col).getDisk().isFrozen()==false) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * zisti ci je hracia doska zaplnena
     * @return true ak je hracia doska plna, inak false
     */
    public boolean all_full() {
        
        for (int col=1;col<=this.board.getSize();col++) {
            for (int row=1;row<=this.board.getSize();row++) {
                if (this.getBoard().getField(row, col).getDisk()==null) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * urci ktory hrac ma viac diskov na hracej ploche, pouziva sa na konci hry
     * @return true ak vyhral biely, inak false
     */
    public boolean who_won() {
        
        int black_points=0;
        int white_points=0;
        
        for (int col=1;col<=this.board.getSize();col++) {
            for (int row=1;row<=this.board.getSize();row++) {
                if (this.getBoard().getField(row, col).getDisk()!=null) {
                    if (this.getBoard().getField(row, col).getDisk().isWhite()==true) {
                        white_points++;
                    } else if (this.getBoard().getField(row, col).getDisk().isWhite()==false) {
                        black_points++;
                    }
                }
            }
        }
        if (white_points>black_points) {
            return true;
        } else {
            return false;
        }
        
    }
    
    
    
}
