package com.friendspharma.app.core.theme

const val seventyTwo = 72
const val hundredNinetyTwo = 192
const val twoHundredTen = 210
const val threeHundred = 300
const val fourHundred = 400
const val twelveHundred = 1280

fun replaceSize(url: String, replaceWith: Int): String{
    return url.replace("<\$size\$>", replaceWith.toString())
}