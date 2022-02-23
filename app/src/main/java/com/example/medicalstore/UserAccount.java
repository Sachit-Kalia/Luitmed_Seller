package com.example.medicalstore;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserAccount extends AppCompatActivity {

    TextView emailTv;
    EditText nameTv, phoneTv;
    String name, email, phone;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    Button update;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);
        phoneTv = findViewById(R.id.uAcPhone);
        nameTv = findViewById(R.id.uAcNAme);
        emailTv = findViewById(R.id.uAcEmail);
        update = findViewById(R.id.updateBtn);
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        getUserData();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData();
            }
        });
    }

    private void updateUserData() {

        db.collection("sellers").document(firebaseAuth.getUid()).update("phone", phoneTv.getText().toString(), "name", nameTv.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UserAccount.this, "Users credentials updated", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void getUserData() {

        db.collection("sellers").document(firebaseAuth.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        name = documentSnapshot.get("name").toString();
                        email = documentSnapshot.get("email").toString().trim();
                        phone = documentSnapshot.get("phone").toString();

                        phoneTv.setText("+91-" + phone);
                        nameTv.setText(name);
                        emailTv.setText(email);
                    }
                });
    }
}