package com.bytecod.wallpaperhd.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bytecod.wallpaperhd.R;
import com.bytecod.wallpaperhd.adapters.CategoriesAdapter;
import com.bytecod.wallpaperhd.models.Category;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private List<Category> categoryList;
    private ProgressBar progressBar;
    private DatabaseReference dbCategories;

    private RecyclerView recyclerView;
    private CategoriesAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // colocando visivel o progressbar existente
        progressBar = view.findViewById(R.id.progressbar);
        progressBar.setVisibility(view.VISIBLE);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        categoryList = new ArrayList<>();
        adapter = new CategoriesAdapter(getActivity(), categoryList);
        recyclerView.setAdapter(adapter);

        //referenciando ao db

        Map<String, Object> data = new HashMap<>();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("getCategories")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, ArrayList>() {
                    @Override
                    public ArrayList then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        ArrayList result = (ArrayList) task.getResult().getData();
                        progressBar.setVisibility(view.GONE);

                        for (Object o: result) {
                            Map m = (Map) o;
                            String key = (String) m.get("key");
                            String desc = (String) m.get("desc");
                            String thumb = (String) m.get("_thumbnail");
                            Boolean cached = (Boolean) m.get("_usingCachedImage");

                            Log.i("CY-DEBUG", "Using cache: " + key + ", " + cached);

                            Category c = new Category(key, desc, thumb);
                            categoryList.add(c);
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

                        dbCategories = FirebaseDatabase.getInstance().getReference("categories");
                        dbCategories.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    progressBar.setVisibility(view.GONE);
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        String name = ds.getKey();
                                        String desc = ds.child("desc").getValue(String.class);
                                        String thumb = ds.child("_thumbnail").getValue(String.class);

                                        if (thumb == null)
                                            thumb = ds.child("thumbnail").getValue(String.class);

                                        Category c = new Category(name, desc, thumb);
                                        categoryList.add(c);
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });

    }
}
