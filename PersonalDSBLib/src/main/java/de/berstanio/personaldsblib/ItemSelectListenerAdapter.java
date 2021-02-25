package de.berstanio.personaldsblib;

import java.util.ArrayList;

import de.berstanio.ghgparser.CoreCourse;
import de.berstanio.ghgparser.GHGParser;

/**
 * Eine Art Oberklasse, welche Regeln soll, wie beim Plan eintragen Klicks verarbeitet werden sollen. Sie ist aber nicht weiter relevant
 */
public class ItemSelectListenerAdapter {

    private int id;
    private ArrayList<CoreCourse> remaining;

    public ItemSelectListenerAdapter(int id, ArrayList<CoreCourse> remaining){
        setId(id);
        setRemaining(remaining);
    }

    public ArrayList<CoreCourse> onItemSelected(CoreCourse coreCourse) {
        if (coreCourse.getCourses().get(0).getCourseName().equalsIgnoreCase("Leer")) return new ArrayList<>();
        ArrayList<CoreCourse> selected = new ArrayList<>();
        selected.add(coreCourse);
        GHGParser.remainingCoreCourses(selected, getRemaining());

        if (getId() >= 0 && getId() <= 7){
            setId(getId() + 1);
            return getRemaining();
        }
        return new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<CoreCourse> getRemaining() {
        return remaining;
    }

    public void setRemaining(ArrayList<CoreCourse> remaining) {
        this.remaining = remaining;
    }
}
