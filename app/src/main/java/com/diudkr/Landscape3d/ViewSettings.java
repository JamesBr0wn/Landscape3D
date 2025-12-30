package com.diudkr.Landscape3d;

import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.diudkr.Landscape3D.R;


public class ViewSettings {
    private static ViewSettings theViewSettings = null;
    private int iDetailLevel;
    private MainActivity theMainActivity = null;
    private int drawmethod; // 0..5
    private boolean hasUsedWater = false;

    public static int maxViewSetting = 6;
    private ViewSettings() {
        super();
        drawmethod = 4;
    }

    public boolean getHasUsedWater() {
        return hasUsedWater;
    }
    public int getDrawmethod() {
        return drawmethod;
    }

    public MainActivity getMainActivity() {
        return theMainActivity;
    }

    public void addRiver() {
        hasUsedWater = true;
        LandscapeData.getLandscape().createRiver();
        updateLandscapeView();
    }

    public void add5River() {
        hasUsedWater = true;
        for (int i=0; i<5; i++) {
            LandscapeData.getLandscape().createRiver();
        }
        updateLandscapeView();
        updateCtrlsState();
    }

    public void addOcean() {
        hasUsedWater = true;
        LandscapeData.getLandscape().floodOcean();
        updateLandscapeView();
        updateCtrlsState();
    }

    public void incDrawmethod() {
        drawmethod++;
        if (drawmethod > maxViewSetting) {
            drawmethod = 0;
        }
        updateLandscapeView();
        updateDrawMethodCtrls();
    }

    public void decDrawmethod() {
        drawmethod--;
        if (drawmethod < 0) {
            drawmethod = maxViewSetting;
        }
        updateLandscapeView();
        updateDrawMethodCtrls();
    }

    public void setDrawmethod(int d) {
        Log.i("diudkr", "ViewSettings.setDrawmethod1 " + d + " current: " + drawmethod);
        if ( d == drawmethod) {
            return;
        }
        if ( (d<0) || (d>maxViewSetting) ) {
            d=4;
        }
        drawmethod = d;
        updateLandscapeView();
        updateDrawMethodCtrls();
    }

    public void updateDrawMethodCtrls() {
        Spinner spn;
        if (null != theMainActivity) {
            spn = (Spinner) theMainActivity.findViewById(R.id.spnDrawmethod);
            if (null!= spn) {
                spn.setSelection(drawmethod, false);
            }
        }
        else {
            Log.w("diudkr", "ViewSettings.updateDrawMethodBtn: no MainActivity!");
        }
    }

    public void setTheMainActivity(MainActivity theMainActivity) {
        this.theMainActivity = theMainActivity;
    }

    public int getDetailLevel() {
        return iDetailLevel;
    }

    public void setDetailLevel(int iDetailLevel) {
        if (null == theMainActivity) {
            return;
        }
        if(iDetailLevel<LandscapeData.MIN_DETAIL) {
            iDetailLevel = LandscapeData.MIN_DETAIL;
        }
        if(iDetailLevel>LandscapeData.MAX_DETAIL) {
            iDetailLevel = LandscapeData.MAX_DETAIL;
        }
        this.iDetailLevel = iDetailLevel;
        updateDetailtxt();
        updateLandscapeView();
        updateCtrlsState();
    }


    public void incDetailLevel() {
        if (null == theMainActivity) {
            return;
        }
        iDetailLevel++;
        if(iDetailLevel >= LandscapeData.MAX_DETAIL)
        {
            iDetailLevel = LandscapeData.MAX_DETAIL;
            Toast toast = Toast.makeText(theMainActivity, theMainActivity.getString(R.string.txt_max_detail_toast, "" + iDetailLevel), Toast.LENGTH_SHORT);
            toast.show();
        }
        updateDetailtxt();
        updateLandscapeView();
        updateCtrlsState();
    }

    public void decDetailLevel() {
        if (null == theMainActivity) {
            return;
        }
        iDetailLevel--;
        if(iDetailLevel <= LandscapeData.MIN_DETAIL)
        {
            iDetailLevel = LandscapeData.MIN_DETAIL;
            Toast toast = Toast.makeText(theMainActivity, theMainActivity.getString(R.string.txt_min_detail_toast), Toast.LENGTH_SHORT);
            toast.show();
        }
        updateDetailtxt();
        updateLandscapeView();
        updateCtrlsState();
    }

    public void updateDetailtxt() {
        if (null != theMainActivity) {
            String txt = theMainActivity.getString(R.string.lblCurrentDetailNr, "" + iDetailLevel, "" + LandscapeData.MAX_DETAIL);
            ActionBar ab = theMainActivity.getSupportActionBar();
            if (null != ab) {
                ab.setSubtitle(txt);
            }
        }
        else {
            Log.w("diudkr", "ViewSettings.updateDetailtxt: no MainActivity!");
        }
    }

    public void updateLandscapeView() {
        if (null != theMainActivity) {
            LandscapeView lv = (LandscapeView) theMainActivity.findViewById(R.id.landscapeView);
            if (lv != null) {
                lv.invalidate();
            }
            else {
                Log.w("diudkr", "ViewSettings.updateLandscapeView: no LandscapeView!");
            }
        }
        else {
            Log.w("diudkr", "ViewSettings.updateLandscapeView: no MainActivity!");
        }
    }

    public static ViewSettings getViewSettings() {
        if (theViewSettings == null) {
            theViewSettings = new ViewSettings();
        }
        return theViewSettings;
    }

    public void updateCtrlsState() {
        // if detail level = MAX then enable the water buttons
        // if water has been used, deactivate the change level buttons
        Log.i("diudkr", "ViewSettings.updateCtrlsState: level = " + iDetailLevel + " hasUsedWater = " + hasUsedWater);
        if ( (true == hasUsedWater) || (LandscapeData.MAX_DETAIL == iDetailLevel) ) {
            Button btn;
            btn = (Button) getMainActivity().findViewById(R.id.btn5Rivers);
            btn.setEnabled(true);
            btn = (Button) getMainActivity().findViewById(R.id.btnFlood);
            btn.setEnabled(true);
            Log.i("diudkr", "ViewSettings.updateCtrlsState: enable water btns");
        }
        else {
            Button btn;
            btn = (Button) getMainActivity().findViewById(R.id.btn5Rivers);
            btn.setEnabled(false);
            btn = (Button) getMainActivity().findViewById(R.id.btnFlood);
            btn.setEnabled(false);
            Log.i("diudkr", "ViewSettings.updateCtrlsState: disable water btns");
        }
        if (true == hasUsedWater) { // disable both detail buttons
            Button btn;
            btn = (Button) getMainActivity().findViewById(R.id.btnIncDetail);
            btn.setEnabled(false);
            btn = (Button) getMainActivity().findViewById(R.id.btnDecDetail);
            btn.setEnabled(false);
            Log.i("diudkr", "ViewSettings.updateCtrlsState: disable detail btns");
        }
        else {
            Button btn;
            btn = (Button) getMainActivity().findViewById(R.id.btnDecDetail);
            if (LandscapeData.MIN_DETAIL == iDetailLevel) {
                btn.setEnabled(false);
            }
            else {
                btn.setEnabled(true);
            }
            btn = (Button) getMainActivity().findViewById(R.id.btnIncDetail);
            if (LandscapeData.MAX_DETAIL == iDetailLevel) {
                btn.setEnabled(false);
            }
            else {
                btn.setEnabled(true);
            }
        }
    }

    public void resetCtrlsState() {
        hasUsedWater = false;
        updateCtrlsState();
    }
}
