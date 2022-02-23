package com.example.medicalstore;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SellerProducts extends AppCompatActivity {

    SearchView searchView;
    ImageButton backBtn;
    RelativeLayout filterBtn;
    RecyclerView productsRecyclerView;
    ImageView productIcon;
    TextView productTitle, productCategory, price, discountedPrice;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    private ArrayList<SellerProduct> productList;
    private SellerProductAdapter sellerProductAdapter;
    ShimmerFrameLayout shimmerFrameLayout;
    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_products);

        filterBtn = findViewById(R.id.filter);
        backBtn = findViewById(R.id.spBackButton);
        productList = new ArrayList<>();
        productsRecyclerView = findViewById(R.id.spRecycleView);
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        shimmerFrameLayout = findViewById(R.id.shimmerLayout);
        searchView = findViewById(R.id.searchBar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loadAllProducts();

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                categoryChoose();


            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                try{
                    sellerProductAdapter.getFilter().filter(query);
                }catch(Exception e){
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try{
                    sellerProductAdapter.getFilter().filter(newText);
                }catch(Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });


    }

    private void categoryChoose() {

        AlertDialog.Builder builder = new AlertDialog.Builder(SellerProducts.this);
        builder.setTitle("Choose Category")
                .setItems(Categories.productCategories1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedItem = Categories.productCategories1[which];
                        if(selectedItem.equals("All")){
                            loadAllProducts();
                        }else{
                            // filter
                            int subcategory = 1;
                            if(selectedItem.equals("Health Care Products")){
                                subcategory = 2;
                            }
                            else if(selectedItem.equals("COVID-19")){
                                subcategory = 3;
                            }
                            subcategoryChoose(subcategory);

                        }
                    }
                })
                .show();
    }

    private void subcategoryChoose(Integer subCat) {

        String[] choice = Categories.subcategories1;
        if(subCat == 2){
            choice = Categories.subcategories2;
        }else if(subCat == 3){
            choice = Categories.subcategories3;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(SellerProducts.this);
        String[] finalChoice = choice;
        builder.setTitle("Choose Subcategory")
                .setItems(choice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedItem = finalChoice[which];
                        loadFilteredProducts(selectedItem);
                    }
                })
                .show();


    }

    private void loadFilteredProducts(String selectedItem) {
        productList = new ArrayList<>();
        // get products from firestore
        shimmerFrameLayout.startShimmerAnimation();

        db.collection("products").whereEqualTo("subcategory", selectedItem).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                productList.clear();
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                        String category = documentSnapshot.get("category").toString();
                        String productID = documentSnapshot.get("productID").toString();
                        String title = documentSnapshot.get("title").toString();
                        String description = documentSnapshot.get("description").toString();
                        String quantity = documentSnapshot.get("quantity").toString();
                        String stock = documentSnapshot.get("stock").toString();
                        String subcategory = documentSnapshot.get("subcategory").toString();
                        String productIcon = documentSnapshot.get("productIcon").toString();
                        String price = documentSnapshot.get("price").toString();
                        String discountedPrice = documentSnapshot.get("discountedPrice").toString();
                        String timestamp = documentSnapshot.get("timestamp").toString();
                        String uid = documentSnapshot.get("uid").toString();
                        SellerProduct sellerProduct = new SellerProduct(productID, title, description, category, subcategory, quantity, productIcon, price, discountedPrice, timestamp, uid, stock);
                        productList.add(sellerProduct);
                }
                sellerProductAdapter = new SellerProductAdapter(SellerProducts.this, productList);
                productsRecyclerView.setAdapter(sellerProductAdapter);
                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
                productsRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();
        // get products from firestore
        shimmerFrameLayout.startShimmerAnimation();
        db.collection("products").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                productList.clear();
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                    String productID = documentSnapshot.get("productID").toString();
                    String title = documentSnapshot.get("title").toString();
                    String description = documentSnapshot.get("description").toString();
                    String category = documentSnapshot.get("category").toString();
                    String subcategory = documentSnapshot.get("subcategory").toString();
                    String quantity = documentSnapshot.get("quantity").toString();
                    String stock = documentSnapshot.get("stock").toString();
                    String productIcon = documentSnapshot.get("productIcon").toString();
                    String price = documentSnapshot.get("price").toString();
                    String discountedPrice = documentSnapshot.get("discountedPrice").toString();
                    String timestamp = documentSnapshot.get("timestamp").toString();
                    String uid = documentSnapshot.get("uid").toString();

                    SellerProduct sellerProduct = new SellerProduct(productID, title, description, category, subcategory, quantity, productIcon, price, discountedPrice, timestamp, uid, stock);
                    productList.add(sellerProduct);
                }
                sellerProductAdapter = new SellerProductAdapter(SellerProducts.this, productList);
                productsRecyclerView.setAdapter(sellerProductAdapter);
                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
                productsRecyclerView.setVisibility(View.VISIBLE);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllProducts();
    }
}