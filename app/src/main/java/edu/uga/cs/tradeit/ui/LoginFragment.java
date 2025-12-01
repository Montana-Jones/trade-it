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
import com.google.firebase.auth.FirebaseUser;

import edu.uga.cs.tradeit.MainActivity;
import edu.uga.cs.tradeit.R;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoRegister;
    private String savedEmail = "";
    private String savedPassword = "";


    public LoginFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToMain();
        }

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnGoRegister = view.findViewById(R.id.btnGoRegister);

        btnLogin.setOnClickListener(v -> login());
        btnGoRegister.setOnClickListener(v -> {
            // Navigate to RegisterFragment
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });


        MainActivity activity = (MainActivity) requireActivity();
        activity.setToolbarTitle("Login");

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
        // Set toolbar title
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Login");
        }

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


    @Override
    public void onPause() {
        super.onPause();
        savedEmail = etEmail != null ? etEmail.getText().toString() : "";
        savedPassword = etPassword != null ? etPassword.getText().toString() : "";
    }


    private void login() {
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

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        goToMain();
                    } else {
                        Toast.makeText(getActivity(),
                                "Unknown email/password combination",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToMain() {
        // Navigate to the main content fragment
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new SplashFragment())
                .commit();
    }

}
