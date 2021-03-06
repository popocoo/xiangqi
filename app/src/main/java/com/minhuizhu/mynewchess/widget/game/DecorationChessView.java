package com.minhuizhu.mynewchess.widget.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.minhuizhu.mynewchess.presenter.Position;
import com.minhuizhu.mynewchess.R;
import com.minhuizhu.mynewchess.manager.MusicManager;
import com.minhuizhu.common.util.WidgetUtil;

/**
 * Created by zhuminh on 2017/2/2.
 */
public class DecorationChessView extends View {

    //COMPUTER_BLACK COMPUTER_RED 用于判断电脑执红还是执黑
    private static final int COMPUTER_BLACK = 0;
    private static final int COMPUTER_RED = 1;
    private static final int COMPUTER_NONE = 2;


    private static final String TAGS = "ChessView";

    private Bitmap imgBackground, imgXQWLight;
    private static final String[] IMAGE_NAME = {null, null, null, null, null,
            null, null, null, "rk", "ra", "rb", "rn", "rr", "rc", "rp", null,
            "bk", "ba", "bb", "bn", "br", "bc", "bp", null,};
    private int widthBackground, heightBackground;

    static final int RS_DATA_LEN = 512;


    private Position pos = new Position();

    private int cursorX, cursorY;


    private boolean init = false;
    private Bitmap imgBoard;
    private Bitmap[] imgPieces = new Bitmap[24];
    private int squareSize, width, height, left, right, top, bottom;
    private Context context;
    private Paint paint = new Paint();
    public int moveMode;
    private int sqSelected;
    private byte[] chooseChess = {0, 0, 0, 20, 21, 22, 0, 0, 0, 0, 14, 13, 12, 0, 0, 0, 0, 0, 0, 16, 17, 18, 19, 0, 0, 11, 10, 9, 8, 0, 0, 0};
    private Bitmap imgSelected;
    private byte selectedChoosePc;
    private float moveX;
    private float moveY;

    public void setMoveMode(int moveMode) {
        if (this.moveMode == moveMode || moveMode < 0 || moveMode > 1) {
            return;
        }
        this.moveMode = moveMode;
        load();
    }

    public void drawChooseChess(Canvas canvas) {
        int length = chooseChess.length;
        for (int i = 0; i < length; i++) {
            if (chooseChess[i] == 0) {
                continue;
            }
            int chooseCursorY = i & 15;
            int chooseCursorX = i >> 4;
            if (moveMode != 0) {
                chooseCursorY = 15 - chooseCursorY;
            }
            int x = width + (chooseCursorX - 2) * squareSize;
            int y = (chooseCursorY - 3) * squareSize;
            Bitmap mBitmap = Bitmap.createScaledBitmap(imgPieces[chooseChess[i]], squareSize, squareSize, true);
            canvas.drawBitmap(mBitmap, x, y, paint);
        }
    }

    public DecorationChessView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        imgBackground = BitmapFactory.decodeResource(getResources(),
                R.drawable.background);
        imgXQWLight = BitmapFactory.decodeResource(getResources(),
                R.drawable.xqwlight);
        widthBackground = imgBackground.getWidth();
        heightBackground = imgBackground.getHeight();
        this.context = context;

    }


    public void load() {
        initSelect();
        sqSelected = 0;
        pos.fromFen(Position.STARTUP_FEN[0]);
        invalidate();
    }

    private void initSelect() {
        cursorX = cursorY = -1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawChess(canvas);
        super.onDraw(canvas);
    }

    protected void drawChess(Canvas canvas) {
        initBitmap();
        drawBackground(canvas);
        drawBoard(canvas);
        drawEveryChess(canvas);
        drawChooseChess(canvas);
        drawSelectedChess(canvas);
        drawMoveChess(canvas);
    }

    private void drawMoveChess(Canvas canvas) {
        if (selectedChoosePc == 0) {
            return;
        }
        int drawX = (int) (moveX - squareSize * 0.75);
        int drawY = (int) (moveY - squareSize * 0.75);
        Bitmap mBitmap = Bitmap.createScaledBitmap(imgPieces[selectedChoosePc], (int) (squareSize * 1.5), (int) (squareSize * 1.5), true);
        canvas.drawBitmap(mBitmap, drawX, drawY, paint);
    }

    private void drawSelectedChess(Canvas canvas) {
        if (sqSelected == 0) {
            return;
        }
        drawSquare(canvas, imgSelected, sqSelected);
    }


    private void drawEveryChess(Canvas canvas) {
        for (int sq = 0; sq < 256; sq++) {  //绘制棋子
            if (Position.IN_BOARD(sq)) {
                int pc = pos.squares[sq];
                if (pc > 0) {
                    drawSquare(canvas, imgPieces[pc], sq);
                }
            }
        }
    }

    private void drawBoard(Canvas canvas) {
        Bitmap mBitmap = Bitmap.createScaledBitmap(imgBoard, right - left + 32,  //绘制棋盘
                bottom - top + 32, false);
        canvas.drawBitmap(mBitmap, left, 0, paint);
    }

    private void drawBackground(Canvas canvas) {
        for (int x = 0; x < width; x += widthBackground) {    //绘制象棋的背景
            for (int y = 0; y < height; y += heightBackground) {
                canvas.drawBitmap(imgBackground, x, y, paint);
            }
        }

    }

    private void initBitmap() {
        if (init) {
            return;
        }
        init = true;
        width = getWidth();
        height = getHeight();
        squareSize = Math.min(width / 9, height / 10);
        int boardWidth = squareSize * 9;
        int boardHeight = squareSize * 10;
        try {
            imgBoard = BitmapFactory.decodeResource(getResources(),
                    R.drawable.board);
            imgSelected = BitmapFactory.decodeResource(getResources(),
                    R.drawable.selected);
            for (int pc = 0; pc < 24; pc++) {
                if (IMAGE_NAME[pc] == null) {
                    imgPieces[pc] = null;
                } else {
                    imgPieces[pc] = BitmapFactory.decodeResource(
                            getResources(),
                            getResources().getIdentifier(
                                    "" + IMAGE_NAME[pc], "drawable",
                                    context.getPackageName()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        left = (width - boardWidth) / 2;
        top = (height - boardHeight) / 2;
        right = left + boardWidth - 32;
        bottom = top + boardHeight - 32;
    }

    private void drawSquare(Canvas canvas, Bitmap bitmap, int sq) {
        int sqFlipped = (moveMode == COMPUTER_RED ? Position.SQUARE_FLIP(sq)
                : sq);
        int sqX = left + (Position.FILE_X(sqFlipped) - Position.FILE_LEFT)
                * squareSize;
        int sqY = top + (Position.RANK_Y(sqFlipped) - Position.RANK_TOP)
                * squareSize;
        if (bitmap != null) {

            Bitmap mBitmap = Bitmap.createScaledBitmap(bitmap, squareSize, squareSize, true);
            canvas.drawBitmap(mBitmap, sqX, sqY, paint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                selectedChoosePc = 0;
                if (x > left && x < left + squareSize * 9) {
                    pressCurrentChess(x, y);
                } else if (x > width - 2 * squareSize) {
                    pressChooseChess(x, y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = x;
                moveY = y;
                if (selectedChoosePc != 0) {
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (selectedChoosePc != 0) {
                    onChooseChessUp(event);
                }
                break;
        }
        return true;
    }

    private void onChooseChessUp(MotionEvent event) {
        cursorX = WidgetUtil.MIN_MAX(0, ((int) event.getX() - left) / squareSize, 8);
        cursorY = WidgetUtil.MIN_MAX(0, ((int) event.getY() - top) / squareSize, 9);
        int chooseUpSq = Position.COORD_XY(cursorX + Position.FILE_LEFT, cursorY
                + Position.RANK_TOP);
        if (moveMode == COMPUTER_RED) {
            chooseUpSq = Position.SQUARE_FLIP(chooseUpSq);
        }
        if (pos.isLegalAdd(selectedChoosePc, chooseUpSq)) {
            pos.squares[chooseUpSq] = selectedChoosePc;
        }
        selectedChoosePc = 0;
        invalidate();
    }

    private void pressChooseChess(float x, float y) {
        if (y > squareSize * 4 && y < height - squareSize * 4) {
            return;
        }
        int chooseCursorX = (width - x < squareSize) ? 1 : 0;
        int chooseCursorY = (int) (y / squareSize) + 3;
        if (moveMode != 0) {
            chooseCursorY = 15 - chooseCursorY;
        }
        selectedChoosePc = chooseChess[chooseCursorX * 16 + chooseCursorY];
    }

    protected void pressCurrentChess(float x, float y) {

        cursorX = WidgetUtil.MIN_MAX(0, ((int) x - left) / squareSize, 8);
        cursorY = WidgetUtil.MIN_MAX(0, ((int) y - top) / squareSize, 9);
        int currentSq = Position.COORD_XY(cursorX + Position.FILE_LEFT, cursorY
                + Position.RANK_TOP);
        if (moveMode == COMPUTER_RED) {
            currentSq = Position.SQUARE_FLIP(currentSq);
        }
        if (sqSelected == currentSq) {
            pos.squares[sqSelected] = 0;
            sqSelected = 0;

        } else {
            if (pos.squares[currentSq] != 0) {
                sqSelected = currentSq;
                MusicManager.getInstance().playBtnDownMusic();
            } else {
                sqSelected = 0;
            }

        }
        invalidate();
    }

    public int getMoveMode() {
        return moveMode;
    }

    public byte[] getSquare() {
        return pos.squares;
    }
}






























































































