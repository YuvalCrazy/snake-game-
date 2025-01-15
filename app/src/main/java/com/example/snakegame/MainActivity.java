package com.example.snakegame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }
    Button login= findViewById(R.id.buttonlog);
    Button signin = findViewById(R.id.buttonsign);
    EditText password = findViewById(R.id.editTextTextPassword);
    EditText email = findViewById(R.id.editTextTextEmailAddress);



}
