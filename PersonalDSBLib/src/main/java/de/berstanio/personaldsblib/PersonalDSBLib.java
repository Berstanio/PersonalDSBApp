package de.berstanio.personaldsblib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.ghgparser.GHGParser;
import de.berstanio.ghgparser.User;

public class PersonalDSBLib {

    private static User user = null;
    private static boolean isLoading = true;

    public static void init(InputStream rawHTML, File baseDir) throws IOException, DSBNotLoadableException {
        try {
            GHGParser.init(rawHTML, baseDir);
        }catch (Exception e){
            isLoading = false;
            throw e;
        }
        if (GHGParser.getUsers().size() != 0){
            user = GHGParser.getUsers().get(0);
        }
        isLoading = false;
    }

    public static String generateHTMLFile(int week) throws DSBNotLoadableException {
        User tmp = getUser();
        if (tmp == null) return GHGParser.getRawHtml();
        return GHGParser.generateHtmlFile(tmp, week);
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
}