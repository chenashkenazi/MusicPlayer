package com.example.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.transform.Result;

import static android.app.Activity.RESULT_OK;

public class NewSongFragment extends Fragment {

    final int CAMERA_REQUEST = 1;
    final int PICK_IMAGE = 2;

    Bitmap bitmap;
    ImageView songImage;
    String uri;

    interface OnNewSongFragmentListener {
        void onNewSong(String songName, String artistName, String link, String image);
    }

    OnNewSongFragmentListener callback;

    public static NewSongFragment newInstance() {
        NewSongFragment newSongFragment = new NewSongFragment();
        return newSongFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.new_song_fragment, container, false);

        EditText songNameEt = rootView.findViewById(R.id.song_name_et);
        EditText artistNameEt = rootView.findViewById(R.id.artist_name_et);
        EditText songURLEt = rootView.findViewById(R.id.url_et);

        Button takePicture = rootView.findViewById(R.id.take_picture_btn);
        Button chooseFromGallery = rootView.findViewById(R.id.choose_from_gallery_btn);
        Button doneBtn = rootView.findViewById(R.id.done_btn);

        songImage = rootView.findViewById(R.id.selected_song_picture);

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST);
            }
        });

        chooseFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_IMAGE);
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onNewSong(songNameEt.getText().toString(), artistNameEt.getText().toString(), songURLEt.getText().toString(), uri);
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (OnNewSongFragmentListener)context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("Implement error");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            bitmap = (Bitmap)data.getExtras().get("data");
            songImage.setImageBitmap(bitmap);
        }

        else if (requestCode == PICK_IMAGE) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(imageStream);
                Glide.with(songImage.getContext())
                        .load(imageUri)
                        .into(songImage);
                uri = imageUri.toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
