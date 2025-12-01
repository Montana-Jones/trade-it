package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import edu.uga.cs.tradeit.MainActivity;
import edu.uga.cs.tradeit.R;

public class RegisterFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnRegister, btnGoLogin;

    private String savedEmail = "";
    private String savedPassword = "";

    public RegisterFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnGoLogin = view.findViewById(R.id.btnGoLogin);

        // Your existing code below...
        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(v -> register());
        btnGoLogin.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });

        MainActivity activity = (MainActivity) requireActivity();
        activity.setToolbarTitle("Register");

        Toolbar toolbar = activity.findViewById(R.id.toolbar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            etEmail.setText(savedInstanceState.getString("email", ""));
            etPassword.setText(savedInstanceState.getString("password", ""));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        savedEmail = etEmail != null ? etEmail.getText().toString() : "";
        savedPassword = etPassword != null ? etPassword.getText().toString() : "";
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (etEmail != null) {
            outState.putString("email", etEmail.getText().toString());
        }
        if (etPassword != null) {
            outState.putString("password", etPassword.getText().toString());
        }
    }



    private void register() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password required");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Registered successfully", Toast.LENGTH_SHORT).show();

                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container, new CategoryListFragment())
                                .commit();
                    } else {
                        Toast.makeText(
                                getActivity(),
                                "Registration failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
