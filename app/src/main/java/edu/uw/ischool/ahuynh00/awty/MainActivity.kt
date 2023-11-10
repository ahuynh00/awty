package edu.uw.ischool.ahuynh00.awty

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.text.NumberFormat

const val ALARM_ACTION = "AWTY"
class MainActivity : AppCompatActivity() {
    private lateinit var etmessage: EditText
    private lateinit var etphoneNumber: EditText
    private lateinit var etinterval: EditText
    private lateinit var startStopBtn: Button
    var receiver : BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etmessage = findViewById(R.id.editTextUserMessage)
        etphoneNumber = findViewById(R.id.editTextPhone)
        etinterval = findViewById(R.id.editTextMinutesBetween)
        startStopBtn = findViewById(R.id.buttonStartStop)
        startStopBtn.isEnabled = false
        startStopBtn.text = "Start"
        var running = false


        etmessage.addTextChangedListener( object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                toggleEnable()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        etphoneNumber.addTextChangedListener( object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                toggleEnable()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        etinterval.addTextChangedListener( object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                validateInterval()
                toggleEnable()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        startStopBtn.setOnClickListener{
            running = toggleStartStop(running)
            if (running) {
                startMsgs()
            } else {
                endMsgs()
            }


        }


    }

    fun toggleEnable() {
        startStopBtn.isEnabled = (etmessage.text.toString().length !== 0
                && etphoneNumber.text.toString().length !== 0
                && etinterval.text.toString().length !== 0
                && etinterval.error == null)
    }

    fun validateInterval() {
        val  interval = etinterval.text.toString().trim()
        if (Regex("0+").matches(interval)) {
            etinterval.error = "Interval must be non-zero"
        } else {
            etinterval.error = null
        }
    }

    private fun toggleStartStop(running: Boolean): Boolean {
        if (running) {
            startStopBtn.text = "Start"
        } else {
            startStopBtn.text = "Stop"

        }
        return !running
    }

    private fun startMsgs() {
        val activityThis = this
        if (receiver == null) {
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Toast.makeText(activityThis, "${etphoneNumber.text}: ${etmessage.text}", Toast.LENGTH_LONG).show()
                    Log.d("toast", "interval rn??")
                }
            }
            val filter = IntentFilter(ALARM_ACTION)
            registerReceiver(receiver, filter)
        }

        // Create the PendingIntent
        val intent = Intent(ALARM_ACTION)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Get the Alarm Manager
        val alarmManager : AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intervalMS : Long = 1000 * etinterval.text.toString().toLong()
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            intervalMS,
            pendingIntent)
    }

    fun endMsgs() {
        unregisterReceiver(receiver)
        receiver = null
    }





}