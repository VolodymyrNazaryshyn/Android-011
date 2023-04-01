package step.learning.course;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // inflate - InitializeComponent

        findViewById(R.id.button_calc).setOnClickListener(this::buttonCalcClick);
    }

    private void buttonCalcClick(View view) {
        Intent activityIntent = new Intent(MainActivity.this, CalcActivity.class);
        startActivity(activityIntent);
    }
}
/*
    Запуск дополнительных активностей
    1. создаем новую активность как ресурс (ПКМ - new - Activity - Empty Activity)
    2. Проверяем регистрацию новой активности в манифесте, добавляем
        android:parentActivityName=".MainActivity"
        это укажет иерархию и на активности будет кнопка "назад", возвращающая по иерархии
    3. см. buttonCalcClick
*/
/*
Андроид Студия
1. IDE (Type - Standard)
2. SDK - компилятор-сборщик (можно указать путь установки C:/Android/SDK
    это позволит проще ее использовать для других программ, например, Unity)
3. Экран нового проекта
    - New Project
    - Empty Activity -> Next
    - Name: Android-011
    - Package: step.learning.course
    - Lang: Java
    - Min SDK: API 25
     - Finish

4.1. Системная виртуализация.
   проверяем: диспетчер задач - производительность - ЦП (смотрим "виртуализация")
   если "выключено", то необходимо ее включить в BIOS

4.2. Виртуальное устройство - эмулятор
- Device Manager
- Create Device
- Выбираем размер и разрешение. Можно исходить из того, что
   на типичном экране FullHD размер экрана не будет более
   400х800 - 500х1000. При этом чем больше размер, тем больше
   нагрузка на память и процессор.
- Скачиваем образ ОС (Андроид) выбор произвольный - либо по физическому тлф
   либо по возможности ПК. Версия Андроид не ниже 26

4.3. Физическое устройство
 - Активируем в устройстве режим разработчика. Зависит от тлф, обычно
   в меню "Об устройстве" несколько раз нужно кликнуть номер версии
 - Заходим в настройки - появляется дополнительное меню "Режим разработчика"
   находим "Отладка по USB" / "Установка по USB" / "Запуск по USB" - отмечаем
   всё что есть.
 - Data кабель (4 провода, зарядный не подойдет) - подключаем к ПК
 - Разблокировать телефон и подключить по USB - появляется сообщение "Отладка по USB"
   [] доверять данному ПК
   Даем разрешение. Через некоторое время тлф должен опознаться в устройствах
 */
