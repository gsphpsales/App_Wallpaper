package com.bytecod.wallpaperhd.adapters;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bytecod.wallpaperhd.BuildConfig;
import com.bytecod.wallpaperhd.R;
import com.bytecod.wallpaperhd.models.Wallpaper;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpapersViewHolder> {

    private Context mCtx;
    private List<Wallpaper> wallpaperList;
    //private String category;
    public WallpaperAdapter(Context mCtx, List<Wallpaper> wallpaperList) {
        this.mCtx = mCtx;
        this.wallpaperList = wallpaperList;
      //  this.category = category;
    }
    @Override
    public WallpapersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.recycleviewwallpaper, parent, false);
        return new WallpapersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WallpapersViewHolder holder, int position) {
        Wallpaper w = wallpaperList.get(position);
        //holder.textView.setText(w.title);
        Glide.with(mCtx)
                .load(w.url)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(25)))
                .into(holder.imageViewBlur);
        Glide.with(mCtx)
                .load(w.url)
                .into(holder.imageView);
        if (w.isFavourite){
            holder.checkBoxFav.setChecked(true);
        }
    }

    @Override
    public int getItemCount() {
        return wallpaperList.size();
    }

    class WallpapersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            CompoundButton.OnCheckedChangeListener {

        //TextView textView;
        ImageView imageView;
        ImageView imageViewBlur;
        FloatingActionButton fab2, fab1;
        CheckBox checkBoxFav;
        //ImageButton buttonShare, button_set;


        public WallpapersViewHolder(View itemView) {
            super(itemView);
            //textView = itemView.findViewById(R.id.text_view_title);
            imageView = itemView.findViewById(R.id.imageview_reclycler);
            imageViewBlur = itemView.findViewById(R.id.imageview_reclycler_blur);
            checkBoxFav = itemView.findViewById(R.id.checkbox_favorite);
            //buttonShare =  itemView.findViewById(R.id.button_share);
            fab1 = itemView.findViewById(R.id.material_design_floating_action_menu_item2);
            fab2 = itemView.findViewById(R.id.material_design_floating_action_menu_item3);
            //button_set = itemView.findViewById(R.id.button_set);

            checkBoxFav.setOnCheckedChangeListener(this);
            //buttonShare.setOnClickListener(this);
            fab1.setOnClickListener(this);
            fab2.setOnClickListener(this);
            //button_set.setOnClickListener(this);

            //imageView.setImageBitmap();
        }
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.material_design_floating_action_menu_item2:

                    shareWallpaper(wallpaperList.get(getAdapterPosition()));

                    break;


                case R.id.material_design_floating_action_menu_item3:
                    setWallpaper(wallpaperList.get(getAdapterPosition()));
                    break;
            }

        }

        private void setWallpaper(Wallpaper w){
            ((Activity) mCtx).findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
            Glide.with(mCtx)
                    .asBitmap()
                    .load(w.urlSource)
                    .into(new SimpleTarget<Bitmap>() {
                              @Override
                              public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                  ((Activity) mCtx).findViewById(R.id.progressbar).setVisibility(View.GONE);
                             //     Bitmap bitmap = BitmapFactory.decodeResource(mCtx.getResources(), R.id.image_view);

                                  WallpaperManager manager = WallpaperManager.getInstance(mCtx.getApplicationContext());

                                  try {
                                      manager.setBitmap(resource);
                                      Toast.makeText(mCtx, "Wallpaper salvo com sucesso", Toast.LENGTH_LONG).show();

                                  }catch (IOException e){
                                      Toast.makeText(mCtx,"Error", Toast.LENGTH_SHORT).show();
                                  }
                                 // mCtx.startActivity(Intent.createChooser(intent, "Wallpapers Hub"));
                              }
                          }
                    );
            //Bitmap bitmap = BitmapFactory.decodeResource(mCtx.getResources(), R.id.image_view);

        }

        private void shareWallpaper(Wallpaper w) {
            ((Activity) mCtx).findViewById(R.id.progressbar).setVisibility(View.VISIBLE);

            Glide.with(mCtx)
                    .asBitmap()
                    .load(w.urlSource)
                    .into(new SimpleTarget<Bitmap>() {
                              @Override
                              public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                  ((Activity) mCtx).findViewById(R.id.progressbar).setVisibility(View.GONE);

                                  Intent intent = new Intent(Intent.ACTION_SEND);
                                  intent.setType("images/*");
                                  intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(resource));

                                  mCtx.startActivity(Intent.createChooser(intent, "wallpaper_hub"));
                              }
                          }
                    );
        }

        private Uri getLocalBitmapUri(Bitmap bmp) {
            Uri bmpUri = null;
            try {
                File file = new File(mCtx.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "wallpaper_hub" + System.currentTimeMillis() + ".png");
                FileOutputStream out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
                bmpUri = FileProvider.getUriForFile(mCtx, BuildConfig.APPLICATION_ID + ".provider", file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmpUri;
        }


        private void downloadWallpaper(final Wallpaper wallpaper) {
            ((Activity) mCtx).findViewById(R.id.progressbar).setVisibility(View.VISIBLE);

            Glide.with(mCtx)
                    .asBitmap()
                    .load(wallpaper.urlSource)
                    .into(new SimpleTarget<Bitmap>() {
                              @Override
                              public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                  ((Activity) mCtx).findViewById(R.id.progressbar).setVisibility(View.GONE);

                                  Intent intent = new Intent(Intent.ACTION_VIEW);

                                  Uri uri = saveWallpaperAndGetUri(resource, wallpaper.id);

                                  if (uri != null) {
                                      intent.setDataAndType(uri, "images/*");
                                      mCtx.startActivity(Intent.createChooser(intent, "wallpaper_hub"));
                                  }
                              }
                          }
                    );
        }


        private Uri saveWallpaperAndGetUri(Bitmap bitmap, String id) {
            if (ContextCompat.checkSelfPermission(mCtx, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat
                        .shouldShowRequestPermissionRationale((Activity) mCtx, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

                    Uri uri = Uri.fromParts("package", mCtx.getPackageName(), null);
                    intent.setData(uri);

                    mCtx.startActivity(intent);

                } else {
                    ActivityCompat.requestPermissions((Activity) mCtx, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }
                return null;
            }

            File folder = new File(Environment.getExternalStorageDirectory().toString() + "/wallpaper_hub");
            folder.mkdirs();

            File file = new File(folder, id + ".jpg");
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                return FileProvider.getUriForFile(mCtx, BuildConfig.APPLICATION_ID + ".provider", file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(mCtx, "Faça o login primeiro...", Toast.LENGTH_LONG).show();
                compoundButton.setChecked(false);
                return;
            }


            int position = getAdapterPosition();
            Wallpaper w = wallpaperList.get(position);


            DatabaseReference dbFavs = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites")
                    .child(w.category);

            if (b) {
                dbFavs.child(w.id).setValue(w);
            } else {
                dbFavs.child(w.id).setValue(null);
            }
        }
    }
}
