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
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;

import java.util.List;

public class ObjectLabel extends AppCompatActivity {

    private ImageView mImageView;
    private FloatingActionButton mCapture;
    private Button mMonument;
    private Button mObject;
    private Button mQRCode;
    private TextView mText;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_label);

        mImageView = (ImageView)findViewById(R.id.imageView);
        mCapture = (FloatingActionButton) findViewById(R.id.capture);
        mMonument = (Button)findViewById(R.id.monument_btn);
        mObject = (Button)findViewById(R.id.object_btn);
        mQRCode = (Button)findViewById(R.id.qr_code);
        mText = (TextView)findViewById(R.id.textView);

        FirebaseVisionOnDeviceImageLabelerOptions options =
                new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.8f)
                        .build();

        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();

            }
        });

        mQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent monument_intent = new Intent(ObjectLabel.this, MainActivity.class);
                startActivity(monument_intent);
                finish();
            }
        });

        mMonument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent monument_intent = new Intent(ObjectLabel.this, MonumentActivity.class);
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
            objectLabel();
        }
    }

    private void objectLabel() {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                .getCloudImageLabeler();

        labeler.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                        for (FirebaseVisionImageLabel label: labels) {
                            String text = label.getText();
                            mText.setText(text);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ObjectLabel.this, "Error: " + e, Toast.LENGTH_LONG).show();
                    }
                });


    }
}
