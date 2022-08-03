package com.yin.gesturecontrolapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class PracticeRecording extends AppCompatActivity {

    private static final String PRACTICE = "_PRACTICE_";
    private static final int SELECT_MULTIPLE_VIDEOS = 1;
    private static final String FLASK_SERVER_URL = "http://192.168.1.9:5000/";

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView cameraPreview;
    private Button recordingBtn;
    private Button uploadBtn;
    private VideoCapture videoCapture;
    private String gestureName;
    private String selectedVideo = "";
    private HashMap<String, Integer> recordingCountMap = new HashMap<>();
    private boolean videosSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_recording);

        Intent intent = getIntent();
        gestureName = intent.getExtras().getString("gestureName");

        cameraPreview = findViewById(R.id.recording_view);
        recordingBtn = findViewById(R.id.record_btn);
        uploadBtn = findViewById(R.id.upload_btn);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());

        recordingBtn.setOnClickListener(v -> {
            int currentCount = recordingCountMap.getOrDefault(gestureName, 0) + 1;
            recordingCountMap.put(gestureName, currentCount);
            recordVideo();
        });

        uploadBtn.setOnClickListener(v -> {
            videoSelector(v);
        });
    }


    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, videoCapture);
    }

    @SuppressLint({"RestrictedApi", "MissingPermission"})
    private void recordVideo() {
        if (videoCapture == null) {
            return;
        }

        String recordingName = gestureName + PRACTICE + recordingCountMap.get(gestureName);
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, recordingName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

        VideoCapture.OutputFileOptions recordOutput = new VideoCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        VideoCapture.OnVideoSavedCallback videoSavedCallback = new VideoCapture.OnVideoSavedCallback() {
            @Override
            public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                Toast.makeText(PracticeRecording.this, "Video has been saved successfully.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                Toast.makeText(PracticeRecording.this, "Error saving video: " + message, Toast.LENGTH_SHORT).show();
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        videoCapture.startRecording(
                recordOutput,
                getExecutor(),
                videoSavedCallback
        );

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> videoCapture.stopRecording(), 6000);
    }

    private void videoSelector(View v) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_MULTIPLE_VIDEOS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                Uri uri = data.getData();
                selectedVideo = getPath(getApplicationContext(), uri);
            } else {
                // Stackoverflow answer : https://stackoverflow.com/a/34047251/5426539
                if (data.getClipData() != null) {
                    ClipData clipData = data.getClipData();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        selectedVideo = getPath(getApplicationContext(), uri);
                    }
                }
            }
        }

        if (selectedVideo.isEmpty()) {
            Toast.makeText(this, "You haven't selected any video.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,  "You have selected this video " + selectedVideo, Toast.LENGTH_LONG).show();
        }

        super.onActivityResult(requestCode, resultCode, data);

        try {
            connectFlask();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Thanks @ephemient sharing solution at https://stackoverflow.com/a/45123953
     * @throws FileNotFoundException
     */
    private void connectFlask() throws FileNotFoundException {
        File vFile = new File(selectedVideo);
        Uri videoUri = Uri.fromFile(vFile);
        ContentResolver contentResolver = this.getContentResolver();
        final AssetFileDescriptor fd = contentResolver.openAssetFileDescriptor(videoUri, "r");
        if (fd == null) {
            throw new FileNotFoundException("could not open file descriptor");
        }
        RequestBody videoFile = new RequestBody() {
            @Override public long contentLength() { return fd.getDeclaredLength(); }
            @Override public MediaType contentType() { return MediaType.parse("video/*"); }
            @Override public void writeTo(BufferedSink sink) throws IOException {
                try (InputStream is = fd.createInputStream()) {
                    sink.writeAll(Okio.buffer(Okio.source(is)));
                }
            }
        };
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("video", vFile.getName(), videoFile)
                .build();

        Request request = new Request.Builder()
                .url(FLASK_SERVER_URL)
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                try {
                    fd.close();
                } catch (IOException ex) {
                    e.addSuppressed(ex);
                }
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                fd.close();
            }
        });
    }

    /**
     *  Thanks @Paul Burke's answer: https://stackoverflow.com/a/20559175/5426539
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

}













