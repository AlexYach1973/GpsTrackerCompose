package com.alexyach.compose.gpstracker.data.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class ConverterBitmap {
    @TypeConverter
    fun fromBitmap(bitMap: Bitmap): ByteArray {

        // Уменьшить размер
       /* val bitMapShort = Bitmap.createScaledBitmap(
            bitMap,
            bitMap.width/10,
            bitMap.height/10,
            false
        )*/

        val outputStream = ByteArrayOutputStream()
//        bitMapShort.compress(Bitmap.CompressFormat.PNG,100, outputStream)
        bitMap.compress(Bitmap.CompressFormat.PNG,100, outputStream)

        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}
