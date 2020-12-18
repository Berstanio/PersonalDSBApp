package de.berstanio.personaldsb.ui.loadplan;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

                            int color = 0;
                            int nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                            switch (nightModeFlags) {
                                case Configuration.UI_MODE_NIGHT_YES:

                                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                                    color = Color.WHITE;
                                    break;

                                case Configuration.UI_MODE_NIGHT_NO:
                                    color = Color.BLACK;
                                    break;
                            }

                            int year = Integer.parseInt(yearAdapter.getItem(position));


                            JahresStundenPlan jahresStundenPlan;
                            try {
                                 jahresStundenPlan = PersonalDSBLib.getJahresStundenPlan(year);

                                for (int i = 1; i <= 5; i++) {
                                    String s = DayOfWeek.of(i).name().substring(0, 2);
                                    for (int j = 1; j <= 8; j++) {
                                        String id = s + j;
                                        Button button = MainActivity.mainActivity.findViewById(MainActivity.mainActivity.getResources().getIdentifier("button" + id,"id", MainActivity.mainActivity.getPackageName()));
                                        CoreCourse coreCourseTmp = null;

                                        int finalI = i;
                                        int finalJ = j;
                                        int finalColor = color;

                                        if (PersonalDSBLib.getUser() != null && PersonalDSBLib.getUser().getYear() == year){
                                            List<CoreCourse> coreCourseList = PersonalDSBLib.getUser().getCoreCourses().stream()
                                                    .filter(coreCourse -> coreCourse.getCourses().stream()
                                                            .anyMatch(course -> course.getDay().equals(DayOfWeek.of(finalI)) && course.getLesson() == finalJ))
                                                    .collect(Collectors.toList());
                                            if (coreCourseList.size() != 0){
                                                coreCourseTmp = coreCourseList.get(0);
                                            }
                                        }


                                        CoreCourse finalCoreCourseTmp = coreCourseTmp;
                                        MainActivity.mainActivity.runOnUiThread(() -> {
                                            if (finalCoreCourseTmp == null) {
                                                button.setTextSize(10);
                                                button.setTextColor(Color.TRANSPARENT);
                                                button.setText("LHGW12 MAFF");
                                            }else {
                                                String string = finalCoreCourseTmp.getCourseName() + " " + finalCoreCourseTmp.getTeacher();
                                                if (string.contains("/")){
                                                    button.setTextSize(9);
                                                }else {
                                                    button.setTextSize(10);
                                                }
                                                button.setTextColor(finalColor);
                                                button.setText(string);
                                            }
                                        });

                                        button.setOnClickListener(new View.OnClickListener() {
                                            private DayOfWeek day = DayOfWeek.of(finalI);
                                            private int lesson = finalJ;
                                            private CoreCourse coreCourse = finalCoreCourseTmp;
                                            @Override
                                            public void onClick(View v) {
                                                List<CoreCourse> coreCourseList = jahresStundenPlan.getCoreCourses().stream()
                                                        .filter(coreCourse -> coreCourse.getCourses().stream()
                                                        .anyMatch(course -> course.getDay().equals(day) && course.getLesson() == lesson))
                                                        .collect(Collectors.toList());
                                                PopupMenu popupMenu = new PopupMenu(MainActivity.mainActivity, v);
                                                coreCourseList.forEach(coreCourse -> popupMenu.getMenu().add(coreCourse.getCourseName() + " " + coreCourse.getTeacher()));
                                                if (!button.getText().toString().equalsIgnoreCase("LHGW12 MAFF")){
                                                    popupMenu.getMenu().add("FREI");
                                                }
                                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                                    @Override
                                                    public boolean onMenuItemClick(MenuItem item) {
                                                        if (item.getTitle().toString().equalsIgnoreCase("FREI")){
                                                            coreCourse.getCourses().forEach(course -> {
                                                                        String s = course.getDay().name().substring(0, 2) + course.getLesson();
                                                                        Button button = MainActivity.mainActivity.findViewById(MainActivity.mainActivity.getResources().getIdentifier("button" + s, "id", MainActivity.mainActivity.getPackageName()));
                                                                        button.setTextColor(Color.TRANSPARENT);
                                                                        button.setText("LHGW12 MAFF");
                                                                    });
                                                            return true;
                                                        }
                                                        String[] menuItem = item.getTitle().toString().split(" ");
                                                        CoreCourse clicked = coreCourseList.stream().filter(coreCourse -> coreCourse.getTeacher().equalsIgnoreCase(menuItem[1])
                                                                        && coreCourse.getCourseName().equalsIgnoreCase(menuItem[0])).findAny().get();
                                                        coreCourse = clicked;
                                                        clicked.getCourses().forEach(course -> {
                                                            String s = course.getDay().name().substring(0, 2) + course.getLesson();
                                                            Button button = MainActivity.mainActivity.findViewById(MainActivity.mainActivity.getResources().getIdentifier("button" + s,"id", MainActivity.mainActivity.getPackageName()));

                                                            button.setText(item.getTitle());
                                                            if (menuItem[1].contains("/")){
                                                                button.setTextSize(9);
                                                            }else {
                                                                button.setTextSize(10);
                                                            }
                                                            button.setTextColor(finalColor);
                                                        });
                                                        return true;
                                                    }
                                                });
                                                popupMenu.show();
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
                            Button button = root.findViewById(R.id.createUser);
                            button.setOnClickListener(v -> {
                                ArrayList<CoreCourse> coreCourses = new ArrayList<>();

                                for (int i = 1; i <= 5; i++) {
                                    String s = DayOfWeek.of(i).name().substring(0, 2);
                                    for (int j = 1; j <= 8; j++) {
                                        String id = s + j;
                                        Button planButton = MainActivity.mainActivity.findViewById(MainActivity.mainActivity.getResources().getIdentifier("button" + id, "id", MainActivity.mainActivity.getPackageName()));
                                        if (!planButton.getText().toString().equalsIgnoreCase("LHGW12 MAFF")){
                                            String[] menuItem = planButton.getText().toString().split(" ");
                                            CoreCourse clicked = jahresStundenPlan.getCoreCourses().stream()
                                                    .filter(coreCourse -> coreCourse.getTeacher().equalsIgnoreCase(menuItem[1])
                                                    && coreCourse.getCourseName().equalsIgnoreCase(menuItem[0]))
                                                    .filter(coreCourse -> coreCourse.getCourses().stream().anyMatch(course -> {
                                                return (course.getDay().name().substring(0,2) + course.getLesson()).equalsIgnoreCase(id);
                                            })).findAny().get();
                                            coreCourses.add(clicked);
                                        }
                                    }
                                }

                                User user = new User(coreCourses, year);
                                PersonalDSBLib.setUser(user);

                                MainActivity.mainActivity.navController.navigate(R.id.nav_thisweek);
                            });
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
}