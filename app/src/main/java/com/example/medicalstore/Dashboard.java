package com.example.medicalstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalstore.Adapters.OrderAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    TextView sellerName, sellerShop, sellerPhone, pendingOrders, completedOrders, totalSale;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ArrayList<Order> orderList;
    private OrderAdapter adapter;
    ShimmerFrameLayout shimmerFrameLayout;
    LinearLayout mainLayout;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        toolbar = findViewById(R.id.toolbar);

//        subscribeToTopic(Common.createOrder());

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.sideNav);
        firebaseAuth = FirebaseAuth.getInstance();
        orderList = new ArrayList<>();
        recyclerView = findViewById(R.id.dashboardRV);
        pendingOrders = findViewById(R.id.pendingOrders);
        completedOrders = findViewById(R.id.completedOrders);
        totalSale = findViewById(R.id.totalSale);
        shimmerFrameLayout = findViewById(R.id.dashboardShimmer);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        mainLayout = findViewById(R.id.mainLayout);
        db = FirebaseFirestore.getInstance();

        Common.currentUser = firebaseAuth.getCurrentUser();
        updateToken();

        Menu menu = navigationView.getMenu();
        if(firebaseAuth.getCurrentUser() != null){
            menu.findItem(R.id.mLogin).setVisible(false);
        }else{
            menu.findItem(R.id.mLogout).setVisible(false);
            menu.findItem(R.id.mAccount).setVisible(false);
        }

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar, R.string.navOpen, R.string.navClose);
//        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.mHome);

        View headerView = navigationView.getHeaderView(0);

        sellerName = (TextView) headerView.findViewById(R.id.sNameTV);
        sellerPhone = (TextView) headerView.findViewById(R.id.sPhoneTV);
        sellerShop = (TextView) headerView.findViewById(R.id.sShopTV);


        loadAccountInfo();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadOrders();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void updateToken() {

        FirebaseMessaging.getInstance().getToken()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Dashboard.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                Common.updateToken(Dashboard.this, s, true, false);
            }
        });

    }

//    private void subscribeToTopic(String order) {
//        FirebaseMessaging.getInstance()
//                .subscribeToTopic(order)
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(Dashboard.this, "" +e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(!task.isSuccessful()){
//                    Toast.makeText(Dashboard.this, "Failed: " + task.getException(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }

    @Override
    public void onBackPressed() {

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.mHome:
                break;
            case R.id.mAccount:
                startActivity(new Intent(getApplicationContext(), UserAccount.class));
                break;
            case R.id.mAddProduct:
                Intent addIntent = new Intent(getApplicationContext(), AddProduct.class);
                startActivity(addIntent);
                break;
            case R.id.mProducts:
                Intent productsIntent = new Intent(getApplicationContext(), SellerProducts.class);
                startActivity(productsIntent);
                break;
            case R.id.mBanners:
                startActivity(new Intent(getApplicationContext(), Banners.class));
                break;
            case R.id.mOrders:
                startActivity(new Intent(getApplicationContext(), Orders.class));
                break;
            case R.id.mPrescriptions:
                startActivity(new Intent(getApplicationContext(), Prescriptions.class));
                break;
            case R.id.mLogout:
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadAccountInfo(){
        shimmerFrameLayout.startShimmerAnimation();
        DocumentReference documentReference = db.collection("sellers").document(firebaseAuth.getCurrentUser().getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    String sName = doc.get("name").toString();
                    String sShop = doc.get("shopName").toString();
                    String sPhone = doc.get("phone").toString();
                    sellerName.setText(sName);
                    sellerPhone.setText(sPhone);
                    sellerShop.setText(sShop);

                    loadOrders();
                }else{
                    Toast.makeText(Dashboard.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private int oPending, oCompleted;
    private double oSale;
    private void loadOrders() {


        db.collection("orders").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        orderList.clear();
                        adapter = new OrderAdapter(Dashboard.this, orderList);
                        recyclerView.setAdapter(adapter);
                        oPending = oCompleted = 0;
                        oSale = 0.0;

                        // get month and year
                        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH) + 1;

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String oid = documentSnapshot.get("orderId").toString();
                            String oStatus = documentSnapshot.get("orderStatus").toString();
                            String oCost = documentSnapshot.get("orderCost").toString();
                            String oDate = documentSnapshot.get("orderTime").toString();
                            String oMonth = documentSnapshot.get("month").toString();
                            String oYear = documentSnapshot.get("year").toString();

                            if(oStatus.equals("In Progress") || oStatus.equals("Dispatched")){
                                Order order = new Order(oid, oDate, oStatus, oCost);
                                orderList.add(0, order);
                                adapter.notifyDataSetChanged();
                                oPending++;
                            }else if(oStatus.equals("Delivered") && oMonth.equals("" + month) && oYear.equals("" + year)){
                                oCompleted++;
                                oSale += Double.parseDouble(oCost);
                            }
                        }

                        pendingOrders.setText("" + oPending);
                        completedOrders.setText("" + oCompleted);
                        double finalVal = (double)(Math.round(oSale*1000)/1000);
                        totalSale.setText("₹" + finalVal);
                        mainLayout.setVisibility(View.VISIBLE);
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmerAnimation();
                    }
                });
    }
}