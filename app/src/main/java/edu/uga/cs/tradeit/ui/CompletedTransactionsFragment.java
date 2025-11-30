package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.uga.cs.tradeit.MainActivity;
import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Item;
import edu.uga.cs.tradeit.models.Transaction;

public class CompletedTransactionsFragment extends Fragment {

    private RecyclerView rvTransactions;
    private TextView tvNoTransactions;

    private CompletedTransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private DatabaseReference transactionsRef;

    public CompletedTransactionsFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed_transactions, container, false);

        tvNoTransactions = view.findViewById(R.id.tvNoTransactions);
        rvTransactions = view.findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CompletedTransactionAdapter(transactionList);
        rvTransactions.setAdapter(adapter);

        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");

        MainActivity activity = (MainActivity) requireActivity();
        activity.setToolbarTitle("Completed Transactions");

        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> activity.getSupportFragmentManager().popBackStack());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadTransactions();
    }

    private void loadTransactions() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Transaction t = child.getValue(Transaction.class);
                    if (t != null && (t.buyerId.equals(currentUserId) || t.sellerId.equals(currentUserId)) && t.completed) {
                        transactionList.add(t);
                    }
                }
                Collections.sort(transactionList, (a, b) -> Long.compare(b.timestamp, a.timestamp));
                adapter.notifyDataSetChanged();

                if (transactionList.isEmpty()) {
                    tvNoTransactions.setVisibility(View.VISIBLE);
                    rvTransactions.setVisibility(View.GONE);
                } else {
                    tvNoTransactions.setVisibility(View.GONE);
                    rvTransactions.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // -------------------------------
    // Adapter
    // -------------------------------
    private class CompletedTransactionAdapter extends RecyclerView.Adapter<CompletedTransactionAdapter.ViewHolder> {
        private final List<Transaction> transactions;

        CompletedTransactionAdapter(List<Transaction> transactions) { this.transactions = transactions; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_completed_transaction, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Transaction t = transactions.get(position);
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            boolean isBuyer = currentUserId.equals(t.buyerId);

            holder.tvItemName.setText(t.itemName);
            holder.tvRole.setText(isBuyer ? "Buyer" : "Seller");
            holder.tvTimestamp.setText(DateFormat.getDateTimeInstance().format(t.timestamp));
                        
        }

        @Override
        public int getItemCount() { return transactions.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemName, tvRole, tvTimestamp;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvRole = itemView.findViewById(R.id.tvRole);
                tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            }
        }
    }
}
