package edu.uga.cs.tradeit.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Item;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private final List<Item> items;

    public interface OnItemActionListener {
        void onEditItem(Item item);
        void onDeleteItem(Item item);
        void onRequestItem(Item item);
    }

    private final OnItemActionListener actionListener;

    public ItemAdapter(List<Item> items, OnItemActionListener listener) {
        this.items = items;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);

        holder.tvName.setText(item.name);
        holder.tvPrice.setText(item.free ? "Free" : "$" + item.price);

        // Show popup menu for edit/delete
        holder.itemView.setOnLongClickListener(v -> {
            if (actionListener != null) showPopupMenu(v, item);
            return true;
        });

        // Click to request/buy
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onRequestItem(item);
        });
    }

    private void showPopupMenu(View view, Item item) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");

        popup.setOnMenuItemClickListener(menuItem -> {
            String choice = menuItem.getTitle().toString();

            if (choice.equals("Edit")) actionListener.onEditItem(item);
            else if (choice.equals("Delete")) actionListener.onDeleteItem(item);

            return true;
        });

        popup.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
