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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Item;

public class ItemListFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "categoryId";
    private static final String ARG_CATEGORY_NAME = "categoryName";

    private String categoryId;
    private String categoryName;

    private RecyclerView rvItems;
    private FloatingActionButton fabAddItem;
    private ItemAdapter adapter;
    private List<Item> itemList = new ArrayList<>();
    private DatabaseReference itemsRef;
    private ValueEventListener itemsListener;

    public static ItemListFragment newInstance(String categoryId, String categoryName) {
        ItemListFragment f = new ItemListFragment();
        Bundle b = new Bundle();
        b.putString(ARG_CATEGORY_ID, categoryId);
        b.putString(ARG_CATEGORY_NAME, categoryName);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
            requireActivity().setTitle(categoryName);
        }

        rvItems = view.findViewById(R.id.rvItems);
        fabAddItem = view.findViewById(R.id.fabAddItem);

        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemAdapter(itemList);
        rvItems.setAdapter(adapter);

        itemsRef = FirebaseDatabase.getInstance().getReference("items");

        fabAddItem.setOnClickListener(v -> openAddItem());

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
        if (itemsListener == null) {
            itemsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    itemList.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Item item = child.getValue(Item.class);
                        if (item != null) {
                            itemList.add(item);
                        }
                    }
                    // Sort newest to oldest
                    Collections.sort(itemList,
                            (a, b) -> Long.compare(b.postedAt, a.postedAt));
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            };

            itemsRef.orderByChild("categoryId").equalTo(categoryId)
                    .addValueEventListener(itemsListener);
        }
    }

    private void detachListener() {
        if (itemsListener != null) {
            itemsRef.removeEventListener(itemsListener);
            itemsListener = null;
        }
    }

    private void openAddItem() {
        ItemPostFragment fragment = ItemPostFragment.newInstance(categoryId, categoryName);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
