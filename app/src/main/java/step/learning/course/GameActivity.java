package step.learning.course;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private final int N = 4;
    private final int[][] cells = new int[N][N]; // значения в ячейках поля
    private final int[][] saves = new int[N][N]; // предыдущий ход
    private final TextView[][] tvCells = new TextView[N][N]; // ссылки на ячейки поля
    private final Random random = new Random();

    private int score;
    private int bestScore;
    private TextView tvScore;
    private TextView tvBestScore;
    private Animation spawnAnimation;
    private Animation collapseAnimation;

    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvScore = findViewById(R.id.game_tv_score);
        tvBestScore = findViewById(R.id.game_tv_best_score);
        tvScore.setText(getString(R.string.game_score, "69.6k"));
        tvBestScore.setText(getString(R.string.game_best_score, "69.6k"));

        spawnAnimation = AnimationUtils.loadAnimation(this, R.anim.cell_spawn);
        spawnAnimation.reset();
        collapseAnimation = AnimationUtils.loadAnimation(this, R.anim.cell_collapse);
        collapseAnimation.reset();

        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) {
                tvCells[i][j] = findViewById( // R.id.game_cell_12);
                        getResources().getIdentifier(
                                "game_cell_" + i + j,
                                "id",
                                getPackageName()
                        )
                );
            }
        }

        findViewById(R.id.game_field)
                .setOnTouchListener(
                        new OnSwipeTouchListener(GameActivity.this) {
                            @Override
                            public void onSwipeRight() {
                                //saveField(); // [0204] -> [0024] -> no move [undo]
                                if (canMoveRight()) {
                                    saveField();
                                    moveRight();
                                    spawnCell(1);
                                    showField();
                                }
                                else {
                                    Toast.makeText(
                                                    GameActivity.this,
                                                    "No Right Move",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                            @Override
                            public void onSwipeLeft() {
                                Toast.makeText(
                                                GameActivity.this,
                                                "Left",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                            @Override
                            public void onSwipeTop() {
                                Toast.makeText(
                                                GameActivity.this,
                                                "Top",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                            @Override
                            public void onSwipeBottom() {
                                Toast.makeText(
                                                GameActivity.this,
                                                "Bottom",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                );

        findViewById(R.id.game_new).setOnClickListener(this::newGame);
        newGame(null);
    }

    private void newGame(View view) {
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) {
                cells[i][j] = 0;
            }
        }
        score = 0;
        spawnCell(2);
        showField();
    }

    private boolean canMoveRight() {
       /*
        Д.З. Реализовать проверку возможности хода вправо (без изменения состояния поля)
        ** реализовать ходы и проверки по другим направлениям
         */
        return true;
    }

    @SuppressLint("DiscouragedApi")
    private void showField() {
        Resources resources = getResources();
        String packageName = getPackageName();
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) {
                tvCells[i][j].setText(String.valueOf(cells[i][j]));
                float textSize = resources.getDimension( // R.dimen.txt_size_game_cell_4096
                        resources.getIdentifier(
                        "txt_size_game_cell_" + cells[i][j],
                        "dimen",
                        packageName
                ));
                tvCells[i][j].setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvCells[i][j].setTextAppearance( // R.style.GameCell_16
                            resources.getIdentifier(
                                    "GameCell_" + cells[i][j],
                                    "style",
                                    packageName
                            )
                    );
                }
                else {
                    tvCells[i][j].setTextColor( // R.style.GameCell_16
                            resources.getColor(resources.getIdentifier(
                                    "game_fg_" + cells[i][j],
                                    "color",
                                    packageName
                            ))
                    );
                }
                // setTextAppearance не "подтягивает" фоновый цвет
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvCells[i][j].setBackgroundColor(
                            resources.getColor( // R.color.game_bg_16,
                                    resources.getIdentifier(
                                            "game_bg_" + cells[i][j],
                                            "color",
                                            packageName
                                    ),
                                    getTheme()
                            )
                    );
                }
                else {
                    tvCells[i][j].setBackgroundColor(
                            resources.getColor( // R.color.game_bg_16,
                                    resources.getIdentifier(
                                            "game_bg_" + cells[i][j],
                                            "color",
                                            packageName
                                    )
                            )
                    );
                }
            }
        }
        tvScore.setText(getString(R.string.game_score, String.valueOf(score)));
    }

    private boolean spawnCell(int n) {
        // собираем данные о пустых ячейках
        List<Coord> coordinates = new ArrayList<>();
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) {
                if (cells[i][j] == 0) {
                    coordinates.add(new Coord(i, j));
                }
            }
        }
        // проверяем есть ли пустые ячейки
        int cnt = coordinates.size();
        if (cnt < n) return false;

        for (int i = 0; i < n; ++i) {
            // генерируем случайный индекс
            int randIndex = random.nextInt(cnt);
            // извлекаем координаты
            int x = coordinates.get(randIndex).getX();
            int y = coordinates.get(randIndex).getY();
            // ставим в ячейку 2 / 4
            cells[x][y] = random.nextInt(10) == 0 ? 4 : 2;
            // проигрываем анимацию для появившейся ячейки
            tvCells[x][y].startAnimation(spawnAnimation);
        }

        return true;
    }

    private boolean moveRight() {
        boolean isMoved = false ;    // [0002]->[0002], [0200]->[0002], [2020]->[0022]->[0004]

        for( int i = 0; i < N; ++i ) {
            // сдвиги
            boolean wasReplace;
            do {
                wasReplace = false;
                for (int j = N - 1; j > 0; --j) {
                    if (cells[i][j] == 0          // текущая ячейка 0
                            && cells[i][j - 1] != 0) {    // а перед ней - не 0
                        cells[i][j] = cells[i][j - 1];
                        cells[i][j - 1] = 0;
                        wasReplace = true;
                        isMoved = true;
                    }
                }
            } while (wasReplace);

            // collapse
            for (int j = N - 1; j > 0; --j) {  // [2202] -> [0222] -> [0204] -> [0024]
                if (cells[i][j] == cells[i][j - 1] && cells[i][j] != 0) {  // соседние ячейки равны  [2222]
                    score += cells[i][j] + cells[i][j - 1] ;   // счет = сумма всех объединенных ячеек
                    cells[i][j] *= -2 ;  // [2224]; "-" - признак для анимации
                    cells[i][j - 1] = 0 ;   // [2204]
                    isMoved = true ;
                }
            }  // [0404]  при коллапсе может понадобиться дополнительное смещение
            for (int j = N - 1; j > 0; --j) {
                if (cells[i][j] == 0 && cells[i][j - 1] != 0) {
                    cells[i][j] = cells[i][j - 1];
                    cells[i][j - 1] = 0;
                }
            }
            for (int j = N - 1; j > 0; --j) {
                if( cells[i][j] < 0 ) {  // надо включить анимацию
                    cells[i][j] = -cells[i][j] ;
                    tvCells[i][j].startAnimation( collapseAnimation ) ;
                }
            } // [0044]
        }
        return isMoved ;
    }
    private void saveField() {
        for (int i = 0; i < N; i++) {
            System.arraycopy(cells[i], 0, saves[i], 0, N);
        }
    }
    private void undoMove() {
        for (int i = 0; i < N; i++) {
            System.arraycopy(saves[i], 0, cells[i], 0, N);
        }
    }

    private class Coord {
        private int x;
        private int y;

        public Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
/*
Анимации
1. Создаем ресурсную папку res/anim (ПКМ - папка - anim)
2. Создаем ресурсный файл cell_spawn.xml, корневой элемент alpha (см файл)
3. Создаем переменную типа Animation spawnAnimation
4. Загружаем анимацию
    spawnAnimation = AnimationUtils.loadAnimation(this, R.anim.cell_spawn);
    spawnAnimation.reset();
5. Проигрываем анимацию (на любом View)
    tvCells[x][y].startAnimation(spawnAnimation);

Виды анимаций
alpha - прозрачность
rotate - вращение
translate - перемещение
scale - размер (масштаб)
 */