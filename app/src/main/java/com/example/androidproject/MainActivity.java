package com.example.androidproject;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_EXPENSE_REQUEST = 1;

    private int currentBudget = 0;
    private int maxBudget = 500000;

    private List<Expense> expenseList = new ArrayList<>();
    private ExpenseAdapter expenseAdapter;

    private ProgressBar budgetProgressBar;
    private TextView budgetTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        budgetProgressBar = findViewById(R.id.budgetProgressBar);
        budgetTextView = findViewById(R.id.budgetTextView);
        RecyclerView expenseRecyclerView = findViewById(R.id.expenseRecyclerView);
        FloatingActionButton addExpenseFab = findViewById(R.id.addExpenseFab);
        TextView budgetTitleTextView = findViewById(R.id.budgetTitleTextView);

        // Setup Budget Title click listener
        budgetTitleTextView.setOnClickListener(v -> showSetBudgetDialog());

        // Setup RecyclerView
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(expenseList);
        expenseRecyclerView.setAdapter(expenseAdapter);

        // Setup FAB click listener
        addExpenseFab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivityForResult(intent, ADD_EXPENSE_REQUEST);
        });

        updateBudgetUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_EXPENSE_REQUEST && resultCode == RESULT_OK && data != null) {
            Expense newExpense = (Expense) data.getSerializableExtra("new_expense");
            if (newExpense != null) {
                expenseList.add(newExpense);
                currentBudget += newExpense.getAmount();
                expenseAdapter.notifyDataSetChanged();
                updateBudgetUI();
            }
        }
    }

    private void showSetBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("새 예산 설정");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("금액을 입력하세요");
        builder.setView(input);

        builder.setPositiveButton("확인", (dialog, which) -> {
            String newBudgetText = input.getText().toString();
            if (!newBudgetText.isEmpty()) {
                maxBudget = Integer.parseInt(newBudgetText);
                updateBudgetUI();
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateBudgetUI() {
        // Update a TextView
        budgetTextView.setText(String.format("%,d원 / %,d원", currentBudget, maxBudget));

        // Animate a ProgressBar
        int progress = (maxBudget > 0) ? (int) ((double) currentBudget / maxBudget * 100) : 0;
        ObjectAnimator.ofInt(budgetProgressBar, "progress", progress)
                .setDuration(500)
                .start();
    }
}
