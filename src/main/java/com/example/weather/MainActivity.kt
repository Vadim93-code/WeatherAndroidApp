package com.example.weather

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.adapter.Item
import com.example.weather.adapter.ItemAdapter
import com.google.android.gms.location.*
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val adapter = ItemAdapter()

    var CITY: String = "Kyiv"
    val API: String = "23d450fa83c223148d611fcf6d35e23f"

    lateinit var lat: String
    lateinit var lon: String
    lateinit var city: String
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    lateinit var geocoder: Geocoder

    val REQUEST_CODE = 1000
    var po = true

    var preferences: SharedPreferences? = null
    var preferencesDay: SharedPreferences? = null

    val array_days = mutableListOf<String>()
    val array_status = mutableListOf<String>()
    val array_temp_max = mutableListOf<String>()
    val array_temp_min = mutableListOf<String>()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = getSharedPreferences("VALUE", MODE_PRIVATE)
        preferencesDay = getSharedPreferences("DAY", MODE_PRIVATE)

        city = intent.getStringExtra("city").toString()

        if (city != "GPS") {
            CITY = city
            getAddress()
        } else {
            // проверка на gps
            if (!isLocationEnabled(applicationContext)) {
                GPSDialog()
            }else {
                checkStatus()
            }
        }

        if (isOnline(applicationContext)){
            Handler().postDelayed(Runnable {
                weatherTask().execute()
                findViewById<TextView>(R.id.address).text = CITY
                Handler().postDelayed(Runnable {
                    init()
                }, 500)
            }, 500)
        }else{
            NetworkDialog()
            fillingArrayNotNetwork()
            init()
            getValueWeatherNoNetwork()
        }


    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStart() {
        super.onStart()

        //переход на другое активити
        val buuton_favorite: LinearLayout = findViewById(R.id.favorote)
        buuton_favorite.setOnClickListener {
            intentFavoriteActivity()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun intentFavoriteActivity() {
        if (isOnline(applicationContext)) {
            val intent = Intent(this@MainActivity, FavoriteActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(applicationContext, "У вас нету интернет-подключения, мы не можем открыть ваши избранные", Toast.LENGTH_LONG).show()
        }
    }

    private fun getAddress(){
        val geocoder = Geocoder(this)
        val cityArr = geocoder.getFromLocationName(city, 1)
        lat = cityArr[0].latitude.toString()
        lon = cityArr[0].longitude.toString()
    }

    private fun getValueWeatherNoNetwork() {
        findViewById<TextView>(R.id.temp).text = preferences?.getString("temp", "0")
        findViewById<TextView>(R.id.address).text = preferences?.getString("city", "0")
        findViewById<TextView>(R.id.temp_min).text = preferences?.getString("temp_min", "0")
        findViewById<TextView>(R.id.temp_max).text = preferences?.getString("temp_max", "0")
        findViewById<TextView>(R.id.sunrise).text = preferences?.getString("sunrise", "0")
        findViewById<TextView>(R.id.sunset).text = preferences?.getString("sunset", "0")
        findViewById<TextView>(R.id.wind).text = preferences?.getString("wind", "0")
        findViewById<TextView>(R.id.pressure).text = preferences?.getString("pressure", "0")
        findViewById<TextView>(R.id.humidity).text = preferences?.getString("humidity", "0")
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

    // Заполненния массивов без интернета
    private fun fillingArrayNotNetwork(){
        val sdf = SimpleDateFormat("EEEE", Locale.ENGLISH)
        for (i in 1..8){
            val key1 = "Day$i tempMax"
            val key2 = "Day$i tempMin"
            val key3 = "Day$i status"
            val key4 = "Day$i day"

            val day = preferencesDay?.getString(key4, "0")
            val tempMax = preferencesDay?.getString(key1, "0")
            val tempMin = preferencesDay?.getString(key2, "0")
            val status = preferencesDay?.getString(key3, "0")

            val date = sdf.format(day!!.toLong() * 1000L)

            array_days.add(date.toString())
            array_status.add(status.toString())
            array_temp_max.add(tempMax.toString())
            array_temp_min.add(tempMin.toString())
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

    fun init() {
        val rcView: RecyclerView = findViewById(R.id.rcView)
        rcView.layoutManager = GridLayoutManager(this@MainActivity, 1)
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

    private fun checkStatus(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
        else {
            buildLocationRequest()
            buildLocationCallBack()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

            if(ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED   )
                {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                    Looper.myLooper()!!
                )
        }
    }

    private fun buildLocationCallBack() {
        locationCallback = object :LocationCallback() {

            override fun onLocationResult(p0: LocationResult) {
                var location = p0!!.locations.get(p0!!.locations.size-1)
                val city = getAddress(location.latitude, location.longitude)
                CITY = city
                lon = location.longitude.toString()
                lat = location.latitude.toString()
            }

        }
    }

    private fun getAddress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(lat, lng, 1)
        val city = list[0].locality
        CITY = city
        val editor = preferences?.edit()
        editor?.putString("city", city)
        editor?.apply()
        return city
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    // проверка на gps
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

    // диалоговое окно для интернет-сойдинения
    fun NetworkDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Проблемы с интернет соединениям")
        builder.setMessage("У вас нету интернет-соединения. Вы не можете обновить данные")
        builder.setNeutralButton("Ok"){ dialogInterface, i ->

        }
        builder.show()
    }

    // диалоговое окно для GPS сойдинения
    fun GPSDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Проблемы с GPS соединениям")
        builder.setMessage("У вас нету GPS соединения. Мы не сможем отследить ваш город, и дать вам информацию о погоде в нем. Включите пожалуйста")
        builder.setNeutralButton("Ok"){ dialogInterface, i ->

        }
        builder.show()
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
                val editor = preferences?.edit()
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("current")
                val weather = main.getJSONArray("weather").getJSONObject(0)
                val daily = jsonObj.getJSONArray("daily")
                val today = daily.getJSONObject(0)

                val temp = main.getInt("temp").toString()+"°C"
                val tempMin = "Min Temp: " + today.getJSONObject("temp").getString("min") +"°C"
                val tempMax = "Max Temp: " + today.getJSONObject("temp").getString("max") +"°C"
                editor?.putString("temp_min", tempMin)
                editor?.putString("temp_max", tempMax)
                editor?.putString("temp", temp)

                val pressure = main.getInt("pressure").toString()
                val humidity = main.getInt("humidity").toString()
                val sunrise:Long = main.getLong("sunrise")
                val sunset:Long = main.getLong("sunset")
                editor?.putString("pressure", pressure)
                editor?.putString("humidity", humidity)
                editor?.putString("sunrise", sunrise.toString())
                editor?.putString("sunset", sunset.toString())

                val windSpeed = main.getString("wind_speed")
                editor?.putString("wind_speed", windSpeed)

                editor?.apply()
                var size_element = daily.length()-1
                for (i in 0..size_element){
                    val editor = preferencesDay?.edit()
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

                    editor?.putString(key1, tempMax)
                    editor?.putString(key2, tempMin)
                    editor?.putString(key3, status)
                    editor?.putString(key4, day.toString())

                    editor?.apply()
                    fillingArrayNetwork(day, status, tempMax, tempMin)
                }

                /* Populating extracted data into our views */
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
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

}
