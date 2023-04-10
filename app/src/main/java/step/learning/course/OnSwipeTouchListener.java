package step.learning.course;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class OnSwipeTouchListener implements View.OnTouchListener {
    // детектор жестов - основа синтеза свайпов
    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context context) {
        // создаем жестовый детектор на заданном контексте (активность или ее часть)
        this.gestureDetector = new GestureDetector(
                context,
                new GestureListener() // используем свой обработчик жестов
        );
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }

    public void onSwipeRight() {}    // Методы, доступные для перегрузки / реализации
    public void onSwipeLeft() {}     // в конкрентых обработчиках, наш алгоритм
    public void onSwipeTop() {}      // должен вызывать один из них
    public void onSwipeBottom() {}   //

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int MIN_DISTANCE = 100;   // минимальное расстояние, которое может считаться свайпом
        private static final int MIN_VELOCITY = 100;   // минимальная скорость - 100 пк / сек
        private static final float COEFFICIENT = 1.5f; // коеф. отношения X и Y для разделения вертикального и гориз. свайпов

        // событие касания - не обрабатываем, ждем "протяжки"
        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }
        // событие проведения: e1 - точка касания, e2 - отпускания
        @Override
        public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            /* алгоритм разбора свайпов: анализируем взаимное расположение точек е1 и е2,
               а также устанавливаем минимальный предел скорости - медленное движение не
               считаем за свайпы.
             */
            boolean result = false;
            float deltaX = e2.getX() - e1.getX(); // Расстояние между точками е1 и е2
            float deltaY = e2.getY() - e1.getY(); // по горизонтали и вертикали
            if (Math.abs(deltaX) > COEFFICIENT * Math.abs(deltaY)) { // горизонталь
                // в горизонтальном режиме анализируем X, игнорируем Y
                if (Math.abs(deltaX) > MIN_DISTANCE && Math.abs(velocityX) > MIN_VELOCITY) {
                    if (deltaX > 0) { // e1 -----> e2
                        onSwipeRight();
                    }
                    else { // e2 <----- e1
                        onSwipeLeft();
                    }
                    result = true; // событие обработано
                }
            }
            else if (Math.abs(deltaY) > COEFFICIENT * Math.abs(deltaX)) { // вертикаль
                if (Math.abs(deltaY) > MIN_DISTANCE && Math.abs(velocityY) > MIN_VELOCITY) {
                    if (deltaY > 0) { // e1.Y = 0
                        onSwipeBottom();
                    }
                    else {            // e2.Y = 200
                        onSwipeTop();
                    }
                    result = true;
                }
            }
            return result;
        }
    }
}
/*
OnSwipeTouchListener - класс, реализующий обработку событий Touch
и делающий из них события Swipe
 */