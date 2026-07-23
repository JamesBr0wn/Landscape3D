package com.diudkr.Landscape3d;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.diudkr.Landscape3D.R;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    PopupWindow mPopupWindow = null;
    Toolbar mToolbar = null;
    ViewSettings mViewSettings = null;

    private static final int SWIPE_DELTA = 150;
    private static final int SWIPE_VELOCITY = 50;
    private GestureDetectorCompat mDetector;

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("diudkr","onDown returning true: " + event.toString());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d("diudkr", "OnF XV = " + velocityX + " YV = " + velocityY);
            if ( Math.abs(e1.getY() - e2.getY()) > Math.abs(e1.getX() - e2.getX())) { // bigger swipe in Y dir
                if ((e1.getY() - e2.getY()) > SWIPE_DELTA && Math.abs(velocityY) > SWIPE_VELOCITY) {
                    if (!ViewSettings.getViewSettings().getHasUsedWater()) {
                        ViewSettings.getViewSettings().incDetailLevel();
                    }
                    return true;
                }
                if ((e2.getY() - e1.getY()) > SWIPE_DELTA && Math.abs(velocityY) > SWIPE_VELOCITY) {
                    if (!ViewSettings.getViewSettings().getHasUsedWater()) {
                        ViewSettings.getViewSettings().decDetailLevel();
                    }
                    return true;
                }
            }
            else {
                if (e1.getX() - e2.getX() > SWIPE_DELTA && Math.abs(velocityX) > SWIPE_VELOCITY) {
                    ViewSettings.getViewSettings().decDrawmethod();
                    return true;
                }
                if (e2.getX() - e1.getX() > SWIPE_DELTA && Math.abs(velocityX) > SWIPE_VELOCITY) {
                    ViewSettings.getViewSettings().incDrawmethod();
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // return super.onTouchEvent(event);
        boolean b = this.mDetector.onTouchEvent(event);
        return b || super.onTouchEvent(event);
    }

    static class MyOnApplyWindowInsetsListener implements OnApplyWindowInsetsListener {
        @NonNull
        @Override
        public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
            Insets deltas = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(deltas.left, deltas.top, deltas.right, deltas.bottom);
            // return insets;
            return WindowInsetsCompat.CONSUMED;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("diudkr", "MainActivity.onCreate1");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        doHide();
        Log.i("diudkr", "MainActivity.onCreate2");
        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());

        View mainview = findViewById(R.id.main);
        OnApplyWindowInsetsListener myListener = new MyOnApplyWindowInsetsListener();
        ViewCompat.setOnApplyWindowInsetsListener(mainview, myListener);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Log.i("diudkr", "MainActivity.onCreate end");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbarmenu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        doHide();
        Log.i("diudkr", "MainActivity.onResume1");
        mViewSettings = ViewSettings.getViewSettings();
        mViewSettings.setTheMainActivity(this);
        LandscapeData.getLandscape().calculateLandscape();
        mViewSettings.setDetailLevel(LandscapeData.MAX_DETAIL - 1);
        // Drawing method spinner
        Spinner spn = (Spinner) ViewSettings.getViewSettings().getMainActivity().findViewById(R.id.spnDrawmethod);
        if (null != spn) {
            ArrayAdapter<String> adapter;
            adapter = new ArrayAdapter<>(ViewSettings.getViewSettings().getMainActivity(),
                        android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            adapter.add(ViewSettings.getViewSettings().getMainActivity().getString(R.string.btnDrawPoints));  // 0
            adapter.add(ViewSettings.getViewSettings().getMainActivity().getString(R.string.btnDrawBigPoints)); // 1
            adapter.add(ViewSettings.getViewSettings().getMainActivity().getString(R.string.btnDrawLinesX)); // 2
            adapter.add(ViewSettings.getViewSettings().getMainActivity().getString(R.string.btnDrawLinesY)); // 3
            adapter.add(ViewSettings.getViewSettings().getMainActivity().getString(R.string.btnDrawSquares)); // 4
            adapter.add(ViewSettings.getViewSettings().getMainActivity().getString(R.string.btnDrawTriangles)); // 5
            adapter.add(ViewSettings.getViewSettings().getMainActivity().getString(R.string.btnDrawShaded)); // 6
            spn.setAdapter(adapter);
            // spn.setPromptId(R.string.spnPrompt);
            spn.setOnItemSelectedListener(this);
            spn.setSelection(4, false);
        }
        else {
            Log.w("diudkr", "MainActivity.onResume3: no Spinner!");
        }
        mViewSettings.setDrawmethod(4);
        onBtnNew(null);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        doHide();
    }

    private void doHide()
    {
        View decorView = getWindow().getDecorView();
        int uiOptions = 0;
        uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        ;
        decorView.setSystemUiVisibility(uiOptions);
    }

    void showPopup() {
        // Inflate the popup view
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.aboutpopup, null);

        // Initialize a new instance of popup window
        mPopupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        mPopupWindow.setElevation(5.0f);
        Button closeButton = popupView.findViewById(R.id.about_popup_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupWindow.dismiss();
            }
        });

        // Finally, show the popup window at the center location of root relative layout
        View mMainContainer = findViewById(R.id.main);
        mPopupWindow.showAtLocation(mMainContainer, Gravity.CENTER,0,0);
        // Note: for unknown reasons, the url navifation does not work in the emulator
    }

    /////////////////// UI elements (Buttons etc) handlers
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // system menu (...) commands
        int id = item.getItemId();
        if (R.id.mi_about == id) {
            this.showPopup();
            return true;
        }
        if (R.id.mi_close == id) {
            this.finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // draw method spinner selection
        mViewSettings.setDrawmethod(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // draw method spinner just closed - do nothing
    }

    public void onBtnNew(View v) {
        LandscapeData.getLandscape().calculateLandscape();
        mViewSettings.updateLandscapeView();
        mViewSettings.resetCtrlsState();
    }

    public void onBtnIncDetail(View v) {
        mViewSettings.incDetailLevel();
    }

    public void onBtnDecDetail(View v) {
        mViewSettings.decDetailLevel();
    }

    public void onBtn5Rivers(View v) {
        mViewSettings.add5River();
    }

    public void onBtnFlood(View v) {
        mViewSettings.addOcean();
    }

}