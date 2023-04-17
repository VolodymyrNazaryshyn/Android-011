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
    private final int CELLS_SIZE = 4;
    private final int[][] cells = new int[CELLS_SIZE][CELLS_SIZE]; // значения в ячейках поля
    private final int[][] prevCells = new int[CELLS_SIZE][CELLS_SIZE]; // предыдущий ход
    private final TextView[][] tvCells = new TextView[CELLS_SIZE][CELLS_SIZE]; // ссылки на ячейки поля
    private final Random random = new Random();

    private int score;
    private int prevScore;
    private int bestScore;
    private int prevBestScore;
    private TextView tvScore;
    private TextView text;
    private TextView tvBestScore;
    private Animation spawnAnimation;
    private Animation collapseAnimation;

    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvScore = findViewById(R.id.game_tv_score);
        text = findViewById(R.id.text);
        tvBestScore = findViewById(R.id.game_tv_best_score);
        tvScore.setText(getString(R.string.game_score, "69.6k"));
        tvBestScore.setText(getString(R.string.game_best_score, "69.6k"));

        spawnAnimation = AnimationUtils.loadAnimation(this, R.anim.cell_spawn);
        spawnAnimation.reset();
        collapseAnimation = AnimationUtils.loadAnimation(this, R.anim.cell_collapse);
        collapseAnimation.reset();

        for (int i = 0; i < CELLS_SIZE; ++i) {
            for (int j = 0; j < CELLS_SIZE; ++j) {
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
                                if (isGaveOver()) {
                                    text.setText(R.string.game_over);
                                    return;
                                }
                                if (canMoveRight(cells)) {
                                    updatePrevCells();
                                    moveRight();
                                    spawnCell(1);
                                    showField();
                                    Toast.makeText(
                                                    GameActivity.this,
                                                    "Right",
                                                    Toast.LENGTH_SHORT)
                                            .show();
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
                                if (isGaveOver()) {
                                    text.setText(R.string.game_over);
                                    return;
                                }
                                if (canMoveLeft(cells)) {
                                    updatePrevCells();
                                    moveLeft();
                                    spawnCell(1);
                                    showField();
                                    Toast.makeText(
                                                    GameActivity.this,
                                                    "Left",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                                else {
                                    Toast.makeText(
                                                    GameActivity.this,
                                                    "No Left Move",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                            @Override
                            public void onSwipeTop() {
                                if (isGaveOver()) {
                                    text.setText(R.string.game_over);
                                    return;
                                }
                                if (canMoveTop(cells)) {
                                    updatePrevCells();
                                    moveTop();
                                    spawnCell(1);
                                    showField();
                                    Toast.makeText(
                                                    GameActivity.this,
                                                    "Top",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                                else {
                                    Toast.makeText(
                                                    GameActivity.this,
                                                    "No Top Move",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                            @Override
                            public void onSwipeBottom() {
                                if (isGaveOver()) {
                                    text.setText(R.string.game_over);
                                    return;
                                }
                                if (canMoveBottom(cells)) {
                                    updatePrevCells();
                                    moveBottom();
                                    spawnCell(1);
                                    showField();
                                    Toast.makeText(
                                                    GameActivity.this,
                                                    "Bottom",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                                else {
                                    Toast.makeText(
                                                    GameActivity.this,
                                                    "No Bottom Move",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        }
                );

        findViewById(R.id.game_new).setOnClickListener(this::newGame);
        findViewById(R.id.game_undo).setOnClickListener(this::undoMove);
        newGame(null);
    }
    private void newGame(View view) {
        for (int i = 0; i < CELLS_SIZE; ++i) {
            for (int j = 0; j < CELLS_SIZE; ++j) {
                cells[i][j] = 0;
            }
        }
        score = 0;
        spawnCell(2);
        showField();
        text.setText(R.string.game_description);
    }
    private boolean spawnCell(int n) {
        // собираем данные о пустых ячейках
        List<Coord> coordinates = new ArrayList<>();
        for (int i = 0; i < CELLS_SIZE; ++i) {
            for (int j = 0; j < CELLS_SIZE; ++j) {
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
    private boolean isGaveOver() {
        return !canMoveRight(cells) && !canMoveLeft(cells)
                && !canMoveTop(cells) && !canMoveBottom(cells);
    }

    // region [canMove functions]
    private boolean canMoveRight(int[][] cells) {
        boolean canMoveRight = false;
        for (int[] row : cells) {
            for (int col = row.length - 2; col >= 0; col--) {
                if (row[col] != 0) {
                    if (row[col+1] == 0 || row[col+1] == row[col]) {
                        canMoveRight = true;
                        break;
                    }
                }
            }
            if (canMoveRight) {
                break;
            }
        }
        return canMoveRight;
       /*
        Д.З. Реализовать проверку возможности хода вправо (без изменения состояния поля)
        ** реализовать ходы и проверки по другим направлениям
         */
    }
    public boolean canMoveLeft(int[][] cells) {
        boolean canMoveLeft = false;
        for (int[] row : cells) {
            for (int col = 1; col < row.length; col++) {
                if (row[col] != 0) {
                    if (row[col-1] == 0 || row[col-1] == row[col]) {
                        canMoveLeft = true;
                        break;
                    }
                }
            }
            if (canMoveLeft) {
                break;
            }
        }
        return canMoveLeft;
    }
    public boolean canMoveTop(int[][] cells) {
        boolean canMoveTop = false;
        for (int col = 0; col < cells[0].length; col++) {
            for (int row = 1; row < cells.length; row++) {
                if (cells[row][col] != 0) {
                    if (cells[row-1][col] == 0 || cells[row-1][col] == cells[row][col]) {
                        canMoveTop = true;
                        break;
                    }
                }
            }
            if (canMoveTop) {
                break;
            }
        }
        return canMoveTop;
    }
    public boolean canMoveBottom(int[][] cells) {
        boolean canMoveBottom = false;
        for (int col = 0; col < cells[0].length; col++) {
            for (int row = cells.length - 2; row >= 0; row--) {
                if (cells[row][col] != 0) {
                    if (cells[row+1][col] == 0 || cells[row+1][col] == cells[row][col]) {
                        canMoveBottom = true;
                        break;
                    }
                }
            }
            if (canMoveBottom) {
                break;
            }
        }
        return canMoveBottom;
    }
    // endregion

    // region [move functions]
    private boolean moveRight() {
        boolean isMoved = false ;    // [0002]->[0002], [0200]->[0002], [2020]->[0022]->[0004]

        for( int i = 0; i < CELLS_SIZE; ++i ) {
            // сдвиги
            boolean wasReplace;
            do {
                wasReplace = false;
                for (int j = CELLS_SIZE - 1; j > 0; --j) {
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
            for (int j = CELLS_SIZE - 1; j > 0; --j) {  // [2202] -> [0222] -> [0204] -> [0024]
                if (cells[i][j] == cells[i][j - 1] && cells[i][j] != 0) {  // соседние ячейки равны  [2222]
                    score += cells[i][j] + cells[i][j - 1] ;   // счет = сумма всех объединенных ячеек
                    if (score > bestScore) {
                        bestScore = score;
                    }
                    cells[i][j] *= -2 ;  // [2224]; "-" - признак для анимации
                    cells[i][j - 1] = 0 ;   // [2204]
                    isMoved = true ;
                }
            }  // [0404]  при коллапсе может понадобиться дополнительное смещение
            for (int j = CELLS_SIZE - 1; j > 0; --j) {
                if (cells[i][j] == 0 && cells[i][j - 1] != 0) {
                    cells[i][j] = cells[i][j - 1];
                    cells[i][j - 1] = 0;
                }
            }
            for (int j = CELLS_SIZE - 1; j > 0; --j) {
                if( cells[i][j] < 0 ) {  // надо включить анимацию
                    cells[i][j] = -cells[i][j] ;
                    tvCells[i][j].startAnimation( collapseAnimation ) ;
                }
            } // [0044]
        }
        return isMoved ;
    }
    private boolean moveLeft() {
        boolean isMoved = false ;

        for( int i = 0; i < CELLS_SIZE; ++i ) {
            // сдвиги
            boolean wasReplace;
            do {
                wasReplace = false;
                for (int j = 0; j < CELLS_SIZE - 1; ++j) {
                    if (cells[i][j] == 0 && cells[i][j + 1] != 0) {
                        cells[i][j] = cells[i][j + 1];
                        cells[i][j + 1] = 0;
                        wasReplace = true;
                        isMoved = true;
                    }
                }
            } while (wasReplace);

            // collapse
            for (int j = 0; j < CELLS_SIZE - 1; ++j) {
                if (cells[i][j] == cells[i][j + 1] && cells[i][j] != 0) {
                    score += cells[i][j] + cells[i][j + 1];
                    if (score > bestScore) {
                        bestScore = score;
                    }
                    cells[i][j] *= -2;
                    cells[i][j + 1] = 0;
                    isMoved = true;
                }
            }
            for (int j = 0; j < CELLS_SIZE - 1; ++j) {
                if (cells[i][j] == 0 && cells[i][j + 1] != 0) {
                    cells[i][j] = cells[i][j + 1];
                    cells[i][j + 1] = 0;
                }
            }
            for (int j = 0; j < CELLS_SIZE - 1; ++j) {
                if (cells[i][j] < 0) {
                    cells[i][j] = -cells[i][j];
                    tvCells[i][j].startAnimation(collapseAnimation);
                }
            }
        }
        return isMoved;
    }
    private boolean moveTop() {
        boolean isMoved = false;

        for (int j = 0; j < CELLS_SIZE; ++j) {
            // сдвиги
            boolean wasReplace;
            do {
                wasReplace = false;
                for (int i = 0; i < CELLS_SIZE - 1; ++i) {
                    if (cells[i][j] == 0 && cells[i + 1][j] != 0) {
                        cells[i][j] = cells[i + 1][j];
                        cells[i + 1][j] = 0;
                        wasReplace = true;
                        isMoved = true;
                    }
                }
            } while (wasReplace);

            // collapse
            for (int i = 0; i < CELLS_SIZE - 1; ++i) {
                if (cells[i][j] == cells[i + 1][j] && cells[i][j] != 0) {
                    score += cells[i][j] + cells[i + 1][j];
                    if (score > bestScore) {
                        bestScore = score;
                    }
                    cells[i][j] *= -2;
                    cells[i + 1][j] = 0;
                    isMoved = true;
                }
            }
            for (int i = 0; i < CELLS_SIZE - 1; ++i) {
                if (cells[i][j] == 0 && cells[i + 1][j] != 0) {
                    cells[i][j] = cells[i + 1][j];
                    cells[i + 1][j] = 0;
                }
            }
            for (int i = 0; i < CELLS_SIZE - 1; ++i) {
                if (cells[i][j] < 0) {
                    cells[i][j] = -cells[i][j];
                    tvCells[i][j].startAnimation(collapseAnimation);
                }
            }
        }
        return isMoved;
    }
    private boolean moveBottom() {
        boolean isMoved = false;

        for (int j = 0; j < CELLS_SIZE; ++j) {
            // сдвиги
            boolean wasReplace;
            do {
                wasReplace = false;
                for (int i = CELLS_SIZE - 1; i > 0; --i) {
                    if (cells[i][j] == 0 && cells[i - 1][j] != 0) {
                        cells[i][j] = cells[i - 1][j];
                        cells[i - 1][j] = 0;
                        wasReplace = true;
                        isMoved = true;
                    }
                }
            } while (wasReplace);

            // collapse
            for (int i = CELLS_SIZE - 1; i > 0; --i) {
                if (cells[i][j] == cells[i - 1][j] && cells[i][j] != 0) {
                    score += cells[i][j] + cells[i - 1][j];
                    if (score > bestScore) {
                        bestScore = score;
                    }
                    cells[i][j] *= -2;
                    cells[i - 1][j] = 0;
                    isMoved = true;
                }
            }
            for (int i = CELLS_SIZE - 1; i > 0; --i) {
                if (cells[i][j] == 0 && cells[i - 1][j] != 0) {
                    cells[i][j] = cells[i - 1][j];
                    cells[i - 1][j] = 0;
                }
            }
            for (int i = CELLS_SIZE - 1; i > 0; --i) {
                if (cells[i][j] < 0) {
                    cells[i][j] = -cells[i][j];
                    tvCells[i][j].startAnimation(collapseAnimation);
                }
            }
        }
        return isMoved;
    }
    // endregion

    @SuppressLint("DiscouragedApi")
    private void showField() {
        Resources resources = getResources();
        String packageName = getPackageName();
        for (int i = 0; i < CELLS_SIZE; ++i) {
            for (int j = 0; j < CELLS_SIZE; ++j) {
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
        tvBestScore.setText(getString(R.string.game_best_score, String.valueOf(bestScore)));
    }

    // Делаем копию текущего игрового поля и сохраняем ее в prevCells
    private void savePrevCells() {
        for (int i = 0; i < CELLS_SIZE; i++) {
            System.arraycopy(cells[i], 0, prevCells[i], 0, CELLS_SIZE);
        }
    }

    // Проверяем, не изменилось ли игровое поле, и если да, сохраняем новое состояние
    private void updatePrevCells() {
        boolean changed = false;
        for (int i = 0; i < CELLS_SIZE; i++) {
            for (int j = 0; j < CELLS_SIZE; j++) {
                if (cells[i][j] != prevCells[i][j]) {
                    changed = true;
                    prevScore = score;
                    prevBestScore = bestScore;
                    break;
                }
            }
            if (changed) {
                break;
            }
        }
        if (changed) {
            savePrevCells();
        }
    }

    // Восстанавливаем предыдущее состояние игрового поля из prevCells
    private void undoMove(View view) {
        for (int i = 0; i < CELLS_SIZE; i++) {
            System.arraycopy(prevCells[i], 0, cells[i], 0, CELLS_SIZE);
        }
        // Восстанавливаем другие переменные состояния игры
        score = prevScore;
        bestScore = prevBestScore;
        showField();
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