package com.example.medicalstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalstore.Services.JavaMailAPI;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class OrderDetails extends AppCompatActivity {

    Toolbar toolbar;
    ImageButton back, editStatus, assignOrder;
    String id;
    TextView costTvf, statusTvf, dateTvf, addressTvf, idTVf, nameTvf, phoneTvf;
    RecyclerView recyclerView;
    ArrayList<CartItem> orderList;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    OrderDetailsAdapter adapter;
    LinearLayout linearLayout;
    ShimmerFrameLayout shimmerFrameLayout;
    String[] statusArr = {"In Progress", "Dispatched", "Cancelled", "Delivered"};
    ProgressDialog progressDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        toolbar = findViewById(R.id.toolbar1);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        back = toolbar.findViewById(R.id.tBarBack);
        editStatus = findViewById(R.id.editStatus);
        idTVf = findViewById(R.id.detailID);
        statusTvf = findViewById(R.id.detailStatus);
        costTvf = findViewById(R.id.detailCost);
        dateTvf = findViewById(R.id.detailDate);
        addressTvf = findViewById(R.id.detailAddress);
        nameTvf = findViewById(R.id.detailName);
        phoneTvf = findViewById(R.id.detailPhone);

        recyclerView = findViewById(R.id.orderItemRV);
        shimmerFrameLayout = findViewById(R.id.odShimmer);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        linearLayout = findViewById(R.id.odLayout);
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        orderList = new ArrayList<>();


        idTVf.setText(id);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loadOrderDetails();

        editStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptions();
            }
        });
    }



    private void showOptions() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit order status").
                setItems(statusArr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newStatus = statusArr[which];
                        if(!newStatus.equals(statusTvf.getText().toString()) && !(statusTvf.getText().toString().equals("Cancelled")) && !(statusTvf.getText().toString().equals("Delivered"))){
                            statusTvf.setText(newStatus);
                            updateStatus(newStatus);
                        }
                    }
                }).create();

        builder.show();
    }

    private void updateStatus(String newStatus) {

        progressDialog.show();

        db.collection("orders").document(id)
                .update("orderStatus", newStatus).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                if(newStatus.equals("Cancelled")){
                    cancelOrder();
                }else{
                    Toast.makeText(OrderDetails.this, "Status updated successfully!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });

    }

    private void cancelOrder() {

        DocumentReference documentReference = db.collection("orders").document(id);

        documentReference.collection("items").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot documentSnapshot: queryDocumentSnapshots.getDocuments()){
                    String pid = documentSnapshot.get("pId").toString();
                    int num = Integer.parseInt(documentSnapshot.get("number").toString());

                    db.collection("products").document(pid).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot ds) {

                                    int stock = Integer.parseInt(ds.get("stock").toString()) + num;
                                    db.collection("products").document(pid).update("stock", "" + num);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(OrderDetails.this, "Failed to cancel the order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                documentReference.update("orderStatus", "Cancelled")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
//                                statusTvf.setText("Cancelled");
                                progressDialog.dismiss();
                                sendConfirmationMail();
                                Toast.makeText(OrderDetails.this, "Order cancelled successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    String sellerId, image, cost, date, status, address, formattedDate, name, phone;

    private void loadOrderDetails() {

        shimmerFrameLayout.startShimmerAnimation();


        db.collection("orders").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot ds) {

                        orderList.clear();

                        cost = ds.get("orderCost").toString();
                        date = ds.get("orderTime").toString();
                        status = ds.get("orderStatus").toString();
                        address = ds.get("Address").toString();
                        name = ds.get("name").toString();
                        phone = ds.get("phone").toString();

                        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aaa  dd/MM/yyyy");
                        String formattedDate = formatter.format(new Date(Long.parseLong(date)));

                        costTvf.setText("₹" + cost);
                        dateTvf.setText(formattedDate);
                        statusTvf.setText(status);
                        addressTvf.setText(address);
                        nameTvf.setText(name);
                        phoneTvf.setText(phone);


                        db.collection("orders").document(id).collection("items").get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                                            String title = documentSnapshot.get("name").toString();
                                            String number = documentSnapshot.get("number").toString();
                                            String price = documentSnapshot.get("price").toString();
                                            String quantity = documentSnapshot.get("quantity").toString();
                                            String pid = documentSnapshot.get("pId").toString();


                                            db.collection("products").document(pid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    image = documentSnapshot.get("productIcon").toString();

                                                    CartItem cartItem = new CartItem("1", pid, title, price, price, quantity, number, image);
                                                    orderList.add(cartItem);
                                                    adapter = new OrderDetailsAdapter(OrderDetails.this, orderList);
                                                    recyclerView.setAdapter(adapter);
                                                }
                                            });
                                        }
                                        shimmerFrameLayout.stopShimmerAnimation();
                                        shimmerFrameLayout.setVisibility(View.GONE);
                                        linearLayout.setVisibility(View.VISIBLE);
                                    }
                                });

                    }
                });
    }

    private String mailId = "";
    private void sendConfirmationMail() {

        db.collection("orders").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String cid = documentSnapshot.get("orderBy").toString().trim();

                        db.collection("users").document(cid).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                          mailId = documentSnapshot.get("email").toString();
                                        String msg = "<h3>Order cancelled!!</h3> <p>Your order with order id " + id+ " has been cancelled. Sorry for any inconvenience.</p> </br></br> <p>Prashanti Medicos</p>";
                                        String subject = "Your PRASHANTI-MEDICOS order #"+ id + " has been cancelled by seller.";

                                        // send mail
                                        JavaMailAPI javaMailAPI = new JavaMailAPI(OrderDetails.this, mailId, subject, msg);
                                        javaMailAPI.execute();
                                    }
                                });
                    }
                });
    }

}