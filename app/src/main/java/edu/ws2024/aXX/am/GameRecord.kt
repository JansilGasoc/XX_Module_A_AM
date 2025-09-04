package edu.ws2024.aXX.am

import android.annotation.SuppressLint
import android.os.Parcelable

import kotlinx.parcelize.Parcelize

@SuppressLint("ParcelCreator")


@Parcelize
data class GameRecord(
    val id: Int,
    val playerName: String,
    val coins: Int,
    val duration: Int
) :Parcelable