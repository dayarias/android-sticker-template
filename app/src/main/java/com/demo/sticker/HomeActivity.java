package com.demo.sticker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.content.Intent;
import android.net.Uri;
import java.io.File;
import android.graphics.Bitmap;
import java.io.FileOutputStream;
import android.support.v4.content.FileProvider;
import java.io.IOException;
import android.graphics.BitmapFactory;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.demo.sticker.BuildConfig;

public class HomeActivity extends AppCompatActivity {

    public static Integer currentSticker = 0;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getBoolean("firstTime", false)) {
            // run your one time code
            AppIndexingService.enqueueWork(HomeActivity.this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
        }

        GridView gridView = (GridView)findViewById(R.id.gridview);
        final StickersAdapter stickersAdapter = new StickersAdapter(this, stickers);
        gridView.setAdapter(stickersAdapter);

        image = (ImageView)findViewById(R.id.mainImageView);

        ImageButton ShareStickerBtn = findViewById(R.id.shareSticker);
        ShareStickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap icon = BitmapFactory.decodeResource(HomeActivity.this.getResources(),stickers[currentSticker].getImageResource());
                shareImage(icon);

            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                Sticker sticker = stickers[position];

                image.setImageResource(sticker.getImageResource());
                currentSticker = position;

                // This tells the GridView to redraw itself
                // in turn calling StickerAdapter's getView method again for each cell
                stickersAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // set back the currently selected sticker we're viewing
        image.setImageResource(stickers[currentSticker].getImageResource());

    }

    private void shareImage(Bitmap bitmap){
        // save bitmap to cache directory
        try {
            File cachePath = new File(this.getCacheDir(), "images");
            cachePath.mkdirs(); // don't forget to make the directory
            FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to copy sticker", Toast.LENGTH_SHORT)
                    .show();
        }
        File imagePath = new File(this.getCacheDir(), "images");
        File newFile = new File(imagePath, "image.png");
        Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", newFile);

        if (contentUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.setType("image/png");
            startActivity(Intent.createChooser(shareIntent, "Choose an app"));
        }
    }

    public static Sticker[] stickers = {
            new Sticker("Duccky", R.drawable.duccky,
                     "https://drive.google.com/file/d/1FLyCYIdG4gQpyubDMcJ_HrTRRSwhX-fL/view?usp=sharing",
                    new String [] {"Pato1"})
            };
}