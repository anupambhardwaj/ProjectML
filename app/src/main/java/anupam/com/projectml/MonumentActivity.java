
package anupam.com.projectml;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;

import java.util.List;

public class MonumentActivity extends AppCompatActivity {

    private ImageView mImageView;
    private FloatingActionButton mCapture;
    private Button mMonument;
    private Button mObject;
    private Button mQRCode;
    private TextView mText;
    private TextView mLong;
    private TextView mLat;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monument);

        mImageView = (ImageView)findViewById(R.id.imageView);
        mCapture = (FloatingActionButton) findViewById(R.id.capture);
        mMonument = (Button)findViewById(R.id.monument_btn);
        mObject = (Button)findViewById(R.id.object_btn);
        mQRCode = (Button)findViewById(R.id.qr_code);
        mText = (TextView)findViewById(R.id.textView);
        mLong = (TextView)findViewById(R.id.textView2);
        mLat= (TextView)findViewById(R.id.textView3);

        FirebaseVisionCloudDetectorOptions options =
                new FirebaseVisionCloudDetectorOptions.Builder()
                        .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                        .setMaxResults(15)
                        .build();

        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        mObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent translate_intent = new Intent(MonumentActivity.this, ObjectLabel.class);
                startActivity(translate_intent);
                finish();
            }
        });

        mQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent monument_intent = new Intent(MonumentActivity.this, MainActivity.class);
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
            recognizeLandmark();
        }
    }

    private void recognizeLandmark() {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                .getVisionCloudLandmarkDetector();

        Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                        for (FirebaseVisionCloudLandmark landmark: firebaseVisionCloudLandmarks) {

                           // Rect bounds = landmark.getBoundingBox();
                            String landmarkName = landmark.getLandmark();
                           // String entityId = landmark.getEntityId();
                           // float confidence = landmark.getConfidence();

                            mText.setText(landmarkName);

                            // Multiple locations are possible, e.g., the location of the depicted
                            // landmark and the location the picture was taken.
                            for (FirebaseVisionLatLng loc: landmark.getLocations()) {
                                double latitude = loc.getLatitude();
                                double longitude = loc.getLongitude();

                                mLong.setText((int) longitude);
                                mLat.setText((int) latitude);

                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MonumentActivity.this, "Error: " + e, Toast.LENGTH_LONG).show();
                    }
                });


    }

}
