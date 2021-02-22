package de.berstanio.personaldsblib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.ghgparser.GHGParser;
import de.berstanio.ghgparser.JahresStundenPlan;
import de.berstanio.ghgparser.Plan;
import de.berstanio.ghgparser.User;

public class PersonalDSBLib {

    private static User user = null;
    private static boolean isLoading = true;
    private static boolean useExternServer = true;

    public static void init(InputStream rawHTML, File baseDir, boolean useExternServer) throws IOException, DSBNotLoadableException {
        setUseExternServer(useExternServer);
        try {
            if (isUseExternServer()){
                Client.sendToServer(-1);
            }
        }catch (Exception e){
            setUseExternServer(false);
        }
        if (!isUseExternServer()) {
            try {
                GHGParser.init(rawHTML, baseDir);
            } catch (Exception e) {
                isLoading = false;
                throw e;
            }

        }else {
            GHGParser.setBasedir(baseDir);
            ArrayList<User> users = User.loadUsers();
            GHGParser.setUsers(users);
        }
        if (GHGParser.getUsers().size() != 0) {
            user = GHGParser.getUsers().get(0);
        }
        isLoading = false;
    }

    public static JahresStundenPlan getJahresStundenPlan(int year) throws IOException, ClassNotFoundException {
        if (!isUseExternServer()){
           return GHGParser.getJahresStundenPlan(year);
        }else {
            return (JahresStundenPlan) Client.sendToServer(0, year);
        }
    }

    public static JahresStundenPlan getJahresStundenPlan() throws IOException, ClassNotFoundException {
        return getJahresStundenPlan(getUser().getYear());
    }

    public static void reloadPlans(){
        if (!isUseExternServer()){
            Arrays.stream(GHGParser.getBasedir().listFiles()).filter(File::isFile).filter(file -> file.getName().contains("plan")).forEach(File::delete);
        }
    }

    public static String generateHTMLFile(int week) throws DSBNotLoadableException, IOException, ClassNotFoundException {
        User tmp = getUser();
        if (tmp == null) return GHGParser.getRawHtml();
        if (!isUseExternServer()) {
            return GHGParser.generateHtmlFile(tmp, week);
        }else {
            return (String) Client.sendToServer(week, tmp);
        }
    }


    public static User getUser() {
        if (user == null){
            while (isLoading){

            }
        }
        return user;
    }

    public static void setUser(User user) {
        ArrayList<User> tmp = (ArrayList<User>) GHGParser.getUsers().clone();
        tmp.forEach(User::deleteUser);
        GHGParser.getUsers().clear();
        if (user == null) return;

        GHGParser.getUsers().add(user);
        PersonalDSBLib.user = user;
    }

    public static boolean isUseExternServer() {
        return useExternServer;
    }

    public static void setUseExternServer(boolean useExternServer) {
        PersonalDSBLib.useExternServer = useExternServer;
    }
}