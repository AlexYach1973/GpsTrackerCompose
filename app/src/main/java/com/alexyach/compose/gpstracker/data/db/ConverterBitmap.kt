package com.alexyach.compose.gpstracker.data.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class ConverterBitmap {
    @TypeConverter
    fun fromBitmap(bitMap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitMap.compress(Bitmap.CompressFormat.PNG,100, outputStream)

        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}
