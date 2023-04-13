package step.learning.course;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
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
    private String plusSymbol;
    private String minusSymbol;
    private String multiplicationSymbol;
    private String divideSymbol;
    private boolean needClear; // необходимо почистить экран при вводе новой цифры
    private int digitCount; // счетчик цифр
    private String operator; // +, -, *, /
    private String oldNumber; // число, стоящее до оператора

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        commaSign = getString(R.string.calc_comma_sign);
        minusSign = getString(R.string.calc_minus_sign);
        zeroSymbol = getString(R.string.calc_btn_0_text);
        plusSymbol = getString(R.string.calc_btn_plus_text);
        minusSymbol = getString(R.string.calc_btn_minus_text);
        multiplicationSymbol = getString(R.string.calc_btn_multiplication_text);
        divideSymbol = getString(R.string.calc_btn_divide_text);
        digitCount = 0;
        operator = "";
        oldNumber = "";

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
        findViewById(R.id.calc_btn_percent).setOnClickListener(this::percentClick);
        findViewById(R.id.calc_btn_root).setOnClickListener(this::rootClick);
        findViewById(R.id.calc_btn_plus).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_minus).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_multiplication).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_divide).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_equal).setOnClickListener(this::equalClick);
    }

    // При изменении конфигурации устройства перезапускается активность и данные исчезают

    // Данный метод-событие вызывается при разрушении данной конфигурации
    @Override
    protected void onSaveInstanceState(@NonNull Bundle savingState) {
        // savingState - ~словарь сохраняющихся данных
        super.onSaveInstanceState(savingState); // Оставить, нужно обязательно
        Log.d("CalcActivity", "onSaveInstanceState");
        // добавляем к сохраняемым данным свои значения
        savingState.putCharSequence("history", tvHistory.getText());
        savingState.putCharSequence("result", tvResult.getText());
        savingState.putBoolean("needClear", needClear);
        savingState.putInt("digitCount", digitCount);
        savingState.putCharSequence("operator", operator);
        savingState.putCharSequence("oldNumber", oldNumber);
    }
    // Вызов при восстановлении конфигурации
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        Log.d("CalcActivity", "onRestoreInstanceState");

        tvHistory.setText(savedState.getCharSequence("history"));
        tvResult.setText(savedState.getCharSequence("result"));
        needClear = savedState.getBoolean("needClear");
        digitCount = savedState.getInt("digitCount");
        operator = savedState.getCharSequence("operator").toString();
        oldNumber = savedState.getCharSequence("oldNumber").toString();
    }

    private void equalClick(View view) {
        String newNumber = tvResult.getText().toString();

        if (!oldNumber.equals("") && !operator.equals("")) {
            tvHistory.setText("");
            tvHistory.setText(oldNumber + " " + operator);
        }

        if (operator.equals("")) {
            tvHistory.setText(
                    getString(
                            R.string.calc_equal_one_number_history,
                            newNumber
                    )
            );
            return;
        }

        needClear = true;

        if (newNumber.endsWith(commaSign)) {
            tvHistory.setText(
                getString(
                        R.string.calc_equal_one_number_history,
                        newNumber.substring(0, newNumber.length() - 1)
                )
            );
            return;
        }

        double newArg = parseDoubleFromText(newNumber);
        if (newArg == 0) return;
        double oldArg = parseDoubleFromText(oldNumber);
        if (oldArg == 0) return;

        double result = 0.0;

        switch (operator) {
            case "+": result = oldArg + newArg; break;
            case "-": result = oldArg - newArg; break;
            case "*": result = oldArg * newArg; break;
            case "/": result = oldArg / newArg; break;
        }
        tvHistory.setText(
                getString(
                        R.string.calc_equal_history,
                        tvHistory.getText().toString(),
                        newNumber
                )
        );
        displayResult(result);
    }

    private void operationClick(View view) {
        needClear = true;
        oldNumber = tvResult.getText().toString();

        switch (view.getId()) {
            case R.id.calc_btn_plus: operator = "+"; break;
            case R.id.calc_btn_minus: operator = "-"; break;
            case R.id.calc_btn_multiplication: operator = "*"; break;
            case R.id.calc_btn_divide: operator = "/"; break;
        }

        operator = operator
                .replace("+", plusSymbol)
                .replace("-", minusSymbol)
                .replace("*", multiplicationSymbol)
                .replace("/", divideSymbol);

        if (oldNumber.endsWith(commaSign)) {
            tvHistory.setText(
                getString(
                    R.string.calc_operation_history,
                    oldNumber.substring(0, oldNumber.length() - 1),
                    operator
                )
            );
        }
        else {
            tvHistory.setText(
                getString(
                    R.string.calc_operation_history,
                    oldNumber,
                    operator
                )
            );
        }

        operator = operator
                .replace(plusSymbol,"+")
                .replace(minusSymbol, "-")
                .replace(multiplicationSymbol, "*")
                .replace(divideSymbol, "/");
    }

    private void rootClick(View view) {
        String result = tvResult.getText().toString();
        double arg = parseDoubleFromText(result);
        if (arg == 0) return;
        if (arg < 0) {
            // Корень из отрицательного числа не извлекается (в действительных числах)
            /* Доступ к системным устройствам на примере вибрации
            Прежде всего нужно получить разрешение на использование устройства.
            Некоторые устройства не требуют подтверждение от пользователя, но все они
            должны запросить разрешение от системы.
            Заявка на доступ к устройству (и другие разрешения) указываются в манифесте
                <uses-permission android:name="android.permission.VIBRATE"/>
            Дальнейшая работа с устройством может зависеть от версии API на которую рассчитано
            приложение.
             */
            /* Самый простой подход - deprecated from 0 (Oreo, API 26)
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(250); // вибрация 250 мс
            */
            // начиная с S (API 31) изменились правила доступа к устройствам
            Vibrator vibrator;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vibratorManager = (VibratorManager)
                        getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                vibrator = vibratorManager.getDefaultVibrator();
            }
            else {
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            }

            // шаблон вибрации 1 - пауза, 2 - работа, 3 - пауза, 4 - работа, .....
            long[] vibratePattern = {0, 500, 110, 500, 110, 450, 110, 200, 110, 170, 40, 450, 110, 200, 110, 170, 40, 500};

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // однократное включение
                // vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE));

                vibrator.vibrate(
                        VibrationEffect.createWaveform(
                                vibratePattern, -1 // индекс повтора, -1 - без повторов, один раз
                        )
                );
            }
            else {
                // vibrator.vibrate(250); // вибрация 250 мс
                vibrator.vibrate(vibratePattern, -1); // по шаблону
            }
/*
            Получение вибратора:
                        vibratorManager
            API >= 31 <
                        getSystemService

            Использование:
                        vibrator.vibrate( VibrationEffect.... )
            API >= 26 <
                        vibrator.vibrate()
 */
        }
        tvHistory.setText(getString(R.string.calc_root_history, result));
        arg = Math.sqrt(arg);
        displayResult(arg);
        needClear = true;
    }

    private void percentClick(View view) {
        if (operator.equals("")) {
            String result = tvResult.getText().toString();
            double arg = parseDoubleFromText(result);
            if (arg == 0) return;
            arg /= 100;
            displayResult(arg); // 200% = 2
            needClear = true;
        }
        else {
            String newNumber = tvResult.getText().toString();
            double newArg = parseDoubleFromText(newNumber);
            if (newArg == 0) return;
            double oldArg = parseDoubleFromText(oldNumber);
            if (oldArg == 0) return;

            switch (operator) {
                case "+": newArg = oldArg + oldArg * newArg / 100; break; // 200 + 5% = 210
                case "-": newArg = oldArg - oldArg * newArg / 100; break; // 200 - 5% = 190
                case "*": newArg = oldArg * newArg / 100; break;
                case "/": newArg = oldArg / newArg * 100; break;
            }
            displayResult(newArg);
            operator = "";
        }
    }

    private void inverseClick(View view) {
        String result = tvResult.getText().toString();
        double arg = parseDoubleFromText(result);
        if (arg == 0) return;
        tvHistory.setText(getString(R.string.calc_inverse_history, result));
        arg = 1 / arg;
        displayResult(arg);
        needClear = true;
    }

    private void squareClick(View view) {
        String result = tvResult.getText().toString();
        double arg = parseDoubleFromText(result);
        if (arg == 0) return;
        tvHistory.setText(getString(R.string.calc_square_history, result));
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
        operator = "";
        oldNumber = "";
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
        }
        if(digitCount >= 10) {
            return;
        }
        String digit = ((Button) view).getText().toString();
        result += digit;
        digitCount++; // увеличиваем счетчик цифр
        displayResult(result);
    }

    private void displayResult(String result) {
        if ("".equals(result) || minusSign.equals(result)) {
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
                .replace(".", commaSign)
                .replace("+", plusSymbol)
                .replace("-", minusSymbol)
                .replace("*", multiplicationSymbol)
                .replace("/", divideSymbol);

        displayResult(result);
    }

    private double parseDoubleFromText(String text) {
        double arg;
        try {
            arg = Double.parseDouble(
                    text
                            .replace(minusSign, "-")
                            .replaceAll(zeroSymbol, "0")
                            .replace(commaSign, ".")
                            .replace(plusSymbol, "+")
                            .replace(minusSymbol, "-")
                            .replace(multiplicationSymbol, "*")
                            .replace(divideSymbol, "/")
            );
            return arg;
        }
        catch (NumberFormatException | NullPointerException ignored) {
            Toast.makeText(                             // Всплывающее сообщение
                            this,                       // контекст - родительская активность
                            R.string.calc_error_parse,  // текст либо ресурс
                            Toast.LENGTH_SHORT)         // длительность (во времени)
                    .show();                            // !! не забывать - запуск тоста
            return 0;
        }
    }
}
