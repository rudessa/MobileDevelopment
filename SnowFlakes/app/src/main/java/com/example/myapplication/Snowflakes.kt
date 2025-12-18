package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.AsyncTask
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

data class Snowflake(var x: Float, var y: Float, val velocity: Float, val radius: Float, val color: Int)
lateinit var snow: Array<Snowflake>
val paint = Paint()
var h = 1000; var w = 1000

open class Snowflakes(ctx: Context) : View(ctx) {
    lateinit var moveTask: MoveTask
    var animationStarted = false
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.BLUE)

        for (s in snow) {
            paint.color = s.color
            canvas.drawCircle(s.x, s.y, s.radius, paint)
        }

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        h = bottom - top; w = right - left
        val r = Random(0)
        snow = Array(100) {
            val randomColor = Color.rgb(r.nextInt(200, 255), r.nextInt(200, 255), 255)
            Snowflake(x = r.nextFloat() * w, y =  r.nextFloat() * h,
            velocity = 15 + 10 * r.nextFloat(), radius = 30 + 20 * r.nextFloat(),
            color = randomColor)
        }
        Log.d("mytag", "snow: " + snow.contentToString())
    }

    fun moveSnowflakes() {
        for (s in snow) {
            s.y += s.velocity * (s.y / h)
            s.x += Math.sin((s.y / 100).toDouble()).toFloat() * 5
            if (s.y > h) {
                s.y -= h
            }
            if (s.x < 0) {
                s.x += w
            }
            if (s.x > w) {
                s.x -= w
            }
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!animationStarted) {
            moveTask = MoveTask(this)
            moveTask.execute(50)
            animationStarted = true
        }
        return super.onTouchEvent(event)

    }
    class MoveTask(val s: Snowflakes) : AsyncTask<Int,Int,Int>() {
        override fun doInBackground(vararg params: Int?): Int {
            val delay = params[0] ?: 200
            while (!isCancelled) {
                Thread.sleep(delay.toLong())
                s.moveSnowflakes()
            }
            return 0
        }
    }
}