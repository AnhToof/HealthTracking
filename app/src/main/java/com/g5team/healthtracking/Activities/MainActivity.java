package com.g5team.healthtracking.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.g5team.healthtracking.Adapters.SectionsPagerAdapter;
import com.g5team.healthtracking.Fragments.HomeFragment;
import com.g5team.healthtracking.Fragments.ProfileFragment;
import com.g5team.healthtracking.R;
import com.g5team.healthtracking.Utils.AppConfig;
import com.g5team.healthtracking.Utils.AppController;
import com.g5team.healthtracking.Utils.SessionManager;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG_HOME = "HOME";
    private static final String TAG_PROFILE = "PROFILE";
    public static String TAG = "MainActivity";
    public static String CURRENT_TAG;
    public static TapTargetSequence targetSequence;
    private static long back_pressed_time;
    private static long PERIOD = 1000;
    private SessionManager session;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private TextView tvFullName, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        session = new SessionManager(getApplicationContext());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        displaySelectedScreen(R.id.nav_home);
        navigationView.setCheckedItem(R.id.nav_home);

        View header = navigationView.getHeaderView(0);
        tvFullName = header.findViewById(R.id.tv_fullname_main);
        tvEmail = header.findViewById(R.id.tv_email_main);
        tvEmail.setText(AppConfig.EMAIL);
        tvFullName.setText(AppConfig.FULLNAME);


    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }


        if (CURRENT_TAG == TAG_HOME) {
            if (HomeFragment.mViewPager.getCurrentItem() == 0) {
                if (back_pressed_time + PERIOD > System.currentTimeMillis()) {
                    System.exit(0);
                } else
                    Toast.makeText(MainActivity.this, "Nhấn trở lại lần nữa để thoát!", Toast.LENGTH_SHORT).show();
                back_pressed_time = System.currentTimeMillis();
            } else {
                HomeFragment.mViewPager.setCurrentItem(0, false);
            }

        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, new HomeFragment(), TAG_HOME);
            ft.commit();
        }

    }

    public void guide() {
        targetSequence = new TapTargetSequence(MainActivity.this)
                .targets(
                        TapTarget.forView(findViewById(R.id.btn_floating), "Nhập chỉ số cơ thể", "Để chúng tôi có thể đo chính xác, bạn phải nhập chính xác chiều cao và cân nặng")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(60))
                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        // Yay
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Boo
                    }
                });
    }
    private void displaySelectedScreen(int id){

        Fragment fragment = null;
        CURRENT_TAG = null;
        switch (id){
            case R.id.nav_home:
                fragment = new HomeFragment();
                CURRENT_TAG = TAG_HOME;
                break;
            case R.id.nav_profile:
                fragment = new ProfileFragment();
                CURRENT_TAG = TAG_PROFILE;
                break;
            case R.id.nav_exit:
                logout();
                break;
            default:
                fragment = new HomeFragment();
                CURRENT_TAG = TAG_HOME;
                break;
        }
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment, CURRENT_TAG);
            ft.commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        displaySelectedScreen(id);
        return true;
    }
    private void logout(){
        final StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOGOUT,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "LOGOUT onResponse: " + response.toString());
                        session.setLogin(false);
                        Toast.makeText(MainActivity.this, "Bạn đã đăng xuất thành công", Toast.LENGTH_SHORT);
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "LOGOUT onErrorResponse: " + error.getMessage());
                // hide the progress dialog
                Toast.makeText(getBaseContext(), "Đăng xuất thất bại", Toast.LENGTH_SHORT);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("Authorization", AppConfig.TOKEN_TYPE+ " " +AppConfig.ACCESS_TOKEN);
                return headers;
            }
        };
        AppController.getInstance(getBaseContext()).addToRequestQueue(stringRequest);
    }


}
