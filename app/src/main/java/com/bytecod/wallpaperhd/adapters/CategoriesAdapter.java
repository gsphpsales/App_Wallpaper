package com.bytecod.wallpaperhd.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bytecod.wallpaperhd.R;
import com.bytecod.wallpaperhd.activites.WallpaperActivity;
import com.bytecod.wallpaperhd.models.Category;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.w3c.dom.Text;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {
    private Context mCtx;
    private List<Category> categoryList;




    public CategoriesAdapter(Context mCtx, List<Category> categoryList){
        this.mCtx = mCtx;
        this.categoryList = categoryList;

    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.recycleviewcategories, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        Category c = categoryList.get(position);
        //holder.textView.setText(c.name);
        Glide.with(mCtx)
                .load(c.thumb)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        //TextView textView;
        ImageView imageView;


        public CategoryViewHolder(View itemView) {
            super(itemView);
           // textView = itemView.findViewById(R.id.textView_cat_name);
            imageView = itemView.findViewById(R.id.imageview_reclycler);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int p =  getAdapterPosition();
            Category c = categoryList.get(p);
            Intent intent = new Intent(mCtx, WallpaperActivity.class);
            intent.putExtra("category", c.name);
            mCtx.startActivity(intent);
        }

    }

}
