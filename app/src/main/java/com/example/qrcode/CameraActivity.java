package com.example.qrcode;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        previewView = findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        textView = findViewById(R.id.orientation);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindImageAnalysis(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = imageProxy.getImage();
                if (mediaImage != null) {
                    InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                    scanBarcodes(image);
                }
            }
        });

//        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
//            @Override
//            public void onOrientationChanged(int orientation) {
//                textView.setText(Integer.toString(orientation));
//            }
//        };

//        orientationEventListener.enable();
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.createSurfaceProvider());
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector,
                imageAnalysis, preview);
    }

//    private class QRCodeAnalyzer implements ImageAnalysis.Analyzer {
//        @Override
//        public void analyze(ImageProxy imageProxy) {
//            Image mediaImage = imageProxy.getImage();
//            if (mediaImage != null) {
//                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
//                scanBarcodes(image);
//            }
//        }
//    }

    private void scanBarcodes(InputImage image) {
        // [START set_detector_options]
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_QR_CODE,
                                Barcode.FORMAT_AZTEC)
                        .build();
        // [END set_detector_options]

        // [START get_detector]
        BarcodeScanner scanner = BarcodeScanning.getClient();
        // Or, to specify the formats to recognize:
        // BarcodeScanner scanner = BarcodeScanning.getClient(options);
        // [END get_detector]

        // [START run_detector]
        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        // Task completed successfully
                        // [START_EXCLUDE]
                        // [START get_barcodes]

                        for (Barcode barcode : barcodes) {
//                            Rect bounds = barcode.getBoundingBox();
//                            Point[] corners = barcode.getCornerPoints();

                            String rawValue = barcode.getRawValue();

                            Log.v("Barcode", rawValue);

                            sendQRCodeToMainActivity(rawValue);

//                            int valueType = barcode.getValueType();
                            // See API reference for complete list of supported types
//                            switch (valueType) {
//                                case Barcode.TYPE_WIFI:
//                                    String ssid = barcode.getWifi().getSsid();
//                                    String password = barcode.getWifi().getPassword();
//                                    int type = barcode.getWifi().getEncryptionType();
//                                    break;
//                                case Barcode.TYPE_URL:
//                                    String title = barcode.getUrl().getTitle();
//                                    String url = barcode.getUrl().getUrl();
//                                    break;
//                            }
                        }
                        // [END get_barcodes]
                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("Fail", e.toString());
                        // Task failed with an exception
                        // ...
                    }
                });
        // [END run_detector]
    }

    private void sendQRCodeToMainActivity(String QRCodeValue) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("QRCodeValue", QRCodeValue);
        startActivity(intent);
    }
}