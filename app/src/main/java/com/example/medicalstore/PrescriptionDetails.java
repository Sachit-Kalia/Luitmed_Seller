package com.example.medicalstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class PrescriptionDetails extends AppCompatActivity {

    private String id = "", phone = "";
    private ImageButton back;
    private PhotoView photoView;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private TextView nameTv, phoneTv, emailTv, dateTv, idTv;
    ShimmerFrameLayout shimmerFrameLayout;
    private ScrollView mainLayout;
    private Button whatsapp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_details);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        back = findViewById(R.id.prdBackButton);
        photoView = findViewById(R.id.photoView);
        nameTv = findViewById(R.id.prdName);
        phoneTv = findViewById(R.id.prdPhone);
        emailTv = findViewById(R.id.prdEmail);
        dateTv = findViewById(R.id.prdDate);
        idTv = findViewById(R.id.prdID);
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        shimmerFrameLayout = findViewById(R.id.pdShimmer);
        mainLayout = findViewById(R.id.pdMainLayout);
        whatsapp = findViewById(R.id.chatWhatsapp);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isWhatappInstalled()){

                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=+91"+phone+
                            "&text="));
                    startActivity(i);

                }else {

                    Toast.makeText(PrescriptionDetails.this,"Whatsapp is not installed",Toast.LENGTH_SHORT).show();

                }

            }
        });

        loadDetails();

    }

    private void loadDetails() {

        shimmerFrameLayout.startShimmerAnimation();

        db.collection("prescriptions").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        // set all details
                        String cid = documentSnapshot.get("cId").toString();
                        String date = documentSnapshot.get("timestamp").toString();
                        String image = documentSnapshot.get("image").toString();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(date));
                        String formattedDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

                        db.collection("users").document(cid).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String name = documentSnapshot.get("name").toString();
                                        phone = documentSnapshot.get("phone").toString();
                                        String email = documentSnapshot.get("email").toString();

                                        idTv.setText(id);
                                        nameTv.setText(name);
                                        phoneTv.setText(phone);
                                        emailTv.setText(email);
                                        dateTv.setText(formattedDate);

                                        try{
                                            Picasso.get().load(image).placeholder(R.color.light_gray).into(photoView);
                                        }catch (Exception e){
                                            photoView.setImageResource(R.drawable.splash_image);
                                        }
                                        shimmerFrameLayout.stopShimmerAnimation();
                                        mainLayout.setVisibility(View.VISIBLE);
                                        shimmerFrameLayout.setVisibility(View.GONE);
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private boolean isWhatappInstalled(){

        PackageManager packageManager = getPackageManager();
        boolean whatsappInstalled;

        try {

            packageManager.getPackageInfo("com.whatsapp",PackageManager.GET_ACTIVITIES);
            whatsappInstalled = true;


        }catch (PackageManager.NameNotFoundException e){

            whatsappInstalled = false;

        }

        return whatsappInstalled;

    }

}