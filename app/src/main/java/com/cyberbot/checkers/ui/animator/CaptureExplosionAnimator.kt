package com.cyberbot.checkers.ui.animator

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import com.cyberbot.checkers.game.GridEntry
import java.lang.RuntimeException

class CaptureExplosionAnimator(singleCellSize: Float) :
    PieceAnimator(singleCellSize, sequential = false) {

    var riseAnimationDuration = 1000L
    var fallAnimationDuration = 200L

    var targetEntries = ArrayList<GridEntry>()

    fun setDestroyerPiece(
        entry: GridEntry,
        srcX: Float,
        srcY: Float,
        srcScale: Float,
        dstEntry: GridEntry,
        dstScale: Float = 1F,
        topScale: Float = 3F,
        lowScale: Float = 0.8F
    ) {
        addPieceInternal(entry, srcX, srcY, srcScale)
        val values =
            animatedPieces[entry] ?: throw RuntimeException("Piece not added to animatedPieces set")

        val dstX = (dstEntry.x + 0.5F) * singleCellSize
        val dstY = (dstEntry.y + 0.5F) * singleCellSize

        val hitTarget = calculateTarget()

        val riseAnimator = AnimatorSet().apply {
            playTogether(
                ValueAnimator.ofFloat(srcScale, topScale).apply {
                    addUpdateListener {
                        values.scale = it.animatedValue as Float
                        onUpdate(entry, values)
                    }
                },
                ValueAnimator.ofFloat(srcX, hitTarget.first).apply {
                    addUpdateListener {
                        values.x = it.animatedValue as Float
                        onUpdate(entry, values)
                    }
                },
                ValueAnimator.ofFloat(srcY, hitTarget.second).apply {
                    addUpdateListener {
                        values.y = it.animatedValue as Float
                        onUpdate(entry, values)
                    }
                }
            )

            duration = riseAnimationDuration
        }

        val moveDstAnimator = AnimatorSet().apply {
            playTogether(
                ValueAnimator.ofFloat(hitTarget.first, dstX).apply {
                    addUpdateListener {
                        values.x = it.animatedValue as Float
                        onUpdate(entry, values)
                    }
                },
                ValueAnimator.ofFloat(hitTarget.second, dstY).apply {
                    addUpdateListener {
                        values.y = it.animatedValue as Float
                        onUpdate(entry, values)
                    }
                })
            duration = 500
        }

        animators.add(
            AnimatorSet().apply {
                playSequentially(
                    riseAnimator,
                    ValueAnimator.ofFloat(topScale, lowScale).apply {
                        addUpdateListener {
                            values.scale = it.animatedValue as Float
                            onUpdate(entry, values)
                        }

                        duration = fallAnimationDuration
                    },
                    ValueAnimator.ofFloat(lowScale, dstScale).apply {
                        addUpdateListener {
                            values.scale = it.animatedValue as Float
                            onUpdate(entry, values)
                        }

                        duration = 750
                    },
                    moveDstAnimator
                )
            }
        )
    }

    fun addTargetPiece(entry: GridEntry) {
        targetEntries.add(entry)
        addPieceInternal(entry)
    }

    private fun calculateTarget(): Pair<Float, Float> {
        val avgX = targetEntries.map { (it.x + 0.5F) * singleCellSize }.average()
        val avgY = targetEntries.map { (it.y + 0.5F) * singleCellSize }.average()

        return Pair(avgX.toFloat(), avgY.toFloat())
    }
}