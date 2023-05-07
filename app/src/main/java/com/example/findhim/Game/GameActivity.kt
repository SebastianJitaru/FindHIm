package com.example.findhim.Game

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.findhim.FinalActivity
import com.example.findhim.R
import java.util.*


class GameActivity : AppCompatActivity() {
    private lateinit var gridView: GridView
    private lateinit var textInput1: TextView
    private lateinit var gridContainer: FrameLayout
    private lateinit var backgroundImage: Drawable
    private lateinit var bgImage: ImageView
    private lateinit var chronometer: Chronometer
    private var cellSize: Int = 0
    private var clicks: Int = 0
    private var imageIndex: Int = -1
    private var numCols: Int = 0
    private var numRows: Int = 0
    private var message: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_layout)

        gridView = findViewById(R.id.gridView)
        textInput1 = findViewById(R.id.text_input1)
        gridContainer = findViewById(R.id.grid_container)
        chronometer = findViewById(R.id.chronometer)


        if (savedInstanceState != null) {
            imageIndex = savedInstanceState.getInt("wally_pos")
            clicks = savedInstanceState.getInt("attempts")
            numCols = savedInstanceState.getInt("cols")
            numRows = savedInstanceState.getInt("rows")
            chronometer.base = savedInstanceState.getLong("chrono_time")
            chronometer.start()
        }

        // Get the input values passed from the MainActivity
        bgImage = findViewById(R.id.map)
        val backgroundImageId = intent.getIntExtra(SELECTED_LEVEL_IMAGE_KEY, 0)
        backgroundImage = ContextCompat.getDrawable(this, backgroundImageId)!!
        bgImage.setImageResource(backgroundImageId)


        message = intent.getStringExtra(MESSAGE_KEY)
        cellSize = intent.getIntExtra(CELL_SIZE, 100)
        onBack()

        //We will use a listener to wait for the image to be drawn and calculate the cells
        val vto: ViewTreeObserver = bgImage.viewTreeObserver
        vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                // Remove after the first run so it doesn't fire forever
                bgImage.viewTreeObserver.removeOnPreDrawListener(this)
                val finalHeight = bgImage.height
                val finalWidth = bgImage.width

//                Toast.makeText(this@GameActivity, "$numCols $numRows", Toast.LENGTH_SHORT ).show()
                if (numCols == 0 && numRows == 0) {
                    numCols = finalWidth / cellSize
                    numRows = finalHeight / cellSize
                }
                val numCells = numRows * numCols

                gridView.columnWidth = cellSize

                // Display the input values in the UI
                textInput1.text =
                    getString(R.string.game_title, message, clicks, finalWidth, finalHeight)

                // Create a list of cell values
                if (imageIndex == -1) {
                    val random = Random()
                    imageIndex = random.nextInt(numCells)
                }
                val cellValues =
                    Array(numCells) { if (it == imageIndex) R.drawable.wally else R.drawable.rounded_rectangle_blue }

                // Create the grid of cells with random values
                val adapter = object : BaseAdapter() {
                    override fun getCount(): Int {
                        return numCells
                    }

                    override fun getItem(position: Int): Any? {
                        return null
                    }

                    override fun getItemId(position: Int): Long {
                        return 0
                    }

                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup?
                    ): View {
                        val imageView: ImageView
                        if (convertView == null) {
                            imageView = ImageView(applicationContext)
                            imageView.layoutParams = ViewGroup.LayoutParams(cellSize, cellSize)
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                            imageView.setPadding(8, 8, 8, 8)
                        } else {
                            imageView = convertView as ImageView
                        }

                        imageView.setImageResource(cellValues[position])
                        return imageView
                    }
                }
                // Set the number of rows and columns of the grid based on the calculated values
                gridView.numColumns = numCols
                gridView.stretchMode = GridView.NO_STRETCH
                gridView.adapter = adapter
                var toast: Toast? = null
                Toast.makeText(
                    this@GameActivity,
                    "${gridView.width} ${gridView.height}",
                    Toast.LENGTH_SHORT
                ).show()
                // Set onItemClick Listener for each cell to detect when the user clicks on it
                gridView.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        clicks++
                        textInput1.text =
                            getString(R.string.game_title, message, clicks)
                        // If the user clicks on the image, display a message and finish the activity
                        if (position == imageIndex) {
                            chronometer.stop()

                            toast?.cancel()
                            toast =
                                Toast.makeText(this@GameActivity, "You win!", Toast.LENGTH_SHORT)
                            toast?.show()

                            val intent = Intent(this@GameActivity, FinalActivity::class.java)
                            setLogs(intent)
                            startActivity(intent)
                            finish()
                        } else {
                            // Do something else if the user clicked on a cell without the letter
                            // For example, you can show a Toast message
                            toast?.cancel()
                            toast =
                                Toast.makeText(this@GameActivity, "Try again!", Toast.LENGTH_SHORT)
                            toast?.show()
                        }
                    }

                // It doesn't matter if it has already been started
                chronometer.start()

                return true
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("chrono_time", chronometer.base)
        outState.putInt("wally_pos", imageIndex)
        outState.putInt("attempts", clicks)
        outState.putInt("cols", numCols)
        outState.putInt("rows", numRows)
    }

    override fun onDestroy() {
        chronometer.stop()
        super.onDestroy()
    }

    private fun onBack() {
        onBackPressedDispatcher.addCallback(
            this /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Back is pressed... Finishing the activity
                    finish()
                }
            })
    }


    private fun setLogs(intent: Intent): Intent {
        intent.putExtra("clicks", clicks.toString())
        intent.putExtra("time", chronometer.text.toString())
        intent.putExtra("nickname", message)
        return intent
    }

    companion object {
        const val MESSAGE_KEY = "message"
        const val SELECTED_LEVEL_IMAGE_KEY = "selectedLevelImage"
        const val CELL_SIZE = "cellsize"
    }
}