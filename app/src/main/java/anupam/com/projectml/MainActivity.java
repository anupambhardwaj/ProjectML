package anupam.com.projectml;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton mCapture;
    private ImageView mImageView;
    private TextView mText;
    private Bitmap imageBitmap;
    private Button mObject;
    private Button mMonument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView)findViewById(R.id.imageView);
        mCapture = (FloatingActionButton) findViewById(R.id.capture);
        mMonument = (Button) findViewById(R.id.monument_btn);
        mObject = findViewById(R.id.object_btn);
        mText = (TextView)findViewById(R.id.textView);

        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_QR_CODE,
                                FirebaseVisionBarcode.FORMAT_AZTEC,
                                FirebaseVisionBarcode.FORMAT_CODE_39,
                                FirebaseVisionBarcode.FORMAT_CODE_93,
                                FirebaseVisionBarcode.FORMAT_CODE_128)
                        .build();



        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();

            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanCode();
            }
        });

         mObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent object_intent = new Intent(MainActivity.this, ObjectLabel.class);
                startActivity(object_intent);
                finish();
            }
        });

        mMonument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent monument_intent = new Intent(MainActivity.this, MonumentActivity.class);
                startActivity(monument_intent);
                finish();
            }
        });
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);

        }
    }

    private void scanCode() {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector();

        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        for (FirebaseVisionBarcode barcode: barcodes) {
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();

                            String rawValue = barcode.getRawValue();

                            int valueType = barcode.getValueType();
                            // See API reference for complete list of supported types
                            switch (valueType) {
                                case FirebaseVisionBarcode.TYPE_WIFI:
                                  //  String ssid = barcode.getWifi().getSsid();
                                    String password = barcode.getWifi().getPassword();
                                  //  int type = barcode.getWifi().getEncryptionType();
                                    mText.setText(password);
                                    break;
                                case FirebaseVisionBarcode.TYPE_URL:
                                  //  String title = barcode.getUrl().getTitle();
                                    String url = barcode.getUrl().getUrl();
                                    mText.setText(url);
                                    break;
                                case FirebaseVisionBarcode.TYPE_ISBN:
                                    String isbn = barcode.getDisplayValue();
                                    mText.setText(isbn);

                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_LONG).show();
                    }
                });

    }

}


