package de.berstanio.personaldsblib;


import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.ghgparser.DayOfWeek;

/**
 * Klasse, welche das FreeRoom-HTML bereitstellt
 */
public class FreeRoomDSB {

    /**
     * Läd das Plan-HTML mit Platzhaltern
     * @param rawHTMLFile Ein InputStream, durch welches das HTML mit Platzhaltern geladen werden kann
     * @return Das Platzhalter-HTML als String
     */
    public static String readHtmlFile(InputStream rawHTMLFile){
        String s = null;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(rawHTMLFile))){
            s = bufferedReader.lines().collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * Generiert aus einer Liste der freien Räume das FreeRoom-HTML
     * @param dayMap Die Liste der freien Räume
     * @param rawHTML Ein InputStream, durch welches das HTML mit Platzhaltern geladen werden kann
     * @return Das fertige FreeRoom-HTML
     */
    public static String generateHTML(HashMap<DayOfWeek, ArrayList<String>> dayMap, InputStream rawHTML){
        String s = readHtmlFile(rawHTML);
        for (Map.Entry<DayOfWeek, ArrayList<String>> entry : dayMap.entrySet()) {
            DayOfWeek dayOfWeek = entry.getKey();
            ArrayList<String> strings = entry.getValue();
            for (int i = 0; i < 5; i++) {
                StringBuilder stringBuilder = new StringBuilder(strings.get(i));
                String identifier = dayOfWeek.name().substring(0, 2) + (i + 1);
                for (int j = 3; j <= stringBuilder.length(); j += 4) {
                    if (j% 3 == 0){
                        if (j == stringBuilder.length()){
                            s = s.replace(identifier + "C", stringBuilder.substring(j-3,j) + "<br>" + identifier + "C");
                        }else {
                            s = s.replace(identifier + "R", stringBuilder.substring(j-3,j) + "<br>" + identifier + "R");
                        }
                    }else if(j % 3 == 1){
                        s = s.replace(identifier + "C", stringBuilder.substring(j-3,j) + "<br>" + identifier + "C");
                    }else {
                        s = s.replace(identifier + "L", stringBuilder.substring(j-3,j) + "<br>" + identifier + "L");
                    }
                }
                s = s.replace("<br>" + identifier + "R", "")
                        .replace("<br>" + identifier + "C", "")
                        .replace("<br>" + identifier + "L", "");
            }
        }
        for (int i = 0; i < 5; i++) {
            DayOfWeek dayOfWeek = DayOfWeek.of(i + 1);
            for (int j = 0; j < 8; j++) {
                String identifier = dayOfWeek.name().substring(0, 2) + (j + 1);
                s = s.replace(identifier + "R", "")
                        .replace(identifier + "C", "")
                        .replace(identifier + "L", "");
            }
        }
        return s;
    }

    /**
     * Läd für einen Raum das zugehörige Plan-HTML runter
     * @param roomNumber Die Raumnummer des Raumes
     * @param token Der Token für den Zugriff auf das DSB
     * @return Das Plan-HTML des Raumes als String
     * @throws DSBNotLoadableException Wenn der Plan nicht geladen werden kann
     */
    public static String download(String roomNumber, String token) throws DSBNotLoadableException {

        Calendar calendar = Calendar.getInstance(Locale.GERMANY);
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);

        String week = weekOfYear + "";
        if (week.length() == 1){
            week = "0" + week;
        }
        try {
            URL connectwat = new URL("https://light.dsbcontrol.de/DSBlightWebsite/Data/a7f2b46b-4d23-446e-8382-404d55c31f90/" + token + "/" + week + "/r/r" + roomNumber + ".htm");
            HttpsURLConnection urlConnection = (HttpsURLConnection) connectwat.openConnection();

            urlConnection.connect();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.ISO_8859_1));
            return bufferedReader.lines().collect(Collectors.joining());
        }catch (IOException e) {
            throw new DSBNotLoadableException(e);
        }

    }

    /**
     * Läd den aktuellen Token für das DSB
     * @return Den Token als String
     * @throws DSBNotLoadableException Wenn der Token nicht geladen werden kann
     */
    public static String getToken() throws DSBNotLoadableException {
        try {
            URL connectwat = new URL("https://mobileapi.dsbcontrol.de/dsbtimetables?authid=a7f2b46b-4d23-446e-8382-404d55c31f90");
            HttpsURLConnection urlConnection = (HttpsURLConnection) connectwat.openConnection();

            urlConnection.connect();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String s = bufferedReader.lines().collect(Collectors.joining());
            JSONArray array = new JSONArray(s);
            JSONObject object = (JSONObject) array.get(0);
            return (String) object.get("Id");
        }catch (IOException e) {
            throw new DSBNotLoadableException(e);
        }
    }

    /**
     * Generiert das FreeRoom-HTML neu und läd alles neu runter
     * @param rawHTML Ein InputStream, durch welches das HTML mit Platzhaltern geladen werden kann
     * @return Das fertige FreeRoom-HTML
     * @throws IOException Wenn es ein Problem mit der Verbindung zum Server gibt
     * @throws ClassNotFoundException Wenn der Server eine andere Version von der GHGSEK2DSBParser Bibliothek benutzt
     * @throws DSBNotLoadableException Wenn notwendige Teile vom DSB nicht geladen werden können
     */
    public static String refresh(InputStream rawHTML) throws ClassNotFoundException, DSBNotLoadableException, IOException {
        if (PersonalDSBLib.isUseExternServer()){
            return (String) Client.sendToServer(-1);
        }
        String token = getToken();
        HashMap<DayOfWeek, ArrayList<String>> allMap = getFreeLessons(download("00027", token));
        for (int i = 28; i <= 46; i++) {
            String dl = download("000" + i, token);
            HashMap<DayOfWeek, ArrayList<String>> map = getFreeLessons(dl);
            for (Map.Entry<DayOfWeek, ArrayList<String>> entry : map.entrySet()) {
                DayOfWeek dayOfWeek = entry.getKey();
                ArrayList<String> strings = entry.getValue();
                ArrayList<String> allStrings = allMap.get(dayOfWeek);
                for (int j = 0; j < strings.size(); j++) {
                    String s = strings.get(j);
                    if (!s.isEmpty()) {
                        if (!allStrings.get(j).isEmpty()) {
                            allStrings.set(j, allStrings.get(j) + ";" + s);
                        } else {
                            allStrings.set(j, s);
                        }
                    }
                }
            }
        }
        return generateHTML(allMap, rawHTML);
    }

    /**
     * Parst von einem RaumPlan-HTML, wann er frei ist
     * @param s Das RaumPlan-HTML als String
     * @return Eine HashMap, welche wiedergibt, wann der Raum frei ist(wenn der String leer ist)
     */
    public static HashMap<DayOfWeek, ArrayList<String>> getFreeLessons(String s){
        HashMap<DayOfWeek, ArrayList<String>> dayListMap = new HashMap<>();
        dayListMap.put(DayOfWeek.MONDAY, new ArrayList<>());
        dayListMap.put(DayOfWeek.TUESDAY, new ArrayList<>());
        dayListMap.put(DayOfWeek.WEDNESDAY, new ArrayList<>());
        dayListMap.put(DayOfWeek.THURSDAY, new ArrayList<>());
        dayListMap.put(DayOfWeek.FRIDAY, new ArrayList<>());

        Document document = Jsoup.parse(s);

        String room = document.getElementsByAttributeValue("color","#0000FF").get(0).text();

        Elements tables = document.getElementsByAttributeValue("rules", "all");
        Element table = tables.get(0);
        Elements columnsLessons =  table.child(0).children();
        for (int i = 1, z = 0; i < columnsLessons.size() - 1; i += 2, z++) {
            Element columnLessons = columnsLessons.get(i);
            Elements days = columnLessons.children();
            days.remove(0);
            for (int j = 0, dayi = 0; j < days.size(); j++, dayi++) {
                Element day = days.get(j);
                DayOfWeek dayEnum = DayOfWeek.of(dayi+1);
                if (dayListMap.get(dayEnum).size() != z){
                    j--;
                    continue;
                }
                dayEnum = DayOfWeek.of(dayi+1);
                String write;
                if (!day.attr("colspan").equals("12")){
                    write = "";
                    j++;
                }else if (day.getElementsByTag("tr").size() <= 1){
                    write = room;
                }else if (day.getElementsByTag("strike").size() != 0){
                    write = room;
                }else {
                    write = "";
                }

                int row = Integer.parseInt(day.attr("rowspan"));
                int count = row / 2;
                for (int k = 0; k < count; k++) {
                    dayListMap.get(dayEnum).add(write);
                }
            }
        }
        return dayListMap;
    }


}
