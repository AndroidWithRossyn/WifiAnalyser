package com.example.wifianalyser.utils.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun View.hide() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.inVisible() {
    this.visibility = View.INVISIBLE
}

//fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
//    val safeClickListener = SafeClickListener {
//        onSafeClick(it)
//    }
//    setOnClickListener(safeClickListener)
//}

// Get bitmap from a View
fun View.getBitmapFromView(): Bitmap? {
    val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val c = Canvas(bitmap)
    this.layout(this.left, this.top, this.right, this.bottom)
    this.draw(c)
//    val canvas = Canvas(bitmap)
//    draw(canvas)
    return bitmap
}

fun ConstraintLayout.getBitmapFromView(): Bitmap {
    val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    CoroutineScope(Dispatchers.IO).launch {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        var childBitmap : Bitmap? = null
        if(child.width > 0 && child.height > 0) {
            childBitmap = child.getBitmapFromView()
        }
        if (childBitmap != null) {
            canvas.drawBitmap(childBitmap, child.left.toFloat(), child.top.toFloat(), null)
        }
    }

    }
    return bitmap
}
