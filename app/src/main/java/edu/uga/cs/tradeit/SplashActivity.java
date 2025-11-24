package edu.uga.cs.tradeit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import edu.uga.cs.tradeit.MainActivity;
import edu.uga.cs.tradeit.PendingTransactionsActivity;
import edu.uga.cs.tradeit.R;

public class SplashActivity extends AppCompatActivity {

    private Button btnBrowseItems, btnPendingTransactions, btnCompletedTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        btnBrowseItems = findViewById(R.id.btnBrowseItems);
        btnPendingTransactions = findViewById(R.id.btnPendingTransactions);
        btnCompletedTransactions = findViewById(R.id.btnCompletedTransactions);

        btnBrowseItems.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class); // Replace with your browse activity
            startActivity(intent);
        });

        btnPendingTransactions.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, PendingTransactionsActivity.class);
            startActivity(intent);
        });

        btnCompletedTransactions.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
