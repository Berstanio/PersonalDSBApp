package de.berstanio.personaldsb;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.berstanio.ghgparser.CoreCourse;
import de.berstanio.ghgparser.GHGParser;
import de.berstanio.personaldsblib.ItemSelectListenerAdapter;

public class ItemSelectedListener implements AdapterView.OnItemSelectedListener {

    private View root;
    private ItemSelectListenerAdapter adapter;


    public ItemSelectedListener(View root, int id, ArrayList<CoreCourse> remaining){
        setRoot(root);
        setAdapter(new ItemSelectListenerAdapter(id, remaining));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ArrayList<CoreCourse> remaining = getAdapter().onItemSelected(((CoreCourse)parent.getItemAtPosition(position)));

        if (!remaining.isEmpty()) {
            TextView textView = getRoot().findViewById(getRoot().getResources().getIdentifier("textView" + getAdapter().getId(),"id", getRoot().getContext().getPackageName()));
            textView.setText("Wähle jetzt deinen nächsten Kurs!");

            Spinner spinner = getRoot().findViewById(getRoot().getResources().getIdentifier("spinner" + getAdapter().getId(),"id", getRoot().getContext().getPackageName()));
            List<CoreCourse> courses = (List<CoreCourse>) remaining.clone();
            ArrayAdapter<CoreCourse> englishAdapter = new ArrayAdapter<>(getRoot().getContext(), R.layout.spinner_center, courses);
            englishAdapter.setDropDownViewResource(R.layout.spinner_center);
            spinner.setAdapter(englishAdapter);
            englishAdapter.add((CoreCourse) parent.getItemAtPosition(parent.getCount() - 1));
            spinner.setSelection(courses.size() - 1);
            spinner.setVisibility(View.VISIBLE);
            spinner.setOnItemSelectedListener(new ItemSelectedListener(getRoot(), getAdapter().getId(), remaining));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public View getRoot() {
        return root;
    }

    public void setRoot(View root) {
        this.root = root;
    }

    public ItemSelectListenerAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(ItemSelectListenerAdapter adapter) {
        this.adapter = adapter;
    }
}
