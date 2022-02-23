package com.example.medicalstore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.medicalstore.Adapters.BannerAdapter;
import com.example.medicalstore.Models.Banner;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class Banners extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton back;
    private RecyclerView ubRecyclerView, lbRecyclerView;
    private BannerAdapter upperAdapter, lowerAdapter;
    private ArrayList<Banner> upperBanners, lowerBanners;
    private FirebaseFirestore db;
    private HashMap<String, String> bannerLoc;
    private Button addUpper, addLower;
    public int action = -1;
    private LinearProgressIndicator linearProgressIndicator;
    private LinearLayout mainLayout;

    // permission constants
    private static final int STORAGE_REQUEST_CODE = 300;

    // image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    // permission arrays
    private String[] storagePermissions;

    //image uri
    private Uri imageURI;
    private ProgressDialog progressDialog;
    public String bannerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banners);

        toolbar = findViewById(R.id.bannerToolbar);
        back = toolbar.findViewById(R.id.tBarBack);
        ubRecyclerView = findViewById(R.id.upperBannerRV);
        lbRecyclerView = findViewById(R.id.lowerBannerRV);
        addLower = findViewById(R.id.addLowerBanner);
        addUpper = findViewById(R.id.addUpperBanner);
        linearProgressIndicator = findViewById(R.id.bannerProgress);
        mainLayout = findViewById(R.id.bannersLayout);
        upperBanners = new ArrayList<>();
        lowerBanners = new ArrayList<>();
        bannerLoc = new HashMap<>();
        db = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addUpper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action = 1;
                showImagePickDialog();
            }
        });

        addLower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action = 2;
                showImagePickDialog();
            }
        });

        loadUpperBanners();
        loadLowerBanners();
    }

    private void loadLowerBanners() {

        db.collection("lowerBanners").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                lowerBanners.clear();
                lowerAdapter = new BannerAdapter(Banners.this, lowerBanners);
                lbRecyclerView.setAdapter(lowerAdapter);

                for(DocumentSnapshot documentSnapshot: queryDocumentSnapshots.getDocuments()){
                    String url = documentSnapshot.get("url").toString();
                    String id = documentSnapshot.getId();

                    Banner banner = new Banner(url, id);
                    lowerBanners.add(banner);
                    bannerLoc.put(id, "lowerBanners");
                    lowerAdapter.notifyDataSetChanged();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Banners.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUpperBanners() {

        db.collection("upperBanners").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                upperBanners.clear();
                upperAdapter = new BannerAdapter(Banners.this, upperBanners);
                ubRecyclerView.setAdapter(upperAdapter);

                for(DocumentSnapshot documentSnapshot: queryDocumentSnapshots.getDocuments()){
                    String url = documentSnapshot.get("url").toString();
                    String id = documentSnapshot.getId();

                    Banner banner = new Banner(url, id);
                    upperBanners.add(banner);
                    bannerLoc.put(id, "upperBanners");
                    upperAdapter.notifyDataSetChanged();
                }
                mainLayout.setVisibility(View.VISIBLE);
                linearProgressIndicator.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                linearProgressIndicator.setVisibility(View.GONE);
                Toast.makeText(Banners.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void showImagePickDialog(){
        String[] options = {"Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(checkStoragePermissions()) pickFromGallery();
                        else requestStoragePermissions();
                    }
                }).show();
    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermissions(){
        boolean res = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return res;
    }

    private void requestStoragePermissions(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    // handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storageAccepted) {
                    pickFromGallery();
                } else {
                    Toast.makeText(this, "Storage access is required. Please grant permissions!!", Toast.LENGTH_SHORT).show();
                }
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
                updateImage();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void updateImage() {
        // add image to storage
        progressDialog.setMessage("Uploading to storage..");
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePath = "banner_images/";
        if(action == 3){
             filePath +=  bannerId;
        }else if(action == 1 || action == 2){
             filePath += timestamp;
        }


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

                            if(action == 3){
                                // update on firestore
                                db.collection(bannerLoc.get(bannerId)).document(bannerId).update("url", uri).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(Banners.this, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                        if(bannerLoc.get(bannerId).equals("lowerBanners")){
                                            loadLowerBanners();
                                        }else{
                                            loadUpperBanners();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(Banners.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            else if(action == 1){
                                HashMap<String, Object> upperBanner = new HashMap<>();
                                upperBanner.put("url", uri);
                                upperBanner.put("id", timestamp);
                                db.collection("upperBanners").document(timestamp).set(upperBanner)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(Banners.this, "Added successfully!", Toast.LENGTH_SHORT).show();
                                                loadUpperBanners();
                                            }
                                        });
                            }else if(action == 2){
                                HashMap<String, Object> lowerBanner = new HashMap<>();
                                lowerBanner.put("url", uri);
                                lowerBanner.put("id", timestamp);
                                db.collection("lowerBanners").document(timestamp).set(lowerBanner)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(Banners.this, "Added successfully!", Toast.LENGTH_SHORT).show();
                                                loadLowerBanners();
                                            }
                                        });
                            }
                        }

                    }
                });
    }

    public void deleteBanner(){

        progressDialog.setMessage("Deleting banner..");
        progressDialog.show();

        db.collection(bannerLoc.get(bannerId)).document(bannerId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Toast.makeText(Banners.this, "Banner deleted successfully.", Toast.LENGTH_SHORT).show();
                if(bannerLoc.get(bannerId).equals("lowerBanners")){
                    loadLowerBanners();
                }else{
                    loadUpperBanners();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(Banners.this, "Couldn't delete this banner.", Toast.LENGTH_SHORT).show();
            }
        });

    }

}