package com.example.mediaplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlendMode;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gu.toolargetool.TooLargeTool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SongFragment.OnSongFragmentListener, NewSongFragment.OnNewSongFragmentListener {

    final String SONG_FRAGMENT_TAG = "song_fragment";
    final String NEW_SONG_FRAGMENT_TAG = "new_song_fragment";
    final int WRITE_PERMISSION_REQUEST = 1;

    boolean isPlaying = false;

    ArrayList<Song> songs = new ArrayList<>();

    private Song newSong;

    private Song song1 = new Song("Blinding Lights", "The Weeknd", "https://www.mboxdrive.com/drums.mp3" ,
            "https://upload.wikimedia.org/wikipedia/he/e/e6/The_Weeknd_-_Blinding_Lights.png");
    private Song song2 = new Song("Blinding Lights2", "The Weeknd", "https://www.syntax.org.il/xtra/bob2.mp3" ,
            "https://upload.wikimedia.org/wikipedia/he/e/e6/The_Weeknd_-_Blinding_Lights.png");
    private Song song3 = new Song("Blinding Lights3", "The Weeknd", "https://www.syntax.org.il/xtra/bob1.m4a" ,
            "https://upload.wikimedia.org/wikipedia/he/e/e6/The_Weeknd_-_Blinding_Lights.png");
    private Song song4 = new Song("Blinding Lights4", "The Weeknd", "https://www.syntax.org.il/xtra/bob2.mp3" ,
            "https://upload.wikimedia.org/wikipedia/he/e/e6/The_Weeknd_-_Blinding_Lights.png");
    private Song song5 = new Song("Blinding Lights5", "The Weeknd", "https://www.syntax.org.il/xtra/bob1.m4a" ,
            "https://upload.wikimedia.org/wikipedia/he/e/e6/The_Weeknd_-_Blinding_Lights.png");

    SongAdapter songAdapter;

    final String PREFS_NAME = "MyPrefsFile";

    File songsFile;

    @Override
    public void onNewSong(String songName, String artistName, String link, String image) {

        newSong = new Song(songName, artistName, link, image);
        songs.add(newSong);
        saveList(this);

        Fragment newSongFragment = getSupportFragmentManager().findFragmentByTag(NEW_SONG_FRAGMENT_TAG);
        getSupportFragmentManager().beginTransaction().remove(newSongFragment).commit();
    }


    @Override
    public void onSong() {
        Fragment songFragment = getSupportFragmentManager().findFragmentByTag(SONG_FRAGMENT_TAG);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_page, songFragment).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Toast.makeText(this, "There was a problem with external storage", Toast.LENGTH_SHORT).show();
        }

        if(Build.VERSION.SDK_INT>=23) {
            int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
            }
        }

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        System.out.println(settings.getBoolean("my_first_time", true));

        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            Log.i("Comments", "First time");

            // first time task
            makeFirstArrayList();

            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).commit();
        } else {
            Log.i("Comments", "NOT First time");
            loadList(this);
        }

        settings.edit().remove("my_first_time").commit();
        System.out.println(settings.getBoolean("my_first_time", true));

        Button addNewSongBtn = findViewById(R.id.add_new_song_btn);
        addNewSongBtn.setBackgroundResource(R.drawable.fade_button);
        addNewSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewSongFragment newSongFragment = NewSongFragment.newInstance();

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.main_page, newSongFragment, NEW_SONG_FRAGMENT_TAG);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        songAdapter = new SongAdapter(songs);

        songAdapter.setListener(new SongAdapter.MySongListener() {
            @Override
            public void onSongClick(int position, View view) {

                isPlaying = !isPlaying;

                String songName = songs.get(position).getName();
                String artistName = songs.get(position).getArtist();
                String link = songs.get(position).getLink();
                String songImage = songs.get(position).getImage();

                SongFragment songFragment = SongFragment.newInstance(songName, artistName, link, songImage, songs, position);

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.main_page, songFragment, SONG_FRAGMENT_TAG);
                transaction.addToBackStack(null);
                transaction.commit();

            }

            @Override
            public void onSongLongClick(int position, View view) {

            }
        });

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                Collections.swap(songs, fromPosition, toPosition);

                recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);

                saveList(MainActivity.this);

                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Confirm delete").setMessage("Are you sure you want to delete this song?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                songs.remove(viewHolder.getAdapterPosition());
                                songAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                                saveList(MainActivity.this);

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                songAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                            }
                        })
                        .setCancelable(false)
                        .show();
            }

        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(songAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveList(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Can't!!!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void makeFirstArrayList() {
        songs.add(song1);
        songs.add(song2);
        songs.add(song3);
        songs.add(song4);
        songs.add(song5);
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }


    public void saveList(AppCompatActivity activity) {
        try {
            FileOutputStream fos = activity.openFileOutput("songsList", activity.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(songs);
            Toast.makeText(MainActivity.this, "list saved", Toast.LENGTH_SHORT).show();
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(MainActivity.this, "FileNotFound", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "IOException", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void loadList (AppCompatActivity activity) {
        try {
            Toast.makeText(MainActivity.this, "list loaded from file", Toast.LENGTH_SHORT).show();
            FileInputStream fis = activity.openFileInput("songsList");
            ObjectInputStream ois = new ObjectInputStream(fis);
            songs = (ArrayList<Song>)ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            Toast.makeText(MainActivity.this, "Fuck it", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}