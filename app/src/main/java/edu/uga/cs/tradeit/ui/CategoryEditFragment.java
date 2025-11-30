package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.uga.cs.tradeit.MainActivity;
import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.models.Category;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CategoryEditFragment extends Fragment {

    private static final String ARG_ID = "id";
    private static final String ARG_NAME = "name";

    private EditText etName;
    private Button btnSave;

    private String categoryId;
    private DatabaseReference categoriesRef;

    public static CategoryEditFragment newInstance(@Nullable Category category) {
        CategoryEditFragment f = new CategoryEditFragment();
        if (category != null) {
            Bundle args = new Bundle();
            args.putString(ARG_ID, category.id);
            args.putString(ARG_NAME, category.name);
            f.setArguments(args);
        }
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_edit, container, false);
        etName = view.findViewById(R.id.etName);
        btnSave = view.findViewById(R.id.btnSave);

        categoriesRef = FirebaseDatabase.getInstance().getReference("categories");

        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_ID);
            String name = getArguments().getString(ARG_NAME);
            etName.setText(name);
        }

        btnSave.setOnClickListener(v -> saveCategory());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();
        activity.setToolbarTitle("Edit Category");

        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> activity.getSupportFragmentManager().popBackStack());
    }


    private void saveCategory() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();

        if (categoryId == null) {
            // New category
            categoryId = categoriesRef.push().getKey();
            Category c = new Category(categoryId, name, user.getUid(), now);
            categoriesRef.child(categoryId).setValue(c);
        } else {
            // Update category
            Category c = new Category(categoryId, name, user.getUid(), now);
            categoriesRef.child(categoryId).setValue(c);
        }

        Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}
