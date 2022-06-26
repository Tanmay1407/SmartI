package com.herokuapp.tanmaypatel.smarti


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import org.json.JSONException


class MainActivity : AppCompatActivity() {
    private lateinit var captureIV: ImageView
    private lateinit var snapBtn: Button
    private lateinit var getSearchResultBtn: Button
    private lateinit var resultRv: RecyclerView
    private lateinit var searchRVAdapter: SearchRVAdapter
    private lateinit var list: ArrayList<SearchRVModel>
    private lateinit var lodingBar: ImageView
    private lateinit var label: TextView
    private lateinit var imageBitMap: Bitmap
    private var title = "";
    private var link = "";
    private var displayed_link = ""
    private var snippet = ""
    private lateinit var activityResultLaucher: ActivityResultLauncher<Intent>
    private var backButtonCount = 0;
    private var snapTaken = false
    private val CAMERA_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        captureIV = findViewById(R.id.image)
        snapBtn = findViewById(R.id.btnSnap)
        getSearchResultBtn = findViewById(R.id.btnResult)
        resultRv = findViewById(R.id.RVSearchResults)
        lodingBar = findViewById(R.id.PBLoading)
        label = findViewById(R.id.tvLabel)

        list = ArrayList()
        searchRVAdapter = SearchRVAdapter(this, list)

        //resultRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        resultRv.adapter = searchRVAdapter

        activityResultLaucher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
                ActivityResultCallback { result ->
                    if (result.resultCode == RESULT_OK && result.data != null) {
                        val extras = result.data?.extras
                        imageBitMap = extras?.get("data") as Bitmap
                        snapTaken = true
                        captureIV.setImageBitmap(imageBitMap)

                    }
                })
        snapBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //Toast.makeText(this@MainActivity,"Camera not working..",Toast.LENGTH_SHORT).show()

                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    list.clear()
                    label.text = ""
                    searchRVAdapter.notifyDataSetChanged()
                    takePictureIntent();
                } else {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_CODE

                    )
                }

            }
        })

        getSearchResultBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                list.clear()
                label.text = ""
                searchRVAdapter.notifyDataSetChanged()
                lodingBar.visibility = View.VISIBLE
                getResults()
            }
        })
    }

    fun getResults() {
        if (!snapTaken) {
            lodingBar.visibility = View.GONE
            captureIV.setImageDrawable(getResources().getDrawable(R.drawable.scan))
            Toast.makeText(this, "Take a snap first..", Toast.LENGTH_SHORT).show()
            return;
        }
        snapTaken = false
        var image = InputImage.fromBitmap(imageBitMap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        labeler.process(image)
            .addOnSuccessListener { labels ->
                var searchQuery = labels[0].text
                Log.d("MainActivity", searchQuery)
                label.text = "Label \uD83C\uDFF7️ : " + searchQuery
                getSearchResults(searchQuery)

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to Detect image . .", Toast.LENGTH_SHORT)
            }

    }

    private fun getSearchResults(searchQuery: String) {
        var url =
            "https://serpapi.com/search?q=" + searchQuery + "&location=Delhi,India&api_key=22ceac46aa359ce26ad8b50c5cc7bab6ab34cd21c73d19ad692a5f04549ba3ec&hl=en&gl=us&google_domain=google.com"
        var queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                lodingBar.visibility = View.GONE
                try {
                    var organicArray = response.getJSONArray("organic_results")
                    Log.d("MainActivity", organicArray.toString())
                    for (i in 0..4) {
                        var organicObject = organicArray.getJSONObject(i)
                        if (organicObject.has("title")) {
                            title = organicObject.getString("title")
                            Log.d("MainActivity", title)
                        }
                        if (organicObject.has("link")) {
                            link = organicObject.getString("link")
                            Log.d("MainActivity", link)
                        }
                        if (organicObject.has("displayed_link")) {
                            displayed_link = organicObject.getString("displayed_link")
                            Log.d("MainActivity", displayed_link)
                        }
                        if (organicObject.has("snippet")) {
                            snippet = organicObject.getString("snippet")
                            Log.d("MainActivity", snippet)
                        }
                        list.add(SearchRVModel(title, link, displayed_link, snippet))
                        searchRVAdapter.notifyDataSetChanged()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                lodingBar.visibility = View.GONE
                label.text = "No Match Found \uD83D\uDC4E"
                Toast.makeText(this, "No results found. .", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
//            val extras = data?.extras
//            imageBitMap = extras?.get("data") as Bitmap
//            captureIV.setImageBitmap(imageBitMap)
//
//        }
//
//    }

    private fun takePictureIntent() {
        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            //startActivityForResult(intent,REQUEST_CODE)
            activityResultLaucher.launch(intent)
        } else {
            Toast.makeText(
                this, "There is no app that support this action",
                Toast.LENGTH_SHORT
            ).show();
        }

    }

    override fun onBackPressed() {
        if (backButtonCount >= 1) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } else {
            Toast.makeText(
                this,
                "Press the back button once again to close the application.",
                Toast.LENGTH_SHORT
            ).show()
            backButtonCount++
        }
    }

}