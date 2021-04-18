package com.example.mediaplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gu.toolargetool.TooLargeTool;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static com.example.mediaplayer.ApplicationClass.ACTION_NEXT;
import static com.example.mediaplayer.ApplicationClass.ACTION_PLAY;
import static com.example.mediaplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.mediaplayer.ApplicationClass.CHANNEL_ID_1;
import static com.example.mediaplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.mediaplayer.SongFragment.fragmentSongsList;

public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    final int NOTIF_ID = 1;

    IBinder mBinder = new MyBinder();

    private MediaPlayer mediaPlayer;
    ArrayList<String> musicLinks = new ArrayList<>();

    int position = -1;
    int length = 0;

    ActionPlaying actionPlaying;

    MediaSessionCompat mediaSessionCompat;


    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int myPosition = intent.getIntExtra("servicePosition", -1);

        String actionName = intent.getStringExtra("ActionName");

        if (myPosition != -1) {
            playMedia(myPosition);
        }
        
        if(actionName != null) {
            switch(actionName) {
                case "playPause":
                    if (actionPlaying != null) {
                        actionPlaying.playPauseBtnClicked();
                    }
                    break;
                case "next":
                    if (actionPlaying != null) {
                        actionPlaying.nextBtnClicked();
                    }
                    break;
                case "previous":
                    if (actionPlaying != null) {
                        actionPlaying.previousBtnClicked();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        for (int i = 0; i < fragmentSongsList.size(); i++) {
            musicLinks.add(fragmentSongsList.get(i).getLink());
        }
        position = startPosition;
        Log.i("testing", "playmedia position: "+ position);
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (musicLinks != null) {
                createMediaPlayer(position);
            }
        } else {
            createMediaPlayer(position);
        }
    }

    void start() {
        mediaPlayer.start();
    }

    boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    void stop() {
        mediaPlayer.stop();
    }

    void release() {
        mediaPlayer.release();
    }

    void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    int getDuration() {
        System.out.println("get duration length: " + length);
        return length;
    }

    int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    void createMediaPlayer(int positionInner) {
        position = positionInner;
        String link = musicLinks.get(position);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);

        try {
            mediaPlayer.setDataSource(link);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
    }

    void pause() {
        mediaPlayer.pause();
    }

    void OnCompleted() {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
            if (mediaPlayer != null) {
                OnCompleted();
            }
        }
    }

    void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

    void showNotification(int playPauseButton) {

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOnlyAlertOnce(true);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);

        Intent previousIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent previousPendingIntent = PendingIntent.getBroadcast(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notif_previous_btn, previousPendingIntent);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notif_play_btn, pausePendingIntent);

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notif_next_btn, nextPendingIntent);

        remoteViews.setTextViewText(R.id.notif_song_title, fragmentSongsList.get(position).getName());
        remoteViews.setTextViewText(R.id.notif_artist, fragmentSongsList.get(position).getArtist());
        remoteViews.setImageViewResource(R.id.notif_play_btn, playPauseButton);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.setContent(remoteViews);
        notificationManager.notify(NOTIF_ID, notification.build());

        startForeground(NOTIF_ID, notification.build());

    }

}
