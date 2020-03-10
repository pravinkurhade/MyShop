package com.bsktech.myshop.utils

object AppUtils {
    fun toCamalCase(name: String?): CharSequence? {
        val words = name!!.split(" ").toMutableList()
        var output = ""
        for (word in words) {
            output += word.capitalize() + " "
        }
        output = output.trim()
        return output
    }

}