package com.example.findhim.fragments.game

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.findhim.FinalActivity
import com.example.findhim.R
import com.example.findhim.databinding.FragmentGameBinding
import com.example.findhim.game.GameActivity
import com.example.findhim.game.GameGridAdapter
import com.example.findhim.persistency.Game
import com.example.findhim.persistency.GameApplication
import com.example.findhim.persistency.GameViewModel
import com.example.findhim.persistency.GameViewModelFactory
import java.util.Random
import java.util.Calendar


class GameFragment : Fragment() {
    private val totalTime: Long = 90000
    private lateinit var binding: FragmentGameBinding
    private lateinit var gameActivity: GameActivity
    private val gameViewModel: GameViewModel by viewModels {
        GameViewModelFactory((requireActivity().application as GameApplication).repository)
    }

    private lateinit var gridView: GridView
    private lateinit var bgImage: ImageView

    private var backgroundImageId: Int = 0
    private var cellSize: Int = 100
    private var imageIndex: Int = -1
    private var numCols: Int = 0
    private var numRows: Int = 0

    private var mListener: GameFragmentListener? = null

    private var message: String? = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mListener = try {
            context as GameFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement GameFragmentListener")
        }
        binding = FragmentGameBinding.inflate(inflater, container, false)

        if (savedInstanceState != null) {
            imageIndex = savedInstanceState.getInt(getString(R.string.waldopos), -1)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameActivity = requireActivity() as GameActivity

        arguments?.let {
            cellSize = it.getInt(ARG_CELL_SIZE, 100)
            backgroundImageId = it.getInt(ARG_MAP, 0)
            message = it.getString(ARG_NICKNAME)
        }

        gridView = binding.gridView
        bgImage = binding.map

        bgImage.setImageResource(backgroundImageId)

        createGrid()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(getString(R.string.waldopos), imageIndex)
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun createGrid() {
        val vto: ViewTreeObserver = bgImage.viewTreeObserver
        vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                bgImage.viewTreeObserver.removeOnPreDrawListener(this)
                val finalHeight = bgImage.height
                val finalWidth = bgImage.width

                if (numCols == 0 && numRows == 0) {
                    numCols = finalWidth / cellSize
                    numRows = finalHeight / cellSize
                }
                val numCells = numRows * numCols

                gridView.columnWidth = cellSize

                if (imageIndex == -1) {
                    val random = Random()
                    imageIndex = random.nextInt(numCells)
                }

                val cellValues = createCells(numCells)
                val adapter = GameGridAdapter(requireContext(), cellValues, cellSize)

                gridView.numColumns = numCols
                gridView.stretchMode = GridView.STRETCH_COLUMN_WIDTH
                gridView.adapter = adapter
                var toast: Toast? = null

                gridView.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        mListener?.onUserClick()

                        if (position == imageIndex) {
                            mListener?.onStopTime()

                            toast?.cancel()
                            toast =
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.win_text),
                                    Toast.LENGTH_SHORT
                                )
                            toast?.show()

                            val intent = Intent(requireContext(), FinalActivity::class.java)
                            setLogs(intent)
                            requireActivity().startActivity(intent)
                            requireActivity().finish()
                        } else {
                            toast?.cancel()
                            toast =
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.try_again),
                                    Toast.LENGTH_SHORT
                                )
                            toast?.show()
                        }
                    }

                mListener?.onStartTime()

                return true
            }
        })
    }

    private fun createCells(numCells: Int): Array<Int> {
        return Array(numCells) { if (it == imageIndex) R.drawable.wally else R.drawable.transparent_square }
    }

    private fun setLogs(intent: Intent): Intent {
        val game = Game(
            null,
            message,
            mListener?.getAttempts().toString(),
            mListener?.onGetTime()?.text.toString(),
            getCurrentTime()
        )
        gameViewModel.insert(game)
        val bundle = Bundle()
        bundle.putParcelable(getString(R.string.game_key), game)
        intent.putExtras(bundle)
        return intent
    }

    fun update(cellSize: Int, backgroundImageId: Int, nickname: String?) {
        this.cellSize = cellSize
        this.backgroundImageId = backgroundImageId
        this.message = nickname
    }

    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        return String.format("%02d:%02d:%02d", hour, minute, second)
    }

    fun maxTime() {
        //Ends game
        Toast.makeText(
            requireContext(),
            getString(R.string.lost),
            Toast.LENGTH_SHORT
        ).show()
        val intent = Intent(requireContext(), FinalActivity::class.java)
        setLogs(intent) // Set logs will create a bundle with a game object that is parcelable
        requireActivity().startActivity(intent) // Pass the game object to the next activity
        requireActivity().finish()
    }

    companion object {
        private const val ARG_CELL_SIZE = "arg_cell_size"
        private const val ARG_MAP = "arg_map"
        private const val ARG_NICKNAME = "arg_nickname"

        fun newInstance(cellSize: Int, backgroundImageId: Int, nickname: String?): GameFragment {
            val args = Bundle().apply {
                putInt(ARG_CELL_SIZE, cellSize)
                putInt(ARG_MAP, backgroundImageId)
                putString(ARG_NICKNAME, nickname)
            }
            return GameFragment().apply {
                arguments = args
            }
        }
    }
}
