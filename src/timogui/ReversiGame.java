/*
 * Implementacia hry Reversi do projetku z IJA 2015/2016
 * @author xtimko00
 */
package timogui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Timer;

import javax.swing.*;
import java.io.*;
import java.util.*;

import board.*;
import game.*;
import java.awt.event.MouseEvent;
import static java.lang.Math.abs;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import static multiplayer.Client.username;
import static java.lang.Math.abs;


/**
 *  Trieda reprezentujuca chod hry a graficke vykreslovanie.
 */
public class ReversiGame implements Runnable, ActionListener{

    private JFrame frame;
    private final int myWIDTH;
    private final int myHEIGHT;

    private final Painter painter;
    private BufferedImage greenBoard;
    private BufferedImage blackDisk;
    private BufferedImage whiteDisk;
    private BufferedImage canPutImg;
    private BufferedImage frozenWhite;
    private BufferedImage frozenBlack;

    private final int lengthOfCell = 50;

    private int position = -1;
    private int size;
    private int xLabel;
    private int yLabel;

    private JLabel scoreLabelNum = new JLabel();
    private int counterW = 0;
    private int counterB = 0;

    private ReversiRules myRules;
    private Board myBoard;
    private Game myGame;
    private Player blackPlayer;
    private Player whitePlayer;


    private boolean ai=true;//ci je povolene ai
    private boolean ai_1;
    private boolean ai_2;
    private int aiMod;  // No AI - 0, Very Easy (ai_1) - 1, Easy (ai_2) - 2

    private long freeze_time=0;
    private boolean frozen_disks=false;

    private boolean freeze_enabled = false;//ci je povoleny freeze
    private boolean first_freeze=true;

    private boolean move_disabled=false;
    private boolean move_happening=false;
    private boolean game_over=false;

    private long unfreeze_length;   //kolko nanosekund kym sa freeznute disky unfreeznu
    private int freeze_length;      //kolko milisekund kym sa freeznu disky
    private int num_frozen;         //pocet freeznutych diskov

    private boolean recent_load=false;

    private boolean amIblack;   // ak som cierny skryju sa mozne tahy bieleho hraca

    /*xormos00*/
    String gameRep;    // reprezentacia hry
    PrintWriter out;   // socket servera
    private int mpHrac = 0;        // urcuje hraca na tahu 1-cierny 0-biely
    static final boolean DEBUG = false;  
    int MPhintColor = 0;
    int MPmyColor = 0;
    int MPsave = 0;
    int MPload = 0;
    String MPloadName;
    boolean MPstop = false;
    /*xormos00*/
    
    
    //multiplayer freeze
    boolean first_move_freeze=true;  //co ide o prvy tah, specialny pripad pre trigger freezu
    int freeze_length_precise=0;
    long unfreeze_length_precise=0;
    String freeze_reprezentation="";
    long freeze_nanotime=0;
    boolean freeze_trigger=false; //has freeze triggered with this move?
    


    /**
     * Konstruktor inicializuje celu dosku, zabezpecuje chod hry a prekresluje dosku.
     * @param size2 pociatocna velkost dosky zadana uzivatelom.
     *      Od nej sa prepocitavaju vsetky velkosti a dlzky okien, tlacitok...
     * @param outServer Socket servera (pre kontakt servera s hrou, nech ju moze ovladat
     */
    public ReversiGame(int size2, int difficulty, boolean freeze_enabled2, int num_frozen, long unfreeze_length, int freeze_length, PrintWriter outServer) {

        /* Ak nebola zadana velkost dosky, implicitna hodnota je 8 */
        if (loadGameGUI.loadGameBool == false && MPload == 0) {
            if (size2 == 6 || size2 == 10 || size2 == 12)
                this.size = size2;
            else
                this.size = 8;
            
            // Priradenie obtiaznosti
            aiMod = difficulty;
            if (aiMod == 1) {
                ai_1 = true;
                ai_2 = false;
            }
            else if (aiMod == 2) {
                ai_1 = false;
                ai_2 = true;
            }
            else { //(aiMod == 0)
                ai=false;
            }
            
            amIblack = true;

            this.freeze_enabled = freeze_enabled2;
            this.num_frozen = num_frozen;
            this.unfreeze_length = unfreeze_length;
            this.freeze_length = freeze_length;
            /*xormos00*/
            this.out = outServer;

            myRules = new ReversiRules(this.size);
            myBoard = new Board(myRules);
            myGame = new Game(myBoard);
            blackPlayer = new Player(false);
            whitePlayer = new Player(true);

            myGame.addPlayer(blackPlayer);
            myGame.addPlayer(whitePlayer);
            myGame.nextPlayer();
            
            /* Ak ide o nacitanie ulozenej hry */
        } else {

//            MPload = 1;
//            MPloadName = loadGameGUI.loadGameString;
//            out.println("LOAD" + MPloadName); 
            load_game(null);
//            MPload = 0;

        }
        
        /* Nacitanie obrazkov hracej dosky a kamenov */
        loadImages();

        /* Vytvorenie a nastavenie vsetkeho pre okno */
        myWIDTH  = this.size *50 + 160;
        myHEIGHT = this.size *50 + 42;

        painter = new Painter();

        /* Okno hry */
        frame = new JFrame();
            frame.setTitle("Reversi");
            frame.setContentPane(painter);
            Container c = frame.getContentPane();
            Dimension d = new Dimension(myWIDTH,myHEIGHT);
            c.setPreferredSize(d);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setResizable(false);
            frame.setVisible(true);
            frame.setLayout(null);


        /* Pridanie tlacitok a napisov score pri hre */
        JButton buttonUndo = new JButton();
        buttonUndo.setFont(new java.awt.Font("Monospaced", 0, 13));
        buttonUndo.setText("Undo");
        buttonUndo.setBounds(this.size*50 +50, 30, 100, 30);

        JButton buttonSave = new JButton();
        buttonSave.setFont(new java.awt.Font("Monospaced", 0, 12));
        buttonSave.setText("Save game");
        buttonSave.setBounds(this.size*50 +50 , this.size*50 - 50, 100, 30);


        JButton buttonClose = new JButton();
        buttonClose.setFont(new java.awt.Font("Monospaced", 0, 13));
        buttonClose.setText("End game");
        buttonClose.setBounds(this.size*50 + 50, this.size*50, 100, 30);

        JLabel scoreLabel = new JLabel();
        scoreLabel.setFont(new java.awt.Font("Monospaced", 0, 15));
        scoreLabel.setText("Score");
        scoreLabel.setBounds(this.size*50 + 75, 80, 100, 30);

        JLabel scoreLabelWB = new JLabel();
        scoreLabelWB.setFont(new java.awt.Font("Monospaced", 0, 15));
        scoreLabelWB.setText("W : B");
        scoreLabelWB.setBounds(this.size*50 + 75, 100, 100, 30);
        
        JLabel playerName = new JLabel();
        playerName.setFont(new java.awt.Font("Monospaced", 0, 15));
        playerName.setText(username);
        playerName.setBounds(this.size*50 + 75, 150, 100, 30);


        if (this.size == 6) {
            xLabel = 27; yLabel = 122;
        } else if (this.size == 8) {
            xLabel = 77; yLabel = 172;
        } else if (this.size == 10) {
            xLabel = 127; yLabel = 222;
        } else if (this.size == 12) {
            xLabel = 177; yLabel = 272;
        }


        final JLabel blackWon = new JLabel();
        blackWon.setFont(new java.awt.Font("Monospaced", 0, 30));
        blackWon.setText("BLACK PLAYER WON");
        blackWon.setBounds(xLabel, yLabel, 289, 48);
        blackWon.setForeground(Color.BLACK);
        blackWon.setBackground(Color.WHITE);
        blackWon.setOpaque(true);


        final JLabel whiteWon = new JLabel();
        whiteWon.setFont(new java.awt.Font("Monospaced", 0, 30));
        whiteWon.setText("WHITE PLAYER WON");
        whiteWon.setBounds(xLabel, yLabel, 288, 48);
        whiteWon.setForeground(Color.WHITE);
        whiteWon.setBackground(Color.BLACK);
        whiteWon.setOpaque(true);


        scoreLabelNum.setFont(new java.awt.Font("Monospaced", 0, 15));
        frame.add(scoreLabelNum);
        frame.add(scoreLabel);
        frame.add(scoreLabelWB);
        frame.add(playerName);   

        /* Jednotlive eventy po kliknuti tlacitok */
        /* UNDO */
        frame.add(buttonUndo);
        buttonUndo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myGame.last_move();
                myGame.last_move();
                painter.repaint();
            }
        });
        /* SAVE GAME */
        frame.add(buttonSave);
        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                /* Okno pre ulozenie hry */
                final JFrame saveFrame = new JFrame();
                Container c = saveFrame.getContentPane();
                Dimension d = new Dimension(230,200);
                c.setPreferredSize(d);
                saveFrame.pack();
                saveFrame.setLocationRelativeTo(null);
                saveFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                saveFrame.setResizable(false);
                saveFrame.setVisible(true);
                saveFrame.setLayout(null);

                JButton save = new JButton();
                save.setFont(new java.awt.Font("Monospaced", 0, 24));
                save.setText("SAVE");
                save.setBounds(65, 100, 100, 41);

                final JTextField chooseFile = new JTextField();
                chooseFile.setFont(new java.awt.Font("Monospaced", 0, 14));
                chooseFile.setBounds(40, 65, 151, 27);

                JLabel saveText = new JLabel();
                saveText.setFont(new java.awt.Font("Monospaced", 0, 14));
                saveText.setText("Enter save name");
                saveText.setBounds(52, 30, 160, 30);

                saveFrame.add(save);
                saveFrame.add(chooseFile);
                saveFrame.add(saveText);

                save.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                    MPsave = 1;
                    save_game(chooseFile.getText());
                    MPsave = 0;
                    saveFrame.dispose();
                    }
                });

                chooseFile.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                    MPsave = 1;                        
                    save_game(chooseFile.getText());
                    MPsave = 0;                    
                    saveFrame.dispose();
                    }
                });

            }
        });
        
        /* END GAME */
        frame.add(buttonClose);
        buttonClose.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });


        /**
         * Aktualizuje obraz a stara sa o cely chod hry.
         */
        Timer gameTimer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                
                
                if (game_stalemate()==true && game_over==false) {
                    
                     if (freeze_enabled==true && frozen_disks==true) {
                       first_freeze=false;
                        for (int col=1;col<=size;col++) {
                            for (int row=1;row<=size;row++) {
                                if (myGame.getBoard().getField(row,col).getDisk()!=null) {
                                    if (myGame.getBoard().getField(row,col).getDisk().isWhite()==false) {
                                        myGame.getBoard().getField(row,col).getDisk().unfreeze();
                                    }
                                }
                            }
                        }
                        frozen_disks=false;
                     } else {
                     game_over=true;
                        if (myGame.who_won()==true) {
                            frame.add(whiteWon);


                        } else {
                            frame.add(blackWon);
                        }
                     }
                }

                if (myGame.currentPlayer().isWhite()==true && recent_load==true) {

                    recent_load=false;

                     Timer timerai = new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {                      
                        if (myGame.moves_available()==false) {
                            myGame.nextPlayer();
                        } else {        
                        if (ai_1==true)
                            myGame.ai_1();
                        else if (ai_2==true)
                            myGame.ai_2();
                        myGame.nextPlayer();
                        move_disabled=false;
                        move_happening=false;
                            }
                    }
                    });
                    timerai.setRepeats(false);
                    timerai.start();
                }


                if (myGame.moves_available()==false && move_happening==false && game_over==false && ai == true) {

                    if (myGame.all_full()==true) {
                        game_over=true;

                        if (myGame.who_won()==true) {
                            frame.add(whiteWon);


                        } else {
                            frame.add(blackWon);                           
                        }
                    }

                    move_happening=true;
                     myGame.nextPlayer();
                     move_disabled=true;
                     
                    /**
                     * Timer pre fiktivne rozmyslanie AI.
                     */
                    Timer timerai = new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {

                    if (myGame.moves_available()==false) {
                        myGame.nextPlayer();
                    } else {
                    if (ai_1==true)
                        myGame.ai_1();
                    else if (ai_2==true)
                        myGame.ai_2();
                    myGame.nextPlayer();
                    move_disabled=false;
                    move_happening=false;
                        }
                    }
                    });
                    timerai.setRepeats(false);
                    timerai.start();

            }

            if (myGame.all_same_color(true) && game_over==false) {
                frame.add(whiteWon);              
                game_over=true;
                }
            if (myGame.all_same_color(false) && game_over==false) {
                frame.add(blackWon);          
                game_over=true;
            }

                   painter.repaint();
            }
        });
//        if (MPstop = false) {
          gameTimer.start();
//        } else {
//          MPstop = false;
//          gameTimer.stop();
//        }
    }

    /**
    * Funkcia pre rendeovanie obrazu.
    * @param g konkretny graficky objekt.
    */
    private void render(Graphics g) {


        Field myField;
        /* Nacitanie dosky. 1,1 pre malicky ramcek na okrajoch */
        g.drawImage(greenBoard, 1, 1, null);

         /* Prejdenie celeho pola Field a vykreslenie kamenov leziacich na doske */
        for (int i = 1; i <= size; i++) {
            for (int j = 1; j <= size; j++) {

                if (myBoard.getField(i,j).getDisk() != null) {

                    if (myBoard.getField(i,j).getDisk().isWhite() == true) {
                        if (myBoard.getField(i,j).getDisk().isFrozen() == true)
                            g.drawImage(frozenWhite, 23 + (i-1) * lengthOfCell, 23 + (j-1) * lengthOfCell, null);
                        else
                            g.drawImage(whiteDisk, 23 + (i-1) * lengthOfCell, 23 + (j-1) * lengthOfCell, null);
                        counterW++;
                    }
                    else if (myBoard.getField(i,j).getDisk().isWhite() == false) {
                        if (myBoard.getField(i,j).getDisk().isFrozen() == true)
                            g.drawImage(frozenBlack, 23 + (i-1) * lengthOfCell, 23 + (j-1) * lengthOfCell, null);
                        else
                            g.drawImage(blackDisk, 23 + (i-1) * lengthOfCell, 23 + (j-1) * lengthOfCell, null);
                        counterB++;
                    }
                }
            if (ai != false) {
                if (amIblack == true) {
                    if (myGame.currentPlayer().isWhite() == false) {
                        myField = myBoard.getField(i, j);
                        if (myGame.currentPlayer().canPutDisk(myField) == true)
                            g.drawImage(canPutImg, 22 + (i-1) * lengthOfCell, 22 + (j-1) * lengthOfCell, null);
                    }
                } else  if (amIblack == false) {
                    if (myGame.currentPlayer().isWhite() == true) {
                        myField = myBoard.getField(i, j);
                        if (myGame.currentPlayer().canPutDisk(myField) == true)
                            g.drawImage(canPutImg, 22 + (i-1) * lengthOfCell, 22 + (j-1) * lengthOfCell, null);
                    }
                }
                
            } else if (MPhintColor == 1) {
                if (amIblack == true) {
                    myField = myBoard.getField(i, j);
                    if (myGame.MPPlayer(MPmyColor).canPutDisk(myField) == true)
                        g.drawImage(canPutImg, 22 + (i-1) * lengthOfCell, 22 + (j-1) * lengthOfCell, null);                    
                } else if (amIblack == false) {
                    myField = myBoard.getField(i, j);
                    if (myGame.MPPlayer(MPmyColor).canPutDisk(myField) == true)
                        g.drawImage(canPutImg, 22 + (i-1) * lengthOfCell, 22 + (j-1) * lengthOfCell, null);                    
                }
            }

            }
        }
        if (counterW >= 10)
            scoreLabelNum.setBounds(this.size*50 + 66, 120, 100, 30);
        else
            scoreLabelNum.setBounds(this.size*50 + 75, 120, 100, 30);

        scoreLabelNum.setText(counterW + " : " + counterB);
                    counterB = 0; counterW = 0;
    }

    /**
     * Nacita vsetky potrebne obrazky.
     * Nacita obrazky hracej dosky a kamenov.
     */
    private void loadImages() {
        try {
            if (size == 6)
                greenBoard = ImageIO.read(getClass().getResourceAsStream("/img/board6x6.png"));
            else if (size == 10)
                greenBoard = ImageIO.read(getClass().getResourceAsStream("/img/board10x10.png"));
            else if (size == 12)
                greenBoard = ImageIO.read(getClass().getResourceAsStream("/img/board12x12.png"));
             else
                greenBoard = ImageIO.read(getClass().getResourceAsStream("/img/board8x8.png"));

            blackDisk = ImageIO.read(getClass().getResourceAsStream("/img/black.png"));
            whiteDisk = ImageIO.read(getClass().getResourceAsStream("/img/white.png"));

            frozenWhite = ImageIO.read(getClass().getResourceAsStream("/img/frozenWhite.png"));
            frozenBlack = ImageIO.read(getClass().getResourceAsStream("/img/frozenBlack.png"));

            canPutImg = ImageIO.read(getClass().getResourceAsStream("/img/canput.png"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
            painter.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }


    /**
     * Zastupca kreslenia, rozsireny o interface MouseListener, cez ktory sa 
     * kontroluje spravnost kliku a spusta sa prekreslenie obrazu.
     */
    private class Painter extends JPanel implements MouseListener {

        public Painter() {
            setFocusable(true);
            requestFocus();
            setBackground(Color.LIGHT_GRAY);
            addMouseListener(this);
        }

        /**
         * Pociatocne vyrenderovanie obrazu.
         * @param g graficky objekt
         */
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            render(g);
        }

        /**
         * Spracovanie kliku a pokyn prekreslenia.
         * @param e event mysi - klik
         */
        @Override
        public void mouseClicked(MouseEvent e) {

            Field myField;

            /* Prepocitavanie suradnic kliku na suradnice hracej dosky*/
            int x = (e.getX() - 22) / lengthOfCell +1;
            int y = (e.getY() - 22) / lengthOfCell +1;

            /* Pomocne suradnice aby sa nedalo kliknut mimo hracich poli */
            int xx = e.getX();
            int yy = e.getY();


            if (xx >= 20 && xx <= size*50+19 && yy >= 20 && yy <= size*50+19) {

            myField = myGame.getBoard().getField(x, y);


            /**
             * Multiplayer
             * Ulozi aktualnu reprezentaciu hry pomocou save_game do premenej gameRep
             * Nasledne ju pomocou socketu out odosle na server, ktory rozhodne,
             * aky hrac je prave na tahu a posle mu danu rep. hry
             */
            if (ai == false) {                   
                if (MPmyColor != mpHrac) {
                    if (myGame.MPPlayer(MPmyColor).canPutDisk(myField) == true) {
                        myGame.MPPlayer(MPmyColor).putDisk(myField);                       

                        if (DEBUG) {
                            System.out.println("Klikam si mpHrac:" + mpHrac + "| a hint je:" + MPhintColor);

                            System.out.println("Hrame za " + myGame.currentPlayer());

                            System.out.println("Ukladam svoju reprezentaciu hru \n");
                        }

                        MPhintColor = 0;

                        if (freeze_enabled==true) {
    
                            if (abs(System.nanoTime()-freeze_nanotime)>(freeze_length_precise*1000000+unfreeze_length_precise)) {//freeze trigger
                                 
                                first_move_freeze=false;
                                freeze_trigger=true;    
                                
                                for (int col=1;col<=size;col++) { //unfreeze everything
                                    for (int row=1;row<=size;row++) {
                                        if (myGame.getBoard().getField(row,col).getDisk()!=null) {
                                                
                                            myGame.getBoard().getField(row,col).getDisk().unfreeze();
                                                
                                        }
                                    }
                                }
                                
                                
                                freeze_length_precise=(int)(Math.random() * ((freeze_length - 0) + 1)); //time until next freeze
                                unfreeze_length_precise=(long)(Math.random() * ((unfreeze_length - 0) + 1)); //time until unfreeze
                                
                                Board test_board=new Board(myRules); //urci kde sa freeznu disky
                                
                                for (int col=1;col<=size;col++) { //hlboka kopia hracej dosky
                                    for (int row=1;row<=size;row++) {
                                        if (myGame.getBoard().getField(row,col).getDisk()!=null) {
                                                
                                            test_board.getField(row,col).placeDisk(myGame.getBoard().getField(row,col).getDisk());                                               
                                        }
                                    }
                                }
                                
                                int freeze_counter=0;

                                while (freeze_counter!=num_frozen && 
                                        (!test_board.all_frozen_board(!amIblack) || !test_board.all_frozen_board(amIblack))) {//freezovanie "na necisto"

                                    int frozen_disk=(int)(Math.random() * ((test_board.number_disks_board(!amIblack)+test_board.number_disks_board(amIblack) - 0) + 1));
                                    int passed_counter=0;

                                    for (int col=1;col<=size;col++) {
                                        for (int row=1;row<=size;row++) {
                                            if (test_board.getField(row,col).getDisk()!=null) {
                                                passed_counter++;
                                                if (passed_counter==frozen_disk) {
                                                    if (test_board.getField(row,col).getDisk().isFrozen()==false){
                                                        test_board.getField(row,col).getDisk().freeze();
                                                        freeze_counter++;
                                                    }
                                                }  
                                            }
                                        }
                                    }
                                }
                                
                                freeze_reprezentation="";
                                
                                for (int col=1;col<=size;col++) { //reprezentacia freeznutych diskov v hracej doske
                                    for (int row=1;row<=size;row++) {
                                        if (test_board.getField(row,col).getDisk()!=null) {
                                            if (test_board.getField(row,col).getDisk().isFrozen()==true) {
                                                freeze_reprezentation+="F"; 
                                            } else {
                                                freeze_reprezentation+="N"; 
                                            }
                                                                                       
                                        } else {
                                           freeze_reprezentation+="N"; 
                                        }
                                    }
                                }
                                
                                Timer timer = new Timer(freeze_length_precise, new ActionListener() { //create freeze counter
                                @Override
                                public void actionPerformed(ActionEvent arg0) { //this will freeze 
                                    
                                    String freeze_string=freeze_reprezentation;

                                    for (int col=1;col<=size;col++) {
                                        for (int row=1;row<=size;row++) {
                                            if ((freeze_string.substring(0, 1).equals("F"))) {
                                                myGame.getBoard().getField(row,col).getDisk().freeze();
                                            }
                                            freeze_string=freeze_string.substring(1);
                                        }
                                    } 
                                }
                                });
                                timer.setRepeats(false);
                                timer.start();
                                
                                freeze_nanotime=System.nanoTime();
                                     
                            } else {
                                freeze_trigger=false;
                            }                                                        
                        }
                        
                        //Ulozenie reprezentacie hry
                        save_game(gameRep);

                        //Odoslanie reprezentacie hry na sevrer, ktory to posle
                        //dalsiemu hracovi
                        out.println("GAME" + mpHrac + gameRep);                               
                    } else {
                        if (DEBUG) { System.out.println("Tu si nekliknes mpHrac:" + mpHrac); }
                    }
                } else {
                    if (DEBUG) { System.out.println("mpHrac:" + mpHrac + "|MPmyColor:" + MPmyColor); }                    
                }
                                                        
            } else if (ai==true) {

                if (move_disabled==false) {


                        if (myGame.currentPlayer().canPutDisk(myField) == true) {
                        myGame.currentPlayer().putDisk(myField);
                        myGame.nextPlayer();

                    recent_load=false;
                    move_disabled=true;
                    
                    if (myGame.moves_available()==true) {

                    Timer timerai = new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {

                        
                    if (myGame.moves_available()==false) {
                        myGame.nextPlayer();
                    } else {
                    if (ai_1==true)
                        myGame.ai_1();
                    else if (ai_2==true)
                        myGame.ai_2();
                    myGame.nextPlayer();
                    move_disabled=false;
                        }
                    }
                    });
                    timerai.setRepeats(false);
                    timerai.start();
                    
                    } else {
                        myGame.nextPlayer();
                        move_disabled=false;
                    }

                    if (freeze_enabled==true) {

                    if ((abs(freeze_time-System.nanoTime())>(long)(Math.random() * ((unfreeze_length - 0) + 1)) && frozen_disks==true) || first_freeze==true) {
                        first_freeze=false;
                        for (int col=1;col<=size;col++) {
                            for (int row=1;row<=size;row++) {
                                if (myGame.getBoard().getField(row,col).getDisk()!=null) {
                                    if (myGame.getBoard().getField(row,col).getDisk().isWhite()==false) {
                                        myGame.getBoard().getField(row,col).getDisk().unfreeze();
                                    }
                                }
                            }
                        }
                        frozen_disks=false;

                        Timer timer = new Timer((int)(Math.random() * ((freeze_length - 0) + 1)), new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {

                            int freeze_counter=0;

                            while (freeze_counter!=num_frozen && !myGame.all_frozen(false)) {

                                int frozen_disk=(int)(Math.random() * ((myGame.number_disks(false) - 0) + 1));
                                int passed_counter=0;

                                for (int col=1;col<=size;col++) {
                                    for (int row=1;row<=size;row++) {
                                        if (myGame.getBoard().getField(row,col).getDisk()!=null) {
                                            if (myGame.getBoard().getField(row,col).getDisk().isWhite()==false) {
                                                passed_counter++;
                                                if (passed_counter==frozen_disk) {
                                                    if (myGame.getBoard().getField(row,col).getDisk().isFrozen()==false){
                                                        myGame.getBoard().getField(row,col).getDisk().freeze();
                                                        freeze_counter++;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            freeze_time=System.nanoTime();
                            frozen_disks=true;
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    }
                    }
                }
                }
            }

            for (int col=1;col<=size;col++) {
                for (int row=1;row<=size;row++) {
                    if (myGame.getBoard().getField(row,col).getDisk()!=null) {
                        if (myGame.getBoard().getField(row,col).getDisk().isWhite()==true) {
                            myGame.getBoard().getField(row,col).getDisk().unfreeze();
                        }
                    }
                }
            }

            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {
        }


    }


/**
 * ulozi hru, takisto vytvori MP protokol
 * @param filename meno suboru do ktoreho hru chceme ulozit
 */
   public void save_game(String filename) {//implementacia save_game

        String save;

        save=""+this.size+"\n";

        //@override
        if (ai != false) {
          save+=myGame.get_counter()+"\n";
        } else {
          save += 1 + "\n";
        }

        /*xormos00*/
        if (MPmyColor == 0) {
            save += 0 + "\n";
            mpHrac = 0;
        } else {
            save += 1 + "\n";
            mpHrac = 1;
        }    
        /*xormos00*/

        if (ai_1==true) {
            save+=1+"\n";
        } else if (ai_2==true) {
            save+=2+"\n";
        } else 
            save +=0+"\n";
        
        
        if (freeze_enabled==false) {
            save+=0+"\n"+"0\n"+"0\n"+"0 0 0 0\n";
        } else if (freeze_enabled==true) {
            save+=1+"\n"+freeze_length/1000+"\n"+unfreeze_length/1000000000L+"\n"+num_frozen+
                    " ";
            if (freeze_trigger==false) {
                 save+="0 0 0\n";
            } else {
                save+="1 "+freeze_length_precise+" "+unfreeze_length_precise+" "+freeze_reprezentation+"\n";
            }
        }

        if (ai != false) {
            for (int i=0;i<=myGame.get_counter()-1;i++) {
                for (int col=1;col<=size;col++) {
                    for (int row=1;row<=size;row++) {
                        if (myGame.get_board(i).getField(row,col).getDisk()==null) {
                            save+="N";
                        } else if (myGame.get_board(i).getField(row,col).getDisk().isWhite()==true) {
                            save+="W";
                        } else if (myGame.get_board(i).getField(row,col).getDisk().isWhite()==false) {
                            save+="B";
                        }
                    }
                }
                save+="\n";
            }
            save+="E\n";
        } else {
            for (int col=1;col<=size;col++) {
                for (int row=1;row<=size;row++) {
                    if (myGame.getBoard().getField(row,col).getDisk()==null) {
                        save+="N";
                    } else if (myGame.getBoard().getField(row,col).getDisk().isWhite()==true) {
                        save+="W";
                    } else if (myGame.getBoard().getField(row,col).getDisk().isWhite()==false) {
                        save+="B";
                    }
                }                  
            }
            save+="\n";
            save+="E";            
        }

        //@override
        if (MPsave == 1 || ai != false) {   
        File file2 = new File("./examples/"+filename);         
          try(  PrintWriter out = new PrintWriter(file2 )  ){
              out.print(save);
              out.close();
              
              // Pridavanie odkazu na save do save suboru
	      String line=filename+"\n";
              // Pridavanie odkazu na save do save suboru
              Files.write(Paths.get("./examples/"+"saves.rvr"), line.getBytes(), 		StandardOpenOption.APPEND);
              
          } catch (Exception e) {
              System.out.println("subor nejde vytvorit");
          }
        } else {
          gameRep = save;
        }

    }

   /**
    * nacita hru, pouziva sa pri MP na reinicializaciu dosky po tahu
    * @param gameRep reprezentacia hry pre MP
    */
   public void load_game(String gameRep) {

            int num_games;
            String load;
            
            try{
                //zatial to vzdy ocakava subor s nazvom save.txt
                if (ai != false) {
                  load = new Scanner(new File("./examples/"+loadGameGUI.loadGameString)).useDelimiter("\\Z").next();
                } else if (gameRep.startsWith("LOAD")) {
                  load = new Scanner(new File("./examples/"+gameRep.substring(4))).useDelimiter("\\Z").next();
                } else {
                  load = gameRep;
                }

                    //System.out.println("LOAD-TimoGUI\n" + load);                      

                if (load.indexOf("\n") == -1) {
                    System.out.println("zly format suboru");
                }
                size=Integer.parseInt(load.substring(0,load.indexOf("\n")));
                load=load.substring(load.indexOf("\n")+1);

                recent_load=true;

                if (load.indexOf("\n") == -1) {
                    System.out.println("zly format suboru2");
                }
                num_games=Integer.parseInt(load.substring(0,load.indexOf("\n")));
                load=load.substring(load.indexOf("\n")+1);

                if (load.indexOf("\n") == -1) {
                    System.out.println("zly format suboru3");
                }

                //@xormos00
                mpHrac = Integer.parseInt(load.substring(0,load.indexOf("\n")));
                load=load.substring(load.indexOf("\n")+1);

                if (load.indexOf("\n") == -1) {
                    System.out.println("Format suboru je zly\nPri nacitani tretieho znaku (typ hraca) som narazil na koniec suboru");
                }
                //@xormos00

                int ai_type=Integer.parseInt(load.substring(0,load.indexOf("\n")));
                if (ai_type==1) {
                    ai_1=true;
                    ai_2=false;
                } else if (ai_type==2) {
                    ai_2=true;
                    ai_1=false;
                } else if (ai_type==0){
                    ai_2=false;
                    ai_1=false;
                }
                load=load.substring(load.indexOf("\n")+1);

                if (ai != false) {
                    amIblack = true; 
                }
                
                if (load.indexOf("\n") == -1) {
                    System.out.println("zly format suboru3");
                }

                int load_freeze=Integer.parseInt(load.substring(0,load.indexOf("\n")));
                if (load_freeze==0) {
                    freeze_enabled=false;
                    load=load.substring(load.indexOf("\n")+1);
                    load=load.substring(load.indexOf("\n")+1);
                    load=load.substring(load.indexOf("\n")+1);
                    load=load.substring(load.indexOf("\n")+1);
                } else if (load_freeze==1) {
                    freeze_enabled=true;
                    load=load.substring(load.indexOf("\n")+1);
                    //System.out.println(load);
                    freeze_length=Integer.parseInt(load.substring(0,load.indexOf("\n")))*1000;
                    load=load.substring(load.indexOf("\n")+1);
                    unfreeze_length=Integer.parseInt(load.substring(0,load.indexOf("\n")))*1000000000L;
                    load=load.substring(load.indexOf("\n")+1);
                    
                    //0 1 4783 2985963541 NFFNNFFNNNNFFNNNFNN
                    
                    num_frozen=Integer.parseInt(load.substring(0,load.indexOf(" ")));
                    load=load.substring(load.indexOf(" ")+1);
                    int trigger=Integer.parseInt(load.substring(0,load.indexOf(" ")));
                    if (trigger==0) freeze_trigger=false;
                    else if (trigger==1) freeze_trigger=true;                  
                    
                    if (freeze_trigger==false) {
                        load=load.substring(load.indexOf("\n")+1); //preskoci riadok
                    } else if (freeze_trigger==true) {
                        load=load.substring(load.indexOf(" ")+1);
                        freeze_length_precise=Integer.parseInt(load.substring(0,load.indexOf(" ")));
                        load=load.substring(load.indexOf(" ")+1);
                        unfreeze_length_precise=Long.parseLong(load.substring(0,load.indexOf(" ")));
                        load=load.substring(load.indexOf(" ")+1);
                        
                        freeze_reprezentation=load.substring(0,load.indexOf("\n")+1);
                        load=load.substring(load.indexOf("\n")+1);
                    }
                }
                
                String freeze_transfer="";
                
                if (myBoard!=null) {
                    for (int col=1;col<=size;col++) { //reprezentacia freeznutych diskov v hracej doske
                        for (int row=1;row<=size;row++) {
                            if (myBoard.getField(row,col).getDisk()!=null) {
                                if (myBoard.getField(row,col).getDisk().isFrozen()==true) {
                                    freeze_transfer+="F"; 
                                } else {
                                    freeze_transfer+="N"; 
                                }
                                                                                       
                            } else {
                                freeze_transfer+="N"; 
                            }
                        }
                    }
                }

                myRules=new ReversiRules(this.size);
                myBoard = new Board(myRules);
                myGame = new Game(myBoard);
                blackPlayer = new Player(false);
                whitePlayer = new Player(true);
                myGame.addPlayer(blackPlayer);
                myGame.addPlayer(whitePlayer);
                if (ai != false) {
                  myGame.nextPlayer();
                }
//                } else {
//                   if (myGame.currentPlayer().equals(blackPlayer)) {
//                        myGame.nextPlayer();
//                   }
//                }


                for (int i=0;i<num_games;i++) {

                    boolean last;

                    if (i==num_games-1) {
                        last=true;
                    } else {
                        last=false;
                    }

                    if (load.indexOf("\n") == -1) {
                    System.out.println("zly format suboru3");
                    }

                    String move=load.substring(0,load.indexOf("\n")+1);

                    load=load.substring(load.indexOf("\n")+1);
                    this.myGame.put_history(move,i,last);
                }
                
                if (ai==false) {
                
                for (int col=1;col<=size;col++) {
                    for (int row=1;row<=size;row++) {
                        if ((freeze_transfer.substring(0, 1).equals("F"))) {
                            myGame.getBoard().getField(row,col).getDisk().freeze();
                        }
                        freeze_transfer=freeze_transfer.substring(1);
                    }
                }
                
                
                if (freeze_trigger==true) {//nastavia sa freezovacie veci
                    
                    freeze_nanotime=System.nanoTime();
                    
                    for (int col=1;col<=size;col++) { //unfreeze everything
                        for (int row=1;row<=size;row++) {
                            if (myGame.getBoard().getField(row,col).getDisk()!=null) {
        
                                myGame.getBoard().getField(row,col).getDisk().unfreeze();
                                                
                            }
                        }
                    }       
                                      
                    
                    Timer timer = new Timer(freeze_length_precise, new ActionListener() { //create freeze counter
                    @Override
                    public void actionPerformed(ActionEvent arg0) { //this will freeze 
                                    
                        String freeze_string=freeze_reprezentation;

                        for (int col=1;col<=size;col++) {
                            for (int row=1;row<=size;row++) {
                                if ((freeze_string.substring(0, 1).equals("F"))) {
                                    myGame.getBoard().getField(row,col).getDisk().freeze();
                                }
                                freeze_string=freeze_string.substring(1);
                            }
                        } 
                    }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    
                }
                }
                
                if (!load.equals("E")) {
                    System.out.println("zly format suboru794");
                }

            } catch (IOException e) {
                System.out.println("Nepodarilo sa otvorit subor");
            }

            //System.out.println("loaded");

            //myGame.nextPlayer();

//            for (int row=1;row<=size;row++) {
//                for (int col=1;col<=size;col++) {
//                    if (myGame.getBoard().getField(row,col).getDisk()==null) {
//                        System.out.print("N");
//                    } else if (myGame.getBoard().getField(row,col).getDisk().isWhite()==true) {
//                        System.out.print("W");
//                    } else if (myGame.getBoard().getField(row,col).getDisk().isWhite()==false) {
//                        System.out.print("B");
//                    }
//                    if (col==size) {
//                        System.out.println(" ");
//                    }
//                }
//            }

            //System.out.println("whet");

   }
   
   /**
    * urci ci existuju v hre nejake tahy pre hociktoreho hraca, ci sa hra nezasekla
    * @return true ak sa hra zasekla, inak false
    */
        public boolean game_stalemate() { //true ak nikto nemoze dat tahy, inak false
        for (int col=1;col<=myBoard.getSize();col++) {
           for (int row=1;row<=myBoard.getSize();row++) {
               Field testField = myBoard.getField(row, col);
               if (blackPlayer.canPutDisk(testField) == true) {
                   return false;
               }
                if (whitePlayer.canPutDisk(testField) == true) {
                   return false;
               }
           }
        }
        return true;
    }
        
        /**
         * Inicializacia hraca pre MP je ina ako pre SP
         * kedze obaja racia spustaju instanciu TimoGUI
         * @param MPPlayerInit identifikacia hraca
         * xormos00
         */
        public void MPPlayerInit(int MPPlayerInit) {
            if (MPPlayerInit == 0) {
                MPmyColor = 0;
                amIblack = false;             
            } else if (MPPlayerInit == 1) {
                MPmyColor = 1;
                amIblack = true;
                MPHint();                 
            } else {
                if (DEBUG) { System.out.println("Chyba pri pridavani farby hrac MPPlayerInit"); }
            }
            if (DEBUG) { System.out.println("Moja farba je MPmycolor:" + MPmyColor); }
        }
        
        /**
         * Riadi nasepkavanie tahu pre MP
         * xormos00
         */
        public void MPHint() {
            if (DEBUG) { System.out.println("Menim hint"); }            
            MPhintColor = 1;
        }
       
}
