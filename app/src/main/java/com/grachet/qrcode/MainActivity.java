package com.grachet.qrcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import com.google.zxing.WriterException;

//https://developers.google.com/ml-kit/vision/barcode-scanning/android#java
//https://www.natigbabayev.com/2019-07-13/building-qr-code-scanner-for-android-using-firebase-ml-kit-and-camerax

public class MainActivity extends AppCompatActivity {

    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    private TextView QRCodeTextField;
    private ImageView QRCodeImage;
    private Button openUrlButton;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QRCodeImage= findViewById(R.id.qrcodeImage);
        openUrlButton = findViewById(R.id.openUrl);
        QRCodeTextField = findViewById(R.id.QRCodeTextField);

        Button enableCamera = findViewById(R.id.enableCamera);
        enableCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasCameraPermission()) {
                    enableCamera();
                } else {
                    requestPermission();
                }
            }
        });

        generateQRCodeImage();
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
    }

    private void enableCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    private void generateQRCodeImage() {

        String text = getIntent().getStringExtra("text");
        String url = getIntent().getStringExtra("url");

        if (text != null) {
            QRCodeTextField.setText(text);

            getQRCodeImage(text);
        }
        if (url != null) {
            openUrlButton.setVisibility(View.VISIBLE);
            openUrlButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        } else {
            openUrlButton.setVisibility(View.GONE);
        }
    }

      private void getQRCodeImage(String text) {
          // below line is for getting
          // the windowmanager service.
          WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);

          // initializing a variable for default display.
          Display display = manager.getDefaultDisplay();

          // creating a variable for point which
          // is to be displayed in QR Code.
          Point point = new Point();
          display.getSize(point);

          // getting width and
          // height of a point
          int width = point.x;
          int height = point.y;

          // generating dimension from width and height.
          int dimen = width < height ? width : height;
          dimen = dimen * 3 / 4;

          // setting this dimensions inside our qr code
          // encoder to generate our qr code.
          qrgEncoder = new QRGEncoder(text, null, QRGContents.Type.TEXT, dimen);
          try {
              // getting our qrcode in the form of bitmap.
              bitmap = qrgEncoder.encodeAsBitmap();
              // the bitmap is set inside our image
              // view using .setimagebitmap method.
              QRCodeImage.setImageBitmap(bitmap);
          } catch (WriterException e) {
              // this method is called for
              // exception handling.
          }
      }
}