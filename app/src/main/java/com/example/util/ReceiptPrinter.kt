package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.data.local.StoreSettings
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import java.io.FileOutputStream

object ReceiptPrinter {

    /**
     * Send receipt directly to Android PrintManager (Bluetooth, Wi-Fi, or Save to PDF)
     */
    fun printReceipt(
        context: Context,
        sale: Sale,
        items: List<SaleItem>,
        settings: StoreSettings,
        isThermal58mm: Boolean
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "SmartStore_Receipt_${sale.receiptNumber}"

        val adapter = object : PrintDocumentAdapter() {
            private var pdfDocument: PdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val pdi = PrintDocumentInfo.Builder("${jobName}.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()

                callback?.onLayoutFinished(pdi, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }

                val doc = if (isThermal58mm) {
                    generateThermal58mmPdf(sale, items, settings)
                } else {
                    generateA4Pdf(sale, items, settings)
                }
                pdfDocument = doc

                try {
                    val out = FileOutputStream(destination?.fileDescriptor)
                    doc.writeTo(out)
                    doc.close()
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    pdfDocument = null
                }
            }
        }

        printManager.print(jobName, adapter, PrintAttributes.Builder().build())
    }

    /**
     * Generates a 58mm thermal receipt (width ~200 points, calculated height)
     */
    fun generateThermal58mmPdf(
        sale: Sale,
        items: List<SaleItem>,
        settings: StoreSettings
    ): PdfDocument {
        val document = PdfDocument()
        val pageWidth = 200 // 58mm width in points (~72 DPI)
        val calculatedHeight = 280 + (items.size * 32)
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, calculatedHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        var y = 24f

        // Store Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        drawCenteredText(canvas, settings.storeName, pageWidth / 2f, y, paint)
        y += 14f

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 8f
        if (settings.storeAddress.isNotBlank()) {
            drawCenteredText(canvas, settings.storeAddress, pageWidth / 2f, y, paint)
            y += 10f
        }
        if (settings.storePhone.isNotBlank()) {
            drawCenteredText(canvas, "Tel: ${settings.storePhone}", pageWidth / 2f, y, paint)
            y += 10f
        }

        // Divider
        y += 4f
        drawDottedLine(canvas, 10f, y, pageWidth - 10f, paint)
        y += 12f

        // Receipt Info
        paint.textSize = 8f
        canvas.drawText("Receipt: ${sale.receiptNumber}", 10f, y, paint)
        y += 10f
        canvas.drawText("Date: ${DateUtils.formatReceiptDateTime(sale.saleDate)}", 10f, y, paint)
        y += 10f
        canvas.drawText("Mode: ${sale.paymentMethod}", 10f, y, paint)
        y += 12f

        // Table Header
        drawDottedLine(canvas, 10f, y, pageWidth - 10f, paint)
        y += 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Item", 10f, y, paint)
        canvas.drawText("Qty x Rate", 100f, y, paint)
        drawRightAlignedText(canvas, "Total", (pageWidth - 10).toFloat(), y, paint)
        y += 6f
        drawDottedLine(canvas, 10f, y, pageWidth - 10f, paint)
        y += 12f

        // Items
        paint.typeface = Typeface.DEFAULT
        for (item in items) {
            val itemName = if (item.productName.length > 22) item.productName.take(20) + ".." else item.productName
            canvas.drawText(itemName, 10f, y, paint)
            y += 9f

            val qtyRate = "${CurrencyUtils.formatQuantity(item.quantity, item.unit)} @ ${CurrencyUtils.formatPaise(item.unitPricePaise, false)}"
            canvas.drawText(qtyRate, 15f, y, paint)
            drawRightAlignedText(canvas, CurrencyUtils.formatPaise(item.totalPaise, false), (pageWidth - 10).toFloat(), y, paint)
            y += 12f
        }

        // Totals
        drawDottedLine(canvas, 10f, y, pageWidth - 10f, paint)
        y += 12f

        paint.textSize = 8.5f
        canvas.drawText("Subtotal:", 20f, y, paint)
        drawRightAlignedText(canvas, CurrencyUtils.formatPaise(sale.subtotalPaise), (pageWidth - 10).toFloat(), y, paint)
        y += 11f

        if (sale.itemDiscountPaise > 0 || sale.billDiscountPaise > 0) {
            val totalDiscount = sale.itemDiscountPaise + sale.billDiscountPaise
            canvas.drawText("Discount:", 20f, y, paint)
            drawRightAlignedText(canvas, "-${CurrencyUtils.formatPaise(totalDiscount)}", (pageWidth - 10).toFloat(), y, paint)
            y += 11f
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("FINAL TOTAL:", 10f, y, paint)
        drawRightAlignedText(canvas, CurrencyUtils.formatPaise(sale.finalTotalPaise), (pageWidth - 10).toFloat(), y, paint)
        y += 14f

        drawDottedLine(canvas, 10f, y, pageWidth - 10f, paint)
        y += 14f

        // Footer Thank you
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 7.5f
        drawCenteredText(canvas, settings.receiptFooterMessage, pageWidth / 2f, y, paint)

        document.finishPage(page)
        return document
    }

    /**
     * Generates a standard A4 invoice format (595 x 842 points)
     */
    fun generateA4Pdf(
        sale: Sale,
        items: List<SaleItem>,
        settings: StoreSettings
    ): PdfDocument {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        var y = 50f
        val margin = 40f
        val contentWidth = pageWidth - (margin * 2)

        // Store Brand & Invoice Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        canvas.drawText(settings.storeName, margin, y, paint)

        paint.textSize = 18f
        paint.color = Color.DKGRAY
        drawRightAlignedText(canvas, "RETAIL INVOICE", pageWidth - margin, y, paint)
        y += 20f

        paint.color = Color.BLACK
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        canvas.drawText(settings.storeAddress, margin, y, paint)
        y += 14f
        canvas.drawText("Contact: ${settings.storePhone}", margin, y, paint)
        y += 24f

        // Invoice Meta Box
        paint.color = Color.LTGRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(margin, y, pageWidth - margin, y + 40f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textSize = 10f
        canvas.drawText("Invoice No: ${sale.receiptNumber}", margin + 12f, y + 16f, paint)
        canvas.drawText("Payment Method: ${sale.paymentMethod}", margin + 12f, y + 30f, paint)

        drawRightAlignedText(canvas, "Date: ${DateUtils.formatReceiptDateTime(sale.saleDate)}", pageWidth - margin - 12f, y + 16f, paint)
        drawRightAlignedText(canvas, "Status: COMPLETED", pageWidth - margin - 12f, y + 30f, paint)
        y += 60f

        // Items Table Header
        paint.color = Color.rgb(240, 243, 246)
        canvas.drawRect(margin, y - 14f, pageWidth - margin, y + 8f, paint)

        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        canvas.drawText("#", margin + 8f, y, paint)
        canvas.drawText("Item Description", margin + 40f, y, paint)
        canvas.drawText("Quantity", margin + 260f, y, paint)
        drawRightAlignedText(canvas, "Unit Price", margin + 390f, y, paint)
        drawRightAlignedText(canvas, "Discount", margin + 460f, y, paint)
        drawRightAlignedText(canvas, "Amount", pageWidth - margin - 8f, y, paint)
        y += 18f

        // Table Rows
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f

        items.forEachIndexed { index, item ->
            canvas.drawText("${index + 1}", margin + 8f, y, paint)
            canvas.drawText(item.productName, margin + 40f, y, paint)
            canvas.drawText("${item.quantity} ${item.unit}", margin + 260f, y, paint)
            drawRightAlignedText(canvas, CurrencyUtils.formatPaise(item.unitPricePaise), margin + 390f, y, paint)
            drawRightAlignedText(canvas, CurrencyUtils.formatPaise(item.discountPaise), margin + 460f, y, paint)
            drawRightAlignedText(canvas, CurrencyUtils.formatPaise(item.totalPaise), pageWidth - margin - 8f, y, paint)
            y += 18f
        }

        // Summary Lines
        y += 10f
        paint.color = Color.LTGRAY
        canvas.drawLine(margin, y, pageWidth - margin, y, paint)
        y += 20f

        paint.color = Color.BLACK
        paint.textSize = 11f
        drawRightAlignedText(canvas, "Subtotal:  ${CurrencyUtils.formatPaise(sale.subtotalPaise)}", pageWidth - margin - 8f, y, paint)
        y += 16f

        val totalDiscount = sale.itemDiscountPaise + sale.billDiscountPaise
        if (totalDiscount > 0) {
            drawRightAlignedText(canvas, "Discount: -${CurrencyUtils.formatPaise(totalDiscount)}", pageWidth - margin - 8f, y, paint)
            y += 16f
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        drawRightAlignedText(canvas, "Total Payable:  ${CurrencyUtils.formatPaise(sale.finalTotalPaise)}", pageWidth - margin - 8f, y, paint)
        y += 40f

        // Footer
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        drawCenteredText(canvas, settings.receiptFooterMessage, pageWidth / 2f, y, paint)

        document.finishPage(page)
        return document
    }

    private fun drawCenteredText(canvas: Canvas, text: String, cx: Float, y: Float, paint: Paint) {
        val width = paint.measureText(text)
        canvas.drawText(text, cx - (width / 2f), y, paint)
    }

    private fun drawRightAlignedText(canvas: Canvas, text: String, rightX: Float, y: Float, paint: Paint) {
        val width = paint.measureText(text)
        canvas.drawText(text, rightX - width, y, paint)
    }

    private fun drawDottedLine(canvas: Canvas, startX: Float, y: Float, endX: Float, paint: Paint) {
        var curX = startX
        while (curX < endX) {
            canvas.drawPoint(curX, y, paint)
            curX += 4f
        }
    }
}
