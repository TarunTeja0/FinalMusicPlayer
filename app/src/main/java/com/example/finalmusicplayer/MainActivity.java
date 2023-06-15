package com.example.finalmusicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> songs;

    private Button button, play, next, prev;
    private SeekBar seekBar;

    private ArrayList<String> songsName;
    int currentSong=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializer();

        Handler handler = new Handler();
        Runnable runnable =  new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        };



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browseSongs();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentSong=position;
                Log.d("number",String.valueOf(position));
                String songPath = songs.get(position);
                playSong(songPath);
                seekBarSize();
                handler.postDelayed(runnable, 1000);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(songs.size() ==  0){
                    Toast.makeText(MainActivity.this, "Select a song", 1).show();
                }
                else if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                else {
                    mediaPlayer.start();
                }
            }
        });

        //Next button
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(currentSong+1 < songsName.size()){
                  String songPath = songs.get(++currentSong);
                  playSong(songPath);
              }
              else{
                  currentSong=0;
                  String songPath = songs.get(currentSong);
                  playSong(songPath);
              }

            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(songs.size() != 0) {
                    if (currentSong - 1 >= 0) {
                        currentSong--;
                        String songPath = songs.get(currentSong);
                        playSong(songPath);
                    } else {
                        currentSong = songs.size() - 1;
                        String songPath = songs.get(currentSong);
                        playSong(songPath);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }


    //Initializing values
    private void initializer(){
        mediaPlayer = new MediaPlayer();
        listView = findViewById(R.id.listView);
        songs = new ArrayList<>();
        songsName = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songsName);
        listView.setAdapter(adapter);
        button = findViewById(R.id.button);
        play = findViewById(R.id.playPauseButton);
        prev =findViewById(R.id.previousButton);
        next = findViewById(R.id.nextButton);
        seekBar= findViewById(R.id.seekBar);

    }

    private void browseSongs() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        startActivityForResult(intent, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    String songPath = uri.toString();
                    songs.add(songPath);
                    songsName.add(getFileNameFromUri(uri));
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri != null) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return fileName;
    }

    private void playSong(String songPath) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(MainActivity.this, Uri.parse(songPath));
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void seekBarHandler(){
//        Handler handler = new Handler();
//        Runnable runnable =  new Runnable() {
//            @Override
//            public void run() {
//                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                    int currentPosition = mediaPlayer.getCurrentPosition();
//                    seekBar.setProgress(currentPosition);
//                }
//                handler.postDelayed(MainActivity.this, 1000);
//            }
//        };
//    }

    //Setting seekbar length
    private void seekBarSize(){
        int songDuration = mediaPlayer.getDuration();
        seekBar.setMax(songDuration);
    }

}


