package io.github.benoitduffez.cupsprint.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import io.github.benoitduffez.cupsprint.R
import kotlinx.android.synthetic.main.activity_test.host
import org.cups4j.CupsClient
import org.cups4j.CupsPrinter
import org.cups4j.PrintJob
import java.io.File
import java.lang.Exception
import java.net.URL
import kotlin.concurrent.thread

class TestActivity : Activity() {
    private lateinit var context: Context

    private lateinit var connectButton: Button
    private lateinit var hostInput: EditText
    private lateinit var zplTextInput: EditText
    private lateinit var printerName: TextView
    private lateinit var sendButton: Button
    private val status: StringBuilder = StringBuilder()

    private var cupsClient: CupsClient? = null
    private var cupsPrinter: CupsPrinter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        setupUi()
        context = this
        connectButton.setOnClickListener {
            connect(host.text.trim().toString())
        }
        sendButton.setOnClickListener {
            sendZpl(zplTextInput.text.trim().toString())
        }
    }

    private fun sendZpl(zpl: String) {
        thread {
            try {
                val attributes: MutableMap<String, String> = HashMap()
                attributes["document-format"] = "application/vnd.cups-raw"
                val printJob: PrintJob = PrintJob.Builder(zpl.toByteArray())
                    .attributes(attributes)
                    .build()
                val printRequestResult = cupsPrinter?.print(printJob, context)
                status.appendln("JobId: ${printRequestResult?.jobId} , Is Successful: ${printRequestResult?.isSuccessfulResult}")
                statusUpdate()

            }catch (e:Exception){
                status.appendln(e.localizedMessage);
            }

        }
    }


    private fun connect(host: String) {
        thread {
            try {
                cupsClient = CupsClient(context, URL(host))
                val printers = cupsClient?.getPrinters("", 100)
                cupsPrinter = printers?.firstOrNull { it.isDefault }
                Log.i("TestActivity", "connect: ${printers?.map { it.name +" "+ it.isDefault  }}")
                if (cupsPrinter != null) {
                    status.appendln("Connected to :${cupsPrinter?.name}")
                }

            } catch (e: Exception) {
                status.appendln(e.localizedMessage)
            }
            statusUpdate()


        }

    }

    private fun setupUi() {
        connectButton = findViewById(R.id.connect)
        hostInput = findViewById(R.id.host)
        zplTextInput = findViewById(R.id.zpl_text)
        printerName = findViewById(R.id.printer_name)
        sendButton = findViewById(R.id.send)
        zplTextInput.setText(ZPL_TEXT_DEOM)
    }
    private fun statusUpdate(){
        runOnUiThread{
            printerName.text = status.toString()
        }
    }

    companion object {
        private val ZPL_TEXT_DEOM = "^XA\n" +
                "^FO50,60^A0,40^FDWorld's Best Griddle^FS\n" +
                "^FO60,120^BY3^BCN,60,,,,A^FD1234ABC^FS\n" +
                "^FO25,25^GB380,200,2^FS\n" +
                "^XZ"
    }


}