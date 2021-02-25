package de.berstanio.personaldsblib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.ghgparser.GHGParser;
import de.berstanio.ghgparser.JahresStundenPlan;
import de.berstanio.ghgparser.Plan;
import de.berstanio.ghgparser.User;

/**
 * Der Knotenpunkt für die Singleton-User-Implementation mit Server-Anbindung der GHGSEK2DSBParser API
 */
public class PersonalDSBLib {

    private static User user = null;
    //Status, ob das Programm noch beim laden des Users ist
    private static boolean isLoading = true;
    private static boolean useExternServer = true;

    /**
     * Die init Methode, welche alles nötige initalisiert(User etc.)
     * @param rawHTML Ein InputStream, durch welches das HTML mit Platzhaltern geladen werden kann
     * @param baseDir Der Ordner, in dem das Programm seine Daten speichern kann/soll
     * @param useExternServer Ob die Daten vom externen Server geladen werden sollen
     * @throws DSBNotLoadableException Wenn der Jahresstundenplan vom DSB nicht geladen werden kann
     */
    public static void init(InputStream rawHTML, File baseDir, boolean useExternServer) throws DSBNotLoadableException {
        setUseExternServer(useExternServer);
        try {
            if (isUseExternServer()){
                //TestAnfrage, ob der Server erreichbar ist
                Client.sendToServer(-1);
            }
        }catch (Exception e){
            e.printStackTrace();
            //Wenn nicht, wechsle in den lokalen Modus
            setUseExternServer(false);
        }
        if (!isUseExternServer()) {
            try {
                GHGParser.init(rawHTML, baseDir);
            } catch (Exception e) {
                //Wenn es einen Fehler beim init gab schauen, ob der User trotzdem geladen wurde. Meistens wäre ein Fehler dort aber Programmkritisch
                if (GHGParser.getUsers().size() != 0) {
                    user = GHGParser.getUsers().get(0);
                }
                isLoading = false;
                throw e;
            }

        }else {
            //Wenn der Server genutzt wird, trotzdem ein Paar Felder setzen/User laden
            GHGParser.setBasedir(baseDir);
            ArrayList<User> users = User.loadUsers();
            GHGParser.setUsers(users);
        }
        //Singleton-Instanz setzen
        if (GHGParser.getUsers().size() != 0) {
            user = GHGParser.getUsers().get(0);
        }
        isLoading = false;
    }

    /*private boolean hasInetConnection(){
        InetAddress.getByName("light.dsbcontrol.de").isReachable(500);
    }*/

    /**
     * Gibt den Jahresstundenplan für einen Jahrgang zurück
     * @param year Der Jahrgang, zu dem der Jahresstundenplan zurück gegeben werden soll
     * @return Den Jahresstundenplan für den Jahrgang
     * @throws IOException Wenn es Probleme mit der Verbindung zum Server gibt
     * @throws ClassNotFoundException Wenn der Server eine andere Version von der GHGSEK2DSBParser Bibliothek benutzt
     */
    public static JahresStundenPlan getJahresStundenPlan(int year) throws IOException, ClassNotFoundException {
        if (!isUseExternServer()){
           return GHGParser.getJahresStundenPlan(year);
        }else {
            return (JahresStundenPlan) Client.sendToServer(0, year);
        }
    }

    /**
     * Gibt den Jahresstundenplan für den User zurück
     * @return Den Jahresstundenplan für den User
     * @throws IOException Wenn es Probleme mit der Verbindung zum Server gibt
     * @throws ClassNotFoundException Wenn der Server eine andere Version von der GHGSEK2DSBParser Bibliothek benutzt
     */
    public static JahresStundenPlan getJahresStundenPlan() throws IOException, ClassNotFoundException {
        return getJahresStundenPlan(getUser().getYear());
    }

    /**
     * Läd alle lokal gespeicherten Pläne neu
     */
    public static void reloadPlans(){
        if (!isUseExternServer()){
            Arrays.stream(GHGParser.getBasedir().listFiles()).filter(File::isFile).filter(file -> file.getName().contains("plan")).forEach(File::delete);
        }
    }

    /**
     * Die Funktion generiert von einem Profil und einer Woche, einen zugehörigen personalisierten HTML-Plan
     * @param week Die Kalenderwoche als int, für die das HTML erzeugt werden soll
     * @return Das generierte HTML als String
     * @throws DSBNotLoadableException Wenn der Plan für die Woche für den Jahrgang des Users nicht geladen werden kann
     * @throws IOException Wenn es Probleme mit der Verbindung zum Server gibt
     * @throws ClassNotFoundException Wenn der Server eine andere Version von der GHGSEK2DSBParser Bibliothek benutzt
     */
    public static String generateHTMLFile(int week) throws DSBNotLoadableException, IOException, ClassNotFoundException {
        User tmp = getUser();
        if (tmp == null) return GHGParser.getRawHtml();
        if (!isUseExternServer()) {
            return GHGParser.generateHtmlFile(tmp, week);
        }else {
            return (String) Client.sendToServer(week, tmp);
        }
    }


    /**
     * Gibt die Singleton Instance des Users zurück. Die Funktion wartet, falls der User noch von der Festplatte geladen wird
     * @return Die Singleton Instance des Users
     */
    public static User getUser() {
        if (user == null){
            while (isLoading){

            }
        }
        return user;
    }

    /**
     * Setzt die Singleton Instanz des Users
     * @param user Der Singelton User
     */
    public static void setUser(User user) {
        ArrayList<User> tmp = (ArrayList<User>) GHGParser.getUsers().clone();
        tmp.forEach(User::deleteUser);
        GHGParser.getUsers().clear();
        if (user == null) return;

        GHGParser.getUsers().add(user);
        PersonalDSBLib.user = user;
    }

    /**
     * Gibt zurück, ob der externe Server genutzt wird
     * @return Boolean, ob der externe Server genutzt wird
     */
    public static boolean isUseExternServer() {
        return useExternServer;
    }

    /**
     * Setzt, ob der externe Server genutzt werden soll
     * @param useExternServer Boolean, ob der externe Server genutzt werden soll
     */
    public static void setUseExternServer(boolean useExternServer) {
        PersonalDSBLib.useExternServer = useExternServer;
    }
}