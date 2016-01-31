package sample.test.samplemusicplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    //private ImageButton btnNext;
    //private ImageButton btnPrevious;
    //private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    // Media Player
    private MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();
    ;
    private SongsManager songManager;
    private Utilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    private HashMap<String, String> lyrics;

    private TextView lyricsView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // All player buttons
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        //btnNext = (ImageButton) findViewById(R.id.btnNext);
        //btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        //btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

        lyricsView = (TextView) findViewById(R.id.lyrics);

        // Mediaplayer
        mp = MediaPlayer.create(this, R.raw.kareyole);
        songManager = new SongsManager();
        utils = new Utilities();

        initializeLyrics();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important

        // Getting all songs list
        //songsList = songManager.getPlayList();

        // By default play first song
        playSong(0);

        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check for already playing
                if (mp.isPlaying()) {
                    if (mp != null) {
                        mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.btn_play);
                        //lyricsView.pauseScroll();
                    }
                } else {
                    // Resume song
                    if (mp != null) {
                        mp.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.btn_pause);
                        //lyricsView.resumeScroll();
                    }
                }

            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
        btnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if (currentPosition + seekForwardTime <= mp.getDuration()) {
                    // forward song
                    mp.seekTo(currentPosition + seekForwardTime);
                } else {
                    // forward to end position
                    mp.seekTo(mp.getDuration());
                }
            }
        });

        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
        btnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if (currentPosition - seekBackwardTime >= 0) {
                    // forward song
                    mp.seekTo(currentPosition - seekBackwardTime);
                } else {
                    // backward to starting position
                    mp.seekTo(0);
                }

            }
        });

        //TODO - Handle next song
        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        /*btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check if next song is there or not
                if (currentSongIndex < (songsList.size() - 1)) {
                    playSong(currentSongIndex + 1);
                    currentSongIndex = currentSongIndex + 1;
                } else {
                    // play first song
                    playSong(0);
                    currentSongIndex = 0;
                }

            }
        });*/

        //TODO - Handle previous song
        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        /*btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (currentSongIndex > 0) {
                    playSong(currentSongIndex - 1);
                    currentSongIndex = currentSongIndex - 1;
                } else {
                    // play last song
                    playSong(songsList.size() - 1);
                    currentSongIndex = songsList.size() - 1;
                }

            }
        });*/

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isRepeat) {
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                } else {
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }
            }
        });

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isShuffle) {
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                } else {
                    // make repeat to true
                    isShuffle = true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }
            }
        });

        //TODO - Handle the playlist
        /**
         * Button Click event for Play list click event
         * Launches list activity which displays list of songs
         * */
        /*btnPlaylist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
                startActivityForResult(i, 100);
            }
        });*/
    }

    private void initializeLyrics() {
        lyrics = new HashMap<>();
        lyrics.put("0:01", "");
        lyrics.put("0:04", "");
        lyrics.put("0:07", "");
        lyrics.put("0:10", "");
        lyrics.put("0:13", "");
        lyrics.put("0:16", "");
        lyrics.put("0:19", "");
        lyrics.put("0:22", "");
        lyrics.put("0:25", "");
        lyrics.put("0:26", "ಕರೆಯೋಲೆ ಕರೆವ ಓಲೆ ಕರೆ ಮಾಡಿ ಕರೆದೋಲೆ");
        lyrics.put("0:31", "ಕರದಲ್ಲಿ ಕಲಮ ಹಿಡಿದು ಕರಿಶಾಯಿ ಬರೆದೋಲೆ");
        lyrics.put("0:36", "ಕಲ್ಲಿನ ಕೊಳಲಲಿ ಕಲರವ ನುಡಿಸಿ ಕೈಯನು ಬೀಸಿ ಕರೆದೋಳೆ");
        lyrics.put("0:41", "ಕಣ್ಣಿಗೆ ಕಾಣದ ಕಾಗದದಲ್ಲಿ ಕು೦ಚದಿ ಕಾವ್ಯವ ಕೊರೆದೋಲೆ....");
        lyrics.put("0:46", "ಕರೆಯೋಲೆ ಕರೆವ ಓಲೆ ಕರೆ ಮಾಡಿ ಕರೆದೋಲೆ");
        lyrics.put("0:50", "");
        lyrics.put("0:53", "");
        lyrics.put("0:55", "");
        lyrics.put("0:58", "");
        lyrics.put("1:00", "ಕನಕಾ೦ಗಿ ಕೈಯಲ್ಲೊ೦ದು ಕ೦ಚಿನ ಕೊಡಪಾನ");
        lyrics.put("1:05", "ಕೆರೆನೀರ ಕುಡಿಯೋದಕ್ಕು ಕಟುವಾದ ಕಡಿವಾಣ");
        lyrics.put("1:14", "ಕೆರೆದ೦ಡೆ ಕಡೆಯಲ್ಲೆಲ್ಲೋ ಕು೦ತೋನೆ ಕಡುಜಾಣ, ಅತೀ ಕ್ಷೀಣ ಸ್ಮೃತಿಯುಳ್ಳೋನ ");
        lyrics.put("1:21", "ಕೆ೦ದಾವರೆ ಲಕುಷಾಣ,ಕೆ೦ಪಾದ ಕಮಲ ಕ೦ಡು ಕೆಸರಲ್ಲೇ ಕಲೆತೋಳೆ ");
        lyrics.put("1:29", "ಕ್ಷಣವೆಲ್ಲ ಕೃತಕಕಥೆಯಲಿ ಕಳೆಯೋದ ಕಲಿತೋಳೆ, ಕಲ್ಲಿನ ಕೊಳಲಲಿ ಕಲರವ");
        lyrics.put("1:36", "ನುಡಿಸಿ ಕೈಯನು ಬೀಸಿ ಕರೆದೋಳ, ೆಕಣ್ಣಿಗೆ ಕಾಣದ ಕಾಗದದಲ್ಲಿ ಕು೦ಚದಿ ");
        lyrics.put("1:42", "ಕಾವ್ಯವ ಕೊರೆದೋಲ ಕರೆಯೋಲೆ ಕರೆವ ಓಲೆ ಕರೆ ಮಾಡಿ ಕರೆದೋಲ ");
        lyrics.put("1:48", "ಕರದಲ್ಲಿ ಕಲಮ ಹಿಡಿದು ಕರಿಶಾಯಿ ಬರೆದೋಲೆ");
        lyrics.put("1:52", "");
    }

    /**
     * Receiving song index from playlist view
     * and play the song
     */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            currentSongIndex = data.getExtras().getInt("songIndex");
            // play selected song
            playSong(currentSongIndex);
        }

    }

    /**
     * Function to play a song
     *
     * @param songIndex - index of song
     */
    public void playSong(int songIndex) {
        // Play song
        try {
            /*mp.reset();
            mp.setDataSource(this, R.raw.kareyole);
            mp.prepare();*/
            mp.start();
            // Displaying Song title
            //String songTitle = songsList.get(songIndex).get("songTitle");
            songTitleLabel.setText("kareyole");

            // Changing Button Image to pause image
            btnPlay.setImageResource(R.drawable.btn_pause);

            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);

            //lyricsView.setRndDuration(mp.getDuration());
            //lyricsView.startScroll();

            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }/* catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {

            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            // Displaying Total Duration time
            songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));

            String text = lyrics.get("" + utils.milliSecondsToTimer(currentDuration));
            if (text != null) {
                lyricsView.setText(text);
            }

            // Updating progress bar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     */
    @Override
    public void onCompletion(MediaPlayer arg0) {

        // check for repeat is ON or OFF
        if (isRepeat) {
            // repeat is on play same song again
            playSong(currentSongIndex);
        } else if (isShuffle) {
            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            playSong(currentSongIndex);
        } else {
            // no repeat or shuffle ON - play next song
            if (currentSongIndex < (songsList.size() - 1)) {
                playSong(currentSongIndex + 1);
                currentSongIndex = currentSongIndex + 1;
            } else {
                // play first song
                playSong(0);
                currentSongIndex = 0;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null)
            mHandler.removeCallbacks(mUpdateTimeTask);
        mp.release();
    }

}
