package com.example.mediaplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.stream.QMediaStoreUriLoader;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class SongFragment extends Fragment implements ActionPlaying, ServiceConnection {

    private Thread playThread, previousThread, nextThread;

    public static ArrayList<Song> fragmentSongsList = new ArrayList<>();

    private String songLink;

    private int fPosition = -1;

    // static MediaPlayer mediaPlayer;

    private TextView songLayoutName, songLayoutArtistName, songLayoutDurationPlayed, songLayoutDurationTotal;
    private ImageView previousBtn, playAndPauseBtn, nextBtn, shuffleBtn;
    boolean isShuffle = false;

    private SeekBar seekBar;

    MusicPlayerService musicPlayerService;

    private Handler handler = new Handler();


    interface OnSongFragmentListener {
        void onSong();
    }

    OnSongFragmentListener callback;

    public static SongFragment newInstance(String songName, String artistName, String link, String image, ArrayList<Song> songsList, int position) {

        SongFragment songFragment = new SongFragment();
        Bundle bundle = new Bundle();

        bundle.putString("song_name", songName);
        bundle.putString("artist_name", artistName);
        bundle.putString("song_link", link);
        bundle.putString("song_image", image);
        bundle.putInt("position", position);

        fragmentSongsList = songsList;

        // song time
        songFragment.setArguments(bundle);
        return songFragment;

        /*
        get intent method:
        get the position - V
        get the songs arraylist - V
        set the playpause btn to pause image -
        set the song link -
        do all the media player shit - V in on create

        */
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.song_layout_fragment, container, false);

        songLayoutName = rootView.findViewById(R.id.song_layout_name);
        songLayoutArtistName = rootView.findViewById(R.id.song_layout_artist_name);
        //songLayoutDurationPlayed = rootView.findViewById(R.id.duration_played);
        //songLayoutDurationTotal = rootView.findViewById(R.id.duration_total);
        ImageView songLayoutImage = rootView.findViewById(R.id.song_layout_image);
        // song time

        //seekBar = rootView.findViewById(R.id.seek_bar);

        previousBtn = rootView.findViewById(R.id.song_layout_previos_btn);
        playAndPauseBtn = rootView.findViewById(R.id.song_layout_play_btn);
        nextBtn = rootView.findViewById(R.id.song_layout_next_btn);
        shuffleBtn = rootView.findViewById(R.id.song_layout_shuffle_btn);

        fPosition = getArguments().getInt("position");
        songLink = getArguments().getString("song_link");
        String songImage = getArguments().getString("song_image");

//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (musicPlayerService != null) {
//                    musicPlayerService.seekTo(progress * 1000);
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });

//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (musicPlayerService != null) {
//                    int mCurrentPosition = musicPlayerService.getCurrentPosition() / 1000;
//                    seekBar.setProgress(mCurrentPosition);
//                    songLayoutDurationPlayed.setText(formattedTime(mCurrentPosition));
//                }
//                handler.postDelayed(this, 1000);
//            }
//        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isShuffle) {
                    isShuffle = false;
                    shuffleBtn.setImageResource(R.drawable.shuffle_btn_off);
                } else {
                    isShuffle = true;
                    shuffleBtn.setImageResource(R.drawable.suffle_btn_on);
                }
            }
        });

//        if(musicPlayerService != null) {
//            musicPlayerService.stop();
//            musicPlayerService.release();
//        }
//
//        musicPlayerService.createMediaPlayer(fPosition);
//        musicPlayerService.start();
//        // seek bar

//        mediaPlayer = new MediaPlayer();
//
//        try {
//            mediaPlayer.setDataSource(songLink);
//            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    mediaPlayer.start();
//                }
//            });
//            mediaPlayer.prepareAsync();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        mediaPlayer.setOnCompletionListener(this);

        songLayoutName.setText(getArguments().getString("song_name"));
        songLayoutArtistName.setText(getArguments().getString("artist_name"));
        playAndPauseBtn.setImageResource(R.drawable.pause_btn);

        Glide.with(songLayoutImage.getContext())
                .load(songImage)
                .into(songLayoutImage);

        Intent intent = new Intent(getActivity(), MusicPlayerService.class);
        intent.putExtra("servicePosition", fPosition);
        getActivity().startService(intent);

        //musicPlayerService.OnCompleted();

        return rootView;
    }

//    private void getDurationTotal(String link) {
//        int length = musicPlayerService.getDuration() / 1000;
//        songLayoutDurationTotal.setText(formattedTime(length));
//    }

//    private String formattedTime(int mCurrentPosition) {
//        String totalOut = "";
//        String totalNew = "";
//        String seconds = String.valueOf(mCurrentPosition % 60);
//        String minutes = String.valueOf(mCurrentPosition / 60);
//        totalOut = minutes + ":" + seconds;
//        totalNew = minutes + ":" + "0" + seconds;
//        if (seconds.length() == 1) {
//            return totalNew;
//        }
//        else {
//            return totalOut;
//        }
//    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (OnSongFragmentListener)context;
        } catch(ClassCastException ex) {
            throw new ClassCastException("Implement error");
        }
    }

    @Override
    public void onResume() {
        Intent intent = new Intent(getActivity(), MusicPlayerService.class);
        getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        previousThreadBtn();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unbindService(this);
    }

    private void previousThreadBtn() {
        previousThread = new Thread() {
            @Override
            public void run() {
                super.run();
                previousBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        previousBtnClicked();

                    }
                });
            }
        };
        previousThread.start();
    }

    public void previousBtnClicked() {
        if (musicPlayerService.isPlaying()) {
            musicPlayerService.stop();
            musicPlayerService.release();
            if (isShuffle) {
                fPosition = getRandom(fragmentSongsList.size() - 1);
            }
            else {
                fPosition = ((fPosition - 1) < 0 ? fragmentSongsList.size() - 1 : fPosition - 1);
            }
            // i dont know
            musicPlayerService.createMediaPlayer(fPosition);
            songLayoutName.setText(fragmentSongsList.get(fPosition).getName());
            songLayoutArtistName.setText(fragmentSongsList.get(fPosition).getArtist());
            //getDurationTotal(songLink);
            //seekBar.setMax(musicPlayerService.getDuration() / 1000);
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (musicPlayerService != null) {
//                        int mCurrentPosition = musicPlayerService.getCurrentPosition() / 1000;
//                        seekBar.setProgress(mCurrentPosition);
//                    }
//                    handler.postDelayed(this, 1000);
//                }
//            });
            musicPlayerService.OnCompleted();
            musicPlayerService.showNotification();
            playAndPauseBtn.setImageResource(R.drawable.pause_btn);
        } else {
            musicPlayerService.stop();
            musicPlayerService.release();
            if (isShuffle) {
                fPosition = getRandom(fragmentSongsList.size() - 1);
            }
            else {
                fPosition = ((fPosition - 1) < 0 ? fragmentSongsList.size() - 1 : fPosition - 1);
            }
            musicPlayerService.createMediaPlayer(fPosition);
            songLayoutName.setText(fragmentSongsList.get(fPosition).getName());
            songLayoutArtistName.setText(fragmentSongsList.get(fPosition).getArtist());
            //getDurationTotal(songLink);
            //seekBar.setMax(musicPlayerService.getDuration() / 1000);
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (musicPlayerService != null) {
//                        int mCurrentPosition = musicPlayerService.getCurrentPosition() / 1000;
//                        seekBar.setProgress(mCurrentPosition);
//                    }
//                    handler.postDelayed(this, 1000);
//                }
//            });
            musicPlayerService.OnCompleted();
            musicPlayerService.showNotification();
            playAndPauseBtn.setImageResource(R.drawable.pause_btn);
            // change play button image
        }
    }

    private void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playAndPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
    
                    }
                });
            }
        };
        playThread.start();
    }

    public void nextBtnClicked() {
        if (musicPlayerService.isPlaying()) {
            musicPlayerService.stop();
            musicPlayerService.release();
            if (isShuffle) {
                fPosition = getRandom(fragmentSongsList.size() - 1);
            }
            else {
                fPosition = ((fPosition + 1) % fragmentSongsList.size()); // maybe wrong
            }
            musicPlayerService.createMediaPlayer(fPosition);
            songLayoutName.setText(fragmentSongsList.get(fPosition).getName());
            songLayoutArtistName.setText(fragmentSongsList.get(fPosition).getArtist());
            //getDurationTotal(songLink);
            musicPlayerService.OnCompleted();
            musicPlayerService.showNotification();
            //seekBar.setMax(musicPlayerService.getDuration() / 1000);
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (musicPlayerService != null) {
//                        int mCurrentPosition = musicPlayerService.getCurrentPosition() / 1000;
//                        seekBar.setProgress(mCurrentPosition);
//                    }
//                    handler.postDelayed(this, 1000);
//                }
//            });
            playAndPauseBtn.setImageResource(R.drawable.pause_btn);
        } else {
            musicPlayerService.stop();
            musicPlayerService.release();
            if (isShuffle) {
                fPosition = getRandom(fragmentSongsList.size() - 1);
            }
            else {
                fPosition = ((fPosition + 1) % fragmentSongsList.size());
            }
            musicPlayerService.createMediaPlayer(fPosition);
            songLayoutName.setText(fragmentSongsList.get(fPosition).getName());
            songLayoutArtistName.setText(fragmentSongsList.get(fPosition).getArtist());
            //getDurationTotal(songLink);
            //seekBar.setMax(musicPlayerService.getDuration() / 1000);
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (musicPlayerService != null) {
//                        int mCurrentPosition = musicPlayerService.getCurrentPosition() / 1000;
//                        seekBar.setProgress(mCurrentPosition);
//                    }
//                    handler.postDelayed(this, 1000);
//                }
//            });
            musicPlayerService.OnCompleted();
            musicPlayerService.showNotification();
            playAndPauseBtn.setImageResource(R.drawable.pause_btn);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
    }

    public void playPauseBtnClicked() {
        if (musicPlayerService.isPlaying()) {
            playAndPauseBtn.setImageResource(R.drawable.play_btn);
            musicPlayerService.showNotification();
            musicPlayerService.pause();
            //seekBar.setMax(musicPlayerService.getDuration() / 1000);
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (musicPlayerService != null) {
//                        int mCurrentPosition = musicPlayerService.getCurrentPosition() / 1000;
//                        seekBar.setProgress(mCurrentPosition);
//                    }
//                    handler.postDelayed(this, 1000);
//                }
//            });
        } else {
            playAndPauseBtn.setImageResource(R.drawable.pause_btn);
            musicPlayerService.showNotification();
            musicPlayerService.start();
            //seekBar.setMax(musicPlayerService.getDuration() / 1000);
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (musicPlayerService != null) {
//                        int mCurrentPosition = musicPlayerService.getCurrentPosition() / 1000;
//                        seekBar.setProgress(mCurrentPosition);
//                    }
//                    handler.postDelayed(this, 1000);
//                }
//            });
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicPlayerService.MyBinder myBinder = (MusicPlayerService.MyBinder)service;
        musicPlayerService = myBinder.getService();

        musicPlayerService.setCallBack(this);

        //Toast.makeText(getActivity(), "Connected" + musicPlayerService, Toast.LENGTH_SHORT).show();
        musicPlayerService.OnCompleted();
        musicPlayerService.showNotification();

        //int songLength = musicPlayerService.getDuration() / 1000;
        //System.out.println("fragment, song length: " + songLength);
        //seekBar.setMax(songLength);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicPlayerService = null;
    }

}
