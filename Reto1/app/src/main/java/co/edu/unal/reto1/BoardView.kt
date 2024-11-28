package co.edu.unal.reto1

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class BoardView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // Constants
    companion object {
        const val GRID_WIDTH = 6
    }

    private lateinit var mHumanBitmap: Bitmap
    private lateinit var mComputerBitmap: Bitmap

    private var mPaint: Paint = Paint()
    private var mGame: TicTacToeGame? = null
    fun setGame(game: TicTacToeGame?) {
        mGame = game
    }

    fun getBoardCellWidth(): Int {
        return width / 3
    }

    fun getBoardCellHeight(): Int {
        return height / 3
    }

    init {
        initialize()
    }

    private fun initialize() {
        mHumanBitmap = BitmapFactory.decodeResource(resources, R.drawable.x_img)
        mComputerBitmap = BitmapFactory.decodeResource(resources, R.drawable.o_img)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Determine the width and height of the View
        val boardWidth = width
        val boardHeight = height

        // Make thick, light gray lines
        mPaint.color = Color.LTGRAY
        mPaint.strokeWidth = GRID_WIDTH.toFloat()

        // Draw the two vertical board lines
        val cellWidth = boardWidth / 3
        canvas.drawLine(cellWidth.toFloat(), 0f, cellWidth.toFloat(), boardHeight.toFloat(), mPaint)
        canvas.drawLine(
            (cellWidth * 2).toFloat(),
            0f,
            (cellWidth * 2).toFloat(),
            boardHeight.toFloat(),
            mPaint
        )

        // Draw the two horizontal board lines
        val cellHeight = boardHeight / 3
        canvas.drawLine( 0f,cellHeight.toFloat(), boardWidth.toFloat(),cellHeight.toFloat(), mPaint)
        canvas.drawLine(
            0f,
            (cellHeight * 2).toFloat(),
            boardWidth.toFloat(),
            (cellHeight * 2).toFloat(),
            mPaint
        )

        // Draw all the X and O images
        for (i in 0 until TicTacToeGame.BOARD_SIZE) {
            val col = i % 3
            val row = i / 3
            // Define the boundaries of a destination rectangle for the image
            val left = col * cellWidth
            val top = row * cellWidth
            val right = (col + 1) * cellWidth
            val bottom = (row + 1) * cellWidth

            if (mGame != null) {
                when (mGame!!.getBoardOccupant(i)) {
                    TicTacToeGame.HUMAN_PLAYER -> {
                        canvas.drawBitmap(
                            mHumanBitmap,
                            null, // src
                            Rect(left, top, right, bottom), // dest
                            null
                        )
                    }

                    TicTacToeGame.COMPUTER_PLAYER -> {
                        canvas.drawBitmap(
                            mComputerBitmap,
                            null, // src
                            Rect(left, top, right, bottom), // dest
                            null
                        )
                    }
                }
            }
        }
    }
}
