package com.example.medicalstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Register extends AppCompatActivity implements LocationListener {

    Button loginBtn, registerBtn;
    ImageButton locationBtn;
    TextInputEditText nameTV, phoneTV, addressTV, emailTV, passwordTV, confirmPasswordTV, shopNameTV;
    ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    private String[] locationPermissions;         //permission array
    private String[] cameraPermissions;
    private String[] storagePermissions;


    private LocationManager locationManager;

    private double longitude, latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        loginBtn = (Button) findViewById(R.id.registerLogin);
        registerBtn = (Button) findViewById(R.id.registerBtn);
        locationBtn = (ImageButton) findViewById(R.id.locationBtn);
        nameTV = (TextInputEditText) findViewById(R.id.fullName);
        shopNameTV = (TextInputEditText) findViewById(R.id.shopName);
        phoneTV = (TextInputEditText) findViewById(R.id.phoneNumber);
        addressTV = (TextInputEditText) findViewById(R.id.addressTV);
        emailTV = (TextInputEditText) findViewById(R.id.emailID);
        passwordTV = (TextInputEditText) findViewById(R.id.password);
        confirmPasswordTV = (TextInputEditText) findViewById(R.id.confirmPassword);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect location
                if (checkLocationPermission()) {
                    detectLocation();
                } else {
                    requestLocationPermission();
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });
    }

    private String fullName, shopName, phone, address, email, password, confirmPassword;

    private void inputData() {
        fullName = nameTV.getText().toString().trim();
        shopName = shopNameTV.getText().toString().trim();
        phone = phoneTV.getText().toString().trim();
        address = addressTV.getText().toString().trim();
        email = emailTV.getText().toString().trim();
        password = passwordTV.getText().toString().trim();
        confirmPassword = confirmPasswordTV.getText().toString().trim();

        //validate data

        if(TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Entering name is mandatory", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(shopName)){
            Toast.makeText(this, "Entering shop name is mandatory", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Entering phone number is mandatory", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(address)){
            Toast.makeText(this, "Entering address is mandatory", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Entering email is mandatory", Toast.LENGTH_SHORT).show();
            return;
        }
        if(longitude == 0.0 || longitude == 0.0){
            Toast.makeText(this, "Please press the GPS button to detect location", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattern, retry!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length() < 6){
            Toast.makeText(this, "Password must be at least 6 characters long!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!password.equals(confirmPassword)){
            Toast.makeText(this, "Passwords doesn't match!", Toast.LENGTH_SHORT).show();
            return;
        }
        createNewAccount();
    }

    private void createNewAccount() {
        progressDialog.setMessage("Please wait while we create your account..");
        progressDialog.show();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        saveUserData();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(Register.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void loginInstead(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void saveUserData() {

        progressDialog.setMessage("Saving account information..");

        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> user = new HashMap<>();
        user.put("uid", "" + firebaseAuth.getUid());
        user.put("email", "" + email);
        user.put("name", "" + fullName);
        user.put("shopName", "" + shopName);
        user.put("phone", "" + phone);
        user.put("address", "" + address);
        user.put("latitude", "" + latitude);
        user.put("longitude", "" + longitude);
        user.put("timestamp", "" + timestamp);
        user.put("accountType", "seller");
        user.put("online", "false");

        //add data to database

        db.collection("sellers").document(firebaseAuth.getUid()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(Register.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private boolean checkLocationPermission() {
        boolean res = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);
        return res;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait..", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();

        findAddress();
    }

    private void findAddress(){
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);

            //Set address
            addressTV.setText(address);

        }catch (Exception e){

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, "Please enable your GPS!!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean isLocationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(isLocationAccepted){
                        detectLocation();
                    }else{
                        Toast.makeText(this, "Location permission is necessary!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}