package com.example.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.io.File;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songs;
    private MySongListener listener;

    interface MySongListener {
        void onSongClick(int position, View view);
        void onSongLongClick(int position, View view);
    }

    public void setListener(MySongListener listener) {
        this.listener = listener;
    }

    public SongAdapter(List<Song> songs) {
        this.songs = songs;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {

        TextView songName;
        TextView songArtistName;
        ImageView songImage;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            songName = itemView.findViewById(R.id.song_name);
            songArtistName = itemView.findViewById(R.id.artist_name);
            songImage = itemView.findViewById(R.id.song_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onSongClick(getAdapterPosition(), v);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    return false;
                }
            });

        }
    }

    @NonNull
    @Override
    public SongAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_cell, parent, false);
        SongViewHolder songViewHolder = new SongViewHolder(view);
        return songViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.songName.setText(song.getName());
        holder.songArtistName.setText(song.getArtist());
        Glide.with(holder.songImage.getContext())
                .load(song.getImage())
                .into(holder.songImage);
    }

    @Override
    public int getItemCount() { return songs.size(); }
}
