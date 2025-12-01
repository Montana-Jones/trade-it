package edu.uga.cs.tradeit.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import edu.uga.cs.tradeit.MainActivity;
import edu.uga.cs.tradeit.R;

public class SplashFragment extends Fragment {

    private Button btnBrowseItems, btnPendingTransactions, btnCompletedTransactions;

    public SplashFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        btnBrowseItems = view.findViewById(R.id.btnBrowseItems);
        btnPendingTransactions = view.findViewById(R.id.btnPendingTransactions);
        btnCompletedTransactions = view.findViewById(R.id.btnCompletedTransactions);

        btnBrowseItems.setOnClickListener(v ->
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new CategoryListFragment())
                        .addToBackStack(null)
                        .commit()
        );

        btnPendingTransactions.setOnClickListener(v ->
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new PendingTransactionsFragment())
                        .addToBackStack(null)
                        .commit()
        );

        btnCompletedTransactions.setOnClickListener(v ->
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new CompletedTransactionsFragment())
                        .addToBackStack(null)
                        .commit()
        );

        MainActivity activity = (MainActivity) requireActivity();
        activity.setToolbarTitle("Home");

        Toolbar toolbar = activity.findViewById(R.id.toolbar);

        toolbar.setNavigationIcon(null);

        return view;
    }
}
