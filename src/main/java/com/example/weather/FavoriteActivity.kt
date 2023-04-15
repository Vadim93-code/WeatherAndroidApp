package com.example.weather

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.adapter.CityAdapter
import com.example.weather.adapter.CityItem
import com.example.weather.adapter.Item
import com.example.weather.databinding.ActivityFavoriteBinding
import kotlinx.android.synthetic.main.activity_favorite_city_deteils.*
import org.json.JSONObject
import java.io.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class FavoriteActivity : AppCompatActivity() {

    lateinit var binding: ActivityFavoriteBinding
    private val adapter = CityAdapter()
    lateinit var city: String
    val cityList = mutableListOf(" ", "Moscow", "Saint Petersburg", "Ivanovo")
    lateinit var citySelect: String

    lateinit var preferences: SharedPreferences
    var sizeCity: Int = 1

    var cityArray = mutableListOf<String>()
    var coordinatesArrayLat = mutableListOf<String>()
    var coordinatesArrayLon = mutableListOf<String>()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences("City2", Context.MODE_PRIVATE)

        sizeCity = preferences.getInt("size", 1)
        getCity()

        if (isOnline(applicationContext)){
            getAddress()
        }
        init()

        val spinner = binding.spinner2
        spinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cityList)

    }

    override fun onStart() {
        super.onStart()

        val spinner = binding.spinner2
        val iCurrentSelection = spinner.selectedItemPosition
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (iCurrentSelection != position) {
                    citySelect = cityList.get(position)
                    sizeCity++
//                ClearSize()
                    SaveSize()
                    SaveCity(citySelect, sizeCity)
                    getAddress()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                return
            }
        }

        binding.button3.setOnClickListener {
            val intent = getIntent()
            finish()
            startActivity(intent)
        }

    }

    private fun SaveSize() {
        val edit = preferences.edit()
        edit.putInt("size", sizeCity)
        edit.apply()
    }

    private fun getCity() {
        for (i in 1..sizeCity-1){
            val key = "city $i"
            val getCity = preferences.getString(key, "").toString()
            cityArray.add(getCity)
        }
    }

    private fun SaveCity(city: String, keySize: Int) {
        val edit = preferences.edit()
        edit.putString("city ${keySize-1}", city)
        edit.apply()
    }

    private fun ClearSize() {
        val edit = preferences.edit()
        edit.clear()
        edit.apply()
    }

    private fun getAddress(){
        val geocoder = Geocoder(this)
        for (i in 0..cityArray.size-1){
            val cityArr = geocoder.getFromLocationName(cityArray[i], 1)
//            val cityArr = geocoder.getFromLocationName(cityArray[i], 1)
            coordinatesArrayLon.add(cityArr[0].longitude.toString())
            coordinatesArrayLat.add(cityArr[0].latitude.toString())
        }
    }

    fun init() {
        val rcView: RecyclerView = findViewById(R.id.rcView)
        rcView.layoutManager = GridLayoutManager(this@FavoriteActivity, 1)
        rcView.adapter = adapter
        adapter.itemList.clear()
        adapter.notifyDataSetChanged()

        for (i in 0..cityArray.size-1) {
            val item = CityItem(
                cityArray[i],
                coordinatesArrayLat[i],
                coordinatesArrayLon[i]
            )
            adapter.addItem(item)
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
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
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
