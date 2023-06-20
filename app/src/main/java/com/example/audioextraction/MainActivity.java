package com.example.audioextraction;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String VIDEO_PATH = "C:\\Users\\Harsh\\OneDrive\\Desktop\\audioextraction\\app\\src\\main\\res\\raw\\dragme.mp4";

    private Button extractButton;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        extractButton = findViewById(R.id.extract_button);
        extractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndExtractAudio();
            }
        });
        Button playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });
    }

    private void checkPermissionsAndExtractAudio() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            extractAudio();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                extractAudio();
            } else {
                Toast.makeText(this, "Permission denied. Cannot extract audio.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void extractAudio() {
        try {
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(VIDEO_PATH);

            int trackCount = extractor.getTrackCount();
            int audioTrackIndex = -1;

            for (int i = 0; i < trackCount; i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i;
                    break;
                }
            }

            if (audioTrackIndex >= 0) {
                extractor.selectTrack(audioTrackIndex);

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(VIDEO_PATH);
                String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long videoDuration = Long.parseLong(duration) * 1000; // In microseconds

                String outputFilePath = Environment.getExternalStorageDirectory() + "/extracted_audio.mp3";
                FileOutputStream outputStream = new FileOutputStream(new File(outputFilePath));

                byte[] buffer = new byte[1024];
                int bufferSize;
                long startTime = System.currentTimeMillis();

                while ((bufferSize = extractor.readSampleData(ByteBuffer.wrap(buffer), 0)) >= 0) {
                    long presentationTime = extractor.getSampleTime();
                    if (presentationTime > videoDuration) {
                        break;
                    }
                    outputStream.write(buffer, 0, bufferSize);
                    extractor.advance();
                }

                long endTime = System.currentTimeMillis();
                long extractionTime = endTime - startTime;

                extractor.release();
                outputStream.flush();
                outputStream.close();

                Toast.makeText(this, "Audio extracted successfully. Extraction time: " + extractionTime + "ms", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No audio track found in the video.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error extracting audio.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void playAudio() {
        if (mediaPlayer == null) {
            String audioFilePath = Environment.getExternalStorageDirectory() + "/extracted_audio.mp3";
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioFilePath);
                mediaPlayer.prepare();
                mediaPlayer.start();
                Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Audio is already playing", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}




