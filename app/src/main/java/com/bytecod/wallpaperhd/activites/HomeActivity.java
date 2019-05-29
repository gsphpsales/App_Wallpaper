package com.bytecod.wallpaperhd.activites;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.bytecod.wallpaperhd.R;
import com.bytecod.wallpaperhd.fragments.FavouritesFragment;
import com.bytecod.wallpaperhd.fragments.HomeFragment;
import com.bytecod.wallpaperhd.fragments.SettingsFragment;
import com.google.android.gms.ads.MobileAds;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        MobileAds.initialize(this,
                "ID APP where");

        bottomNavigationView = findViewById(R.id.botto_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        displayFragment(new HomeFragment());
    }
    private void displayFragment(Fragment fragment){
    getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.content_are, fragment).commit();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()){
            case R.id.nav_home:
                fragment =new HomeFragment();
                break;
            case R.id.nav_fav:
                fragment = new FavouritesFragment();
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                break;
                default:
                    fragment = new HomeFragment();
                    break;
        }
        displayFragment(fragment);
        return true;
    }




}
