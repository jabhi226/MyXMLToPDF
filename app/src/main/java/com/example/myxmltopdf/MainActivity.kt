package com.example.myxmltopdf

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import java.io.*


//val permissions = arrayOf(
//    Manifest.permission.READ_EXTERNAL_STORAGE,
//    Manifest.permission.WRITE_EXTERNAL_STORAGE
//)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermissions()
        setData()

        findViewById<TextView>(R.id.tv).setOnClickListener {
            layoutToImage(findViewById(R.id.main))
        }
    }

    private fun askPermissions() {
        val permissions = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        for (i in permissions) {
            if (ContextCompat.checkSelfPermission(this, i)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    101
                )
            }
        }
    }

    private val adapter = RVAdapter()
    private fun setData() {
        val rv = findViewById<RecyclerView>(R.id.recycler_view)
        rv.layoutManager = LinearLayoutManager(application)
        rv.adapter = adapter
        adapter.submitList(getList())
    }

    private fun getList(): ArrayList<String> {
        val list = ArrayList<String>()
        for (i in 0..100) {
            list.add(i.toString())
        }
        return list
    }

    var dirpath: String? = null
    private fun layoutToImage(view: View) {
        val relativeLayout = view as RelativeLayout
        relativeLayout.isDrawingCacheEnabled = true
        relativeLayout.buildDrawingCache(true)

        val bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom())
        view.draw(canvas)

        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/jpeg"
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        try {
            val f =
                File(
                    ContextWrapper(applicationContext).getDir(
                        "XML",
                        Context.MODE_PRIVATE
                    ).absolutePath + "image.jpg"
                )
            if (f.exists()) {
                f.delete()
                f.createNewFile()
            }
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
//            imageToPDF(view)
            doInBackground()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun doInBackground() {
        val document = PdfDocument()
        try {
            val file = File(
                ContextWrapper(applicationContext).getDir(
                    "XML",
                    Context.MODE_PRIVATE
                ).absolutePath + "image.jpg"
            )

            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val pageInfo = PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            paint.color = Color.parseColor("#ffffff")
            canvas.drawPaint(paint)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            document.finishPage(page)

            val pdfFile = File(
                ContextWrapper(applicationContext).getDir(
                    "XML",
                    Context.MODE_PRIVATE
                ).absolutePath + System.currentTimeMillis().toString() + ".pdf"
            )
            document.writeTo(FileOutputStream(pdfFile))
            Toast.makeText(this, "PDF Generated successfully!..", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } finally {
            document.close()
        }
    }

    @Throws(FileNotFoundException::class)
    fun imageToPDF(view: RelativeLayout) {
        try {
            val document = Document()
            dirpath =
                ContextWrapper(applicationContext).getDir("XML", Context.MODE_PRIVATE).absolutePath
            PdfWriter.getInstance(
                document,
                FileOutputStream("$dirpath/NewPDF.pdf")
            )
            document.open()
            document.addCreationDate()
            document.addAuthor("Abhi")
            document.pageSize = Rectangle(500F, 10000F)
            val img: Image =
                Image.getInstance(
                    ContextWrapper(applicationContext).getDir(
                        "XML",
                        Context.MODE_PRIVATE
                    ).absolutePath + "image.jpg"
                )
            val scaler: Float =
                (document.pageSize.width - document.leftMargin() - document.rightMargin() - 0) / img.width * 100
            img.scalePercent(scaler)
//            img.alignment = Image.ALIGN_CENTER or Image.ALIGN_TOP
            document.add(img)
            document.close()
            Toast.makeText(this, "PDF Generated successfully!..", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}