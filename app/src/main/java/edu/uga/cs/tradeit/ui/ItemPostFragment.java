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

        btnPost.setOnClickListener(v -> postItem());

        cbFree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPrice.setEnabled(false);
                etPrice.setText("");
            } else {
                etPrice.setEnabled(true);
            }
        });

        return view;
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

        String id = itemsRef.push().getKey();
        long now = System.currentTimeMillis();
        Item item = new Item(id, name, categoryId, user.getUid(), now, price, free);

        itemsRef.child(id).setValue(item)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Item posted", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getContext(), "Error posting item",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
