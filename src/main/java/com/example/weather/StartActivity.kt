package com.example.weather

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.core.view.get

class StartActivity : AppCompatActivity() {

    val options = mutableListOf("Moscow", "Saint Petersburg", "Ivanovo")
    lateinit var citySelect: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val spinner: Spinner = findViewById(R.id.spinner)

        spinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                citySelect = options.get(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(applicationContext, "evwvwewvevw", Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onStart() {
        super.onStart()

        val button1: Button = findViewById(R.id.button2)
        val button2: Button = findViewById(R.id.button4)

        button1.setOnClickListener {
            if (citySelect != null){
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("city", citySelect)
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext, "Выберите город из списка", Toast.LENGTH_LONG).show()
            }
        }
        button2.setOnClickListener {
            if (isLocationEnabled(applicationContext)){
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("city", "GPS")
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext, "У вас не работает GPS, выберете город указанный выше в списке", Toast.LENGTH_LONG).show()
            }
        }

    }

    fun isLocationEnabled(context: Context): Boolean {
        var locationMode = 0
        val locationProviders: String
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(
                    context.getContentResolver(),
                    Settings.Secure.LOCATION_MODE
                )
            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
            }
            locationMode != Settings.Secure.LOCATION_MODE_OFF
        } else {
            locationProviders = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED
            )
            !TextUtils.isEmpty(locationProviders)
        }
    }

}