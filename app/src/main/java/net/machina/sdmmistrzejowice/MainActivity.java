package net.machina.sdmmistrzejowice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.machina.sdmmistrzejowice.common.Constants;
import net.machina.sdmmistrzejowice.common.MapData;
import net.machina.sdmmistrzejowice.common.MarkerData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnInfoWindowClickListener {

    protected GoogleMap gmap;
    protected File cacheFile;
    protected int selectedCategory;
    protected static final String SELECTED_CATEGORY = "selectedCategory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectedCategory = savedInstanceState.getInt(SELECTED_CATEGORY, -1);
        } else {
            selectedCategory = -1;
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cacheFile = new File(getCacheDir(), "points.csv");

        //Toast.makeText(MainActivity.this, "Cache length: " + cacheFile.length(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(MainActivity.this, "Cache version: " + getSharedPreferences(getString(R.string.data_shared_prefs),Context.MODE_PRIVATE).getString("version","0"),Toast.LENGTH_SHORT).show();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        MapFragment map = MapFragment.newInstance();
        getFragmentManager().beginTransaction().add(R.id.mapLayout, map).commit();

        if (map != null) {
            map.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    Log.i("SDMMistrzejowice", "onMapReady() callback");
                    gmap = googleMap;
                    gmap.setInfoWindowAdapter(infoWindowAdapter);
                    Log.d("SDMMistrzejowice", gmap.toString());
                    mapIsReady();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_CATEGORY, selectedCategory);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        //int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.menu_cat_accomodation:
                parseMarkers(cacheFile, MapData.POINT_SCHOOL);
                selectedCategory = MapData.POINT_SCHOOL;
                break;
            case R.id.menu_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.menu_cat_all:
                parseMarkers(cacheFile, MapData.POINT_ALL_CATEGORIES);
                selectedCategory = MapData.POINT_ALL_CATEGORIES;
                break;
            case R.id.menu_cat_bank:
                parseMarkers(cacheFile, MapData.POINT_BANK);
                selectedCategory = MapData.POINT_BANK;
                break;
            case R.id.menu_cat_church:
                parseMarkers(cacheFile, MapData.POINT_CHURCH);
                selectedCategory = MapData.POINT_CHURCH;
                break;
            case R.id.menu_cat_exchange:
                parseMarkers(cacheFile, MapData.POINT_EXCHANGE);
                selectedCategory = MapData.POINT_EXCHANGE;
                break;
            case R.id.menu_cat_grocery:
                parseMarkers(cacheFile, MapData.POINT_SHOP);
                selectedCategory = MapData.POINT_SHOP;
                break;
            case R.id.menu_cat_medicalcenter:
                parseMarkers(cacheFile, MapData.POINT_CLINIC);
                selectedCategory = MapData.POINT_CLINIC;
                break;
            case R.id.menu_cat_pharmacy:
                parseMarkers(cacheFile, MapData.POINT_PHARMACY);
                selectedCategory = MapData.POINT_PHARMACY;
                break;
            case R.id.menu_emergency:
                startActivity(new Intent(MainActivity.this, IceActivity.class));
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void mapIsReady() {
        Log.d("SDMMistrzejowice", "called mapIsReady()");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            gmap.setMyLocationEnabled(true);
        }
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(Constants.POINT_CENTER, 14.0f);
        gmap.moveCamera(update);
        gmap.setOnInfoWindowClickListener(this);
        parseMarkers(cacheFile, selectedCategory);
    }

    public void parseMarkers(File input, int category) {
        ArrayList<MarkerData> markers = new ArrayList<>();
        try {
            String line;
            InputStream stream = new FileInputStream(input);
            InputStreamReader reader = new InputStreamReader(stream, Charset.forName("UTF-8"));
            BufferedReader buf = new BufferedReader(reader);

            while((line = buf.readLine()) != null) {
                Pattern pattern = Pattern.compile(Pattern.quote(";"));
                String[] splitted = pattern.split(line);
                MarkerData myMarker = new MarkerData(Double.parseDouble(splitted[1]), Double.parseDouble(splitted[2]), Integer.parseInt(splitted[0]), splitted[3].replace("\"",""));
                markers.add(myMarker);
            }

            gmap.clear();
            //Toast.makeText(MainActivity.this, "Marker count: " + markers.size(), Toast.LENGTH_SHORT).show();
            for(int i = 0; i < markers.size(); i++) {
                if(markers.get(i).getCategory() == category || category == MapData.POINT_ALL_CATEGORIES) {
                    gmap.addMarker(
                            new MarkerOptions()
                                    .position(new LatLng(markers.get(i).getLat(), markers.get(i).getLng()))
                                    .icon(MapData.getPointIcon(markers.get(i).getCategory()))
                                    .title(MapData.getLocalizedName(MainActivity.this, markers.get(i).getCategory()))
                                    .snippet(markers.get(i).getExtra() + "\n\n" + getString(R.string.map_click_to_navigate))
                    );
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            Context context = MainActivity.this; //or getActivity(), YourActivity.this, etc.

            LinearLayout info = new LinearLayout(context);
            info.setOrientation(LinearLayout.VERTICAL);

            TextView title = new TextView(context);
            title.setTextColor(Color.BLACK);
            title.setGravity(Gravity.CENTER);
            title.setTypeface(null, Typeface.BOLD);
            title.setText(marker.getTitle());

            TextView snippet = new TextView(context);
            snippet.setTextColor(Color.GRAY);
            snippet.setGravity(Gravity.CENTER_HORIZONTAL);
            snippet.setText(marker.getSnippet());

            info.addView(title);
            info.addView(snippet);

            return info;
        }
    };

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d("SDMMistrzejowice", "Loading navigation");
        LatLng markerPos = marker.getPosition();
        Uri action = Uri.parse("google.navigation:q=" + markerPos.latitude + "," + markerPos.longitude + "&mode=w");
        Intent intent = new Intent(Intent.ACTION_VIEW, action);
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }
}
