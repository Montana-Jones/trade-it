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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.uga.cs.tradeit.MainActivity;
import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Item;

public class ItemEditFragment extends Fragment {

    private static final String ARG_ID = "id";
    private static final String ARG_NAME = "name";
    private static final String ARG_CATEGORY_ID = "categoryId";
    private static final String ARG_PRICE = "price";
    private static final String ARG_FREE = "free";

    private static final String ARG_POSTEDAT = "postedAt";

    private EditText etName, etPrice;
    private CheckBox cbFree;
    private Button btnSave;

    private String itemId;
    private String categoryId;

    private long postedAt;

    private DatabaseReference itemsRef;

    public static ItemEditFragment newInstance(Item item) {
        ItemEditFragment f = new ItemEditFragment();
        Bundle args = new Bundle();

        args.putString(ARG_ID, item.id);
        args.putString(ARG_NAME, item.name);
        args.putString(ARG_CATEGORY_ID, item.categoryId);
        args.putDouble(ARG_PRICE, item.price);
        args.putBoolean(ARG_FREE, item.free);
        args.putLong(ARG_POSTEDAT, item.postedAt);

        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_edit, container, false);

        etName = view.findViewById(R.id.etName);
        etPrice = view.findViewById(R.id.etPrice);
        cbFree = view.findViewById(R.id.cbFree);
        btnSave = view.findViewById(R.id.btnSave);

        itemsRef = FirebaseDatabase.getInstance().getReference("items");

        if (getArguments() != null) {
            itemId = getArguments().getString(ARG_ID);
            categoryId = getArguments().getString(ARG_CATEGORY_ID);

            etName.setText(getArguments().getString(ARG_NAME));

            postedAt = getArguments().getLong(ARG_POSTEDAT);

            boolean isFree = getArguments().getBoolean(ARG_FREE);
            cbFree.setChecked(isFree);

            if (!isFree) {
                etPrice.setText(String.valueOf(getArguments().getDouble(ARG_PRICE)));
            } else {
                etPrice.setEnabled(false);
            }
        }

        cbFree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPrice.setEnabled(false);
                etPrice.setText("");
            } else {
                etPrice.setEnabled(true);
            }
        });

        btnSave.setOnClickListener(v -> saveItem());

        MainActivity activity = (MainActivity) requireActivity();
        activity.setToolbarTitle("Edit Item");

        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> activity.getSupportFragmentManager().popBackStack());

        return view;
    }

    private void saveItem() {
        if (itemId == null) {
            Toast.makeText(getContext(), "Error: No item ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String priceText = etPrice.getText().toString().trim();
        boolean free = cbFree.isChecked();
        double price = 0.0;

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required");
            return;
        }

        if (!free) {
            if (TextUtils.isEmpty(priceText)) {
                etPrice.setError("Price required");
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

        long now = System.currentTimeMillis();

        Item updated = new Item(
                itemId,
                name,
                categoryId,
                user.getUid(),
                postedAt,
                price,
                free
        );

        itemsRef.child(itemId).setValue(updated)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Item updated", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getContext(), "Error updating item", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
