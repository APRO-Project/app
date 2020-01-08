package com.cyberbot.checkers.fx

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import com.cyberbot.checkers.R

@RawRes
fun getRandomMoveSoundRes(): Int {
    return intArrayOf(
        R.raw.player_move1,
        R.raw.player_move2
    ).random()
}

@RawRes
fun getRandomAiThinkSoundRes(): Int {
    return intArrayOf(
        R.raw.ai_think1,
        R.raw.ai_think2,
        R.raw.ai_think3
    ).random()
}

fun play(context: Context, @RawRes resId: Int) {
    val mp = MediaPlayer.create(context, resId)
    mp.start()
}