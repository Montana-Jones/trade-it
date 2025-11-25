package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
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
import edu.uga.cs.tradeit.models.Transaction;

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
        }

        rvItems = view.findViewById(R.id.rvItems);
        fabAddItem = view.findViewById(R.id.fabAddItem);
        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ItemAdapter(itemList, new ItemAdapter.OnItemActionListener() {
            @Override
            public void onEditItem(Item item) { editItem(item); }

            @Override
            public void onDeleteItem(Item item) { deleteItem(item); }

            @Override
            public void onRequestItem(Item item) { requestItem(item); }
        });
        rvItems.setAdapter(adapter);

        itemsRef = FirebaseDatabase.getInstance().getReference("items");
        fabAddItem.setOnClickListener(v -> openAddItem());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);

        // Show back button
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .popBackStack(); // go back to previous fragment
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
        if (itemsListener == null) {
            itemsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    itemList.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Item item = child.getValue(Item.class);
                        if (item != null) itemList.add(item);
                    }
                    Collections.sort(itemList, (a, b) -> Long.compare(b.postedAt, a.postedAt));
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

    private void editItem(Item item) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (!item.postedBy.equals(currentUserId)) {
            Toast.makeText(getContext(), "You can only edit your own listings.", Toast.LENGTH_SHORT).show();
            return;
        }
        ItemEditFragment fragment = ItemEditFragment.newInstance(item);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void requestItem(Item item) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (item.postedBy.equals(currentUserId)) {
            Toast.makeText(getContext(), "You cannot buy your own item.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove item from category
        itemsRef.child(item.id).removeValue()
                .addOnSuccessListener(aVoid -> itemList.remove(item))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to request item.", Toast.LENGTH_SHORT).show());

        // Add pending transaction
        String transactionId = FirebaseDatabase.getInstance().getReference("transactions").push().getKey();
        long now = System.currentTimeMillis();

        Transaction transaction = new Transaction(
                transactionId,
                item.id,
                item.name,
                currentUserId,
                item.postedBy,
                now
        );

        // Set defaults
        transaction.completed = false;
        transaction.buyerConfirmed = false;
        transaction.sellerConfirmed = false;
        transaction.completedAt = 0;



        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");
        transactionsRef.child(transactionId).setValue(transaction)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Transaction pending for buyer & seller!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to create transaction.", Toast.LENGTH_SHORT).show());

        adapter.notifyDataSetChanged();
    }

    private void deleteItem(Item item) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (!item.postedBy.equals(currentUserId)) {
            Toast.makeText(getContext(), "You can only delete your own listings.", Toast.LENGTH_SHORT).show();
            return;
        }
        itemsRef.child(item.id).removeValue()
                .addOnSuccessListener(aVoid -> {
                    itemList.remove(item);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Item deleted!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete item.", Toast.LENGTH_SHORT).show());
    }
}
