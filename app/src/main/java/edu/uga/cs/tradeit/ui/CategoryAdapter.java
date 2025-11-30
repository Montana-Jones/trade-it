package edu.uga.cs.tradeit.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface CategoryListener {
        void onCategoryClick(Category category);
        void onCategoryMoreClick(View anchor, Category category);
    }

    private List<Category> categories;
    private CategoryListener listener;

    public CategoryAdapter(List<Category> categories, CategoryListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category c = categories.get(position);
        holder.tvName.setText(c.name);
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(c));
        holder.btnMore.setOnClickListener(v -> listener.onCategoryMoreClick(holder.btnMore, c));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageButton btnMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
