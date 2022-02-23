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
import com.squareup.picasso.Picasso;

public class EditProduct extends AppCompatActivity {

    SwitchCompat discountSwitch;
    TextInputLayout discountLayout;
    TextInputEditText titleTV, descTV, priceTV, quantityTV, discPriceTV, stockTV;
    MaterialAutoCompleteTextView categoryTV, subcategoryTV;
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

    String title, description, category, subcategory, quantity, stock, price, discPrice, image, id;
    Boolean isDisc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // get data
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        category = intent.getStringExtra("category");
        subcategory = intent.getStringExtra("subcategory");
        quantity = intent.getStringExtra("quantity");
        stock = intent.getStringExtra("stock");
        price = intent.getStringExtra("price");
        discPrice = intent.getStringExtra("discPrice");
        image = intent.getStringExtra("image");
        isDisc = intent.getBooleanExtra("isDiscount", false);
        id = intent.getStringExtra("id");

        discountLayout = findViewById(R.id.eDiscountLayout);
        discountSwitch = findViewById(R.id.eDiscountSwitch);
        backButton = findViewById(R.id.eBackButton);
        titleTV = findViewById(R.id.epTitle);
        descTV = findViewById(R.id.epDescription);
        priceTV = findViewById(R.id.epPrice);
        quantityTV = findViewById(R.id.epQuantity);
        discPriceTV = findViewById(R.id.epDiscountPrice);
        categoryTV = findViewById(R.id.epCategory);
        subcategoryTV = findViewById(R.id.epSubCategory);
        stockTV = findViewById(R.id.epStock);
        uploadButton = findViewById(R.id.eUploadImage);
        productIcon = findViewById(R.id.eProductIcon);
        addProductBtn = findViewById(R.id.eAddProduct);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // set initial data
        titleTV.setText(title);
        descTV.setText(description);
        priceTV.setText(price);
        quantityTV.setText(quantity);
        discPriceTV.setText(discPrice);
        categoryTV.setText(category);
        subcategoryTV.setText(subcategory);
        stockTV.setText(stock);
        discountSwitch.setChecked(isDisc);
        try{
            Picasso.get().load(image).placeholder(R.color.gray).into(productIcon);
        }catch (Exception e){
            productIcon.setImageResource(R.drawable.splash_image);
        }

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

        subcategoryTV.setOnClickListener(new View.OnClickListener() {
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

    private  String proTitle, proDesc, proCategory, proQuantity, proPrice, proDiscPrice, proStock, proSubCategory;

    private void inputData() {

        proTitle = titleTV.getText().toString().trim();
        proDesc = descTV.getText().toString().trim();
        proCategory = categoryTV.getText().toString().trim();
        proSubCategory = subcategoryTV.getText().toString().trim();
        proQuantity = quantityTV.getText().toString().trim();
        proPrice = priceTV.getText().toString().trim();
        proStock = stockTV.getText().toString().trim();
        isDisc = discountSwitch.isChecked();

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

        if(isDisc){
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

        progressDialog.setMessage("Updating the product..");
        progressDialog.show();

        Double op = Double.parseDouble(proPrice);
        Double dp = Double.parseDouble(proDiscPrice);

        Double discountt = (((op-dp)*100)/op);
        int discount = (int)Math.floor(discountt);
        String disc = String.valueOf(discount);

        if(imageURI == null){

            // update

            db.collection("products").document(id).update("title", "" + proTitle, "description", "" + proDesc, "category", "" + proCategory,
                    "subcategory", "" + proSubCategory,  "quantity", "" + proQuantity, "stock", "" + proStock, "price", "" + proPrice, "discountedPrice", "" + proDiscPrice, "discount", disc)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProduct.this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProduct.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            });

        }
        else{

            // add image to storage
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filePath = "product_images/" + "" + id; //check timestamp
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
                                db.collection("products").document(id).update("title", "" + proTitle, "description", "" + proDesc, "category", "" + proCategory,
                                        "subcategory", "" + proSubCategory, "quantity", "" + proQuantity, "stock", "" + proStock, "price", "" + proPrice, "discountedPrice", "" + proDiscPrice, "productIcon", uri, "discount", disc)
                                        .addOnSuccessListener(aVoid -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(EditProduct.this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProduct.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        }
                    });


        }

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

        if(categoryTV.getText().equals("")){
            Toast.makeText(this, "Select a category first!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(categoryTV.getText().toString().trim().equals("Medicines")){
            toShow = Categories.subcategories1;
        }else if(categoryTV.getText().toString().trim().equals("Health Care Products")){
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
                        subcategoryTV.setText(category);
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