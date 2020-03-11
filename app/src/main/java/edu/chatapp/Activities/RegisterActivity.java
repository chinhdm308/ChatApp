package edu.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import edu.chatapp.Model.User;
import edu.chatapp.R;

public class RegisterActivity extends AppCompatActivity {
    private MaterialEditText username, userEmail, userPassword;
    private Button btnRegister;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");

        // init firebaseauth
        mAuth = FirebaseAuth.getInstance();

        // init views
        username = findViewById(R.id.username);
        userEmail = findViewById(R.id.email);
        userPassword = findViewById(R.id.password);
        btnRegister = findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = userEmail.getText().toString();
                String nameDisplay = username.getText().toString();
                String password = userPassword.getText().toString();

                if (email.isEmpty() || nameDisplay.isEmpty() || password.isEmpty()) {
                    showMessage("Please verify all fields");
                } else {
                    registerUserAccount(email, nameDisplay, password);
                }
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void registerUserAccount(String email, final String username, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    showMessage("Account created");
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    String userID = currentUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
                    User user = new User(userID, username, "default", "offline");
//                    Map<String, String> hashMap = new HashMap<>();
//                    hashMap.put("id", userID);
//                    hashMap.put("username", username);
//                    hashMap.put("imageURL", "default");
//                    hashMap.put("status", "offline");

                    reference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateUI();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showMessage(e.getMessage());
                        }
                    });

                } else {
                    showMessage("account creation failed" + task.getException().getMessage());
                }
            }
        });
    }

    private void updateUI() {
        Intent loginActivity = new Intent(RegisterActivity.this, LoginActivity.class);
        loginActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginActivity);
        finish();
    }
}
