package de.berstanio.personaldsb.ui.loadplan;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.berstanio.ghgparser.CoreCourse;
import de.berstanio.ghgparser.Course;
import de.berstanio.ghgparser.DSBNotLoadableException;
import de.berstanio.ghgparser.DayOfWeek;
import de.berstanio.ghgparser.GHGParser;
import de.berstanio.ghgparser.JahresStundenPlan;
import de.berstanio.ghgparser.User;
import de.berstanio.personaldsb.ItemSelectedListener;
import de.berstanio.personaldsb.MainActivity;
import de.berstanio.personaldsb.R;
import de.berstanio.personaldsblib.PersonalDSBLib;

public class PlanLoadFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_readplan, container, false);



        Spinner year = root.findViewById(R.id.year);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(root.getContext(), R.layout.spinner_center, new String[]{"Wähle deinen Jahrgang!","11","12"});
        yearAdapter.setDropDownViewResource(R.layout.spinner_center);
        year.setAdapter(yearAdapter);
        year.setVisibility(View.VISIBLE);
        year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!yearAdapter.getItem(position).equalsIgnoreCase("Wähle deinen Jahrgang!")){
                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            int year = Integer.parseInt(yearAdapter.getItem(position));


                            JahresStundenPlan jahresStundenPlan;
                            try {
                                 jahresStundenPlan = PersonalDSBLib.getJahresStundenPlan(year);

                                for (int i = 1; i <= 5; i++) {
                                    String s = DayOfWeek.of(i).name().substring(0, 2);
                                    for (int j = 1; j <= 8; j++) {
                                        String id = s + j;
                                        Button button = MainActivity.mainActivity.findViewById(MainActivity.mainActivity.getResources().getIdentifier("button" + id,"id", MainActivity.mainActivity.getPackageName()));
                                        int finalI = i;
                                        int finalJ = j;
                                        button.setOnClickListener(new View.OnClickListener() {
                                            private DayOfWeek day = DayOfWeek.of(finalI);
                                            private int lesson = finalJ;
                                            @Override
                                            public void onClick(View v) {
                                                List<CoreCourse> coreCourseList = jahresStundenPlan.getCoreCourses().stream()
                                                        .filter(coreCourse -> coreCourse.getCourses().stream()
                                                        .anyMatch(course -> course.getDay().equals(day) && course.getLesson() == lesson))
                                                        .collect(Collectors.toList());

                                            }
                                        });
                                    }
                                }
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                e.printStackTrace(pw);
                                Message message = MainActivity.mainActivity.handler.obtainMessage(0, sw.toString());
                                message.sendToTarget();
                                return;
                            }
                            /*ArrayList<CoreCourse> remaining = (ArrayList<CoreCourse>) jahresStundenPlan.getCoreCourses().clone();
                            CoreCourse coreCourseEmpty = new CoreCourse();
                            Course emtpy = new Course();
                            emtpy.setCourseName("Leer");
                            coreCourseEmpty.getCourses().add(emtpy);

                            Spinner german = root.findViewById(R.id.deutschkurse);
                            List<CoreCourse> germanCourses = new ArrayList<>();
                            for (CoreCourse course : jahresStundenPlan.getCoreCourses()) {
                                if (course.toString().contains("LKDE") || course.toString().contains("gkde")) {
                                    germanCourses.add(course);
                                }
                            }
                            ArrayAdapter<CoreCourse> germanAdapter = new ArrayAdapter<>(root.getContext(), R.layout.spinner_center, germanCourses);
                            germanAdapter.setDropDownViewResource(R.layout.spinner_center);
                            germanAdapter.add(coreCourseEmpty);
                            MainActivity.mainActivity.runOnUiThread(() -> {
                                german.setAdapter(germanAdapter);
                                german.setSelection(germanCourses.size() - 1);
                                german.setVisibility(View.VISIBLE);
                            });

                            remaining.removeAll(germanCourses);
                            german.setOnItemSelectedListener(new ItemSelectedListener(root, -2,remaining));

                            Spinner math = root.findViewById(R.id.mathekurse);
                            List<CoreCourse> mathCourses = new ArrayList<>();
                            for (CoreCourse course : jahresStundenPlan.getCoreCourses()) {
                                if (course.toString().contains("LKMA") || course.toString().contains("gkma")) {
                                    mathCourses.add(course);
                                }
                            }
                            ArrayAdapter<CoreCourse> mathAdapter = new ArrayAdapter<>(root.getContext(), R.layout.spinner_center, mathCourses);
                            mathAdapter.add(coreCourseEmpty);
                            mathAdapter.setDropDownViewResource(R.layout.spinner_center);
                            MainActivity.mainActivity.runOnUiThread(() -> {
                                math.setAdapter(mathAdapter);
                                math.setSelection(mathCourses.size() - 1);
                                math.setVisibility(View.VISIBLE);
                            });
                            remaining.removeAll(mathCourses);

                            math.setOnItemSelectedListener(new ItemSelectedListener(root, -1,remaining));

                            Spinner english = root.findViewById(R.id.englishkurse);
                            List<CoreCourse> englishCourses = new ArrayList<>();
                            for (CoreCourse course : jahresStundenPlan.getCoreCourses()) {
                                if (course.toString().contains("LKEN") || course.toString().contains("gken")
                                        || course.toString().contains("LKFR") || course.toString().contains("gkfr")
                                        || course.toString().contains("LKIT") || course.toString().contains("gkit")) {
                                    englishCourses.add(course);
                                }
                            }
                            ArrayAdapter<CoreCourse> englishAdapter = new ArrayAdapter<>(root.getContext(), R.layout.spinner_center, englishCourses);
                            englishAdapter.add(coreCourseEmpty);
                            englishAdapter.setDropDownViewResource(R.layout.spinner_center);
                            MainActivity.mainActivity.runOnUiThread(() -> {
                                english.setAdapter(englishAdapter);
                                english.setSelection(englishCourses.size() - 1);
                                english.setVisibility(View.VISIBLE);
                            });
                            //remaining.removeAll(englishCourses);

                            english.setOnItemSelectedListener(new ItemSelectedListener(root, 0,remaining));
                            Button button = root.findViewById(R.id.createUser);
                            button.setOnClickListener(v -> {
                                ArrayList<CoreCourse> coreCourses = new ArrayList<>();
                                coreCourses.add((CoreCourse) german.getSelectedItem());
                                coreCourses.add((CoreCourse) math.getSelectedItem());
                                coreCourses.add((CoreCourse) english.getSelectedItem());
                                for (int i = 1; i <= 8; i++){
                                    Spinner spinner = MainActivity.mainActivity.findViewById(MainActivity.mainActivity.getResources().getIdentifier("spinner" + i,"id", MainActivity.mainActivity.getPackageName()));
                                    CoreCourse coreCourse = (CoreCourse) spinner.getSelectedItem();
                                    if (coreCourse != null && !coreCourse.getCourses().get(0).getCourseName().equalsIgnoreCase("Leer")) {
                                        coreCourses.add(coreCourse);
                                    }
                                }

                                User user = new User(coreCourses, year);
                                PersonalDSBLib.setUser(user);

                                MainActivity.mainActivity.navController.navigate(R.id.nav_thisweek);
                            });*/
                        }
                    };
                    thread.start();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return root;
    }

    public void onButtonClick(View view){

    }
}