package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Item;

public class ItemPostFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "categoryId";
    private static final String ARG_CATEGORY_NAME = "categoryName";

    private String categoryId;
    private String categoryName;

    private EditText etName, etPrice;
    private CheckBox cbFree;
    private Button btnPost;

    private DatabaseReference itemsRef;

    public static ItemPostFragment newInstance(String categoryId, String categoryName) {
        ItemPostFragment f = new ItemPostFragment();
        Bundle b = new Bundle();
        b.putString(ARG_CATEGORY_ID, categoryId);
        b.putString(ARG_CATEGORY_NAME, categoryName);
        f.setArguments(b);
        return f;
    }

    public static ItemPostFragment newEditInstance(Item item) {
        ItemPostFragment f = new ItemPostFragment();
        Bundle b = new Bundle();
        b.putString("id", item.id);
        b.putString("name", item.name);
        b.putString("categoryId", item.categoryId);
        b.putDouble("price", item.price);
        b.putBoolean("free", item.free);
        f.setArguments(b);
        return f;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_post, container, false);

        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
            requireActivity().setTitle("Post in " + categoryName);
        }

        etName = view.findViewById(R.id.etName);
        etPrice = view.findViewById(R.id.etPrice);
        cbFree = view.findViewById(R.id.cbFree);
        btnPost = view.findViewById(R.id.btnPost);

        itemsRef = FirebaseDatabase.getInstance().getReference("items");

        cbFree.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                etPrice.setEnabled(false);
                etPrice.setText("");
            } else {
                etPrice.setEnabled(true);
            }
        });

        btnPost.setOnClickListener(v -> postItem());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);

        // Show back button
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back); // use a back arrow icon
        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
            // or navigate to main menu fragment
        });
    }


    private void postItem() {
        String name = etName.getText().toString().trim();
        String priceText = etPrice.getText().toString().trim();
        boolean free = cbFree.isChecked();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required");
            return;
        }

        double price = 0.0;
        if (!free) {
            if (TextUtils.isEmpty(priceText)) {
                etPrice.setError("Price required if not free");
                return;
            }
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                etPrice.setError("Invalid price");
                return;
            }
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate Firebase push ID
        String id = itemsRef.push().getKey();
        if (id == null) {
            Toast.makeText(getContext(), "Error generating ID", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();

        Item item = new Item(
                id,
                name,
                categoryId,
                user.getUid(),
                now,
                price,
                free
        );

        itemsRef.child(id).setValue(item)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Item posted", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getContext(), "Error posting item", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
