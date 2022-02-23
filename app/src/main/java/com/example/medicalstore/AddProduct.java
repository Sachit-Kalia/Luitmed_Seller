package com.example.medicalstore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddProduct extends AppCompatActivity {


    SwitchCompat discountSwitch;
    TextInputLayout discountLayout;
    TextInputEditText titleTV, descTV, priceTV, quantityTV, discPriceTV, stockTV;
    MaterialAutoCompleteTextView categoryTV, subcategoryTv;
    ImageButton backButton, uploadButton;
    ImageView productIcon;
    Button addProductBtn;


    // permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    // image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    // permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //image uri
    private Uri imageURI;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        discountLayout = findViewById(R.id.discountLayout);
        discountSwitch = findViewById(R.id.discountSwitch);
        backButton = findViewById(R.id.backButton);
        titleTV = findViewById(R.id.pTitle);
        descTV = findViewById(R.id.pDescription);
        priceTV = findViewById(R.id.pPrice);
        quantityTV = findViewById(R.id.pQuantity);
        discPriceTV = findViewById(R.id.pDiscountPrice);
        categoryTV = findViewById(R.id.pCategory);
        subcategoryTv = findViewById(R.id.pSubCategory);
        stockTV = findViewById(R.id.pStock);
        uploadButton = findViewById(R.id.uploadImage);
        productIcon = findViewById(R.id.productIcon);
        addProductBtn = findViewById(R.id.addProduct);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    discountLayout.setVisibility(View.VISIBLE);
                }else{
                    discountLayout.setVisibility(View.GONE);
                }
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        categoryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategories();
            }
        });

        subcategoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSubcategories();
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // input and validate data
                inputData();
                // add to firestore
            }
        });

    }



    private  String proTitle, proDesc, proCategory, proQuantity, proSubCategory, proPrice, proDiscPrice, proStock;
    private boolean isDiscount = false;

    private void inputData() {

        proTitle = titleTV.getText().toString().trim();
        proDesc = descTV.getText().toString().trim();
        proCategory = categoryTV.getText().toString().trim();
        proQuantity = quantityTV.getText().toString().trim();
        proSubCategory = subcategoryTv.getText().toString().trim();
        proPrice = priceTV.getText().toString().trim();
        proStock = stockTV.getText().toString().trim();
        isDiscount = discountSwitch.isChecked();

        // Validation

        if(TextUtils.isEmpty(proTitle)){
            Toast.makeText(this, "Enter Title.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(proDesc)){
            Toast.makeText(this, "Enter Description.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(proCategory)){
            Toast.makeText(this, "Enter Category.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(proSubCategory)){
            Toast.makeText(this, "Enter Subcategory.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(proQuantity)){
            Toast.makeText(this, "Enter Quantity.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(proStock)){
            Toast.makeText(this, "Enter Stock.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(proPrice)){
            Toast.makeText(this, "Enter Price.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(isDiscount){
            proDiscPrice = discPriceTV.getText().toString().trim();
            if(TextUtils.isEmpty(proDiscPrice)){
                Toast.makeText(this, "Enter Discounted Price.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else{
            proDiscPrice = proPrice;
        }

        addProduct();
    }

    private void addProduct() {

        progressDialog.setMessage("Adding the product..");
        progressDialog.show();

        final String timestamp = "" + System.currentTimeMillis();

        Double op = Double.parseDouble(proPrice);
        Double dp = Double.parseDouble(proDiscPrice);

        Integer discount = (int)(((op-dp)/op)*100);


        if(imageURI == null){   // upload without image
            HashMap<String, Object> product = new HashMap<>();
            HashMap<String, Object> sellerProduct = new HashMap<>();
            product.put("productID", "" + timestamp);
            product.put("title", "" + proTitle);
            product.put("searchTitle", "" + proTitle.toUpperCase());
            product.put("description", "" + proDesc);
            product.put("category", "" + proCategory);
            product.put("subcategory", "" + proSubCategory);
            product.put("quantity", "" + proQuantity);
            product.put("stock", "" + proStock);
            product.put("price", "" + proPrice);
            product.put("discountedPrice", "" + proDiscPrice);
            product.put("discount", discount);
            product.put("productIcon", "");  // no image
            product.put("timestamp", timestamp);
            product.put("uid", "" + firebaseAuth.getUid());
            sellerProduct.put("productID", "" + timestamp);


            // add to firestore

            db.collection("products").document(timestamp).set(product).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressDialog.dismiss();
                    Toast.makeText(AddProduct.this, "Added successfully!", Toast.LENGTH_SHORT).show();
                    clearData();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddProduct.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }
        else{

            // add image to storage

            String filePath = "product_images/" + "" + timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePath);
            storageReference.putFile(imageURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();
                            String uri = downloadImageUri.toString();

                            if(uriTask.isSuccessful()){
                                // url of image received
                                HashMap<String, Object> product = new HashMap<>();
                                HashMap<String, Object> sellerProduct = new HashMap<>();

                                product.put("productID", "" + timestamp);
                                product.put("title", "" + proTitle);
                                product.put("searchTitle", "" + proTitle.toUpperCase());
                                product.put("description", "" + proDesc);
                                product.put("category", "" + proCategory);
                                product.put("subcategory", "" + proSubCategory);
                                product.put("quantity", "" + proQuantity);
                                product.put("stock", "" + proStock);
                                product.put("price", "" + proPrice);
                                product.put("discountedPrice", "" + proDiscPrice);
                                product.put("discount", discount);
                                product.put("productIcon", uri);  // no image
                                product.put("timestamp", timestamp);
                                product.put("uid", "" + firebaseAuth.getUid());
                                sellerProduct.put("productID", "" + timestamp);

                                // add to firestore

                                db.collection("products").document(timestamp).set(product).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddProduct.this, "Added successfully!", Toast.LENGTH_SHORT).show();
                                        clearData();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddProduct.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        }
                    });


        }

    }

    private void clearData() {
        titleTV.setText("");
        descTV.setText("");
        categoryTV.setText("");
        quantityTV.setText("");
        priceTV.setText("");
        stockTV.setText("");
        discPriceTV.setText("");
        productIcon.setImageResource(R.drawable.add_cart_icon);
        imageURI = null;
    }


    private void showCategories(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Categories.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String category = Categories.productCategories[which];
                        categoryTV.setText(category);
                    }
                }).show();

    }

    private void showSubcategories() {

        String [] toShow;

        if(categoryTV.getText().toString().equals("")){
            Toast.makeText(this, "Select a category first!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(categoryTV.getText().toString().equals("Medicines")){
            toShow = Categories.subcategories1;
        }else if(categoryTV.getText().toString().equals("Health Care Products")){
            toShow = Categories.subcategories2;
        }else{
            toShow = Categories.subcategories3;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Subcategories")
                .setItems(toShow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String category = toShow[which];
                        subcategoryTv.setText(category);
                    }
                }).show();


    }

    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            if(checkCameraPermissions()) pickFromCamera();
                            else requestCameraPermissions();
                        }
                        else{
                            if(checkStoragePermissions()) pickFromGallery();
                            else requestStoragePermissions();
                        }
                    }
                }).show();
    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_desc");

        imageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermissions(){
        boolean res = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return res;
    }

    private void requestStoragePermissions(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions(){
        boolean res = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean res1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return (res && res1);
    }

    private void requestCameraPermissions(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    // handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }else{
                        Toast.makeText(this, "Camera and Storage access is required. Please grant permissions!!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length > 0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();
                    }else{
                        Toast.makeText(this, "Storage access is required. Please grant permissions!!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // handle image pick results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                imageURI = data.getData();
                productIcon.setImageURI(imageURI);
            }else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                productIcon.setImageURI(imageURI);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}