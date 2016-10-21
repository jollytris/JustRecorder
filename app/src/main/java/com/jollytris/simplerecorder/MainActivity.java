package com.jollytris.simplerecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import rx.Observable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private View emptyView;
    private View soundContainer;
    private SeekBar seekBar;
    private TextView timeCurrent, timeTotal, soundTitle;
    private Button btnPrev, btnPlay, btnNext;
    private RecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<RecyclerViewData> items;
    private SimpleDateFormat dateFormat;
    private MediaPlayer mediaPlayer;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        soundContainer = findViewById(R.id.playerContainer);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setPadding(0, 0, 0, 0);

        timeCurrent = (TextView) findViewById(R.id.txtCurrent);
        timeTotal = (TextView) findViewById(R.id.txtTotal);
        soundTitle = (TextView) findViewById(R.id.soundTitle);
        btnPrev = (Button) findViewById(R.id.btnPrevious);
        btnPrev.setOnClickListener(this);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);

        emptyView = findViewById(R.id.emptyView);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter((index, item) -> {
            if (index == 0) {
                clickItem(item);
            } else if (index == 1) {
                deleteItem(item);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewAdapter);

        loadItems();
        applyItems();

        dateFormat = new SimpleDateFormat("mm:ss");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        requestPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                showRecordingPopup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            stopPlayer();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnPrevious:
                currentIndex--;
                if (currentIndex < 0) {
                    currentIndex = items.size() - 1;
                }
                stopPlayer();
                clickItem(items.get(currentIndex));
                break;
            case R.id.btnNext:
                currentIndex++;
                if (currentIndex > items.size() - 1) {
                    currentIndex = 0;
                }
                stopPlayer();
                clickItem(items.get(currentIndex));
                break;
            case R.id.btnPlay:
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        pausePlayer();
                        btnPlay.setBackgroundResource(R.drawable.selector_ic_play);
                    } else {
                        playPlayer();
                        btnPlay.setBackgroundResource(R.drawable.selector_ic_pause);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissions = new ArrayList<String>();
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }

            if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.INTERNET);
            }

            if (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
            }

            if (permissions.size() > 0) {
                String[] params = new String[permissions.size()];
                for (int i = 0; i < params.length; i++) {
                    params[i] = permissions.get(i);
                }
                requestPermissions(params, 0);
            }
        }
    }

    private void loadItems() {
        emptyView.setVisibility(View.VISIBLE);
        if (items == null) {
            items = new ArrayList<>();
        } else {
            items.clear();
        }

        File root = getExternalFilesDir(null);
        if (root != null && root.exists()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (File f : files) {
                    items.add(new RecyclerViewData(f.getName().replace(".mp4", ""), f.getAbsolutePath()));
                }
            }
        }

        if (items.size() > 0) {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void applyItems() {
        recyclerViewAdapter.clear();
        Observable.from(items).subscribe(data -> {
            recyclerViewAdapter.add(data);
        });
    }

    private void clickItem(RecyclerViewData item) {
        for (int i = 0; i < items.size(); i++) {
            if (item == items.get(i)) {
                currentIndex = i;
            }
        }

        recyclerViewAdapter.setSelected(item);
        showSoundContainer(item.isSelected());

        if (item.isSelected()) {
            preparePlayer(recyclerViewAdapter.getSelectedItem());
        } else {
            stopPlayer();
        }
    }

    private void deleteItem(RecyclerViewData item) {
        if (recyclerViewAdapter.getSelectedItem() == item) {
            if (recyclerViewAdapter.getItemCount() == 1) {
                showSoundContainer(false);
            } else {
                currentIndex++;
                if (currentIndex > items.size() - 1) {
                    currentIndex = 0;
                }
                stopPlayer();
                clickItem(items.get(currentIndex));
            }
        }
        String path = item.getPath();
        File f = new File(path);
        if (f != null && f.exists()) {
            f.delete();
        }
        recyclerViewAdapter.remove(item);

        if (recyclerViewAdapter.getItemCount() <= 0) {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void showSoundContainer(boolean show) {
        if (show) {
            if (soundContainer.getVisibility() == View.VISIBLE) {
                return;
            }
            soundContainer.setVisibility(View.VISIBLE);
            soundContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));
        } else {
            if (soundContainer.getVisibility() == View.GONE) {
                return;
            }
            soundContainer.setVisibility(View.GONE);
            soundContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_down));
        }
    }

    private void preparePlayer(RecyclerViewData item) {
        if (item == null) {
            showError("Invalid item selected.");
            return;
        }

        soundTitle.setText(item.getTitle());

        if (mediaPlayer != null) {
            stopPlayer();
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(item.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setLooping(true);

        mediaPlayer.setOnPreparedListener(mp -> {
            int duration = mp.getDuration();
            if (duration < 1) {
                showError("Failed to load file.");
                return;
            }

            seekBar.setProgress(0);
            seekBar.setMax(duration);

            timeCurrent.setText("00:00");
            timeTotal.setText(dateFormat.format(new Date(mp.getDuration())));
            mp.seekTo(0);
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            showError("Error (" + what + ", " + extra + ")");
            return false;
        });

        if (item != null && !TextUtils.isEmpty(item.getPath())) {
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void playPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            playerObserveHandler.sendEmptyMessage(0);
        }
    }

    private void pausePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
        playerObserveHandler.removeMessages(0);
    }

    private void stopPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        playerObserveHandler.removeMessages(0);
        btnPlay.setBackgroundResource(R.drawable.selector_ic_play);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showRecordingPopup() {
        stopPlayer();
        showSoundContainer(false);

        RecordingDialog dialog = new RecordingDialog(this);
        dialog.setOnDismissListener(dlg -> {
            loadItems();
            applyItems();
        });
        dialog.show();
    }

    private Handler playerObserveHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (mediaPlayer != null) {
                int position = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(position);
                timeCurrent.setText(dateFormat.format(new Date(position)));
                playerObserveHandler.sendEmptyMessageDelayed(0, 1000);
            }
        }
    };
}
