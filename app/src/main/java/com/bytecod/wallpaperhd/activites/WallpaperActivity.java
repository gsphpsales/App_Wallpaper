package com.bytecod.wallpaperhd.activites;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Path;
import android.icu.text.RelativeDateTimeFormatter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.bytecod.wallpaperhd.R;
import com.bytecod.wallpaperhd.adapters.WallpaperAdapter;
import com.bytecod.wallpaperhd.models.Category;
import com.bytecod.wallpaperhd.models.Wallpaper;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bytecod.wallpaperhd.adapters.WallpaperAdapter;
import com.bytecod.wallpaperhd.models.Wallpaper;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;


public class WallpaperActivity extends AppCompatActivity {

    private AdView mAdView;

    List<Wallpaper> wallpaperList;
    List<Wallpaper> favList;
    RecyclerView recyclerView;
    WallpaperAdapter adapter;

    DatabaseReference dbWallpapers, dbFavs;
    ProgressBar progressBar;

    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3, floatingActionButton4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);
        MobileAds.initialize(this, "ID app here");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        //floatingActionButton1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);



        Intent intent = getIntent();
        final String category = intent.getStringExtra("category");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(category);
        setSupportActionBar(toolbar);

        favList = new ArrayList<>();
        wallpaperList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WallpaperAdapter(this, wallpaperList);

        recyclerView.setAdapter(adapter);



        progressBar = findViewById(R.id.progressbar);
        dbWallpapers = FirebaseDatabase.getInstance().getReference("images")
                .child(category);


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            dbFavs = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites")
                    .child(category);
            fetchFavWallpapers(category);
        } else {
            fetchWallpapers(category);
        }

    }

    private void fetchFavWallpapers(final String category) {
        progressBar.setVisibility(View.VISIBLE);

        dbFavs.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if (dataSnapshot.exists()) {
                    for (DataSnapshot wallpaperSnapshot : dataSnapshot.getChildren()) {

                        String id = wallpaperSnapshot.getKey();
                        String title = wallpaperSnapshot.child("title").getValue(String.class);
                        String desc = wallpaperSnapshot.child("desc").getValue(String.class);
                        String url = wallpaperSnapshot.child("url").getValue(String.class);

                        Wallpaper w = new Wallpaper(id, title, desc, url, url, category);
                        favList.add(w);
                    }
                }
                fetchWallpapers(category);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchWallpapers(final String category) {
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> data = new HashMap<>();
        data.put("category", category);

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("getWallpapers")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, ArrayList>() {
                    @Override
                    public ArrayList then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        ArrayList result = (ArrayList) task.getResult().getData();
                        progressBar.setVisibility(View.GONE);

                        for (Object o: result) {
                            Map m = (Map) o;
                            String key = (String) m.get("key");
                            String title = (String) m.get("title");
                            String desc = (String) m.get("desc");
                            String thumb = (String) m.get("_thumbnail");
                            String thumbSource = (String) m.get("url");
                            Boolean cached = (Boolean) m.get("_usingCachedImage");

                            Log.i("CY-DEBUG", "Using cache: " + key + ", " + cached);

                            Wallpaper w = new Wallpaper(key, title, desc, thumb, thumbSource, category);

                            if (isFavourite(w)) {
                                w.isFavourite = true;
                            }

                            wallpaperList.add(w);
                        }

                        adapter.notifyDataSetChanged();

                        return result;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("CY-DEBUG", "ERROR");
                        e.printStackTrace();

                        dbWallpapers.orderByChild("indexReverse").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                progressBar.setVisibility(View.GONE);
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot wallpaperSnapshot : dataSnapshot.getChildren()) {

                                        String id = wallpaperSnapshot.getKey();
                                        String title = wallpaperSnapshot.child("title").getValue(String.class);
                                        String desc = wallpaperSnapshot.child("desc").getValue(String.class);
                                        String thumb = wallpaperSnapshot.child("_thumbnail").getValue(String.class);
                                        String url = wallpaperSnapshot.child("url").getValue(String.class);

                                        if (thumb == null)
                                            thumb = url;

                                        Wallpaper w = new Wallpaper(id, title, desc, thumb, url, category);

                                        if (isFavourite(w)) {
                                            w.isFavourite = true;
                                        }

                                        wallpaperList.add(w);
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });

    }

    private boolean isFavourite(Wallpaper w) {
        for (Wallpaper f : favList) {
            if (f.id.equals(w.id)) {
                return true;
            }
        }
        return false;
    }
}
