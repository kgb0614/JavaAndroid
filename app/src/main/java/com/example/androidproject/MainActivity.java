package com.example.androidproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/*
 * 앱의 메인 화면을 담당하는 액티비티입니다.
 * 날짜별 지출 내역을 표시하고, 월별 총액을 계산하며, 내역 추가/삭제 기능을 관리합니다.
 */
public class MainActivity extends AppCompatActivity {

    // 다른 액티비티를 호출하고 결과를 받아오기 위한 요청
    private static final int ADD_EXPENSE_REQUEST = 1;

    // 위젯 변수 선언
    private DatePicker datePicker;              // 날짜 선택 위젯
    private LinearLayout expenseListContainer;  // 지출 내역들이 동적으로 추가될 컨테이너
    private TextView monthlyTotalTextView;      // 월별 총 지출액을 표시할 텍스트 뷰
    private String currentFileName; // 현재 선택된 날짜에 해당하는 파일 이름 (예: "2024_7_30.txt")
    private int currentYear;        // 현재 선택된 년도
    private int currentMonth;       // 현재 선택된 월 (0~11)

    /*
     * 액티비티가 처음 생성될 때 호출됩니다.
     * 위젯을 초기화하고, 리스너를 설정하며, 초기 데이터를 화면에 표시합니다.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. XML 레이아웃의 뷰들을 자바 코드와 연결
        datePicker = findViewById(R.id.datePicker);
        expenseListContainer = findViewById(R.id.expenseListContainer);
        monthlyTotalTextView = findViewById(R.id.monthlyTotalTextView);
        Button addButton = findViewById(R.id.addButton);

        // 2. 현재 날짜 정보 가져오기
        Calendar cal = Calendar.getInstance();
        currentYear = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH);
        int cDay = cal.get(Calendar.DAY_OF_MONTH);

        // 3. 데이터 픽커 리스너 설정: 날짜가 변경될 때마다 해당 날짜의 내역을 불러옴
        datePicker.init(currentYear, currentMonth, cDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 파일 이름 형식 생성 (월은 1부터 시작하니 +1)
                currentFileName = String.format("%d_%d_%d.txt", year, monthOfYear + 1, dayOfMonth);
                readAndDisplayExpenses(currentFileName);

                // 월이나 년도가 바뀌었으면 월별 총액을 다시 계산
                if (year != currentYear || monthOfYear != currentMonth) {
                    currentYear = year;
                    currentMonth = monthOfYear;
                    updateMonthlyTotal();
                }
            }
        });

        // 4. 앱 시작 시 오늘 날짜의 내역을 기본으로 표시
        currentFileName = String.format("%d_%d_%d.txt", currentYear, currentMonth + 1, cDay);
        readAndDisplayExpenses(currentFileName);
        updateMonthlyTotal();

        // 5. '내역 추가' 버튼 리스너 설정: AddExpenseActivity를 호출
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                startActivityForResult(intent, ADD_EXPENSE_REQUEST);
            }
        });
    }

    /*
     * startActivityForResult로 시작한 액티비티가 종료되면서 결과를 반환할 때 호출됩니다.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 우리가 보낸 요청(ADD_EXPENSE_REQUEST)이 정상적으로 완료(RESULT_OK)되었는지 확인
        if (requestCode == ADD_EXPENSE_REQUEST && resultCode == RESULT_OK && data != null) {
            // AddExpenseActivity로부터 새로운 지출 항목과 금액을 받아옵니다
            String newItem = data.getStringExtra("newItem");
            String newAmount = data.getStringExtra("newAmount");

            if (newItem != null && newAmount != null) {
                // 파일에 저장할 한 줄 텍스트 생성
                String newEntry = newItem + " : " + newAmount + "원\n";
                // 파일에 내용 추가 및 화면 갱신
                appendToFile(currentFileName, newEntry);
                readAndDisplayExpenses(currentFileName);
                updateMonthlyTotal();
            }
        }
    }

    /*
     * 특정 날짜의 파일 내용을 읽어와 화면의 리니어 레이아웃에 동적으로 뷰를 생성하여 표시합니다.
     */
    private void readAndDisplayExpenses(String fileName) {
        expenseListContainer.removeAllViews(); // 1. 컨테이너를 깨끗하게 비움

        String content = readFileContent(fileName); // 2. 파일 내용 전체를 문자열로 읽어옴
        if (content.isEmpty()) {
            displayEmptyView(); // 3. 내용이 없으면 "내역 없음" 메시지 표시
            return;
        }

        String[] lines = content.split("\n"); // 4. 문자열을 줄바꿈(\n) 기준으로 나눔
        for (final String line : lines) {
            if (line.trim().isEmpty()) continue; // 빈 줄은 무시
            // 5. 각 줄에 해당하는 뷰(텍스트 + 삭제 버튼)를 생성하여 컨테이너에 추가
            View itemView = createExpenseItemView(line);
            expenseListContainer.addView(itemView);
        }
    }

    /*
     * 지출 내역 한 줄에 해당하는 뷰를 동적으로 생성합니다.
     * @param line 표시할 지출 내역 문자열 (예: "점심 : 8000원")
     * @return 생성된 뷰 객체
     */
    private View createExpenseItemView(final String line) {
        // 1. 가로 방향 리니어 레이아웃 생성
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setGravity(Gravity.CENTER_VERTICAL);

        // 2. 지출 내역을 표시할 TextView 생성
        TextView expenseText = new TextView(this);
        // layout_weight=1 설정: 삭제 버튼을 제외한 모든 여백을 차지하도록 함
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        expenseText.setLayoutParams(textParams);
        expenseText.setText(line);
        expenseText.setTextSize(16);

        // 3. '삭제' 버튼 생성
        Button deleteButton = new Button(this);
        deleteButton.setText("삭제");
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭 시 해당 줄을 파일에서 삭제하고 화면을 새로고침
                deleteLineAndRefresh(currentFileName, line);
            }
        });

        // 4. LinearLayout에 TextView와 Button 추가
        itemLayout.addView(expenseText);
        itemLayout.addView(deleteButton);
        return itemLayout; // 완성된 한 줄 뷰 반환
    }

    /*
     * "지출 내역이 없습니다." 메시지를 표시하는 뷰를 생성하여 컨테이너에 추가합니다.
     */
    private void displayEmptyView() {
        TextView emptyView = new TextView(this);
        emptyView.setText("지출 내역이 없습니다.");
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setPadding(0, 40, 0, 0); // 위쪽에 약간의 여백 추가
        expenseListContainer.addView(emptyView);
    }

    /*
     * 현재 월의 모든 지출 내역을 합산하여 화면 상단의 총액 텍스트뷰를 업데이트합니다.
     */
    private void updateMonthlyTotal() {
        long total = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 현재 월의 마지막 날짜 가져오기

        // 1일부터 마지막 날까지 반복
        for (int day = 1; day <= daysInMonth; day++) {
            String fileName = String.format("%d_%d_%d.txt", currentYear, currentMonth + 1, day);
            String content = readFileContent(fileName);
            if (content.isEmpty()) continue; // 파일 없으면 건너뜀

            String[] lines = content.split("\n");
            for (String line : lines) {
                if (line.contains(" : ") && line.contains("원")) {
                    try {
                        // "점심 : 8000원" 에서 "8000" 부분만 추출
                        String amountStr = line.substring(line.indexOf(" : ") + 3, line.indexOf("원"));
                        // 콤마(,)가 포함된 금액 문자열도 처리 가능하도록 replace 추가
                        total += Integer.parseInt(amountStr.trim().replace(",", ""));
                    } catch (Exception e) { /* 금액 파싱 중 오류가 발생해도 앱이 죽지 않도록 무시 */ }
                }
            }
        }
        // 최종 합계 금액을 ,와 함께 표시
        monthlyTotalTextView.setText(String.format("이번 달 총 지출: %,d원", total));
    }

    /**
     * 파일에서 특정 줄을 삭제하고, 화면을 새로고침합니다.
     * @param fileName 처리할 파일 이름
     * @param lineToRemove 삭제할 특정 줄의 내용
     */
    private void deleteLineAndRefresh(String fileName, String lineToRemove) {
        String content = readFileContent(fileName);
        // 파일 내용을 리스트로 변환하여 특정 줄을 쉽게 제거
        List<String> lines = new ArrayList<>(Arrays.asList(content.split("\n")));

        // 문자열 끝의 공백이나 줄바꿈 문자로 인해 비교가 실패하는 것을 방지하기 위해 trim() 사용
        String trimmedLineToRemove = lineToRemove.trim();
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equals(trimmedLineToRemove)) {
                lines.remove(i);
                break; // 해당 줄을 찾아서 지웠으면 반복 중단
            }
        }

        // 리스트를 다시 하나의 문자열로 합침
        StringBuilder newContent = new StringBuilder();
        for (String line : lines) {
            newContent.append(line).append("\n");
        }

        // 기존 파일을 새로운 내용으로 덮어씀
        overwriteFile(fileName, newContent.toString());
        // 화면 및 총액 새로고침
        readAndDisplayExpenses(fileName);
        updateMonthlyTotal();
    }

    /*
     * 파일 전체 내용을 문자열로 읽어옵니다. 파일이 없으면 빈 문자열을 반환합니다.
     */
    private String readFileContent(String fileName) {
        try (FileInputStream inFs = openFileInput(fileName)) {
            byte[] txt = new byte[inFs.available()];
            inFs.read(txt);
            return new String(txt);
        } catch (IOException e) {
            return ""; // 파일 읽기 실패 시 빈 문자열 반환
        }
    }

    /*
     * 파일 끝에 새로운 내용을 추가합니다.
     */
    private void appendToFile(String fileName, String data) {
        try (FileOutputStream outFs = openFileOutput(fileName, Context.MODE_APPEND)){
            outFs.write(data.getBytes());
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "저장 실패", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 파일 전체를 새로운 내용으로 덮어씁니다.
     */
    private void overwriteFile(String fileName, String data) {
        try (FileOutputStream outFs = openFileOutput(fileName, Context.MODE_PRIVATE)){
            outFs.write(data.getBytes());
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "저장 실패", Toast.LENGTH_SHORT).show();
        }
    }
}
