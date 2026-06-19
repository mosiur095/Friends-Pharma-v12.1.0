package com.friendspharma.app.features.domain.services

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.friendspharma.app.R
import com.friendspharma.app.core.util.NumberToWord
import com.friendspharma.app.features.data.remote.model.OrderDetailsDto
import com.friendspharma.app.features.data.remote.model.OrderDetailsDtoItem
import com.itextpdf.commons.utils.DateTimeUtil
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.math.RoundingMode

object GeneratePdf {

    // ── Merge helper: same PID_PRODUCT + same SALES_UNIT → single row ────────
    private fun mergeItems(items: List<OrderDetailsDtoItem>): List<OrderDetailsDtoItem> {
        val map = LinkedHashMap<String, OrderDetailsDtoItem>()
        for (item in items) {
            val key = "${item.PID_PRODUCT}_${item.SALES_UNIT}"
            val existing = map[key]
            if (existing == null) {
                map[key] = item
            } else {
                val mergedQty        = (existing.QUANTITY    ?: 0.0) + (item.QUANTITY    ?: 0.0)
                val mergedTotalPrice = (existing.TOTAL_PRICE ?: 0.0) + (item.TOTAL_PRICE ?: 0.0)
                map[key] = existing.copy(
                    QUANTITY    = mergedQty,
                    TOTAL_PRICE = mergedTotalPrice
                )
            }
        }
        return map.values.toList()
    }

    fun generatePdf(context: Context, orders: OrderDetailsDto, fileName: String) {

        val order       = orders.data?.get(0)
        // Use merged items so invoice rows match the order details screen
        val mergedItems = mergeItems(orders.data ?: emptyList())

        val file      = File(context.filesDir, fileName)
        val pdfWriter = PdfWriter(file.absolutePath)
        val pdfDoc    = PdfDocument(pdfWriter)
        val doc       = Document(pdfDoc, PageSize.A4)

        // ── Header ────────────────────────────────────────────────────────────
        val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1.4f, 1f)))
        headerTable.setWidth(UnitValue.createPercentValue(100f))

        val cell1 = Cell().add(
            Paragraph(
                """
        Date: ${order?.ORDER_DATE}
        Invoice No: ${order?.ORDER_NO}
        Username:
        Delivery Man:
        Phone No.: 
    """.trimIndent()
            ).setFontSize(10f)
        ).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.JUSTIFIED)

        val cell2 = Cell().apply {
            try {
                val drawable = ContextCompat.getDrawable(context, R.drawable.logo_small)
                val bitmap   = (drawable as BitmapDrawable).bitmap
                val stream   = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val imageData = ImageDataFactory.create(stream.toByteArray())
                val image     = Image(imageData)
                    .scaleToFit(50f, 50f).setHorizontalAlignment(HorizontalAlignment.CENTER)
                add(image)
            } catch (e: Exception) {
                println("Image not found or failed to load: ${e.message}")
            }
            add(
                Paragraph("Friends pharma & Departmental Store")
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(12f).setBold()
            )
            add(
                Paragraph("Address: 394/1/1, South paikpara, Mirpur, Dhaka-1216, Bangladesh\nPhone No: +8801826034230")
                    .setFontSize(8f).setTextAlignment(TextAlignment.CENTER)
            )
            setBorder(Border.NO_BORDER)
        }

        val cell3 = Cell().add(
            Paragraph(
                """
        Customer: ${order?.USER_NAME}
        Address: ${order?.DELIVERY_ADDRESS ?: ""}
        Phone No.: 
        Print Date: ${DateTimeUtil.getCurrentTimeDate()}
    """.trimIndent()
            ).setFontSize(10f)
        ).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.JUSTIFIED)

        headerTable.addCell(cell1)
        headerTable.addCell(cell2)
        headerTable.addCell(cell3)
        doc.add(headerTable)
        doc.add(Paragraph("\n"))

        // ── Product table ─────────────────────────────────────────────────────
        val table = Table(
            UnitValue.createPercentArray(floatArrayOf(1f, 3f, 1f, 1f, 1f, 1f, 1f, 1f, 1f))
        ).setWidth(UnitValue.createPercentValue(100f))

        listOf("SL#", "Product", "MRP", "Unit", "Unit QTY", "Total MRP",
            "Discount Amount", "Unit Price", "Total Price").forEach {
            table.addHeaderCell(
                Cell().add(Paragraph(it).setBold()).setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER)
            )
        }

        // Rows use mergedItems — no duplicates
        mergedItems.forEachIndexed { i, item ->
            val qty        = item.QUANTITY    ?: 0.0
            val mrp        = item.MRP_PRICE   ?: 0.0
            val salesPrice = item.SALES_PRICE ?: 0.0
            val totalMrp   = (qty * mrp).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
            val discount   = (qty * mrp - qty * salesPrice).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
            val totalPrice = (qty * salesPrice).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)

            listOf(
                (i + 1).toString(),
                item.PRODUCT ?: "",
                mrp.toString(),
                item.SALES_UNIT ?: "",
                qty.toString(),
                totalMrp.toString(),
                discount.toString(),
                salesPrice.toString(),
                totalPrice.toString()
            ).forEach { cell ->
                table.addCell(
                    Cell().add(Paragraph(cell)).setFontSize(9f)
                        .setTextAlignment(TextAlignment.CENTER)
                )
            }
        }

        doc.add(table)

        // ── Totals (from mergedItems) ─────────────────────────────────────────
        var totalMRP  = 0.0
        var totalSale = 0.0
        mergedItems.forEach {
            totalMRP  += (it.MRP_PRICE   ?: 0.0) * (it.QUANTITY ?: 0.0)
            totalSale += (it.SALES_PRICE ?: 0.0) * (it.QUANTITY ?: 0.0)
        }
        val discount = (totalMRP - totalSale).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)

        val tableTotal = Table(
            UnitValue.createPercentArray(floatArrayOf(7f, 1f, 1f, 1f, 1f))
        ).setWidth(UnitValue.createPercentValue(100f))

        listOf(
            "Total MRP/Discount",
            totalMRP.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString(),
            discount.toString(),
            "Subtotal",
            totalSale.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
        ).forEach {
            tableTotal.addHeaderCell(
                Cell().add(Paragraph(it).setBold()).setFontSize(9f)
                    .setTextAlignment(TextAlignment.RIGHT)
            )
        }
        doc.add(tableTotal)

        // ── Special Discount ──────────────────────────────────────────────────
        val discountTable = Table(UnitValue.createPercentArray(floatArrayOf(10f, 1f)))
            .setWidth(UnitValue.createPercentValue(100f))
        listOf("Special Discount", "").forEach {
            discountTable.addHeaderCell(
                Cell().add(Paragraph(it).setBold()).setFontSize(9f)
                    .setTextAlignment(TextAlignment.RIGHT)
            )
        }
        doc.add(discountTable)

        // ── Delivery Charge ───────────────────────────────────────────────────
        val deliveryChargeTable = Table(UnitValue.createPercentArray(floatArrayOf(10f, 1f)))
            .setWidth(UnitValue.createPercentValue(100f))
        listOf("Delivery Charge", "${order?.DELIVERY_CHARGE ?: 0.0}").forEach {
            deliveryChargeTable.addHeaderCell(
                Cell().add(Paragraph(it).setBold()).setFontSize(9f)
                    .setTextAlignment(TextAlignment.RIGHT)
            )
        }
        doc.add(deliveryChargeTable)

        // ── Total Bill ────────────────────────────────────────────────────────
        val grandTotal = (totalSale + (order?.DELIVERY_CHARGE ?: 0.0))
            .toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)

        val totalBillTable = Table(UnitValue.createPercentArray(floatArrayOf(10f, 1f)))
            .setWidth(UnitValue.createPercentValue(100f))
        listOf("Total Bill", grandTotal.toString()).forEach {
            totalBillTable.addHeaderCell(
                Cell().add(Paragraph(it).setBold()).setFontSize(9f)
                    .setTextAlignment(TextAlignment.RIGHT)
            )
        }
        doc.add(totalBillTable)

        // ── Payment ───────────────────────────────────────────────────────────
        val paymentTable = Table(UnitValue.createPercentArray(floatArrayOf(10f, 1f)))
            .setWidth(UnitValue.createPercentValue(100f))
        listOf("Payment()", "").forEach {
            paymentTable.addHeaderCell(
                Cell().add(Paragraph(it).setBold()).setFontSize(9f)
                    .setTextAlignment(TextAlignment.RIGHT)
            )
        }
        doc.add(paymentTable)

        // ── Balance Due ───────────────────────────────────────────────────────
        val balanceTable = Table(UnitValue.createPercentArray(floatArrayOf(10f, 1f)))
            .setWidth(UnitValue.createPercentValue(100f))
        listOf("Balance Due", grandTotal.toString()).forEach {
            balanceTable.addHeaderCell(
                Cell().add(Paragraph(it).setBold()).setFontSize(9f)
                    .setTextAlignment(TextAlignment.RIGHT)
            )
        }
        doc.add(balanceTable)

        // ── Amount in words ───────────────────────────────────────────────────
        doc.add(
            Paragraph(
                "Amount In Word: ${
                    NumberToWord.convert(
                        (totalSale + (order?.DELIVERY_CHARGE ?: 0.0)).toInt()
                    )
                }"
            )
        )

        doc.close()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportPdfToDownloads(context: Context, fileName: String) {
        val sourceFile = File(context.filesDir, fileName)
        if (!sourceFile.exists()) {
            println("Source file not found: ${sourceFile.absolutePath}")
            return
        }

        val resolver      = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val downloadsUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri          = resolver.insert(downloadsUri, contentValues)

        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    FileInputStream(sourceFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                openPdf(context, uri)
            } catch (e: Exception) {
                println("Error copying PDF: ${e.message}")
            }
        } else {
            println("Failed to create file in Downloads")
        }
    }

    fun openPdf(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No app found to open PDF", Toast.LENGTH_SHORT).show()
        }
    }
}