package step.learning.course;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CalcActivity extends AppCompatActivity {
    private TextView tvHistory;
    private TextView tvResult;
    String commaSign;
    String minusSign;
    String zeroSymbol;
    int digitCount; // счетчик цифр

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        commaSign = getString(R.string.calc_comma_sign);
        minusSign = getString(R.string.calc_minus_sign);
        zeroSymbol = getString(R.string.calc_btn_0_text);
        digitCount = 0;

        tvHistory = findViewById(R.id.tv_history);
        tvResult = findViewById(R.id.tv_result);

        tvHistory.setText("");
        displayResult("");

        // String[] suffixes = {"one", "two"};
        for (int i = 0; i < 10; i++) {
            @SuppressLint("DiscouragedApi") // более эффективно - R.id.calc_btn_5
            int buttonId = getResources().getIdentifier(
                    "calc_btn_" + i, // suffixes[i]
                    "id",
                    getPackageName()
            );
            findViewById(buttonId).setOnClickListener(this::digitClick);
        }
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::backspaceClick);
        findViewById(R.id.calc_btn_plus_minus).setOnClickListener(this::plusMinusClick);
        findViewById(R.id.calc_btn_comma).setOnClickListener(this::commaClick);
    }

    private void commaClick(View view) {
        String result = tvResult.getText().toString();
        if (digitCount >= 10 || result.contains(commaSign)) {
            return; // символ "," не ставиться если уже он есть или количество цифр > 10
        }
        result += commaSign;
        displayResult(result);
    }

    private void plusMinusClick(View view) {
        // Изменение знака: если есть "-" перед числом, то убираем его, если нет - добавляем
        String result = tvResult.getText().toString();
        if (result.equals(zeroSymbol)) {
            return; // перед "0" знак не ставить
        }
        if (result.startsWith(minusSign)) {
            result = result.substring(1);
        }
        else {
            result = minusSign + result;
        }
        displayResult(result);
    }

    private void backspaceClick(View view) {
        String result = tvResult.getText().toString();
        if(!result.endsWith(commaSign)) {
            digitCount--; // уменьшаем счетчик цифр если последний символ не ","
        }
        result = result.substring(0, result.length() - 1);
        displayResult(result);
    }

    private void digitClick(View view) {
        String result = tvResult.getText().toString();
        if(digitCount >= 10) {
            return;
        }
        String digit = ((Button) view).getText().toString();
        if(result.equals("0")) {
            result = "";
        }
        result += digit;
        digitCount++; // увеличиваем счетчик цифр
        displayResult(result);
    }

    private void  displayResult(String result) {
        if("".equals(result) || minusSign.equals(result)) {
            result = zeroSymbol;
        }
        tvResult.setText(result);
    }
}
