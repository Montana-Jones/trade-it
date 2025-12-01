package edu.uga.cs.tradeit;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.uga.cs.tradeit.ui.CategoryListFragment;
import edu.uga.cs.tradeit.ui.LoginFragment;
import edu.uga.cs.tradeit.ui.RegisterFragment;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.getOverflowIcon().setTint(getResources().getColor(android.R.color.white, null));


        if (savedInstanceState == null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                navigateToFragment(new LoginFragment(), "Login", false);
            } else {
                navigateToFragment(new CategoryListFragment(), "Categories", false);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Hide logout if current fragment is Login or Register
        boolean hideLogout = mAuth.getCurrentUser() == null;
        MenuItem logoutItem = menu.findItem(R.id.action_logout);
        logoutItem.setVisible(!hideLogout);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            navigateToFragment(new LoginFragment(), "Login", false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        androidx.fragment.app.Fragment currentFragment = getCurrentFragment();
    }

    // --------------------
    // Fragment navigation
    // --------------------
    private void navigateToFragment(androidx.fragment.app.Fragment fragment, String title, boolean addToBackStack) {
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit(); // <-- use commit(), NOT commitNow()

        // Set toolbar title here or let the fragment do it in onViewCreated
        setToolbarTitle(title);

        invalidateOptionsMenu();
    }



    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private androidx.fragment.app.Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.container);
    }

}
