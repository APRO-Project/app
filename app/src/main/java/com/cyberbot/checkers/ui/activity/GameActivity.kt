package com.cyberbot.checkers.ui.activity

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyberbot.checkers.R
import com.cyberbot.checkers.fx.getRandomMoveSoundRes
import com.cyberbot.checkers.game.Grid
import com.cyberbot.checkers.game.GridEntry
import com.cyberbot.checkers.game.PlayerNum
import com.cyberbot.checkers.ui.view.MoveAttemptListener
import kotlinx.android.synthetic.main.activity_game.*


class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        move_player2.text = getString(R.string.game_player_turn_info)

        val gridData = checkersGridView.gridData

        checkersGridView.moveAttemptListener = object : MoveAttemptListener {
            override fun onForcedMoveStart(grid: Grid, srcEntry: GridEntry, dstEntry: GridEntry) {
                val mp = MediaPlayer.create(applicationContext, getRandomMoveSoundRes())
                mp.start()
                move_player2.text = "Busy"
            }

            override fun onForcedMoveEnd(grid: Grid, srcEntry: GridEntry, dstEntry: GridEntry) {
                grid.attemptMove(srcEntry, dstEntry)
                move_player2.text = getString(R.string.game_player_turn_info)
            }

            override fun onUserMoveStart(grid: Grid, srcEntry: GridEntry) {

            }

            override fun onUserMoveEnd(grid: Grid, srcEntry: GridEntry, dstEntry: GridEntry) {
                if (srcEntry == dstEntry) {
                    return
                }

                grid.attemptMove(srcEntry, dstEntry)
                if (dstEntry.player == PlayerNum.SECOND) {
                    val src: GridEntry = gridData.first {
                        it.player == PlayerNum.FIRST
                    }

                    val dst: GridEntry = gridData.first {
                        it != src && gridData.moveAllowed(src, it)
                    }

                    checkersGridView.attemptMove(src, dst)
                }
            }
        }
    }
}