package com.cyberbot.checkers.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyberbot.checkers.R
import com.cyberbot.checkers.fx.*
import com.cyberbot.checkers.game.Grid
import com.cyberbot.checkers.game.GridEntry
import com.cyberbot.checkers.game.PlayerNum
import com.cyberbot.checkers.preferences.Preferences
import com.cyberbot.checkers.ui.view.MoveAttemptListener
import kotlinx.android.synthetic.main.activity_game.*


class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        move_player2.text = getString(R.string.game_player_turn_info)

        val pref = Preferences.fromContext(this)
        val gridData = Grid.fromPreferences(pref)
        checkersGridView.gridData = gridData
        checkersGridView.playerTurn = PlayerNum.SECOND

        checkersGridView.moveAttemptListener = object : MoveAttemptListener {
            override fun onForcedMoveStart(grid: Grid, srcEntry: GridEntry, dstEntry: GridEntry) {
                move_player2.text = getString(R.string.game_player_move_in_progress)
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
                    val src: GridEntry = grid.getMovableEntries(PlayerNum.FIRST).keys.random()

                    val dst: GridEntry = grid.filter {
                        it != src && gridData.destinationAllowed(src, it)
                    }.random()

                    checkersGridView.playerTurn = PlayerNum.NOPLAYER
                    move_player2.text = getString(R.string.game_ai_thinking)
                    Thread {
                        Sound.playSound(this@GameActivity, SoundType.AI_THINK)
                        Thread.sleep(1000)
                        runOnUiThread {
                            checkersGridView.attemptMove(src, dst)
                        }
                        checkersGridView.playerTurn = PlayerNum.SECOND
                    }.start()
                }
            }
        }
    }
}