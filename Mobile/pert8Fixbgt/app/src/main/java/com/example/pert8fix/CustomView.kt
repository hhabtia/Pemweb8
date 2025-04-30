package com.example.pert8fix

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import java.sql.SQLException

class CustomView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    companion object {
        private const val MAX_POST_LENGTH = 500
    }

    // Drawing properties
    private val radius = 140f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 50f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val normalTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 40f
        textAlign = Paint.Align.LEFT
    }
    private val bioTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val linkTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#87CEFA")
        textSize = 40f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val secondTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 30f
        textAlign = Paint.Align.LEFT
    }
    private val thirdTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 35f
        textAlign = Paint.Align.LEFT
    }

    // Drawables
    private lateinit var vectorDrawable: Drawable
    private lateinit var vectorDrawable2: Drawable
    private lateinit var vectorDrawable3: Drawable
    private lateinit var vectorDrawable4: Drawable
    private lateinit var vectorDrawable5: Drawable
    private lateinit var vectorDrawable6: Drawable
    private lateinit var vectorDrawablePlus: Drawable
    private lateinit var vectorProfil: Drawable

    // UI bounds
    private val buttonRect = RectF()
    private var followingBounds = Rect()
    private var jmlFollowingBounds = Rect()
    private val backButtonBounds = RectF()
    private val moreButtonBounds = RectF()
    private val fabButtonBounds = RectF()

    // Tab properties
    private val tabTitles = listOf("Posts", "Replies", "Highlights", "Articles", "Media")
    private val tabRects = mutableListOf<RectF>()
    private var selectedTabIndex = 0

    // Data
    private val postinganList = mutableListOf<String>()
    private var showUnfollowPopup = false
    private var unfollowConfirmRects: List<RectF> = emptyList()
    private var name: String = ""
    private var username: String = ""
    private var bio: String = ""
    private val idPengguna = 1

    init {
        initializeDrawables()
        ambilDataDariDatabase()
        ambilPostinganDariDatabase()
        isClickable = true
    }

    private fun initializeDrawables() {
        vectorDrawable = ContextCompat.getDrawable(context, R.drawable.baseline_badge_24)!!
        vectorDrawable2 = ContextCompat.getDrawable(context, R.drawable.baseline_add_location_24)!!
        vectorDrawable3 = ContextCompat.getDrawable(context, R.drawable.baseline_calendar_month_24)!!
        vectorDrawable4 = ContextCompat.getDrawable(context, R.drawable.baseline_link_24)!!
        vectorDrawable5 = ContextCompat.getDrawable(context, R.drawable.baseline_arrow_back_241)!!
        vectorDrawable6 = ContextCompat.getDrawable(context, R.drawable.baseline_more_vert_24)!!
        vectorDrawablePlus = ContextCompat.getDrawable(context, R.drawable.baseline_add_24)!!
        vectorProfil = ContextCompat.getDrawable(context, R.drawable.default_profile_icon)!!
    }

    fun insertPostingan(idPengguna: Int, content: String) {
        if (content.length > MAX_POST_LENGTH) {
            Toast.makeText(context, "Maksimal $MAX_POST_LENGTH karakter", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            val conn = koneksi.connection()
            if (conn == null) {
                showToastOnUiThread("Gagal koneksi ke database")
                return@Thread
            }

            try {
                val query = "INSERT INTO post (id_pengguna, content) VALUES (?, ?)"
                val statement = conn.prepareStatement(query)
                statement.setInt(1, idPengguna)
                statement.setString(2, content)

                val rowsAffected = statement.executeUpdate()

                if (rowsAffected > 0) {
                    showToastOnUiThread("Postingan berhasil dikirim")
                    ambilPostinganDariDatabase() // Refresh data setelah insert
                } else {
                    showToastOnUiThread("Gagal mengirim postingan")
                }
            } catch (e: SQLException) {
                logAndShowError("Error SQL saat menyisipkan postingan", e)
            } catch (e: Exception) {
                logAndShowError("Error saat menyisipkan postingan", e)
            } finally {
                closeConnection(conn)
            }
        }.start()
    }

    private fun ambilPostinganDariDatabase() {
        Thread {
            val conn = koneksi.connection()
            if (conn == null) {
                Log.e("DB_ERROR", "Koneksi gagal")
                return@Thread
            }

            try {
                val query = "SELECT content FROM post WHERE id_pengguna = ? ORDER BY id DESC"
                val statement = conn.prepareStatement(query)
                statement.setInt(1, idPengguna)

                val result = statement.executeQuery()
                val tempList = mutableListOf<String>()

                while (result.next()) {
                    result.getString("content")?.let { content ->
                        tempList.add(content)
                    }
                }

                updatePostinganList(tempList)
            } catch (e: SQLException) {
                logAndShowError("Error SQL saat mengambil postingan", e)
            } catch (e: Exception) {
                logAndShowError("Error saat mengambil postingan", e)
            } finally {
                closeConnection(conn)
            }
        }.start()
    }

    private fun ambilDataDariDatabase() {
        Thread {
            val conn = koneksi.connection()
            if (conn == null) {
                Log.e("DB_ERROR", "Koneksi gagal")
                return@Thread
            }

            try {
                val query = "SELECT name, username, bio FROM pengguna WHERE id = 1"
                val statement = conn.createStatement()
                val result = statement.executeQuery(query)

                if (result.next()) {
                    name = result.getString("name") ?: ""
                    username = result.getString("username") ?: ""
                    bio = result.getString("bio") ?: ""

                    Log.d("DB_RESULT", "nama: $name, username: $username, bio: $bio")
                    refreshUi()
                }
            } catch (e: SQLException) {
                logError("Error SQL saat mengambil data pengguna", e)
            } catch (e: Exception) {
                logError("Error saat mengambil data pengguna", e)
            } finally {
                closeConnection(conn)
            }
        }.start()
    }

    private fun showToastOnUiThread(message: String) {
        (context as Activity).runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun logAndShowError(message: String, e: Exception) {
        Log.e("DB_ERROR", "$message: ${e.message}")
        showToastOnUiThread("Error: ${e.message ?: "Terjadi kesalahan"}")
    }

    private fun logError(message: String, e: Exception) {
        Log.e("DB_ERROR", "$message: ${e.message}")
    }

    private fun closeConnection(conn: java.sql.Connection?) {
        try {
            conn?.close()
        } catch (e: SQLException) {
            Log.e("DB_ERROR", "Gagal menutup koneksi: ${e.message}")
        }
    }

    private fun updatePostinganList(newList: List<String>) {
        (context as Activity).runOnUiThread {
            postinganList.clear()
            postinganList.addAll(newList)
            invalidate()
        }
    }

    private fun refreshUi() {
        (context as Activity).runOnUiThread {
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawHeader(canvas)
        drawProfileSection(canvas)
        drawStatsSection(canvas)
        drawFollowingButton(canvas)
        drawFAB(canvas)
        drawTabMenu(canvas)
        drawPostingan(canvas)

        if (showUnfollowPopup) {
            drawUnfollowPopup(canvas)
        }
    }

    private fun drawHeader(canvas: Canvas) {
        paint.color = Color.BLUE
        canvas.drawRect(0f, 0f, width - 0f, height - 1800f, paint)
        drawVectorDrawable(canvas, vectorDrawable5, 45f, 40f, 70, 70, backButtonBounds)
        drawVectorDrawable(canvas, vectorDrawable6, width - 85f, 40f, 70, 70, moreButtonBounds)
    }

    private fun drawProfileSection(canvas: Canvas) {
        val circleX = radius + 30f
        val circleY = height - radius - 1700f

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ppbaru)
        val profileRect = Rect(
            (circleX - radius).toInt(),
            (circleY - radius).toInt(),
            (circleX + radius).toInt(),
            (circleY + radius).toInt()
        )
        canvas.drawBitmap(bitmap, null, profileRect, null)

        val textY = circleY + radius + 60f
        val textX = circleX - radius
        canvas.drawText(name.ifEmpty { "Karina" }, textX, textY, textPaint)
        canvas.drawText("@${username.ifEmpty { "sickandsuffer" }}", textX, textY + 55f, normalTextPaint)
        canvas.drawText(bio.ifEmpty { "pusing guys" }, textX, textY + 125f, bioTextPaint)

        drawVectorDrawable(canvas, vectorDrawable, textX, textY + 165f, 50, 50)
        canvas.drawText("Advertising & Marketing Agency", textX + 70f, textY + 205f, normalTextPaint)

        drawVectorDrawable(canvas, vectorDrawable2, textX, textY + 235f, 50, 50)
        canvas.drawText("Gedangan", textX + 70f, textY + 275f, normalTextPaint)

        drawVectorDrawable(canvas, vectorDrawable3, textX, textY + 305f, 50, 50)
        canvas.drawText("Joined August, 2005", textX + 70f, textY + 345f, normalTextPaint)
    }

    private fun drawStatsSection(canvas: Canvas) {
        val textY = height - radius - 1700f + radius + 60f + 415f
        val textX = radius + 30f - radius

        canvas.drawText("49", textX, textY, bioTextPaint)
        bioTextPaint.getTextBounds("49", 0, "49".length, jmlFollowingBounds)
        jmlFollowingBounds.offset(textX.toInt(), textY.toInt())

        val followingX = textX + 115f
        canvas.drawText("Following", followingX, textY, normalTextPaint)
        normalTextPaint.getTextBounds("Following", 0, "Following".length, followingBounds)
        followingBounds.offset(followingX.toInt(), textY.toInt())

        val jmlFollowerX = followingX + 250f
        canvas.drawText("9702", jmlFollowerX, textY, bioTextPaint)
        canvas.drawText("Followers", jmlFollowerX + 115f, textY, normalTextPaint)
    }

    private fun drawFollowingButton(canvas: Canvas) {
        val btnEditX = width / 4f + 400f
        val btnEditY = height - radius - 1620f
        val btnEditWidth = 350f
        val btnEditHeight = 100f

        buttonRect.set(btnEditX, btnEditY, btnEditX + btnEditWidth, btnEditY + btnEditHeight)
        paint.color = Color.LTGRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRoundRect(buttonRect, 50f, 50f, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textSize = 45f
        val buttonText = "Following"
        val textWidth = paint.measureText(buttonText)
        canvas.drawText(buttonText, buttonRect.centerX() - textWidth / 2, buttonRect.centerY() + 15f, paint)
    }

    private fun drawFAB(canvas: Canvas) {
        val fabSize = 150f
        val fabMargin = 50f
        val fabX = width - fabSize - fabMargin
        val fabY = height - fabSize - fabMargin

        paint.color = Color.parseColor("#1DA1F2")
        paint.style = Paint.Style.FILL
        canvas.drawCircle(fabX + fabSize / 2, fabY + fabSize / 2, fabSize / 2, paint)

        val plusSize = 70
        vectorDrawablePlus.setBounds(
            (fabX + fabSize / 2 - plusSize / 2).toInt(),
            (fabY + fabSize / 2 - plusSize / 2).toInt(),
            (fabX + fabSize / 2 + plusSize / 2).toInt(),
            (fabY + fabSize / 2 + plusSize / 2).toInt()
        )
        vectorDrawablePlus.draw(canvas)
        fabButtonBounds.set(fabX, fabY, fabX + fabSize, fabY + fabSize)
    }

    private fun drawVectorDrawable(canvas: Canvas, drawable: Drawable, x: Float, y: Float, width: Int, height: Int, bounds: RectF? = null) {
        drawable.setBounds(
            x.toInt(),
            y.toInt(),
            x.toInt() + width,
            y.toInt() + height
        )
        drawable.draw(canvas)
        bounds?.set(x, y, x + width, y + height)
    }

    private fun drawTabMenu(canvas: Canvas) {
        val tabHeight = 40f
        val tabStartY = height - 1800f + paint.measureText("Karina") + 490f

        paint.textSize = 40f
        val totalWidth = width.toFloat()
        val totalTabWidth = paint.measureText(tabTitles.joinToString("")) + (tabTitles.size * 80f)
        val startX = (totalWidth - totalTabWidth) / 2

        tabRects.clear()

        var currentX = startX
        tabTitles.forEachIndexed { index, title ->
            val textWidth = paint.measureText(title)
            val tabWidth = textWidth + 80f

            paint.typeface = if (index == selectedTabIndex) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            paint.color = Color.BLACK

            canvas.drawText(title, currentX + 40f, tabStartY + tabHeight / 2 + 15f, paint)

            if (index == selectedTabIndex) {
                paint.color = Color.parseColor("#1DA1F2")
                paint.strokeWidth = 3f
                canvas.drawLine(
                    currentX + 20f,
                    tabStartY + tabHeight - 0f,
                    currentX + tabWidth - 20f,
                    tabStartY + tabHeight - 0f,
                    paint
                )
                paint.strokeWidth = 1f
            }

            tabRects.add(RectF(currentX, tabStartY, currentX + tabWidth, tabStartY + tabHeight))
            currentX += tabWidth
        }
    }

    private fun showPopupDialog() {
        val editText = EditText(context).apply {
            hint = "Tulis sesuatu di sini..."
            setPadding(20, 20, 20, 20)
            inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
            gravity = Gravity.TOP or Gravity.START
        }

        AlertDialog.Builder(context)
            .setTitle("Apa yang sedang terjadi?")
            .setView(editText)
            .setPositiveButton("Post") { dialog, _ ->
                val inputText = editText.text.toString().trim()
                when {
                    inputText.isEmpty() -> Toast.makeText(context, "Isi tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    inputText.length > MAX_POST_LENGTH -> Toast.makeText(context, "Maksimal $MAX_POST_LENGTH karakter", Toast.LENGTH_SHORT).show()
                    else -> insertPostingan(idPengguna, inputText)
                }
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .create()
            .apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                show()
            }
    }

    private fun drawPostingan(canvas: Canvas) {
        val startY = height - 1100f
        var currentY = startY

        val profileSize = (width * 0.1).toInt()
        val profileX = 70f

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ppbaru)

        for (post in postinganList) {
            val profileRect = Rect(
                profileX.toInt(),
                currentY.toInt(),
                (profileX + profileSize).toInt(),
                (currentY + profileSize).toInt()
            )
            canvas.drawBitmap(bitmap, null, profileRect, null)

            val nmaX = profileX + profileSize + 30f

            val nameWidth = textPaint.measureText(name)
            canvas.drawText(name, nmaX, currentY + 40f, textPaint)
            canvas.drawText("@$username", nmaX + nameWidth + 20f, currentY + 40f, textPaint)

            canvas.drawText(post, nmaX, currentY + 90f, normalTextPaint)

            currentY += 170f
        }
    }

    private fun drawUnfollowPopup(canvas: Canvas) {
        val popupWidth = width * 0.8f
        val popupHeight = 300f
        val popupX = (width - popupWidth) / 2
        val popupY = (height - popupHeight) / 2
        val rect = RectF(popupX, popupY, popupX + popupWidth, popupY + popupHeight)

        Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(20f, 0f, 4f, Color.parseColor("#80000000"))
        }.also { canvas.drawRoundRect(rect, 25f, 25f, it) }

        Paint().apply {
            color = Color.BLACK
            textSize = 40f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }.also { canvas.drawText("Apakah kamu ingin unfollow akun ini?", rect.centerX(), popupY + 100f, it) }

        val buttonPaint = Paint().apply {
            style = Paint.Style.FILL
        }
        val buttonTextPaint = Paint().apply {
            textSize = 36f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val cancelButtonRect = RectF(
            rect.left + 40f,
            rect.bottom - 100f,
            rect.left + popupWidth / 2 - 20f,
            rect.bottom - 40f
        )
        buttonPaint.color = Color.LTGRAY
        buttonTextPaint.color = Color.BLACK
        canvas.drawRoundRect(cancelButtonRect, 20f, 20f, buttonPaint)
        canvas.drawText("Batal", cancelButtonRect.centerX(), cancelButtonRect.centerY() + 12f, buttonTextPaint)


        val unfollowButtonRect = RectF(
            rect.centerX() + 20f,
            rect.bottom - 100f,
            rect.right - 40f,
            rect.bottom - 40f
        )
        buttonPaint.color = Color.RED
        buttonTextPaint.color = Color.WHITE
        canvas.drawRoundRect(unfollowButtonRect, 20f, 20f, buttonPaint)
        canvas.drawText("Unfollow", unfollowButtonRect.centerX(), unfollowButtonRect.centerY() + 12f, buttonTextPaint)

        unfollowConfirmRects = listOf(cancelButtonRect, unfollowButtonRect)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (followingBounds.contains(x.toInt(), y.toInt()) ||
                    jmlFollowingBounds.contains(x.toInt(), y.toInt())) {
                    context.startActivity(Intent(context, FollowingActivity::class.java))
                    return true
                }
                if (backButtonBounds.contains(x, y)) return true
                if (moreButtonBounds.contains(x, y)) return true
                if (fabButtonBounds.contains(x, y)) {
                    showPopupDialog()
                    return true
                }
                if (buttonRect.contains(x, y)) {
                    showUnfollowPopup = true
                    invalidate()
                    return true
                }
                if (showUnfollowPopup) {
                    handleUnfollowPopupTouch(x, y)
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (backButtonBounds.contains(x, y)) {
                    Toast.makeText(context, "Back button clicked", Toast.LENGTH_SHORT).show()
                    return true
                }
                if (moreButtonBounds.contains(x, y)) {
                    Toast.makeText(context, "More options clicked", Toast.LENGTH_SHORT).show()
                    return true
                }
                if (showUnfollowPopup) {
                    return handleUnfollowPopupTouch(x, y)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleUnfollowPopupTouch(x: Float, y: Float): Boolean {
        if (unfollowConfirmRects.size < 2) return false

        val cancelRect = unfollowConfirmRects[0]
        val unfollowRect = unfollowConfirmRects[1]

        return when {
            cancelRect.contains(x, y) -> {
                showUnfollowPopup = false
                invalidate()
                true
            }
            unfollowRect.contains(x, y) -> {
                showUnfollowPopup = false
                Toast.makeText(context, "Akun telah di-unfollow", Toast.LENGTH_SHORT).show()
                invalidate()
                true
            }
            else -> false
        }
    }
}