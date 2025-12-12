package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 새로운 지출 내역을 입력받는 화면을 담당하는 액티비티입니다.
 */
public class AddExpenseActivity extends AppCompatActivity {

    // --- UI 위젯 변수 선언 ---
    private EditText itemNameEditText;    // 지출 항목을 입력받는 에디트텍스트
    private EditText itemAmountEditText;  // 금액을 입력받는 에디트텍스트

    /**
     * 액티비티가 처음 생성될 때 호출됩니다.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // 1. XML 레이아웃의 뷰들을 자바 코드와 연결
        itemNameEditText = findViewById(R.id.itemNameEditText);
        itemAmountEditText = findViewById(R.id.itemAmountEditText);
        Button saveButton = findViewById(R.id.saveButton);

        // 2. '저장' 버튼 리스너 설정
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 입력된 텍스트 가져오기
                String item = itemNameEditText.getText().toString();
                String amount = itemAmountEditText.getText().toString();

                // 3. 입력값 유효성 검사: 항목이나 금액이 비어있는지 확인
                if (item.trim().isEmpty() || amount.trim().isEmpty()) {
                    Toast.makeText(AddExpenseActivity.this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return; // 비어있으면 함수 종료
                }

                // 4. 결과를 담을 Intent 객체 생성
                Intent resultIntent = new Intent();
                // "key"-"value" 형태로 데이터 추가
                resultIntent.putExtra("newItem", item);
                resultIntent.putExtra("newAmount", amount);

                // 5. MainActivity에 결과가 성공적(RESULT_OK)이라고 알리고, 데이터(Intent)를 전달
                setResult(RESULT_OK, resultIntent);

                // 6. 현재 액티비티 종료 (메인 화면으로 돌아감)
                finish();
            }
        });
    }
}
