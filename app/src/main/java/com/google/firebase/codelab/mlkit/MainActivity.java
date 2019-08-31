// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.codelab.mlkit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.camera2.CameraManager;

import androidx.annotation.RequiresApi;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;


import com.google.firebase.codelab.mlkit.ui.camera.CameraSource;
import com.google.firebase.codelab.mlkit.ui.camera.CameraSourcePreview;
import com.google.firebase.codelab.mlkit.ui.camera.GraphicOverlay;

import java.util.regex.Pattern;

import static com.google.firebase.codelab.mlkit.R.color.btn_txtcolor;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    private final int SETTINGS_ACTIVITY_REQUEST_CODE = 0;
    private final int STORAGE_PERMISSION_CODE = 1;


    //speechtotext

    //private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    //OCR

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay<OcrGraphic> graphicOverlay;

    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    // A TextToSpeech engine for speaking a String value.
    private TextToSpeech tts;


    //ALPR

    private int accuracy_percent = 100;

    DatabaseHelper myDb;

    private static final String TAG = "MainActivity";
    private ImageView mImageView;
    private Button btnCapture;
    //private Button mTextButton;
    private Button mFaceButton;
    private Button mCloudButton;
    private Button mRunCustomModelButton;
    private ImageButton mSettings;
    private Bitmap mSelectedImage;
    private GraphicOverlay mGraphicOverlay;

    private Button mClick;
    //String directory_path = Environment.getExternalStorageDirectory().getPath() + "/Backup/";

    private TextView mState1;
    private TextView mState2;

    private TextView mYes, mNo, mData1, mData2, mData3;

    private android.hardware.Camera camera;

    private FrameLayout mCameraFrame;

    private TextureView textureView;


    // Max width (portrait mode)
    private Integer mImageMaxWidth;
    // Max height (portrait mode)
    private Integer mImageMaxHeight;
    /**
     * Name of the model file hosted with Firebase.
     */
    private static final String HOSTED_MODEL_NAME = "cloud_model_1";
    private static final String LOCAL_MODEL_ASSET = "mobilenet_v1_1.0_224_quant.tflite";
    /**
     * Name of the label file stored in Assets.
     */
    private static final String LABEL_PATH = "labels.txt";
    /**
     * Number of results to show in the UI.
     */
    private static final int RESULTS_TO_SHOW = 3;
    /**
     * Dimensions of inputs.
     */
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 6;
    private static final int DIM_IMG_SIZE_X = 224;
    private static final int DIM_IMG_SIZE_Y = 224;
    /**
     * Labels corresponding to the output of the vision model.
     */
    private List<String> mLabelList;

    private final PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float>
                                o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });
    /* Preallocated buffers for storing image data. */
    private final int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];
    /**
     * An instance of the driver class to run model inference with Firebase.
     */
    private FirebaseModelInterpreter mInterpreter;
    /**
     * Data configuration of input & output data of model.
     */
    private FirebaseModelInputOutputOptions mDataOptions;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    System.out.println("Speech xyz"+result.get(0));

                    String s = removeSpecialChar(result.get(0));

                    System.out.println("Validate number "+ validateNumber(s,accuracy_percent));
                    if(validateNumber(s, accuracy_percent)) {
                        //mState1.setText(result.get(0));
                        if(s.length() <=4){
                            processForLastNumber(s);
                        }else{
                            process(s);
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Not a valid number plate", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }

            case SETTINGS_ACTIVITY_REQUEST_CODE:{
                if (resultCode == RESULT_OK) {

                    // Get Integer accuracy data from Settings Intent
                    String accuracyString = data.getStringExtra("accuracy");

                    int accuracyInt = Integer.parseInt(accuracyString);

                    Log.e("Accuracyyyyyyyyyyyyyy", accuracyString);

                    accuracy_percent = accuracyInt;

                }
                break;
            }

        }
       // mSelectedImage = (Bitmap)data.getExtras().get("data");
       // mImageView.setImageBitmap(mSelectedImage);
    }

    //Check state orientation of output image
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;


    //Save to FILE
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;



    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice=null;
        }
    };

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);

        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SpeechToText
        //txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        // hide the action bar
        //getActionBar().hide();

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });


        mSettings = (ImageButton) findViewById(R.id.btn_settings);
        myDb = new DatabaseHelper(this);

        //myDb.insertData("MH05XY2341", 2341, 1, "ab", "cx", "cd");
        //myDb.insertData("MH04XY2242", 2242, 1, "afsd", "cfsdx", "afdscd");
        //myDb.insertData("MH05XY4231", 4231, 1, "abfdsf", "cax", "juycd");

        //mImageView = findViewById(R.id.image_view);
        mSelectedImage = (Bitmap) BitmapFactory.decodeResource(getResources(), R.drawable.number);
        //mImageView.setImageBitmap(mSelectedImage);


        //btnCapture = findViewById(R.id.bt_capture);


        //mGraphicOverlay = findViewById(R.id.graphic_overlay);

        //state1 textview
        mState1 = (TextView) findViewById(R.id.tv_state1);
        //mState1.setClickable(false);


        //state2 textview
        mState2 = (TextView) findViewById(R.id.tv_state2);
        //mState1.setClickable(false);

        mYes = (TextView) findViewById(R.id.tv_yes);
        mNo = (TextView) findViewById(R.id.tv_no);

        mClick = (Button) findViewById(R.id.btn_click);


        preview = (CameraSourcePreview) findViewById(R.id.preview);
        graphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);



        // Set good defaults for capturing text.
        boolean autoFocus = true;
        boolean useFlash = false;

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

        }else{
            requestStoragePermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        Snackbar.make(graphicOverlay, "Place the license plate inside the green box",
                Snackbar.LENGTH_LONG)
                .show();

        // Set up the Text To Speech engine.
        TextToSpeech.OnInitListener listener =
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.d("OnInitListener", "Text to speech engine started successfully.");
                            tts.setLanguage(Locale.US);
                        } else {
                            Log.d("OnInitListener", "Error starting the text to speech engine.");
                        }
                    }
                };
        tts = new TextToSpeech(this.getApplicationContext(), listener);

//        mExport.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View view) {
//                // Export SQLite DB as EXCEL FILE
//                SQLiteToExcel sqliteToExcel;
//                sqliteToExcel = new SQLiteToExcel(getApplicationContext(), myDb.DATABASE_NAME, directory_path);
//                sqliteToExcel.exportAllTables("users.xls", new SQLiteToExcel.ExportListener() {
//                    @Override
//                    public void onStart() {
//
//                    }
//
//                    @Override
//                    public void onCompleted(String filePath) {
//                        Utils.showSnackBar(view, "Successfully Exported");
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        Utils.showSnackBar(view, e.getMessage());
//                    }
//                });
//            }
//        });

        mClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float x=OcrGraphic.x;
                float y=OcrGraphic.y;
                OcrGraphic graphic = graphicOverlay.getGraphicAtLocation(x,y);
                TextBlock text = null;
                if (graphic != null) {
                    text = graphic.getTextBlock();
                    if (text != null && text.getValue() != null) {
                        Log.d(TAG, "text data is being spoken! " + text.getValue());

                        Gson gson = new Gson();

                        // Speak the string.
                        // tts.speak(text.getValue(), TextToSpeech.QUEUE_ADD, null, "DEFAULT");
                        String s=text.getValue();
                        Log.e("fsdjhfkjshkjaf", String.valueOf(accuracy_percent));

                        s = removeSpecialChar(s);

                        System.out.println("Validate number "+ validateNumber(s, accuracy_percent));
                        if(validateNumber(s, accuracy_percent)) {
                            Log.e("ghbdg", s);
                            if(s.length() <=4){
                                processForLastNumber(s);
                            }else{
                                process(s);
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), "Not a valid number plate", Toast.LENGTH_LONG).show();
                        }
//

                    }
                    else {
                        Log.d(TAG, "text data is null");
                    }
                }
                else {
                    Log.d(TAG,"no text detected");
                }
            }
        });

        //settings button onclick listener
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_ACTIVITY_REQUEST_CODE);
            }
        });

        //mData1 = findViewById(R.id.tv_data1);
        //mData2 = findViewById(R.id.tv_data2);
        //mData3 = findViewById(R.id.tv_data3);

        //textureView = (TextureView)findViewById(R.id.textureView);


        //From Java 1.4 , you can use keyword 'assert' to check expression true or false
        /*assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setBackgroundColor(Color.BLUE);
                btnCapture.setTextColor(Color.WHITE);
                takePicture();
                //code to wait for 5 sec
                final long changeTime = 3000L;
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //running text recognition

                        runTextRecognition();
                        view.setBackgroundColor(Color.GRAY);
                        btnCapture.setTextColor(Color.BLACK);
                    }
                }, changeTime);

            }
        });




        initCustomModel();*/
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because of this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(graphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A text recognizer is created to find text.  An associated multi-processor instance
        // is set to receive the text recognition results, track the text, and maintain
        // graphics for each text block on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each text block.
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(graphicOverlay));

        if (!textRecognizer.isOperational()) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        cameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(2.0f)
                        .setFlashMode(useFlash ? android.hardware.Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                        .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null) {
            preview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preview != null) {
            preview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == RC_HANDLE_CAMERA_PERM) {


            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted - initialize the camera source");
                // we have permission, so create the camerasource
                boolean autoFocus = getIntent().getBooleanExtra(AutoFocus,true);
                boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
                createCameraSource(autoFocus, useFlash);
                return;
            }

            Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                    " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Multitracker sample")
                    .setMessage(R.string.no_camera_permission)
                    .setPositiveButton(R.string.ok, listener)
                    .show();
        }

       else{
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    /**
     * onTap is called to speak the tapped TextBlock, if any, out loud.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the tap was on a TextBlock
     */
    private boolean onTap(float rawX, float rawY) {
        OcrGraphic graphic = graphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            text = graphic.getTextBlock();
            if (text != null && text.getValue() != null) {
                Log.d(TAG, "text data is being spoken! " + text.getValue());

                Gson gson = new Gson();

                // Speak the string.
              // tts.speak(text.getValue(), TextToSpeech.QUEUE_ADD, null, "DEFAULT");
                String s=text.getValue();

                s = removeSpecialChar(s);

                System.out.println("Validate number "+ validateNumber(s,accuracy_percent));
                if(validateNumber(s, accuracy_percent)) {
                    if(s.length() <=4){
                        processForLastNumber(s);
                    }else{
                        process(s);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Not a valid number plate", Toast.LENGTH_LONG).show();
                }
//                try {
//
//
//                    String lastnumbers = s.substring(s.length()-4);
//
//
//                    int count = 0;
//
//                    for(int i=lastnumbers.length()-1; i>=0; i--){
//                        if(Character.isDigit(lastnumbers.charAt(i))){
//                           count++;
//                        }
//                        else{
//                            break;
//                        }
//                    }
//
//                    lastnumbers = lastnumbers.substring(lastnumbers.length()-count);
//                    for(int i=0; i<lastnumbers.length(); i++){
//                        tts.speak(String.valueOf(lastnumbers.charAt(i)), TextToSpeech.QUEUE_ADD, null, "DEFAULT");
//                    }
//                    //StringBuffer finalNumbers=new StringBuffer(lastnumbers);
//                    for(int i=0;i<4-count;i++){
//                        lastnumbers='0'+lastnumbers;
//                    }
//                    Log.e("last numberxxxx", lastnumbers);
//                    int int_lastnumbers=0;
//                    mState1.setText(s);
//
//                    int_lastnumbers = Integer.parseInt(lastnumbers);
//
//
//
//
//                    //creating cursor
//                    Cursor cursor = myDb.numberOfV(int_lastnumbers);
//
//                    //checking if record exists or not
//                    if (cursor.getCount() <= 0) {
//                        //if not then insert as new record
//                        mNo.setTextColor(Color.WHITE);
//                        mNo.setBackgroundColor(Color.RED);
//
//                        mYes.setTextColor(Color.BLACK);
//                        mYes.setBackgroundColor(Color.WHITE);
//
//                        //ArrayList<String> list_dates=new ArrayList<String>();
//
//                        //assigning today's date
//                        String date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault()).format(new Date());
//
//                        //list_dates.add(date);
//
//
//                        //String dates= gson.toJson(list_dates);
//
//                        myDb.insertData(s, int_lastnumbers, 1, "1", "2", "3", date);
//
//                        //setting textview clickable
//                        mState1.setClickable(true);
//
//                        //displaying corresponding data
//                        selectState(int_lastnumbers, s);
//
//                        //disabling textview
//                        //mState1.setClickable(false);
//
//
//                    } else {
//                        //if yes then extract data and update columns
//                        mYes.setTextColor(Color.WHITE);
//                        mYes.setBackgroundColor(Color.GREEN);
//
//                        mNo.setTextColor(Color.BLACK);
//                        mNo.setBackgroundColor(Color.WHITE);
//
//                        ArrayList<String> state_codes = new ArrayList<>();
//                        while (cursor.moveToNext()) {
//                            state_codes.add(cursor.getString(0).substring(0, 2));
//
//                        }
//
//                        if (!state_codes.contains(s.substring(0, 2))) {
//                            //ArrayList<String> list_dates=new ArrayList<String>();
//
//                            //assigning today's date
//                            String date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault()).format(new Date());
//
//                            //list_dates.add(date);
//
//
//                            //String dates= gson.toJson(list_dates);
//
//                            myDb.insertData(s, int_lastnumbers, 1, "1", "2", "3", date);
//                        }
//
//                        Object[] arr = state_codes.toArray();
//
//                        if (state_codes.size() == 1) {
//                            mState1.setClickable(true);
//                            //mState2.setClickable(false);
//
//                            mState1.setText((String) arr[0]);
//                            mState2.setText("");
//
//                            selectState(int_lastnumbers, s);
//
//                            //mState1.setClickable(false);
//
//                        } else {
//
//                            //setting textviews clickable
//                            mState1.setClickable(true);
//                            mState2.setClickable(true);
//
//                            mState1.setText((String) arr[0]);
//                            mState2.setText((String) arr[1]);
//
//                            selectState(int_lastnumbers, s);
//
//                            //disabling the textview
//                            //mState1.setClickable(false);
//                            //mState2.setClickable(false);
//                        }
//                    }
//                }catch(NumberFormatException | StringIndexOutOfBoundsException ex ){
//                    AlertDialog.Builder alert;
//                    alert = new AlertDialog.Builder(this);
//                    alert.setTitle("Error");
//                    alert.setMessage("Try again");
//                    alert.show();
//                }


            }
            else {
                Log.d(TAG, "text data is null");
            }
        }
        else {
            Log.d(TAG,"no text detected");
        }
        return text != null;
    }

    private boolean validateNumber(String s, int accuracy_percent){
        mState1.setText("");
        mState2.setText("");
        String match100 = ".*[0-9]{1,4}\\b";//previous regex .*[0-9]{1,4}\b //prev2 [A-Za-z]{1,2}[\s.-]*[0-9]{1,2}[\s.-]*[A-Za-z]{1,2}[\s.-]*[0-9]{1,4}\b
       /* String match75 = ".*[0-9]{1,4}\\b";
        String match50 = ".*[0-9]{1,4}\\b";
        String match25 = ".*[0-9]{1,4}\\b";*/

       /* switch (accuracy_percent){
            case 100:
                return Pattern.matches(match100, s);

            case 75:
                return Pattern.matches(match75, s);

            case 50:
                return Pattern.matches(match50, s);

            case 25:
                return Pattern.matches(match25, s);

                default:
                Toast.makeText(getApplicationContext(), "Incorrect accuracy", Toast.LENGTH_SHORT).show();
        }*/

        return Pattern.matches(match100, s);
    }

    private String removeSpecialChar(String s){
        s = s.replaceAll("\\s+", "");
        s = s.replaceAll("\\.+", "");
        s = s.replaceAll("-+", "");

        return s;
    }

    @SuppressLint("ResourceAsColor")
    private void process(String s){
        try {
            if(s.substring(0,3).equals("IND"))
            {
                s=s.substring(3);
            }

            s = s.toUpperCase();
            //TransitionDrawable transition = (TransitionDrawable) mYes.getBackground();

            String lastnumbers = s.substring(s.length()-4);


            int count = 0;

            for(int i=lastnumbers.length()-1; i>=0; i--){
                if(Character.isDigit(lastnumbers.charAt(i))){
                    count++;
                }
                else{
                    break;
                }
            }

            lastnumbers = lastnumbers.substring(lastnumbers.length()-count);
            for(int i=0; i<lastnumbers.length(); i++){
                tts.speak(String.valueOf(lastnumbers.charAt(i)), TextToSpeech.QUEUE_ADD, null, "DEFAULT");
            }
            //StringBuffer finalNumbers=new StringBuffer(lastnumbers);
            for(int i=0;i<4-count;i++){
                lastnumbers='0'+lastnumbers;
            }
            Log.e("last numberxxxx", lastnumbers);
            int int_lastnumbers=0;
            mState1.setText(s);

            int_lastnumbers = Integer.parseInt(lastnumbers);




            //creating cursor
            Cursor cursor = myDb.numberOfV(int_lastnumbers);

            //checking if record exists or not
            if (cursor.getCount() <= 0) {
                mNo.setBackgroundResource(R.drawable.roundedbutton);
                mNo.setBackgroundColor(Color.RED);

                mYes.setTextColor(btn_txtcolor);
                mYes.setBackgroundResource(R.drawable.roundedbutton);
                //if not then insert as new record
                final long changeTime = 1000L;

                mNo.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNo.setBackgroundResource(R.drawable.roundedbutton);
                        mNo.setTextColor(btn_txtcolor);
                    }
                }, changeTime);


                //transition.reverseTransition(1000);

                //ArrayList<String> list_dates=new ArrayList<String>();

                //assigning today's date
                //String date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault()).format(new Date());

                //list_dates.add(date);


                //String dates= gson.toJson(list_dates);

                Intent intent = new Intent(getApplicationContext(), PopupFormActivity.class);

                intent.putExtra("lastnumbers", String.valueOf(lastnumbers));

                startActivity(intent);

                //myDb.insertData(s, int_lastnumbers, 1, "1", "2", "3", date);

                //setting textview clickable
                mState1.setText("");
                mState1.setClickable(false);



                //displaying corresponding data
                //selectState(int_lastnumbers, s);

                //disabling textview
                //mState1.setClickable(false);


            } else {
                mYes.setBackgroundResource(R.drawable.roundedbutton);
                mYes.setBackgroundColor(Color.GREEN);
                mNo.setTextColor(btn_txtcolor);
                mNo.setBackgroundResource(R.drawable.roundedbutton);
                //if yes then extract data and update columns

                final long changeTime = 1000L;
                mYes.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mYes.setBackgroundResource(R.drawable.roundedbutton);
                        mYes.setTextColor(btn_txtcolor);

                    }
                }, changeTime);

                //transition.startTransition(1000);

                ArrayList<String> state_codes = new ArrayList<>();
                //ArrayList<String> numbers = new ArrayList<>();
                while (cursor.moveToNext()) {
                    state_codes.add(cursor.getString(0).substring(0, 2));
                    //numbers.add(cursor.getString(0));
                }

                /*if (!state_codes.contains(s.substring(0, 2))) {
                    //ArrayList<String> list_dates=new ArrayList<String>();

                    //assigning today's date
                    String date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault()).format(new Date());

                    //list_dates.add(date);


                    //String dates= gson.toJson(list_dates);

                    myDb.insertData(s, int_lastnumbers, 1, "1", "2", "3", date);
                }*/

                Object[] arr = state_codes.toArray();
                //Object[] numbers_arr = state_codes.toArray();

                if (state_codes.size() == 1) {
                    mState1.setClickable(true);


                    mState1.setText(s);
                    if(s.equals(new String(arr[0]+s.substring(2, s.length()))))
                    {
                        mState2.setText("");
                        mState2.setClickable(false);
                    }
                    else{
                        mState2.setClickable(true);
                        mState2.setText(new String(arr[0]+s.substring(2, s.length())));
                    }

                    selectState(int_lastnumbers, s);

                    //mState1.setClickable(false);

                } else {

                    //setting textviews clickable
                    mState1.setClickable(true);
                    mState2.setClickable(true);


                    mState1.setText(new String(arr[0]+s.substring(2, s.length())));
                    mState2.setText(new String(arr[1]+s.substring(2, s.length())));

                    selectState(int_lastnumbers, s);

                    //disabling the textview
                    //mState1.setClickable(false);
                    //mState2.setClickable(false);
                }
            }
        }catch(NumberFormatException | StringIndexOutOfBoundsException ex ){
            AlertDialog.Builder alert;
            alert = new AlertDialog.Builder(this);
            alert.setTitle("Error");
            alert.setMessage("Try again");
            alert.show();
        }
    }

    @SuppressLint("ResourceAsColor")
    private void processForLastNumber(String s){
        try {
            if(s.length() >= 3) {
                if (s.substring(0, 3).equals("IND")) {
                    s = s.substring(3);
                }
            }
            //s = s.toUpperCase();
            //TransitionDrawable transition = (TransitionDrawable) mYes.getBackground();

            String lastnumbers = s;


            int count = 0;

            for(int i=lastnumbers.length()-1; i>=0; i--){
                if(Character.isDigit(lastnumbers.charAt(i))){
                    count++;
                }
                else{
                    break;
                }
            }

            lastnumbers = lastnumbers.substring(lastnumbers.length()-count);
            for(int i=0; i<lastnumbers.length(); i++){
                tts.speak(String.valueOf(lastnumbers.charAt(i)), TextToSpeech.QUEUE_ADD, null, "DEFAULT");
            }
            //StringBuffer finalNumbers=new StringBuffer(lastnumbers);
            for(int i=0;i<4-count;i++){
                lastnumbers='0'+lastnumbers;
            }
            Log.e("last numberxxxx", lastnumbers);
            int int_lastnumbers=0;
            mState1.setText(s);

            int_lastnumbers = Integer.parseInt(lastnumbers);




            //creating cursor
            Cursor cursor = myDb.numberOfV(int_lastnumbers);

            //checking if record exists or not
            if (cursor.getCount() <= 0) {
                mNo.setBackgroundResource(R.drawable.roundedbutton);
                mNo.setBackgroundColor(Color.RED);

                mYes.setTextColor(btn_txtcolor);
                mYes.setBackgroundResource(R.drawable.roundedbutton);
                //if not then insert as new record
                final long changeTime = 1000L;

                mNo.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNo.setBackgroundResource(R.drawable.roundedbutton);
                        mNo.setTextColor(btn_txtcolor);
                    }
                }, changeTime);


                //transition.reverseTransition(1000);

                //ArrayList<String> list_dates=new ArrayList<String>();

                //assigning today's date
                //String date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault()).format(new Date());

                //list_dates.add(date);


                //String dates= gson.toJson(list_dates);

                Intent intent = new Intent(getApplicationContext(), PopupFormActivity.class);

                intent.putExtra("lastnumbers", String.valueOf(lastnumbers));

                startActivity(intent);

                //myDb.insertData(s, int_lastnumbers, 1, "1", "2", "3", date);

                //setting textview clickable
                mState1.setText("");
                mState1.setClickable(false);



                //displaying corresponding data
                //selectState(int_lastnumbers, s);

                //disabling textview
                //mState1.setClickable(false);


            } else {
                mYes.setBackgroundResource(R.drawable.roundedbutton);
                mYes.setBackgroundColor(Color.GREEN);
                mNo.setTextColor(btn_txtcolor);
                mNo.setBackgroundResource(R.drawable.roundedbutton);
                //if yes then extract data and update columns

                final long changeTime = 1000L;
                mYes.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mYes.setBackgroundResource(R.drawable.roundedbutton);
                        mYes.setTextColor(btn_txtcolor);

                    }
                }, changeTime);

                //transition.startTransition(1000);

                ArrayList<String> state_codes = new ArrayList<>();
                ArrayList<String> numbers = new ArrayList<>();
                while (cursor.moveToNext()) {
                    state_codes.add(cursor.getString(0).substring(0, 2));
                    numbers.add(cursor.getString(0));
                }

                /*if (!state_codes.contains(s.substring(0, 2))) {
                    //ArrayList<String> list_dates=new ArrayList<String>();

                    //assigning today's date
                    String date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault()).format(new Date());

                    //list_dates.add(date);


                    //String dates= gson.toJson(list_dates);

                    myDb.insertData(s, int_lastnumbers, 1, "1", "2", "3", date);
                }*/

                Object[] arr = state_codes.toArray();
                Object[] numbers_arr = numbers.toArray();

                if (state_codes.size() == 1) {
                    mState1.setClickable(true);


                    mState1.setText((String)numbers_arr[0]);
//                    if(s.equals(new String(arr[0]+s.substring(2, s.length()))))
//                    {
//                        mState2.setText("");
//                        mState2.setClickable(false);
//                    }
//                    else{
//                        mState2.setClickable(true);
//                        mState2.setText(new String(arr[0]+s.substring(2, s.length())));
//                    }

                    mState2.setText("Add new");
                    mState2.setClickable(true);

                    selectState(int_lastnumbers, (String)numbers_arr[0]);

                    //mState1.setClickable(false);

                } else {

                    //setting textviews clickable
                    mState1.setClickable(true);
                    mState2.setClickable(true);

                    System.out.println(numbers_arr[0]+" hey "+numbers_arr[1]);
                    mState1.setText(new String((String)numbers_arr[0]));
                    mState2.setText(new String((String)numbers_arr[1]));

                    selectState(int_lastnumbers, (String)numbers_arr[0]);

                    //disabling the textview
                    //mState1.setClickable(false);
                    //mState2.setClickable(false);
                }
            }
        }catch(NumberFormatException | StringIndexOutOfBoundsException ex ){
            AlertDialog.Builder alert;
            alert = new AlertDialog.Builder(this);
            alert.setTitle("Error");
            alert.setMessage("Try again");
            alert.show();
        }
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (cameraSource != null) {
                cameraSource.doZoom(detector.getScaleFactor());
            }
        }
    }

    //camera methods

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void takePicture() {
        if(cameraDevice == null)
            return;
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if(characteristics != null)
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);

            //Capture image with custom size
            int width = 640;
            int height = 480;
            if(jpegSizes != null && jpegSizes.length > 0)
            {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            final ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            captureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
            captureBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_HIGH_QUALITY);
            captureBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, CaptureRequest.CONTROL_AE_MODE_ON);

            //Check orientation base on device
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int sensorOrientation =  characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            int surfaceRotation = ORIENTATIONS.get(rotation);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, (surfaceRotation + sensorOrientation + 270)%360);

            file = new File(Environment.getExternalStorageDirectory()+"/"+ UUID.randomUUID().toString()+".jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    try{
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        mSelectedImage = enhanceImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), 3, -40);
                        //wrapping the code to run it on the UI thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mImageView.setImageBitmap(mSelectedImage);
                            }
                        });
                        //save(bytes);

                    }
                    /*catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }*/
                    finally {
                        {
                            if(image != null)
                                image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream outputStream = null;
                    try{
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    }finally {
                        if(outputStream != null)
                            outputStream.close();
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener,mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    //Toast.makeText(MainActivity.this, "Saved "+file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try{
                        cameraCaptureSession.capture(captureBuilder.build(),captureListener,mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            },mBackgroundHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreview() {
        try{
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert  texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updatePreview() {
        if (cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera() {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            //Check realtime permission if run higher API 23
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId,stateCallback,null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

            openCamera();
            configureTransform(i, i1);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            configureTransform(i, i1);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };


    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }*/


    /*@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(textureView.isAvailable()) {
            openCamera();
            configureTransform(textureView.getWidth(), textureView.getHeight());
        }
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }*/

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread= null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

        //recognition methods
    private void runTextRecognition() {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mSelectedImage);
        FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        //
        // ..setEnabled(false);
        recognizer.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
                                //mTextButton.setEnabled(true);
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                //mTextButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {

        /*Gson gson = new Gson();
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            showToast("No text found");
            return;
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                Graphic textGraphic = null;
                String s = "";
                for (int k = 0; k < elements.size(); k++) {
                    textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    mGraphicOverlay.add(textGraphic);
                    s += elements.get(k).getText();


                }


                //Extracting last 4 digits


                try {
                String lastnumbers = s.substring(s.length()-4);
                int int_lastnumbers=0;
                mState1.setText(s);

                    int_lastnumbers = Integer.parseInt(lastnumbers);


                    //creating cursor
                    Cursor cursor = myDb.numberOfV(int_lastnumbers);

                    //checking if record exists or not
                    if (cursor.getCount() <= 0) {
                        //if not then insert as new record
                        mNo.setTextColor(Color.WHITE);
                        mNo.setBackgroundColor(Color.RED);

                        mYes.setTextColor(Color.BLACK);
                        mYes.setBackgroundColor(Color.WHITE);

                        ArrayList<String> list_dates=new ArrayList<String>();

                        //assigning today's date
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                        list_dates.add(date);


                        String dates= gson.toJson(list_dates);

                        myDb.insertData(s, int_lastnumbers, 1, "1", "2", "3", dates);

                        //setting textview clickable
                        mState1.setClickable(true);

                        //displaying corresponding data
                        selectState(int_lastnumbers, s);

                        //disabling textview
                        //mState1.setClickable(false);


                    } else {
                        //if yes then extract data and update columns
                        mYes.setTextColor(Color.WHITE);
                        mYes.setBackgroundColor(Color.GREEN);

                        mNo.setTextColor(Color.BLACK);
                        mNo.setBackgroundColor(Color.WHITE);

                        ArrayList<String> state_codes = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            state_codes.add(cursor.getString(0).substring(0, 2));

                        }

                        if (!state_codes.contains(s.substring(0, 2))) {
                            ArrayList<String> list_dates=new ArrayList<String>();

                            //assigning today's date
                            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                            list_dates.add(date);


                            String dates= gson.toJson(list_dates);

                            myDb.insertData(s, int_lastnumbers, 1, "1", "2", "3", dates);
                        }

                        Object[] arr = state_codes.toArray();

                        if (state_codes.size() == 1) {
                            mState1.setClickable(true);

                            mState1.setText((String) arr[0]);
                            mState2.setText("");
                            //mState1.setClickable(false);

                        } else {

                            //setting textviews clickable
                            mState1.setClickable(true);
                            mState2.setClickable(true);

                            mState1.setText((String) arr[0]);
                            mState2.setText((String) arr[1]);

                            selectState(int_lastnumbers, s);

                            //disabling the textview
                            //mState1.setClickable(false);
                            //mState2.setClickable(false);
                        }
                    }
                }catch(NumberFormatException | StringIndexOutOfBoundsException ex ){
                    AlertDialog.Builder alert;
                    alert = new AlertDialog.Builder(this);
                    alert.setTitle("Error");
                    alert.setMessage("Try again");
                    alert.show();
                }
            }
        }*/
    }

    //method to select one state code to change its flag and show its data
    private void selectState(final int lastnumbers, final String number){
        final Gson gson = new Gson();
        mState1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                view.setBackgroundResource(R.drawable.clickbluebg);
                mState1.setTextColor(Color.WHITE);

                final String number1 = mState1.getText().toString();

                final long changeTime = 1000L;
                view.postDelayed(new Runnable() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void run() {

                        if(myDb.checkIfExists(number1) == false){
                            Intent intent = new Intent(getApplicationContext(), PopupFormActivity.class);

                            intent.putExtra("lastnumbers", String.valueOf(lastnumbers));

                            startActivity(intent);
                        }else {

                            String dates;

                        /*Type type = new TypeToken<ArrayList<String>>() {}.getType();

                        ArrayList<String> list_dates = gson.fromJson(dates, type);

                        String date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault()).format(new Date());

                        list_dates.add(date);


                        dates = gson.toJson(list_dates);*/


                            dates = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault()).format(new Date());

                            myDb.updateDates(number1, dates);
                            //System.out.println(number+" abc "+dates);

                            showResult(lastnumbers, mState1.getText().toString());

                            if (myDb.updateFlag(number1) == true) {
                                Log.e("Flag status", "updated successfully");
                            } else {
                                Log.e("Flag status", "failed to update");
                            }

                            Toast.makeText(getApplicationContext(), "Timestamp updated successfully", Toast.LENGTH_SHORT).show();

                        }
                        view.setBackgroundResource(R.drawable.textviewvioletbg);
                        mState1.setTextColor(btn_txtcolor);
                    }
                }, changeTime);



                mState1.setClickable(false);
                mState2.setClickable(false);
                final long changeTime_removeCode = 3000L;
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mState1.setText("");
                        mState2.setText("");
                    }
                }, changeTime_removeCode);


            }
        });

        mState2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                final String number2 = mState2.getText().toString();

                if(mState2.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "Click on valid text", Toast.LENGTH_SHORT).show();
                }
                else if(mState2.getText().toString().equals("Add new")){
                    Intent intent = new Intent(getApplicationContext(), PopupFormActivity.class);

                    intent.putExtra("lastnumbers", String.valueOf(lastnumbers));

                    startActivity(intent);
                }else
                {

                    view.setBackgroundResource(R.drawable.clickbluebg);
                    mState2.setTextColor(Color.WHITE);

                    final long changeTime = 1000L;
                    view.postDelayed(new Runnable() {
                        @SuppressLint("ResourceAsColor")
                        @Override
                        public void run() {

                            String dates;

                        /*Type type = new TypeToken<ArrayList<String>>() {}.getType();

                        ArrayList<String> list_dates = gson.fromJson(dates, type);

                        String date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault()).format(new Date());

                        list_dates.add(date);


                        dates = gson.toJson(list_dates);*/

                            dates= new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault()).format(new Date());

                            myDb.updateDates(number2, dates);

                            /*showResult(lastnumbers, mState2.getText().toString());*/

                            if (myDb.updateFlag(number2) == true) {
                                Log.e("Flag status", "updated successfully");
                            } else {
                                Log.e("Flag status", "failed to update");
                            }
                            view.setBackgroundResource(R.drawable.textviewvioletbg);
                            mState2.setTextColor(btn_txtcolor);
                        }
                    }, changeTime);

                    //mState2.setTextColor(Color.BLACK);

                    Toast.makeText(getApplicationContext(), "Timestamp updated successfully", Toast.LENGTH_SHORT).show();


                }
                mState1.setClickable(false);
                mState2.setClickable(false);

                final long changeTime_removeCode = 3000L;
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mState1.setText("");
                        mState2.setText("");
                    }
                }, changeTime_removeCode);
            }
        });
    }

    //method to show data1, data2, data3
    private void showResult(int lastnumbers, String statecode){
        Cursor cursor = myDb.numberOfV(lastnumbers);
        while(cursor.moveToNext()){
            if(cursor.getString(0).substring(0, 2).equals(statecode)){
                /*mData1.setText(cursor.getString(3));
                mData2.setText(cursor.getString(4));
                mData3.setText(cursor.getString(5));*/
            }
        }
    }

    //method to get dates string
    private String getDates(int lastnumbers, String statecode){
        Cursor cursor = myDb.numberOfV(lastnumbers);
        while(cursor.moveToNext()){
            if(cursor.getString(0).substring(0, 2).equals(statecode)){
                return cursor.getString(6);
            }
        }
        return "";
    }

    //method to add date into the list
    private void addDate(String number){
        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());


    }

    //configure landscapre rotation
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == textureView || null == imageDimension) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, imageDimension.getHeight(), imageDimension.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / imageDimension.getHeight(),
                    (float) viewWidth / imageDimension.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation || rotation == Surface.ROTATION_0) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }


    private void initCustomModel() {
        mLabelList = loadLabelList(this);

        int[] inputDims = {DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE};
        int[] outputDims = {DIM_BATCH_SIZE, mLabelList.size()};
        try {
            mDataOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.BYTE, inputDims)
                            .setOutputFormat(0, FirebaseModelDataType.BYTE, outputDims)
                            .build();
            FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions
                    .Builder()
                    .requireWifi()
                    .build();
            FirebaseRemoteModel remoteModel = new FirebaseRemoteModel.Builder
                    (HOSTED_MODEL_NAME)
                    .enableModelUpdates(true)
                    .setInitialDownloadConditions(conditions)
                    .setUpdatesDownloadConditions(conditions)  // You could also specify
                    // different conditions
                    // for updates
                    .build();
            FirebaseLocalModel localModel =
                    new FirebaseLocalModel.Builder("asset")
                            .setAssetFilePath(LOCAL_MODEL_ASSET).build();
            FirebaseModelManager manager = FirebaseModelManager.getInstance();
            manager.registerRemoteModel(remoteModel);
            manager.registerLocalModel(localModel);
            FirebaseModelOptions modelOptions =
                    new FirebaseModelOptions.Builder()
                            .setRemoteModelName(HOSTED_MODEL_NAME)
                            .setLocalModelName("asset")
                            .build();
            mInterpreter = FirebaseModelInterpreter.getInstance(modelOptions);
        } catch (FirebaseMLException e) {
            showToast("Error while setting up the model");
            e.printStackTrace();
        }
    }

    private void runModelInference() {
        /*if (mInterpreter == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.");
            return;
        }
        // Create input data.
        ByteBuffer imgData = convertBitmapToByteBuffer(mSelectedImage, mSelectedImage.getWidth(),
                mSelectedImage.getHeight());

        try {
            FirebaseModelInputs inputs = new FirebaseModelInputs.Builder().add(imgData).build();
            // Here's where the magic happens!!
            mInterpreter
                    .run(inputs, mDataOptions)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            showToast("Error running model inference");
                        }
                    })
                    .continueWith(
                            new Continuation<FirebaseModelOutputs, List<String>>() {
                                @Override
                                public List<String> then(Task<FirebaseModelOutputs> task) {
                                    byte[][] labelProbArray = task.getResult()
                                            .<byte[][]>getOutput(0);
                                    List<String> topLabels = getTopLabels(labelProbArray);
                                    mGraphicOverlay.clear();
                                    GraphicOverlay.Graphic labelGraphic = new LabelGraphic
                                            (mGraphicOverlay, topLabels);
                                    mGraphicOverlay.add(labelGraphic);
                                    return topLabels;
                                }
                            });
        } catch (FirebaseMLException e) {
            e.printStackTrace();
            showToast("Error running model inference");
        }*/

    }



    /**
     * Gets the top labels in the results.
     */
    private synchronized List<String> getTopLabels(byte[][] labelProbArray) {
        for (int i = 0; i < mLabelList.size(); ++i) {
            sortedLabels.add(
                    new AbstractMap.SimpleEntry<>(mLabelList.get(i), (labelProbArray[0][i] &
                            0xff) / 255.0f));
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }
        List<String> result = new ArrayList<>();
        final int size = sortedLabels.size();
        for (int i = 0; i < size; ++i) {
            Map.Entry<String, Float> label = sortedLabels.poll();
            result.add(label.getKey() + ":" + label.getValue());
        }
        Log.d(TAG, "labels: " + result.toString());
        return result;
    }

    /**
     * Reads label list from Assets.
     */
    private List<String> loadLabelList(Activity activity) {
        List<String> labelList = new ArrayList<>();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(activity.getAssets().open
                             (LABEL_PATH)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                labelList.add(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read label list.", e);
        }
        return labelList;
    }

    /**
     * Writes Image data into a {@code ByteBuffer}.
     */
    private synchronized ByteBuffer convertBitmapToByteBuffer(
            Bitmap bitmap, int width, int height) {
        ByteBuffer imgData =
                ByteBuffer.allocateDirect(
                        DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        imgData.order(ByteOrder.nativeOrder());
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y,
                true);
        imgData.rewind();
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0,
                scaledBitmap.getWidth(), scaledBitmap.getHeight());
        // Convert the image to int points.
        int pixel = 0;
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                imgData.put((byte) ((val >> 16) & 0xFF));
                imgData.put((byte) ((val >> 8) & 0xFF));
                imgData.put((byte) (val & 0xFF));
            }
        }
        return imgData;
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Functions for loading images from app assets.

    // Returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxWidth() {
        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = mImageView.getWidth();
        }

        return mImageMaxWidth;
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxHeight() {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight =
                    mImageView.getHeight();
        }

        return mImageMaxHeight;
    }

    // Gets the targeted width / height.
    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new Pair<>(targetWidth, targetHeight);
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        mGraphicOverlay.clear();
        switch (position) {
            case 0:
                mSelectedImage = getBitmapFromAsset(this, "Please_walk_on_the_grass.jpg");
                break;
            case 1:
                // Whatever you want to happen when the thrid item gets selected
                mSelectedImage = getBitmapFromAsset(this, "nl2.jpg");
                break;
            case 2:
                // Whatever you want to happen when the thrid item gets selected
                mSelectedImage = getBitmapFromAsset(this, "grace_hopper.jpg");
                break;
            case 3:
                // Whatever you want to happen when the thrid item gets selected
                mSelectedImage = getBitmapFromAsset(this, "tennis.jpg");
                break;
            case 4:
                // Whatever you want to happen when the thrid item gets selected
                mSelectedImage = getBitmapFromAsset(this, "mountain.jpg");
                break;
        }
        if (mSelectedImage != null) {
            // Get the dimensions of the View
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            // Determine how much to scale down the image
            float scaleFactor =
                    Math.max(
                            (float) mSelectedImage.getWidth() / (float) targetWidth,
                            (float) mSelectedImage.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            mSelectedImage,
                            (int) (mSelectedImage.getWidth() / scaleFactor),
                            (int) (mSelectedImage.getHeight() / scaleFactor),
                            true);

            mImageView.setImageBitmap(resizedBitmap);
            mSelectedImage = resizedBitmap;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream is;
        Bitmap bitmap = null;
        try {
            is = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    //to enhance brightness of image
    public static Bitmap enhanceImage(Bitmap mBitmap, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });
        Bitmap mEnhancedBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), mBitmap
                .getConfig());
        Canvas canvas = new Canvas(mEnhancedBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(mBitmap, 0, 0, paint);
        return mEnhancedBitmap;
    }
}
