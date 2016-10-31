package multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import timogui.*;

/**
 * Jednoducha klientska aplikacia podporujuca chat a reversi suboj 2 hracov
 * 
 * Client nasleduje protkoly odoslane zo servera a to nasledovne:
 *      SUBMITNAME - klient odpoveda svojim loginom
 *              Server neprestava ziadat login az ho nedostane a je jedinecny
 *      NAMEACCEPTED - server meno akceptoval a dovoluje sa hracovi pripojit do hry
 *      MESSAGE - vsetky znaky nasledujuc tento string budu vytlacene na chat okno
 *                pre vsetkych hracov
 *
 * @author xormos00 | Ormos Michal | xormos00@stud.fit.vutbr.cz
 */
public class Client {

    JFrame frame = new JFrame("Chatter");
    String hostname;
    public static String username;    
    MultiplayerGUI clientGUI;
    BufferedReader in;
    PrintWriter out;
    PrintWriter out2;    
    boolean fight = false;
    String startGame;
    int MPPlayerInit = 0;
    /**
     * Debug pre pomocne vypisovania
     */
    static final boolean DEBUG = false;

    public Client() {  
    }

    /**
     * Ziada zadanie ip adresy na ktory server sa hrac chce pripojit
     */
    private String getServerAddress() { 
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Reversi online lobby",
            JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Ziada a pouzije pozadovany login
     */
    private String getName() {         
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }
    
    /**
     * Po akceptovani mena serverom mu zapnem lobby
     */
    public void turnonGUI () {
        clientGUI.setVisible(true);       
    }

    /**
     * Pripaja sa k serveru, nasledne vstupuje do cyklu kde sa spracuje chovanie
     * podla pozadovanych protkolov, ktore zasle server
     * Protokoly pre vstup do hry a spravu chatu (popisanie nizsie)
     *      SUBMITNAME, NAMEACCEPTED, WELCOME, MESSAGE, NEWPLAYER
     * Protokoly pre spravu lobby (popisanie nizsue)
     *      ERASE, PLAYERINFO, FIGHTACCEPTED, FIGHTREJECTED
     * Protokoly pre hry Reversi (popisane nizsie)
     *      FIGHT, LOAD, VALIDMOVE, OPPONENTMOVE
     */
    public void run() throws IOException {
        hostname = getServerAddress();    
        username = getName();        
        if (DEBUG) { System.out.println("Spustam run! "); }
        if (DEBUG) { System.out.println("Adresa server je " + hostname); }
        Socket socket = new Socket(hostname, 9001);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);  
        
        clientGUI = new MultiplayerGUI(username, out);     
        clientGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);                

        // Vykonava vsetky spravy zo servera, zalezi na protokole
        while (true) {
            if (DEBUG) { System.out.println("While Client"); }
            String line = in.readLine();
            
            // Protokol, ktory poziada hraca o meno a, ze jeho meno bolo
            // akceptovane = je unikatne na servery
            if (line.startsWith("SUBMITNAME")) {
                out.println(username);
            } else if (line.startsWith("NAMEACCEPTED")) {
                if (DEBUG) { System.out.println("Zapni GUI "); }   
                turnonGUI();     
                if (DEBUG) { System.out.println("GUI Zapnute "); }  

            // Protokoly pre privitanie hraca na servery, vypis spravy do chatu
            // a vypisani infromacii o novom prichodzom hracovi
            } else if (line.startsWith("WELCOME")) {
                clientGUI.printTextField(line.substring(7) + "\n");                             
            } else if (line.startsWith("MESSAGE")) {
                clientGUI.printTextField(line.substring(8) + "\n");
            } else if (line.startsWith("NEWPLAYER")) {      
                clientGUI.printTextField(line.substring(9) + "\n"); 
                
            // Protkoly pre refresh informacii o aktualne prihl. hracoch
            } else if (line.startsWith("ERASE")) {
                clientGUI.erasePlayerField();
            } else if (line.startsWith("PLAYERINFO")) {
                clientGUI.printPlayerField(line.substring(10) + "\n");  
                
            // Protkoly ak hrac zadal spravne/nespravne meno supera
            } else if (line.startsWith("FIGHTACCEPTED")) {
                clientGUI.printTextField(line.substring(13) + "\n");    
            } else if (line.startsWith("FIGHTREJECTED")) {
                clientGUI.printTextField(line.substring(13) + "\n"); 
 
            // Protokol pre vytvorenie hry 2 hracov
            // Obom zasle pociatocnu konfiuraciu hry podla hraca ktory vyzival
            // a nasledne im hry spusti
            } else if (line.startsWith("FIGHT")) {
                if (DEBUG) { System.out.println("Client Fight"); }                   
                MPPlayerInit = Integer.parseInt(line.substring(5));
                startGame = in.readLine() + "\n";    
                startGame += in.readLine() + "\n";  
                startGame += in.readLine() + "\n";  
                startGame += in.readLine() + "\n";  
                startGame += in.readLine() + "\n";                  
                if (DEBUG) { System.out.println("MPPlayerInit:" + MPPlayerInit + "|Client Start Game" + startGame); }                  
                clientGUI.startGame(startGame);
                clientGUI.timo.MPPlayerInit(MPPlayerInit);
            // Protokol pre nacitanie ulozenej hry
            // Inicializuje hracu a povie load_game aby cital so suboru
            } else if (line.startsWith("LOAD")) {      
                MPPlayerInit = Integer.parseInt(line.substring(4));                
                clientGUI.startLoadMP(in.readLine()); 
                clientGUI.timo.MPPlayerInit(MPPlayerInit);                  

            // Protokol pre tah 
            // Zasle rep. hry do load_game a napise do chatu "Si na tahu"
            } else if (line.startsWith("VALIDMOVE")) {               
                line = line.substring(9) + "\n";
                line += in.readLine() + "\n"; //boardSize
                line += in.readLine() + "\n";
                line += in.readLine() + "\n";
                line += in.readLine() + "\n";
                line += in.readLine() + "\n";   
                line += in.readLine() + "\n";
                line += in.readLine() + "\n";
                line += in.readLine() + "\n";                
                line += in.readLine() + "\n"; //E             
                clientGUI.eraseChatField();                
                clientGUI.printTextField(in.readLine() + "\n");    
                System.out.println("Nacitavam repzrezantaciu hry \n" + line);              
                clientGUI.loadGame(line);    
                
            // Protokol pre cakanie na tah hraca
            // Zasle rep. hry do load_game a napise do chatu "Cakaj na tah supera"
            } else if (line.startsWith("OPPONENTMOVE")) {  
                line = line.substring(12) + "\n";
                line += in.readLine() + "\n"; //boardSize
                line += in.readLine() + "\n";
                line += in.readLine() + "\n";
                line += in.readLine() + "\n";   
                line += in.readLine() + "\n";
                line += in.readLine() + "\n";
                line += in.readLine() + "\n";
                line += in.readLine() + "\n";                
                line += in.readLine() + "\n"; //E      
                clientGUI.eraseChatField();                
                clientGUI.printTextField(in.readLine() + "\n");   
                clientGUI.MPHint();                  
                clientGUI.loadGame(line);                                
            }        
        }
    }

    /**
     * Spusta clienta ako aplikaciu
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        Client client = new Client();   
        client.run();
    }
}
