package edu.chatapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import edu.chatapp.R;

public class RegisterBottomSheetFragment extends BottomSheetDialogFragment {

    private FirebaseAuth mAuth;
    private MaterialEditText username, userEmail, userPassword;
    private Button btnRegister;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_bottom_sheet, container, false);

        username = view.findViewById(R.id.username);
        userEmail = view.findViewById(R.id.email);
        userPassword = view.findViewById(R.id.password);
        btnRegister = view.findViewById(R.id.btn_register);

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

        return view;
    }

    private void registerUserAccount(String email, final String username, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    showMessage("Account created");
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    String userID = currentUser.getUid();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
//                    User user = new User(userID, username, "default", "offline");
                    Map<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userID);
                    hashMap.put("username", username);
                    hashMap.put("imageURL", "default");
                    hashMap.put("status", "offline");

                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                return;
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

    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
