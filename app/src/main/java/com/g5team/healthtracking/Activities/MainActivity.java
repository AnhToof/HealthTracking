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

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static String TAG = "MainActivity";
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
    private Boolean exit = false;
    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    private void displaySelectedScreen(int id){
        Fragment fragment = null;
        switch (id){
            case R.id.nav_home:
                fragment = new HomeFragment();
                break;
            case R.id.nav_profile:
                fragment = new ProfileFragment();
                break;
            case R.id.nav_exit:
                logout();
                break;
            default:
                fragment = new HomeFragment();
                break;
        }
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
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
                        Toast.makeText(getBaseContext(), "Bạn đã đăng xuất thành công", Toast.LENGTH_SHORT);
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
