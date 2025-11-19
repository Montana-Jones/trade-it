package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Category;

public class CategoryListFragment extends Fragment implements CategoryAdapter.CategoryListener {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();

    private DatabaseReference categoriesRef;
    private DatabaseReference itemsRef;
    private ValueEventListener categoriesListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_list, container, false);

        rvCategories = view.findViewById(R.id.rvCategories);
        Button btnAdd = view.findViewById(R.id.btnAddCategory);

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CategoryAdapter(categoryList, this);
        rvCategories.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
        }

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        categoriesRef = db.getReference("categories");
        itemsRef = db.getReference("items");

        btnAdd.setOnClickListener(v -> openCategoryEdit(null));

        return view;
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
        if (categoriesListener == null) {
            categoriesListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    categoryList.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Category c = child.getValue(Category.class);
                        if (c != null) {
                            categoryList.add(c);
                        }
                    }
                    Collections.sort(categoryList,
                            (a, b) -> a.name.compareToIgnoreCase(b.name));
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            };
            categoriesRef.addValueEventListener(categoriesListener);
        }
    }

    private void detachListener() {
        if (categoriesListener != null) {
            categoriesRef.removeEventListener(categoriesListener);
            categoriesListener = null;
        }
    }

    private void openCategoryEdit(@Nullable Category category) {
        CategoryEditFragment fragment = CategoryEditFragment.newInstance(category);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onCategoryClick(Category category) {
        ItemListFragment fragment = ItemListFragment.newInstance(category.id, category.name);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onCategoryMoreClick(View anchor, Category category) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.menu_category_item, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_category) {
                checkEmptyAndEdit(category);
                return true;
            } else if (item.getItemId() == R.id.action_delete_category) {
                checkEmptyAndDelete(category);
                return true;
            }
            return false;
        });
        menu.show();
    }

    private void checkEmptyAndEdit(Category category) {
        itemsRef.orderByChild("categoryId").equalTo(category.id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(getContext(),
                                    "Category must be empty to update", Toast.LENGTH_SHORT).show();
                        } else {
                            openCategoryEdit(category);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void checkEmptyAndDelete(Category category) {
        itemsRef.orderByChild("categoryId").equalTo(category.id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(getContext(),
                                    "Category must be empty to delete", Toast.LENGTH_SHORT).show();
                        } else {
                            categoriesRef.child(category.id).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }
}
