package com.example.weather

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.adapter.Item
import com.example.weather.adapter.ItemAdapter
import com.example.weather.databinding.ActivityFavoriteCityDeteilsBinding
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class FavoriteCityDeteils : AppCompatActivity() {

    lateinit var binding: ActivityFavoriteCityDeteilsBinding

    lateinit var city: String
    lateinit var lon: String
    lateinit var lat: String

    private val adapter = ItemAdapter()

    val array_days = mutableListOf<String>()
    val array_status = mutableListOf<String>()
    val array_temp_max = mutableListOf<String>()
    val array_temp_min = mutableListOf<String>()

    val API: String = "23d450fa83c223148d611fcf6d35e23f"

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteCityDeteilsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        city = intent.getStringExtra("CityName").toString()
        lat = intent.getStringExtra("Lat").toString()
        lon = intent.getStringExtra("Lon").toString()
//        Toast.makeText(applicationContext, lon.toString(), Toast.LENGTH_LONG).show()

        binding.address.text = city

        Handler().postDelayed(Runnable {
            weatherTask().execute()
            Handler().postDelayed(Runnable {
                init() }, 500)
        }, 500)
    }

    fun init() {
        val rcView: RecyclerView = findViewById(R.id.rcView)
        rcView.layoutManager = GridLayoutManager(this@FavoriteCityDeteils, 1)
        rcView.adapter = adapter

        for (i in 0..array_days.size-1) {
            val item = Item(
                array_days[i].substring(0,3),
                array_status[i],
                "Max: "+array_temp_max[i]+"°C",
                "Min: "+array_temp_min[i]+"°C"
            )
            adapter.addItem(item)
        }
    }

    // Заполненния массивов с интернетом
    private fun fillingArrayNetwork(day_second: Long, status: String, temp_max: String, temp_min: String){
        val sdf = SimpleDateFormat("EEEE", Locale.ENGLISH)
        val date = sdf.format(day_second * 1000L)
        array_days.add(date.toString())
        array_status.add(status)
        array_temp_max.add(temp_max)
        array_temp_min.add(temp_min)
//        Toast.makeText(applicationContext, date.toString(), Toast.LENGTH_LONG).show()
    }

    inner class weatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            /* Showing the ProgressBar, Making the main design GONE */
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/onecall?lat=$lat&lon=$lon&exclude=minutely,hourly,alerts&&units=metric&appid=$API").readText(
                    Charsets.UTF_8
                )
            }catch (e: Exception){
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("current")
                val weather = main.getJSONArray("weather").getJSONObject(0)
                val daily = jsonObj.getJSONArray("daily")
                val today = daily.getJSONObject(0)

                val temp = main.getInt("temp").toString()+"°C"
                val tempMin = "Min Temp: " + today.getJSONObject("temp").getString("min") +"°C"
                val tempMax = "Max Temp: " + today.getJSONObject("temp").getString("max") +"°C"

                val pressure = main.getInt("pressure").toString()
                val humidity = main.getInt("humidity").toString()
                val sunrise:Long = main.getLong("sunrise")
                val sunset:Long = main.getLong("sunset")

                val windSpeed = main.getString("wind_speed")

                var size_element = daily.length()-1
                for (i in 0..size_element){
                    val jsonObject = daily.getJSONObject(i)

                    val key1 = "Day${i+1} tempMax"
                    val key2 = "Day${i+1} tempMin"
                    val key3 = "Day${i+1} status"
                    val key4 = "Day${i+1} day"

                    val day = jsonObject.getLong("dt")
                    val temp = jsonObject.getJSONObject("temp")
                    val tempMax = temp.getString("max")
                    val tempMin = temp.getString("min")
                    val weather = jsonObject.getJSONArray("weather").getJSONObject(0)
                    val status = weather.getString("main")

                    fillingArrayNetwork(day, status, tempMax, tempMin)
                }

                /* Populating extracted data into our views */
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
                    Date(sunrise*1000)
                )
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
                    Date(sunset*1000)
                )
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity

                /* Views populated, Hiding the loader, Showing the main design */
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

            } catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }

        }
    }

    // проверка на интернет-подключения
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                } else {
                    TODO("VERSION.SDK_INT < M")
                }
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
//                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
//                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    // Удаления шапки и панели управления
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            this.window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

    }

}