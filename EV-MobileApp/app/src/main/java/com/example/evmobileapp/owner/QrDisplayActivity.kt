package com.example.evmobileapp.owner

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.graphics.Bitmap

class QrDisplayActivity : AppCompatActivity() {

    private lateinit var qrImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_display)

        qrImageView = findViewById(R.id.qr_image_view)

        // Assuming you have a QR code data (e.g., reservation ID)
        val reservationId = intent.getStringExtra("RESERVATION_ID")

        if (reservationId != null) {
            generateQrCode(reservationId)
        }
    }

    private fun generateQrCode(data: String) {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) getColor(R.color.black) else getColor(R.color.white))
            }
        }

        qrImageView.setImageBitmap(bmp)
    }
}
