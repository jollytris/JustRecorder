package com.jollytris.simplerecorder;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.jollytris.simplerecorder.R.id.fileName;

/**
 * Created by zic325 on 2016. 10. 4..
 */

public class RecordingDialog extends Dialog {

    private EditText editText;
    private TextView recordingStatus;
    private Button btnStart, btnStop;
    private MediaRecorder recorder;
    private SimpleDateFormat dateFormat;
    private long duration;

    public RecordingDialog(Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.dimAmount = 0.8f;
        lp.windowAnimations = R.style.BaseDialogAnimation;

        setContentView(R.layout.dialog_recording);
        editText = (EditText) findViewById(fileName);
        recordingStatus = (TextView) findViewById(R.id.recordingStatus);
        btnStart = (Button) findViewById(R.id.btnRecording);

        btnStart.setOnClickListener(v -> {
            if (TextUtils.isEmpty(editText.getText().toString())) {
                Toast.makeText(getContext(), R.string.empty_filename, Toast.LENGTH_SHORT).show();
                return ;
            }
            btnStart.setEnabled(false);
            editText.setFocusable(false);
            editText.setClickable(false);

            record();
        });

        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(v -> {
            stop();
            dismiss();
        });
    }

    private void record() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        File f = getContext().getExternalFilesDir(null);
        if (f == null) {
            Toast.makeText(getContext(), R.string.cannot_save_file, Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (!f.exists()) {
                f.mkdirs();
            }
        }

        if (!f.exists()) {
            Toast.makeText(getContext(), R.string.cannot_save_file, Toast.LENGTH_SHORT).show();
        } else {
            String fileName = f.getAbsolutePath() + File.separator + editText.getText().toString() + ".mp4";
            recorder.setOutputFile(fileName);
            try {
                recorder.prepare();
                recorder.start();

                if (dateFormat == null) {
                    dateFormat = new SimpleDateFormat("mm:ss");
                }
                duration = System.currentTimeMillis();
                uiHandler.sendEmptyMessage(0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void stop() {
        uiHandler.removeMessages(0);

        if (recorder == null) {
            return;
        }

        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (recorder != null) {
                recordingStatus.setText(dateFormat.format(new Date(System.currentTimeMillis() - duration)));
                sendEmptyMessageDelayed(0, 1000);
            }
        }
    };
}
