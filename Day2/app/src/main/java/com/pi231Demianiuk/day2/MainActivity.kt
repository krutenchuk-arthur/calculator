package com.pi231Demianiuk.day2

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private lateinit var advancedPanel: TableLayout

    private var firstOperand: Double = 0.0
    private var currentOperation: String = ""
    private var isOperationSet: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvDisplay = findViewById(R.id.tvDisplay)
        advancedPanel = findViewById(R.id.advancedPanel)

        val btnMode: ImageButton = findViewById(R.id.btnMode)
        btnMode.setImageResource(R.drawable.ic_function)

        btnMode.setOnClickListener {
            if (advancedPanel.visibility == View.GONE) {
                advancedPanel.visibility = View.VISIBLE
                btnMode.setImageResource(R.drawable.ic_close)
            } else {
                advancedPanel.visibility = View.GONE
                btnMode.setImageResource(R.drawable.ic_function)
            }
        }

        val listenerForDigits = View.OnClickListener { v ->
            val button = v as Button
            val textBtn = button.text.toString()
            val currentText = tvDisplay.text.toString()

            if (!isOperationSet && currentOperation == "") {

            }

            if (currentText == "0" && textBtn != ".") {
                tvDisplay.text = textBtn
            } else {
                if (textBtn == ".") {
                    val lastNumber = getLastNumber(currentText)
                    if (lastNumber.contains(".")) return@OnClickListener
                }
                tvDisplay.append(textBtn)
            }
        }

        listOf(R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot)
            .forEach { id -> findViewById<Button>(id).setOnClickListener(listenerForDigits) }

        val listenerForOps = View.OnClickListener { v ->
            val button = v as Button
            val op = button.text.toString()
            val currentText = tvDisplay.text.toString()

            if (isOperationSet) {
                if (currentText.endsWith(" ")) {
                    val textWithoutOp = currentText.substring(0, currentText.length - 3)
                    tvDisplay.text = "$textWithoutOp $op "
                    currentOperation = op
                    return@OnClickListener
                } else {
                    calculateResult()
                }
            }

            try {
                val lastNumStr = getLastNumber(tvDisplay.text.toString())
                firstOperand = lastNumStr.toDouble()
                currentOperation = op
                isOperationSet = true
                tvDisplay.text = "$lastNumStr $op "
            } catch (e: Exception) {
                showError("Err")
            }
        }

        listOf(R.id.btnPlus, R.id.btnMinus, R.id.btnMult, R.id.btnDiv)
            .forEach { id -> findViewById<Button>(id).setOnClickListener(listenerForOps) }

        findViewById<Button>(R.id.btnEqual).setOnClickListener {
            calculateResult()
            isOperationSet = false
            currentOperation = ""
        }

        val specialFuncListener = View.OnClickListener { v ->
            if (isOperationSet) calculateResult()

            try {
                val text = tvDisplay.text.toString()
                val valueStr = if (isOperationSet) text.substringBefore(" ") else text

                if (valueStr.isEmpty()) return@OnClickListener

                val value = valueStr.toDouble()
                var result = 0.0
                val btn = v as Button

                when (btn.id) {
                    R.id.btnOct -> {
                        val intValue = value.toLong()
                        tvDisplay.text = java.lang.Long.toOctalString(intValue)
                        isOperationSet = false
                        return@OnClickListener
                    }
                    R.id.btnPlusMinus -> result = value * -1
                    R.id.btnReciprocal -> {
                        if (value == 0.0) { showError("Zero Division"); return@OnClickListener }
                        result = 1.0 / value
                    }
                    R.id.btnPi -> {
                        result = if (value == 0.0) Math.PI else value * Math.PI
                    }
                }
                displayResult(result)
            } catch (e: Exception) {
                if (v.id == R.id.btnPi) tvDisplay.text = Math.PI.toString()
                else showError("Error")
            }
        }

        listOf(R.id.btnOct, R.id.btnPlusMinus, R.id.btnReciprocal, R.id.btnPi)
            .forEach { id -> findViewById<Button>(id).setOnClickListener(specialFuncListener) }

        findViewById<Button>(R.id.btnC).setOnClickListener {
            tvDisplay.text = "0"
            firstOperand = 0.0
            currentOperation = ""
            isOperationSet = false
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            val str = tvDisplay.text.toString()
            if (str.length > 1) {
                if (str.endsWith(" ")) {
                    tvDisplay.text = str.substring(0, str.length - 3)
                    isOperationSet = false
                } else {
                    tvDisplay.text = str.substring(0, str.length - 1)
                }
            } else {
                tvDisplay.text = "0"
                isOperationSet = false
            }
        }
    }

    private fun calculateResult() {
        if (!isOperationSet) return

        try {
            val currentText = tvDisplay.text.toString()
            val opIndex = currentText.lastIndexOf(" $currentOperation ")
            if (opIndex == -1) return

            val secondText = currentText.substring(opIndex + currentOperation.length + 2)
            if (secondText.isEmpty()) return

            val secondOperand = secondText.toDouble()
            var result = 0.0

            when (currentOperation) {
                "+" -> result = firstOperand + secondOperand
                "-" -> result = firstOperand - secondOperand
                "X" -> result = firstOperand * secondOperand
                "/" -> {
                    if (secondOperand == 0.0) { showError("Zero Division"); return }
                    result = firstOperand / secondOperand
                }
            }

            displayResult(result)
            firstOperand = result

        } catch (e: Exception) {
            showError("Error")
        }
    }

    private fun getLastNumber(text: String): String {
        val parts = text.split(" ")
        return parts.last()
    }

    private fun displayResult(value: Double) {
        if (value.isNaN() || value.isInfinite()) {
            showError("Error")
            return
        }

        val absVal = abs(value)

        if (value == 0.0) {
            tvDisplay.text = "0"
            return
        }

        if (absVal >= 1e8 || absVal <= 1e-4) {
            tvDisplay.text = String.format(Locale.US, "%.5E", value)
        }
        else {
            if (value % 1.0 == 0.0) {
                tvDisplay.text = value.toLong().toString()
            } else {
                val formatted = String.format(Locale.US, "%.8f", value)
                    .trimEnd('0')
                    .trimEnd('.')
                tvDisplay.text = formatted
            }
        }
    }

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        tvDisplay.text = "Error"
        isOperationSet = false
    }
}