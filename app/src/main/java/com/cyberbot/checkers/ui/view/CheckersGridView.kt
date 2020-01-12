package com.cyberbot.checkers.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.cyberbot.checkers.R
import com.cyberbot.checkers.game.Grid
import com.cyberbot.checkers.game.GridEntry
import com.cyberbot.checkers.game.PieceType
import com.cyberbot.checkers.game.PlayerNum
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.roundToInt


class CheckersGridView(
    context: Context,
    attrs: AttributeSet?
) : View(context, attrs) {

    // <editor-fold defaultstate="collapsed" desc="Colors and paint">
    var gridColorMoveAllowed: Int = 0
        set(value) {
            field = value
            paintGridColorMoveAllowed.color = value
            invalidate()
        }

    var gridColorMoveAllowedHint: Int = 0
        set(value) {
            field = value
            paintGridColorMoveAllowedHint.color = value
            invalidate()
        }

    var gridColorCaptureAllowedHint: Int = 0
        set(value) {
            field = value
            paintGridColorCaptureAllowedHint.color = value
            invalidate()
        }

    var gridColorMoveForbidden: Int = 0
        set(value) {
            field = value
            paintGridColorMoveForbidden.color = value
            invalidate()
        }

    var gridColorMoveSource: Int = 0
        set(value) {
            field = value
            paintGridColorMoveSource.color = value
            invalidate()
        }

    var gridColorLegal: Int = 0
        set(value) {
            field = value
            paintGridColorLegal.color = value
            invalidate()
        }

    var girdColorIllegal: Int = 0
        set(value) {
            field = value
            paintGridColorIllegal.color = value
            invalidate()
        }

    var playerColor1: Int = 0
        set(value) {
            field = value
            paintPlayerColor1.color = value
            invalidate()
        }

    var playerColor2: Int = 0
        set(value) {
            field = value
            paintPlayerColor2.color = value
            invalidate()
        }

    var playerOutlineColor1: Int = 0
        set(value) {
            field = value
            paintPlayerOutlineColor1.color = value
            invalidate()
        }

    var playerOutlineColor2: Int = 0
        set(value) {
            field = value
            paintPlayerOutlineColor2.color = value
            invalidate()
        }

    private val paintGridColorMoveAllowed = Paint(0).apply {
        color = gridColorMoveAllowed
        style = Paint.Style.FILL
    }

    private val paintGridColorMoveAllowedHint = Paint(0).apply {
        color = gridColorMoveAllowedHint
        style = Paint.Style.FILL
    }

    private val paintGridColorCaptureAllowedHint = Paint(0).apply {
        color = gridColorCaptureAllowedHint
        style = Paint.Style.FILL
    }

    private val paintGridColorMoveForbidden = Paint(0).apply {
        color = gridColorMoveForbidden
        style = Paint.Style.FILL
    }

    private val paintGridColorMoveSource = Paint(0).apply {
        color = gridColorMoveSource
        style = Paint.Style.FILL
    }

    private val paintGridColorLegal = Paint(0).apply {
        color = gridColorLegal
        style = Paint.Style.FILL
    }

    private val paintGridColorIllegal = Paint(0).apply {
        color = girdColorIllegal
        style = Paint.Style.FILL
    }

    private val paintPlayerColor1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = playerColor1
        style = Paint.Style.FILL
    }

    private val paintPlayerColor2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = playerColor2
        style = Paint.Style.FILL
    }


    private val paintPlayerOutlineColor1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = playerOutlineColor1
        style = Paint.Style.FILL
    }

    private val paintPlayerOutlineColor2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = playerOutlineColor2
        style = Paint.Style.FILL
    }
    //</editor-fold>

    private var viewMeasureType: Int = 0
    private var viewWidth: Int = 0
    private var singleCellSize: Float = 0F
    private var playerRadius: Float = 0F
    private var playerRadiusOutline: Float = 0F
    private var playerRadiusIcon: Float = 0F
    private var userInteractionEnabled = true

    var allowFirstPlayerMove = false
    var allowSecondPlayerMove = true

    var moveAttemptListener: MoveAttemptListener? = null

    var riseAnimationDuration = 500L
    var artificialAnimationDuration = 100L
    var returnAnimationDuration = 500L
    var playerScaleMoving: Float = 1.35F

    /**
     * In relation to the cell size
     */
    var playerSize: Float = 0.65F
        set(value) {
            field = value
            invalidate()
        }

    /**
     * In relation to player size
     */
    var playerOutlineSize: Float = 1.175F
        set(value) {
            field = value
            invalidate()
        }

    /**
     * In relation to the player size
     */
    var playerIconSize: Float = 0.75F
        set(value) {
            field = value
            invalidate()
        }

    var gridData = Grid(8, 3)
        set(value) {
            field = value

            updateDimensions()
            invalidate()
        }

    private var userInteracting = false
    private var playerScaleCurrent = 1F
    private var movingEntry: GridEntry? = null
    private var moveOffsetX = 0F
    private var moveOffsetY = 0F
    private var moveX = 0F
    private var moveY = 0F
    private var returnAnimatorSet: AnimatorSet? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CheckersGridView,
            0, 0
        ).apply {
            try {
                gridColorMoveAllowed =
                    getColor(
                        R.styleable.CheckersGridView_grid_color_move_allowed,
                        context.getColor(R.color.game_color_grid_move_allowed)
                    )
                gridColorMoveAllowedHint =
                    getColor(
                        R.styleable.CheckersGridView_grid_color_move_allowed_hint,
                        context.getColor(R.color.game_color_grid_move_allowed_hint)
                    )
                gridColorCaptureAllowedHint =
                    getColor(
                        R.styleable.CheckersGridView_grid_color_capture_allowed_hint,
                        context.getColor(R.color.game_color_grid_capture_allowed_hint)
                    )
                gridColorMoveForbidden =
                    getColor(
                        R.styleable.CheckersGridView_grid_color_move_forbidden,
                        context.getColor(R.color.game_color_grid_move_forbidden)
                    )
                gridColorMoveSource =
                    getColor(
                        R.styleable.CheckersGridView_grid_color_move_source,
                        context.getColor(R.color.game_color_grid_move_source)
                    )
                gridColorLegal =
                    getColor(
                        R.styleable.CheckersGridView_grid_color_legal,
                        context.getColor(R.color.game_color_grid_default_legal)
                    )
                girdColorIllegal =
                    getColor(
                        R.styleable.CheckersGridView_grid_color_illegal,
                        context.getColor(R.color.game_color_grid_default_illegal)
                    )
                playerColor1 = getColor(
                    R.styleable.CheckersGridView_player_color1,
                    context.getColor(R.color.game_color_player1)
                )
                playerColor2 = getColor(
                    R.styleable.CheckersGridView_player_color2,
                    context.getColor(R.color.game_color_player2)
                )
                playerOutlineColor1 =
                    getColor(
                        R.styleable.CheckersGridView_player_outline_color1,
                        context.getColor(R.color.game_color_player_outline1)
                    )
                playerOutlineColor2 =
                    getColor(
                        R.styleable.CheckersGridView_player_outline_color1,
                        context.getColor(R.color.game_color_player_outline2)
                    )
                viewMeasureType =
                    getInteger(R.styleable.CheckersGridView_view_size, 0)
            } finally {
                recycle()
            }
        }
    }

    private fun drawGridEntry(
        canvas: Canvas, entry: GridEntry,
        paint: Paint = if (entry.legal()) paintGridColorLegal else paintGridColorIllegal
    ) {
        val left = entry.x * singleCellSize
        val top = entry.y * singleCellSize

        canvas.drawRect(
            left,
            top,
            left + singleCellSize,
            top + singleCellSize,
            paint
        )
    }

    private fun drawPlayer(
        canvas: Canvas,
        entry: GridEntry,
        cx: Float,
        cy: Float,
        scale: Float = 1F
    ) {
        canvas.apply {
            when (entry.player) {
                PlayerNum.FIRST -> {
                    drawCircle(cx, cy, playerRadiusOutline * scale, paintPlayerOutlineColor1)
                    drawCircle(cx, cy, playerRadius * scale, paintPlayerColor1)
                }
                PlayerNum.SECOND -> {
                    drawCircle(cx, cy, playerRadiusOutline * scale, paintPlayerOutlineColor2)
                    drawCircle(cx, cy, playerRadius * scale, paintPlayerColor2)
                }
                PlayerNum.NOPLAYER, null -> {
                    // Do not draw
                }
            }

            if (entry.pieceType == PieceType.KING) {
                val d = resources.getDrawable(R.drawable.ic_king, null)
                when (entry.player) {
                    PlayerNum.NOPLAYER, null -> return
                    PlayerNum.FIRST -> d.setTint(playerOutlineColor1)
                    PlayerNum.SECOND -> d.setTint(playerOutlineColor2)
                }

                d.setBounds(
                    (cx - playerRadiusIcon * scale).roundToInt(),
                    (cy - playerRadiusIcon * scale).roundToInt(),
                    (cx + playerRadiusIcon * scale).roundToInt(),
                    (cy + playerRadiusIcon * scale).roundToInt()
                )

                d.draw(canvas)
            }
        }
    }

    fun attemptMove(srcEntry: GridEntry, dstEntry: GridEntry): Boolean {
        if (!gridData.moveAllowed(srcEntry, dstEntry)) {
            return false
        }

        val srcX = ((srcEntry.x) + 0.5F) * singleCellSize
        val srcY = ((srcEntry.y) + 0.5F) * singleCellSize

        val dstX = ((dstEntry.x) + 0.5F) * singleCellSize
        val dstY = ((dstEntry.y) + 0.5F) * singleCellSize

        moveX = srcX
        moveY = srcY

        movingEntry = srcEntry

        AnimatorSet().apply {
            playSequentially(
                ValueAnimator.ofFloat(1F, playerScaleMoving).apply {
                    addUpdateListener {
                        playerScaleCurrent = it.animatedValue as Float
                        invalidate()
                    }

                    duration = artificialAnimationDuration
                },
                AnimatorSet().apply {
                    playTogether(ValueAnimator.ofFloat(srcX, dstX).apply {
                        addUpdateListener {
                            moveX = it.animatedValue as Float
                            invalidate()
                        }
                    }, ValueAnimator.ofFloat(srcY, dstY).apply {
                        addUpdateListener {
                            moveY = it.animatedValue as Float
                            invalidate()
                        }
                    })

                    duration = artificialAnimationDuration
                },
                ValueAnimator.ofFloat(playerScaleMoving, 1F).apply {
                    addUpdateListener {
                        playerScaleCurrent = it.animatedValue as Float
                        invalidate()
                    }

                    duration = artificialAnimationDuration
                })

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    userInteractionEnabled = false

                    moveAttemptListener?.onForcedMoveStart(gridData, srcEntry, dstEntry)
                }

                override fun onAnimationEnd(animation: Animator) {
                    userInteractionEnabled = true
                    movingEntry = null
                    moveY = 0F
                    moveX = 0F

                    moveAttemptListener?.onForcedMoveEnd(gridData, srcEntry, dstEntry)

                    invalidate()
                }
            })

            duration = artificialAnimationDuration
            start()
        }

        return true
    }

    private fun playerMoveAllowed(player: PlayerNum): Boolean {
        return when (player) {
            PlayerNum.NOPLAYER -> false
            PlayerNum.FIRST -> allowFirstPlayerMove
            PlayerNum.SECOND -> allowSecondPlayerMove
        }
    }

    private fun updateDimensions() {
        singleCellSize = viewWidth.toFloat() / gridData.size
        playerRadius = singleCellSize * playerSize * 0.5F
        playerRadiusOutline = singleCellSize * playerSize * playerOutlineSize * 0.5F
        playerRadiusIcon = singleCellSize * playerSize * playerIconSize * 0.5F
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            gridData.forEach {
                drawGridEntry(this, it)

                if (it != movingEntry) {
                    val cx = (it.x + 0.5F) * singleCellSize
                    val cy = (it.y + 0.5F) * singleCellSize
                    drawPlayer(this, it, cx, cy)
                }
            }

            movingEntry?.let { srcEntry ->
                val x = (moveX / singleCellSize).toInt()
                val y = (moveY / singleCellSize).toInt()
                val dstEntry = gridData.getEntryByCoords(x, y)

                if (userInteracting) {
                    val allowedEntries = gridData.getAllowedMoves(srcEntry, false)
                    allowedEntries?.forEach {
                        drawGridEntry(this, it, paintGridColorMoveAllowedHint)
                    }

                    val allowedCaptures = gridData.getAllowedCaptures(srcEntry, false)
                    allowedCaptures?.forEach {
                        drawGridEntry(
                            this,
                            it.locationAfterCapture,
                            paintGridColorCaptureAllowedHint
                        )
                    }

                    if (dstEntry != movingEntry) {
                        drawGridEntry(
                            this, dstEntry,
                            if (gridData.moveAllowed(srcEntry, dstEntry) ||
                                gridData.captureAllowed(srcEntry, dstEntry)
                            ) paintGridColorMoveAllowed else paintGridColorMoveForbidden
                        )
                    }

                    drawGridEntry(this, srcEntry, paintGridColorMoveSource)

                    val cx = (dstEntry.x + 0.5F) * singleCellSize
                    val cy = (dstEntry.y + 0.5F) * singleCellSize
                    if (dstEntry != srcEntry) {
                        drawPlayer(this, dstEntry, cx, cy)
                    }
                }

                drawPlayer(this, srcEntry, moveX, moveY, playerScaleCurrent)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)

        if (!userInteractionEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                userInteracting = true
                returnAnimatorSet?.let {
                    it.cancel()
                    returnAnimatorSet = null
                }

                val x = (event.x / singleCellSize).toInt()
                val y = (event.y / singleCellSize).toInt()

                val entry = gridData.getEntryByCoords(x, y)
                if (!playerMoveAllowed(entry.player)) {
                    return true
                }

                moveAttemptListener?.onUserMoveStart(gridData, entry)

                val cx = (entry.x + 0.5F) * singleCellSize
                val cy = (entry.y + 0.5F) * singleCellSize

                moveOffsetX = event.x - cx
                moveOffsetY = event.y - cy
                moveX = cx
                moveY = cy

                if (movingEntry == null) {
                    ValueAnimator.ofFloat(1F, playerScaleMoving).apply {
                        addUpdateListener {
                            playerScaleCurrent = it.animatedValue as Float
                            invalidate()
                        }

                        duration = riseAnimationDuration
                        start()
                    }
                }

                movingEntry = entry

                return true
            }
            MotionEvent.ACTION_MOVE -> {
                userInteracting = true
                moveX = max(min(event.x - moveOffsetX, viewWidth.toFloat() - 1), 0F)
                moveY = max(min(event.y - moveOffsetY, viewWidth.toFloat() - 1), 0F)

                invalidate()

                return true
            }
            MotionEvent.ACTION_UP -> {
                val x = (moveX / singleCellSize).toInt()
                val y = (moveY / singleCellSize).toInt()
                val dstEntry = gridData.getEntryByCoords(x, y)

                moveOffsetX = 0F
                moveOffsetY = 0F

                userInteracting = false
                val srcEntry = movingEntry ?: return false
                userInteractionEnabled = false

                movingEntry?.let { gridEntry ->
                    val entry =
                        if (gridData.moveAllowed(gridEntry, dstEntry) ||
                            gridData.captureAllowed(gridEntry, dstEntry)
                        ) dstEntry else gridEntry
                    val dstX = (entry.x + 0.5F) * singleCellSize
                    val dstY = (entry.y + 0.5F) * singleCellSize

                    val srcX = moveX
                    val srcY = moveY

                    returnAnimatorSet = AnimatorSet().apply {
                        playTogether(
                            ValueAnimator.ofFloat(playerScaleMoving, 1F).apply {
                                addUpdateListener {
                                    playerScaleCurrent = it.animatedValue as Float
                                    invalidate()
                                }
                            },
                            ValueAnimator.ofFloat(srcX, dstX).apply {
                                addUpdateListener {
                                    moveX = it.animatedValue as Float
                                }
                            },
                            ValueAnimator.ofFloat(srcY, dstY).apply {
                                addUpdateListener {
                                    moveY = it.animatedValue as Float
                                }
                            })

                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                userInteractionEnabled = true
                                movingEntry = null
                                moveY = 0F
                                moveX = 0F
                                returnAnimatorSet = null

                                moveAttemptListener?.onUserMoveEnd(gridData, srcEntry, entry)
                                invalidate()
                            }
                        })

                        duration = returnAnimationDuration
                        start()
                    }
                }

                return true
            }
        }

        return false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w == 0 || h == 0) {
            return
        }

        viewWidth = w
        updateDimensions()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = when (viewMeasureType) {
            0 -> {
                if (width > height) height else width
            }
            1 -> width
            2 -> height
            else -> throw RuntimeException("Invalid view_size attribute")
        }
        setMeasuredDimension(size, size)
    }
}

interface MoveAttemptListener {
    fun onUserMoveStart(grid: Grid, srcEntry: GridEntry)
    fun onUserMoveEnd(grid: Grid, srcEntry: GridEntry, dstEntry: GridEntry)

    fun onForcedMoveStart(grid: Grid, srcEntry: GridEntry, dstEntry: GridEntry)
    fun onForcedMoveEnd(grid: Grid, srcEntry: GridEntry, dstEntry: GridEntry)
}
