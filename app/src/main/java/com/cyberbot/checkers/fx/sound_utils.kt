package com.cyberbot.checkers.fx

import androidx.annotation.RawRes
import com.cyberbot.checkers.R

@RawRes
fun getRandomMoveSoundRes() : Int{
    return intArrayOf(
        R.raw.player_move1,
        R.raw.player_move2
    ).random()
}