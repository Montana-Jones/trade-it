package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Transaction;

public class PendingTransactionsFragment extends Fragment {

    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private DatabaseReference transactionsRef;
    private ValueEventListener transactionsListener;

    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pending_transactions, container, false);

        rvTransactions = view.findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new TransactionAdapter(transactionList, currentUserId, this::markTransactionCompleted);
        rvTransactions.setAdapter(adapter);

        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");

        return view;
    }

    private void markTransactionCompleted(Transaction transaction) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (!transaction.sellerId.equals(currentUserId)) {
            Toast.makeText(getContext(), "Only the seller can mark the transaction completed.", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        transaction.completed = true;
        transaction.completedAt = now;

        // Update in Firebase
        transactionsRef.child(transaction.id).setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Transaction completed!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to complete transaction.", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        attachListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        detachListener();
    }

    private void attachListener() {
        if (transactionsListener == null) {
            transactionsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    transactionList.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Transaction t = child.getValue(Transaction.class);
                        if (t != null &&
                                t.completed &&
                                (t.buyerId.equals(currentUserId) || t.sellerId.equals(currentUserId))) {
                            transactionList.add(t);
                        }
                    }
                    // Sort by timestamp descending
                    Collections.sort(transactionList, (a, b) -> Long.compare(b.timestamp, a.timestamp));
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            };

            transactionsRef.addValueEventListener(transactionsListener);
        }
    }

    private void detachListener() {
        if (transactionsListener != null) {
            transactionsRef.removeEventListener(transactionsListener);
            transactionsListener = null;
        }
    }
}
