package me.pgb.a2021_03_17_radio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;

import java.io.IOException;
import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MAIN__";
    private MediaPlayer mediaPlayer;
    private String url = "http://stream.whus.org:8000/whusfm"; //";//http://vprbbc.streamguys.net:80/vprbbc24.mp3";
    private Button internetRadioButton;
    private Spinner stationSpinner;
    private boolean radioOn;
    private boolean radioWasOnBefore;
    private SeekBar volumeSeekBar;
    private AudioManager mgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        radioOn = false;
        radioWasOnBefore = false;

        mediaPlayer = new MediaPlayer();

        internetRadioButton = findViewById(R.id.internet_radio_button);

        internetRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (radioOn) { // ON so Turn OFF
                    radioOn = false;
                    internetRadioButton.setText("Turn radio ON");
                    if (mediaPlayer.isPlaying()) {
                        Log.i(TAG, "Radio is playing- turning off " );
                        radioWasOnBefore = true;
                    }
                    mediaPlayer.pause();
                } else { // OFF so Turn ON
                    radioOn = true;
                    internetRadioButton.setText("Turn radio OFF");
                    if (!mediaPlayer.isPlaying()) {
                        if (radioWasOnBefore) {
                            mediaPlayer.release();
                            mediaPlayer = new MediaPlayer();
                        }
                        radioSetup(mediaPlayer);
                        mediaPlayer.prepareAsync();
                    }
                }

            }
        });

        //Set up station selection dropdown menu
        stationSpinner = findViewById(R.id.station_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.radio_stations, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stationSpinner.setAdapter(adapter);

        stationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0){
                    url = "http://stream.whus.org:8000/whusfm";
                }
                else if (i == 1){
                    url = "http://vprbbc.streamguys.net:80/vprbbc24.mp3";
                }
                else if (i == 2){
                    url = "http://playerservices.streamtheworld.com/api/livestream-redirect/WTICFM.mp3";
                }
                else if (i == 3){
                    url = "http://stream.revma.ihrhls.com/zc433/hls.m3u8";
                }
                else if (i == 4){
                    url = "http://stream.revma.ihrhls.com/zc445/hls.m3u8";
                }
                if (radioOn) { // ON so Turn OFF
                    if (mediaPlayer.isPlaying()) {
                        Log.i(TAG, "Radio is playing- turning off " );
                        radioWasOnBefore = true;
                    }
                    mediaPlayer.pause();
                    if (!mediaPlayer.isPlaying()) {
                        if (radioWasOnBefore) {
                            mediaPlayer.release();
                            mediaPlayer = new MediaPlayer();
                        }
                        radioSetup(mediaPlayer);
                        mediaPlayer.prepareAsync();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //PASS - Don't do anything if nothing is selected
            }
        });

        //Set Up Volume Slider
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        try{
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            volumeSeekBar.setMax(mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekBar.setProgress(mgr.getStreamVolume(AudioManager.STREAM_MUSIC));
            volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    mgr.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //Sync volume bar with volume buttons
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            volumeSeekBar.setProgress((volumeSeekBar.getProgress()+1>volumeSeekBar.getMax())?volumeSeekBar.getMax():volumeSeekBar.getProgress()+1);
        }
        else if (keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
            volumeSeekBar.setProgress((volumeSeekBar.getProgress()-1<0)?0:volumeSeekBar.getProgress()-1);
        }

        return super.onKeyDown(keyCode, event);
    }

    public void radioSetup(MediaPlayer mediaPlayer) {

        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i(TAG, "onPrepared" );
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.i(TAG, "onError: " + String.valueOf(what).toString());
                return false;
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(TAG, "onCompletion" );
                mediaPlayer.reset();
            }
        });

        try {
            mediaPlayer.setDataSource(url);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpMediaPlayer() {
        Handler handler = null;

        HandlerThread handlerThread = new HandlerThread("media player") {
            @Override
            public void onLooperPrepared() {
                Log.i(TAG, "onLooperPrepared");

            }
        };

    }
}