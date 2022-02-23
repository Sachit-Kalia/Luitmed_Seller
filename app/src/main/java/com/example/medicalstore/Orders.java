package com.example.medicalstore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.medicalstore.Adapters.OrderAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Orders extends AppCompatActivity {

    ImageButton backBtn;
    RecyclerView recyclerView;
    ArrayList<Order> orderList;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    String userId;
    OrderAdapter adapter;
    ShimmerFrameLayout shimmerFrameLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        backBtn = findViewById(R.id.ordersBackButton);
        orderList = new ArrayList<>();
        recyclerView = findViewById(R.id.orderRV);
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

        db.collection("orders").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        orderList.clear();
                        adapter = new OrderAdapter(Orders.this, orderList);
                        recyclerView.setAdapter(adapter);

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String oid = documentSnapshot.get("orderId").toString();
                            String oStatus = documentSnapshot.get("orderStatus").toString();
                            String oCost = documentSnapshot.get("orderCost").toString();
                            String oDate = documentSnapshot.get("orderTime").toString();

                            Order order = new Order(oid, oDate, oStatus, oCost);
                            orderList.add(0, order);
                            adapter.notifyDataSetChanged();
                        }

                        recyclerView.setVisibility(View.VISIBLE);
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmerAnimation();
                    }
                });
    }
}