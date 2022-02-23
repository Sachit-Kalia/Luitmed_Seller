package com.example.medicalstore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.medicalstore.Adapters.OrderAdapter;
import com.example.medicalstore.Adapters.PrescriptionAdapter;
import com.example.medicalstore.Models.Prescription;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Prescriptions extends AppCompatActivity {

    ImageButton backBtn;
    RecyclerView recyclerView;
    ArrayList<Prescription> prescriptionList;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    String userId;
    PrescriptionAdapter adapter;
    ShimmerFrameLayout shimmerFrameLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescriptions);

        backBtn = findViewById(R.id.prBackButton);
        prescriptionList = new ArrayList<>();
        recyclerView = findViewById(R.id.prescriptionRV);
        shimmerFrameLayout = findViewById(R.id.ordersShimmerLayout);
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = firebaseAuth.getUid();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loadOrders();
    }

    private void loadOrders() {

        shimmerFrameLayout.startShimmerAnimation();

        db.collection("prescriptions").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        prescriptionList.clear();

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String oid = documentSnapshot.get("id").toString();
                            String oDate = documentSnapshot.get("timestamp").toString();

                            Prescription prescription = new Prescription(oid, oDate);
                            prescriptionList.add(prescription);
                        }
                        adapter = new PrescriptionAdapter(Prescriptions.this, prescriptionList);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setVisibility(View.VISIBLE);
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmerAnimation();
                    }
                });
    }
}