package com.example.snakegame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button login, signin;  // Declare UI elements at the class level
    private EditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        login = findViewById(R.id.buttonlog);
        signin = findViewById(R.id.buttonsign);
        email = findViewById(R.id.editTextTextEmailAddress);
        password = findViewById(R.id.editTextTextPassword);
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailsing = email.getText().toString().trim();
                String passwordsing = password.getText().toString().trim();

                mAuth.signInWithEmailAndPassword(emailsing, passwordsing)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    saveUserUID(user.getUid());
                                    Log.d("Login", "Login Successful.");
                                    Toast.makeText(MainActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this,leaderboard.class);
                                    startActivity(intent);
                                } else {
                                    Log.d("Login","Login Failed: " + task.getException().getMessage());
                                    Toast.makeText(MainActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add functionality for login button
            }
        });
    }
    private void saveUserUID(String uid) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_uid", uid);
        editor.apply();  // Save UID to shared preferences
    }

    // Method to retrieve UID from SharedPreferences
    private String getUserUID() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("user_uid", null); // Retrieve UID, null if not found
    }

    private void saveMessage(String messageText) {
        // Reference to Firestore database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new document with data to store
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", messageText); // Key-value pair to store in Firestore, the key is "message" and the value is messageText
        messageData.put("timestamp", System.currentTimeMillis()); // Add a timestamp

        // Save the message to a "messages" collection
        db.collection("messages")
                .add(messageData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Message saved successfully
                        Log.d("Login", "Message saved!");
                        Toast.makeText(getApplicationContext(), "Message saved!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save message
                        Log.d("Login", "Error saving message:");
                        Toast.makeText(getApplicationContext(), "Error saving message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void fetchMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query to get all documents from the "messages" collection
        db.collection("messages")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            StringBuilder messages = new StringBuilder();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Access the "message" field and append it to display
                                String messageText = document.getString("message");
                                messages.append(messageText).append("\n");
                            }
                            // Show the messages in a TextView or a Toast
                            Toast.makeText(getApplicationContext(), messages.toString(), Toast.LENGTH_LONG).show();
                        } else {
                            Log.d("Login","Login Failed: " + task.getException().getMessage());
                            Toast.makeText(getApplicationContext(), "Error getting messages: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }



}

