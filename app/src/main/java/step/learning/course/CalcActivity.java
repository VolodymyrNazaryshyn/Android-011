package step.learning.course;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CalcActivity extends AppCompatActivity {
    private TextView tvHistory;
    private TextView tvResult;
    private String commaSign;
    private String minusSign;
    private String zeroSymbol;
    private boolean needClear; // необходимо почистить экран при вводе новой цифры
    private int digitCount; // счетчик цифр

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

        clearClick(null);

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
        findViewById(R.id.calc_btn_clear).setOnClickListener(this::clearClick);
        findViewById(R.id.calc_btn_ce).setOnClickListener(this::clearEditClick);
        findViewById(R.id.calc_btn_square).setOnClickListener(this::squareClick);
        findViewById(R.id.calc_btn_inverse).setOnClickListener(this::inverseClick);
    }

    private void inverseClick(View view) {
        String result = tvResult.getText().toString();
        double arg;
        try {
            arg = Double.parseDouble(
                    result
                            .replace(minusSign, "-")
                            .replaceAll(zeroSymbol, "0")
                            .replace(commaSign, ".")
            );
        }
        catch (NumberFormatException | NullPointerException ignored) {
            Toast.makeText(
                    this,
                    R.string.calc_error_parse,
                    Toast.LENGTH_SHORT)
                .show();
            return;
        }
        tvHistory.setText("1/" + result + " =");
        arg = 1 / arg;
        displayResult(arg);
        needClear = true;
    }

    private void squareClick(View view) {
        String result = tvResult.getText().toString();
        double arg;
        try {
            arg = Double.parseDouble(
                    result
                            .replace(minusSign, "-")
                            .replaceAll(zeroSymbol, "0")
                            .replace(commaSign, ".")
            );
        }
        catch (NumberFormatException | NullPointerException ignored) {
            Toast.makeText(                     // Всплывающее сообщение
                    this,                       // контекст - родительская активность
                    R.string.calc_error_parse,  // текст либо ресурс
                    Toast.LENGTH_SHORT)         // длительность (во времени)
                .show();                        // !! не забывать - запуск тоста
            return;
        }
        tvHistory.setText(result + "² =");
        arg *= arg;
        displayResult(arg);
        needClear = true;
    }

    private void clearClick(View view) { // C
        tvHistory.setText("");
        clearEditClick(view);
    }

    private void clearEditClick(View view) { // CE
        digitCount = 0;
        displayResult("");
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
        if (needClear) {
            clearClick(view);
            needClear = false;
            return;
        }
        String result = tvResult.getText().toString();
        if(!result.endsWith(commaSign)) {
            digitCount--; // уменьшаем счетчик цифр если последний символ не ","
        }
        result = result.substring(0, result.length() - 1);
        displayResult(result);
    }

    private void digitClick(View view) {
        String result = tvResult.getText().toString();
        if(result.equals(zeroSymbol) || needClear) {
            result = "";
            needClear = false;
            digitCount = 0;
            tvHistory.setText("");
        }
        if(digitCount >= 10) {
            return;
        }
        String digit = ((Button) view).getText().toString();
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

    private void displayResult(double arg) {
        long argInt = (long) arg;
        String result = argInt == arg ? "" + argInt : "" + arg;

        result = result
                .replace("-", minusSign)
                .replaceAll("0", zeroSymbol)
                .replace(".", commaSign);

        displayResult(result);
    }
}
