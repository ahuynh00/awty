package edu.uw.ischool.ahuynh00.awty

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

const val ALARM_ACTION = "AWTY"
class MainActivity : AppCompatActivity() {
    private lateinit var etmessage: EditText
    private lateinit var etphoneNumber: EditText
    private lateinit var etinterval: EditText
    private lateinit var startStopBtn: Button
    var receiver : BroadcastReceiver? = null


//  As a user, when I "Start" the service, it should begin to "send messages" every N minutes,
//  as given by the user in the EditText UI. These should be sent as SMS messages.

//  2pts: send an audio message (doesn't need to be recorded on the device; it can be a static asset)
//  2pts: send a video message (ditto--can be a static asset)
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
                validatePhoneNumber()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        etphoneNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher());
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
                && etphoneNumber.error == null
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

    fun validatePhoneNumber() {
        val phoneNumber = etphoneNumber.text.toString()
        Log.d("validatePhone", phoneNumber)
        if (!Regex("^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]\\d{3}[\\s.-]\\d{4}\$").matches(phoneNumber)) {
            etphoneNumber.error = "Invalid phone number"
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
        etmessage.isEnabled = false
        etinterval.isEnabled = false
        etphoneNumber.isEnabled = false

        val activityThis = this
        if (receiver == null) {
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    sendMsgs("${etphoneNumber.text}", "${etmessage.text}")
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
        val intervalMS : Long = 60 * 1000 * etinterval.text.toString().toLong()
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            intervalMS,
            pendingIntent)

    }

    private fun endMsgs() {
        unregisterReceiver(receiver)
        receiver = null

        // TODO: make this a separate function (and in start MSG)
        etmessage.isEnabled = true
        etinterval.isEnabled = true
        etphoneNumber.isEnabled = true
    }

    private fun sendMsgs(phoneNumber: String, message : String) {
        try {
            val smsManager:SmsManager
            if (Build.VERSION.SDK_INT>=23) {
                smsManager = this.getSystemService(SmsManager::class.java)
            } else{
                smsManager = SmsManager.getDefault()
            }
            val number = phoneNumber.replace("[() -]", "")

            smsManager.sendTextMessage(number, null, message, null, null)
            Log.d("send Msgs", "after send")
        } catch (error: Exception) {
            Log.e("send Msgs", "$error")
        }
    }





}