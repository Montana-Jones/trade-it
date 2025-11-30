package edu.uga.cs.tradeit.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Transaction;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final List<Transaction> transactions;
    private final String currentUserId;
    private final OnTransactionActionListener actionListener;

    public interface OnTransactionActionListener {
        void onMarkCompleted(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactions, String currentUserId, OnTransactionActionListener listener) {
        this.transactions = transactions;
        this.currentUserId = currentUserId;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = transactions.get(position);

        String role = t.buyerId.equals(currentUserId) ? "Buyer" : "Seller";
        holder.tvRole.setText(role);

        holder.tvItem.setText(t.itemName);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.tvTimestamp.setText(sdf.format(new Date(t.timestamp)));

        // Show popup menu only for sellers
        holder.itemView.setOnLongClickListener(v -> {
            if (t.sellerId.equals(currentUserId) && !t.completed) {
                showPopupMenu(v, t);
            }
            return true;
        });
    }

    private void showPopupMenu(View anchor, Transaction transaction) {
        PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
        popup.getMenu().add("Mark as Completed");

        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getTitle().toString().equals("Mark as Completed") && actionListener != null) {
                actionListener.onMarkCompleted(transaction);
            }
            return true;
        });

        popup.show();
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItem, tvTimestamp, tvRole;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItem = itemView.findViewById(R.id.tvItemName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvRole = itemView.findViewById(R.id.tvRole);
        }
    }
}
