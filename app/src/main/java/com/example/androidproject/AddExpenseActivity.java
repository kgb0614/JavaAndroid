package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText expenseNameEditText;
    private EditText expenseAmountEditText;
    private Spinner categorySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        expenseNameEditText = findViewById(R.id.expenseNameEditText);
        expenseAmountEditText = findViewById(R.id.expenseAmountEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        Button saveButton = findViewById(R.id.saveButton);

        // Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.expense_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Setup Save Button
        saveButton.setOnClickListener(v -> saveData());
    }

    private void saveData() {
        String name = expenseNameEditText.getText().toString();
        String amountString = expenseAmountEditText.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();

        if (name.isEmpty() || amountString.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int amount = Integer.parseInt(amountString);

        Expense newExpense = new Expense(name, amount, category);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("new_expense", newExpense);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
