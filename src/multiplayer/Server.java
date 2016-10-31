package multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

/**
 * Viacvlaknovy chat a game server. Ak sa klient pripoji, server
 * poziada a zadanie mena hraca spravou "SUBMITNAME", a bude ziadat
 * az nebude zadane unikatne meno. Ked klient zada unikatne meno,
 * server mu da vediet "NAMEACCEPTED. Nasledne vsetky spravy od klienta
 * budu vysielane vsetkym ostatnym klientom. ktory zadali unikatne
 * meno. Vysielane spravy maju hlavicku "MESSAGE".
 * 
 * Kedze sa jedna o skolsky projekt dolezite veci boli vynechane
 *      
 *      1. Komunikujeme bez protokolu, takze komunikacia je nechranena
 *         a klient moze poslat spravu pre server aby skoncil
 * 
 *      2. Server by mal podporovat prihlasenia
 * 
 * @author xormos00 | Ormos Michal | xormos00@stud.fit.vutbr.cz
 */
public class Server {

    /**
     * Port na ktorom server pocuva
     */
    private static final int PORT = 9001;

    /**
     * Zoznam vsetkych mien klientov, ktory su v prihlaseny.
     * Pouzivane pre overenie, ze novo registrovane mena este nie su
     * pouzite
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * Zoznam vsetkych objektov pre jednotlivych klientov.
     * Toto nam pomaha posielat spravy pre jednotlivych klientov.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    /**
     * Debug pre pomocne vypisovania
     */
     static final boolean DEBUG = false;
     static int playerHashSize = 20;   
     static String[] playersHash = new String[playerHashSize];          
     
    /**
     * Hlavna metoda, ktora len pocuva na porte a spusta vlakna
     */
    public static void main(String[] args) throws Exception {
        for (int i=0; i < playerHashSize; i++) {
            playersHash[i] = "empty";
        }           
        if (DEBUG) { System.out.println("The chat server is running."); }
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }
    /**
     * Ovladac vlakien. Vlakna su spustene z nacuvacieho cyklu triedy
     * vlakien a su zodpovedne za kontakt s jednotlivymi klientami
     * a posielanim sprav pre jednotlivych klientov
     */
    private static class Handler extends Thread {        
        private String name;
        private Socket socket;
        private BufferedReader in;     
        private PrintWriter out;   
        static String loadPlayers;
        static String playerBlack;
        static String playerWhite;
        static String playerBlackObj;
        static String playerWhiteObj;
        static boolean battle = false;
        static String game;
        static Boolean gameBlack = false;
        static Boolean gameWhite = false;
        static int oponent = 0;
        static boolean click = true;
        String startGame;
        static boolean loadBattle = false;

        /**
         * Konstruktory pre spravu vlakien, uchovavaju socket
         * Vsetke podstatna praca sa deje v metode run
         */
        public Handler(Socket socket) {       
            this.socket = socket;
        }

        /**
         * Obsluhuje vlakno klienta opatovnym ziadanim o zadanie mena
         * az jedno unikatne meno nie je zadane. Tak upozorni ostatnych 
         * klientov cez output stream. Dalej opakovane prijma vstupy
         * a vysiela ich ostatnym klientom
         */
        public void run() {
            try {

                // Vytvara stream pre socket
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                              

                /**
                 * Ziada meno od klienta, az pokial mu nie je zadane
                 * a overuje ci je naozaj unikatne
                 * Dolezite je aby kontrola existencie mena a pridanie
                 * do zoznamu mien musi byt vykonavana ked je zamknuty
                 * zoznam mien
                 */
                while (true) {
                    out.println("SUBMITNAME");
                    if (DEBUG) { System.out.println("Pytam meno"); }                    
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            if (DEBUG) { System.out.println("Meno schvalene vitam hraca"); }                                 
                            out.println("WELCOME" + "[SERVER] Vitaj v hre hrac " + name + "\n");                          
                            break;
                        }
                    }
                }
                
                if (DEBUG) {
                    System.out.println("Hrac " + name + " jeho objekt je " + out);  
                }
                
                //uklada hracov do pola
                for (int i=0; i<playerHashSize; i++) {
                    if (playersHash[i].equals("empty")) {
                        playersHash[i] = name;
                        playersHash[i+1] = out.toString(); 
                        break;
                    }
                }

                if (DEBUG) {
                    for (int i=0; i<19; i++)
                    {
                        System.out.println(playersHash[i]);                                     
                    }
                    System.out.println("========================");                      
                }
                                           

                /**
                 * Ak spravne meno hraca bolo zvolene a pridane do 
                 * zoznamov tak mu daj vediet, ze jeho meno bolo akceptovane
                 * a moze posielat spravy hracom a nasledne ich vyzivat
                 */
                out.println("NAMEACCEPTED");
                writers.add(out);
                
                if (DEBUG) {
                    for (PrintWriter writer : writers) {                    
                        System.out.println("Writers pole " + writer);   
                    }
                }                
                
                for (PrintWriter writer : writers) {
                    writer.println("NEWPLAYER" + "[SERVER] Novy hrac " + name + " sa pripojil do hry");
                } 
                
                for (PrintWriter writer : writers) {
                        writer.println("ERASE ");
                    for (String players : names) {
                            writer.println("PLAYERINFO" + players);
                    }
                }                   

                // Prijma spravy od klientov a zasiela ich dalej
                // Ignoruje klientom, ktory nemoze spravu dorucit
                while (true) {
                    if (DEBUG) { System.out.println("While Server"); }                    
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }              
                    
                    if (DEBUG) { System.out.println("Cakam na spravy"); }                     
                    
                    /**********************************************************/
                    /*                 NACITANIA PROTOKOLOV                   */
                    /**********************************************************/                        
                    /**
                     * V priade, ze jeden z hracov vyberie protivnika za subor
                     * server zaregistruje protokol FIGHT a sprauje hracov
                     */
                    if (input.startsWith("FIGHT")) {
                        //Reset premennych pri novom FIGHTe
                        loadPlayers = "";
                        playerBlack = "";
                        playerWhite = "";
                        playerBlackObj = "";
                        playerWhiteObj = "";
                        battle = false;
                        game = "";
                        gameBlack = false;
                        gameWhite = false;
                        oponent = 0;
                        click = true;
                        startGame = "";
                        loadBattle = false;                                 
                        
                        if (DEBUG) { System.out.println("Zaregistroval som suboj"); }  
                        playerBlack = name;
                        playerWhite = input.substring(5);
                        startGame = in.readLine() + "\n";
                        startGame += in.readLine() + "\n";
                        startGame += in.readLine() + "\n";                 
                        startGame += in.readLine() + "\n";
                        startGame += in.readLine() + "\n";     
                        if (DEBUG) { System.out.println("Mam startGame\n" + startGame); }  
                        if(DEBUG) { System.out.println(playerBlack); }
                        if(DEBUG) { System.out.println(playerWhite); }                        
                        // Kontrola ci objaa hraci ktory idu hrat naozaj existuju
                        for (String player1 : names) {
                            if (playerWhite.equals(player1)) {
                                for (String player2 : names) {
                                    if (playerBlack.equals(player2)) {
                                        //out.println("FIGHTACCEPTED" + "[SERVER] Hraci " + playerBlack + " a " + playerWhite + " idu na suboj!");  
                                        if(DEBUG) { System.out.println("Battle prirad true"); }                                              
                                        battle = true;
                                        oponent = 0;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!battle) {
                            out.println("FIGHTREJECTED" + " Zadal si nespravne meno hraca!" + "'" + playerWhite + "'" + "|'" + playerBlack + "'");  
                        }
                        
                    /**
                     * V pripade uz hraneho suboja server rozhoduje, ktory hrac je na tahu
                     * podla protokolu v save_game ->, ktory hrac tah spravil
                     */
                    }
                    else if (input.startsWith("GAME0")) {
                        oponent = 1;
                    } 
                    else if (input.startsWith("GAME1")) {
                        oponent = 2;
                    }
                    else if (input.startsWith("LOAD")) {
                        if(DEBUG) { System.out.println("Zaregistroval som LOAD" + input); }                         
                        loadPlayers = input.substring(4);
                        for (String player1 : names) {
                            if (loadPlayers.contains(player1)) {
                                playerBlack = player1;
                                for (String player2 : names) {
                                    if (loadPlayers.contains(player2)) {
                                        playerWhite = player2;
                                        //out.println("FIGHTACCEPTED" + "[SERVER] Hraci " + playerBlack + " a " + playerWhite + " idu na suboj!");  
                                        if(DEBUG) { System.out.println("Battle prirad true"); }   
                                        battle = true;
                                        loadBattle = true;
                                        break;
                                    }
                                }
                            }
                        }                        
                        
                    }                    
                    else {
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input); 
                        }
                    }                         
                    
                    /**********************************************************/
                    /*            VYKONANIE FLAGOV PODLA PROTOKOLU            */
                    /**********************************************************/                    
                    /**
                     * Ak bol spraveny tah, tak sa precita protokol hry
                     * Informuje hraca, ktory je na tahu a hraca, ktory ma na svoj tah cakat
                     */
                    if (oponent != 0) {
                        if (DEBUG) { System.out.println("Zaregistroval som prijatie reprezentacie hry\n"); }  
                        game = input.substring(5) + "\n";
                        game += in.readLine() + "\n";
                        game += in.readLine() + "\n";
                        game += in.readLine() + "\n";
                        game += in.readLine() + "\n";     
                        game += in.readLine() + "\n";                             
                        game += in.readLine() + "\n";  
                        game += in.readLine() + "\n";  
                        game += in.readLine() + "\n";  
                        game += in.readLine() + "\n";                         
                        if(DEBUG) { System.out.println("Serverova rep. hry\n" + game); }                    
                        battle = false;     
                        if (oponent == 1) {
                            for (PrintWriter writer : writers) {
                                if (writer.toString().equals(playerBlackObj)) {
                                    if(DEBUG) { System.out.println("Hraje cierny hrac\n" + game); }                                      
                                    writer.println("VALIDMOVE" + game + "[SERVER] Cakaj na svoj tah!\n");                                  
                                }  
                            }
                        } else if (oponent == 2) {
                            for (PrintWriter writer : writers) {
                                if (writer.toString().equals(playerBlackObj)) {
                                    if(DEBUG) { System.out.println("Caka cierny hrac\n" + game); }                                        
                                    writer.println("OPPONENTMOVE" + game + "[SERVER] Je tvoj tah \n");   
                                }  
                            }                        
                        }

                        if (oponent == 2) {
                            for (PrintWriter writer : writers) {
                                if (writer.toString().equals(playerWhiteObj)) {
                                    if(DEBUG) { System.out.println("Hraje biely hrac\n" + game); }                                        
                                    writer.println("VALIDMOVE" + game + "[SERVER] Cakaj na svoj tah!\n");                                  
                                }  
                            }                        
                        } else if (oponent == 1) {
                            for (PrintWriter writer : writers) {
                                if (writer.toString().equals(playerWhiteObj)) {
                                    if(DEBUG) { System.out.println("Caka biely hrac\n" + game); }                                                                            
                                    writer.println("OPPONENTMOVE" + game + "[SERVER] Je tvoj tah \n");  
                                }  
                            }                        
                        }
                        oponent = 0;
                    }                    
                    
                    // Ak sa ide hrat zistit cisla objektov hracov, ktory idu hrat
                    // Hned ako ich zistis tak soknci vyhladavanie, nech hraci nedostanu null
                    // Cisla objektov potrebuje aby vedel ktoremu hracovi ma poslat spravu
                    if (battle == true) {
                        for (int i=0; i<20; i++) {
                            if (playersHash[i].equals(playerBlack)) {
                                playerBlackObj = playersHash[i+1];
                            } else if (playersHash[i].equals(playerWhite)) {
                                playerWhiteObj = playersHash[i+1];
                            }
                            if(playerWhiteObj != null && !playerWhiteObj.isEmpty()) {
                                if(playerBlackObj != null && !playerBlackObj.isEmpty()) {
                                    break;
                                }
                            }
                        }
                        // Testovaci vypis pre mena hracov a ich objekty
                        if (DEBUG) {
                            System.out.println("Hrac c.1 na suboj je " + playerBlack + " a jeho objekt je " + playerBlackObj + "\n");
                            System.out.println("Hrac c.2 na suboj je " + playerWhite + " a jeho objekt je " + playerWhiteObj + "\n"); 
                        }
                        if (loadBattle == false) {
                            for (PrintWriter writer : writers) {
                                if (writer.toString().equals(playerBlackObj)) {
                                    if(DEBUG) { System.out.println("Posielam nastavenai hry hracovi ktory vyzval\n" + startGame); }
                                    writer.println("FIGHT0\n" + startGame );
                                    //writer.println("FIGHT " + "[SERVER]" + " Hrac " + name + " ta vyzval na suboj! Spustam hru.");                                  
                                }                             
                                if (writer.toString().equals(playerWhiteObj)) {
                                    if(DEBUG) { System.out.println("Posielam nastavenai hry hracovi ktory je vyzvany\n" + startGame); }                                
                                    writer.println("FIGHT1\n" + startGame);                                
                                    //writer.println("FIGHT " + "[SERVER]" + " Hrac " + name + " ta vyzval na suboj! Spustam hru.");                                  
                                }                           
                                writer.println("MESSAGE " + "[SERVER] Hrac " + playerBlack + " a hrac " + playerWhite + " idu na Reversi suboj\n");  
                            }
                        } else {
                            for (PrintWriter writer : writers) {
                                if (writer.toString().equals(playerBlackObj)) {
                                    writer.println("LOAD0\n" + loadPlayers);                               
                                }                             
                                if (writer.toString().equals(playerWhiteObj)) {                             
                                    writer.println("LOAD1\n" + loadPlayers);
                                }                           
                            }                            
                        }
                    } else {
                        for (PrintWriter writer : writers) {
                                writer.println("ERASE ");
                            for (String players : names) {
                                    writer.println("PLAYERINFO" + players);
                            }
                        }
                    }
                }                
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // Ak sa klient odpoji tak jeho meno vymazeme z nazvov mien
                // a taktiez z zoznamu mien objektov a zatvorime mu socket
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                for (int i=0; i<playerHashSize; i++) {
                    if (playersHash[i].equals(name)) {
                        playersHash[i] = "empty";
                        playersHash[i+1] = "empty";
                        break;
                    }
                }                  
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }    
}
