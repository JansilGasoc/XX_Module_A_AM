package edu.ws2024.aXX.am

import android.content.Context
import android.media.MediaPlayer

class AudioManager(context: Context) {
    private val bgmPlayer = MediaPlayer.create(context, R.raw.bgm).apply {
        isLooping = true
        setVolume(1f, 1f)
    }
    private val jumpPlayer = MediaPlayer.create(context, R.raw.jump)
    private val coinPlayer = MediaPlayer.create(context, R.raw.coin)
    private val gameOverPlayer = MediaPlayer.create(context, R.raw.game_over)

    fun startBgm() = bgmPlayer.start()
    fun pauseBgm() = bgmPlayer.pause()
    fun seekBgmToStart() = bgmPlayer.seekTo(0)
    fun setBgmVolume(left: Float, right: Float) = bgmPlayer.setVolume(left, right)
    fun playJump() = jumpPlayer.start()
    fun playCoin() = coinPlayer.start()
    fun playGameOver() = gameOverPlayer.start()

    fun release() {
        bgmPlayer.release()
        jumpPlayer.release()
        coinPlayer.release()
        gameOverPlayer.release()
    }
}