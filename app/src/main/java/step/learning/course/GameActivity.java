package step.learning.course;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private final int N = 4;
    private final int[][] cells = new int[N][N]; // значения в ячейках поля
    private final TextView[][] tvCells = new TextView[N][N]; // ссылки на ячейки поля
    private final Random random = new Random();

    private int score;
    private int bestScore;
    private TextView tvScore;
    private TextView tvBestScore;

    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvScore = findViewById(R.id.game_tv_score);
        tvBestScore = findViewById(R.id.game_tv_best_score);
        tvScore.setText(getString(R.string.game_score, "69.6k"));
        tvBestScore.setText(getString(R.string.game_best_score, "69.6k"));

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
                                Toast.makeText(
                                        GameActivity.this,
                                        "Right",
                                        Toast.LENGTH_SHORT)
                                    .show();
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

        newGame();
    }

    private void newGame() {
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) {
                cells[i][j] = 0;
            }
        }
        score = 0;
        spawnCell(2);
        showField();
    }

    @SuppressLint("DiscouragedApi")
    private void showField() {
        Resources resources = getResources();
        String packageName = getPackageName();
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) {
                tvCells[i][j].setText(String.valueOf(cells[i][j]));
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
        }

        return true;
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
