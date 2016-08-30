package com.team8.frc2016.auto;

import org.json.simple.JSONArray;

import com.team8.frc2016.auto.actions.DriveForwardAction;
import com.team8.frc2016.auto.modes.DoNothingAutoMode;
import com.team8.frc2016.auto.modes.DriveForwardAutoMode;

import java.util.ArrayList;

public class AutoModeSelector {
    private static AutoModeSelector instance = null;
    private ArrayList<AutoMode> autoModes = new ArrayList<AutoMode>();
    int selectedIndex = 1;
    public static AutoModeSelector getInstance() {
        if (instance == null) {
            instance = new AutoModeSelector();
        }
        return instance;
    }

    public void registerAutonomous(AutoMode auto) {
        autoModes.add(auto);
    }

    public AutoModeSelector() {
        registerAutonomous(new DoNothingAutoMode());
        registerAutonomous(new DriveForwardAutoMode());
    }

    public AutoMode getAutoMode() {
        return autoModes.get(selectedIndex);
    }
    
    public AutoMode getAutoMode(int index) {
        return autoModes.get(index);
    }

    public ArrayList<String> getAutoModeList() {
        ArrayList<String> list = new ArrayList<String>();
        for (AutoMode autoMode : autoModes) {
            list.add(autoMode.getClass().getSimpleName());
        }
        return list;
    }

    public JSONArray getAutoModeJSONList() {
        JSONArray list = new JSONArray();
        list.addAll(getAutoModeList());
        return list;
    }

    public void setFromWebUI(int index) {
        setAutoModeByIndex(index);
    }

    private void setAutoModeByIndex(int which) {
        if (which < 0 || which >= autoModes.size()) {
            which = 0;
        }
        selectedIndex = which;
    }

}