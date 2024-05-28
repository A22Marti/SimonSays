package com.example.simondice

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private val soundResources = intArrayOf(
        R.raw.blue,
        R.raw.green,
        R.raw.red,
        R.raw.yellow,
        R.raw.lose //derrota
    )

    private val simonSequence = mutableListOf<Int>()
    private var userSequenceIndex = 0
    private var score = 0

    private lateinit var textScore: TextView
    private lateinit var textHighScore: TextView
    private lateinit var textHighScoreToday: TextView
    private lateinit var crossImage: ImageView
    private lateinit var databaseHandler: DatabaseHandler

    private var defeatHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaPlayer = MediaPlayer.create(this, soundResources[0])

        textScore = findViewById(R.id.textScore)
        textHighScore = findViewById(R.id.textHighScore)
        textHighScoreToday = findViewById(R.id.textHighScoreToday)
        crossImage = findViewById(R.id.crossImage) // Inicializar crossImage
        databaseHandler = DatabaseHandler(this)

        val buttons = arrayOf(
            findViewById<Button>(R.id.buttonRed),
            findViewById<Button>(R.id.buttonBlue),
            findViewById<Button>(R.id.buttonGreen),
            findViewById<Button>(R.id.buttonYellow)
        )

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                onButtonClick(index, button)
            }
        }

        databaseHandler.addHighscore(0, "")
        val highscore = databaseHandler.getHighscore()
        textHighScore.text = "Highscore: $highscore"

        val highscoreToday = databaseHandler.getHighscoreOfToday()
        textHighScoreToday.text = "Best Score Today: $highscoreToday"

        startSimonSays()
    }

    private fun onButtonClick(index: Int, button: Button) {
        if (index == simonSequence[userSequenceIndex]) {
            animateButton(button)
            playSound(index)

            userSequenceIndex++

            if (userSequenceIndex == simonSequence.size) {
                userSequenceIndex = 0
                addRandomButtonToSimonSequence()
                playSimonSequence()
                score++
                textScore.text = "Score: $score"
            }
        } else {
            handleDefeat()
        }
    }

    private fun handleDefeat() {
        hideButtons()
        showCrossAnimation()

        // Obtener el mejor puntaje de todos los tiempos
        val highscore = databaseHandler.getHighscore()

        // Actualizar el puntaje del día si es mayor que el anterior
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val highscoreToday = databaseHandler.getHighscoreOfToday()
        if (score > highscoreToday) {
            databaseHandler.addHighscore(score, currentDate)
            textHighScoreToday.text = "Best Score Today: $score"
        } else {
            textHighScoreToday.text = "Best Score Today: $highscoreToday"
        }

        // Actualizar el puntaje de todos los tiempos si es necesario
        if (score > highscore) {
            databaseHandler.addHighscore(score, currentDate)
            textHighScore.text = "Highscore: $score"
        } else {
            textHighScore.text = "Highscore: $highscore"
        }

        defeatHandler = Handler()
        defeatHandler?.postDelayed({
            hideCrossAnimation()
            showButtons()
            restartGame()
            score = 0
            textScore.text = "Score: $score"
        }, mediaPlayer.duration.toLong() + 1000)
    }

    private fun showCrossAnimation() {
        crossImage.visibility = View.VISIBLE
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                crossImage.startAnimation(fadeOut)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                crossImage.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        crossImage.startAnimation(fadeIn)
    }

    private fun hideCrossAnimation() {
        crossImage.clearAnimation()
        crossImage.visibility = View.GONE
    }

    private fun hideButtons() {
        val buttons = arrayOf(
            findViewById<Button>(R.id.buttonRed),
            findViewById<Button>(R.id.buttonBlue),
            findViewById<Button>(R.id.buttonGreen),
            findViewById<Button>(R.id.buttonYellow)
        )
        buttons.forEach { it.visibility = View.INVISIBLE }
    }

    private fun showButtons() {
        val buttons = arrayOf(
            findViewById<Button>(R.id.buttonRed),
            findViewById<Button>(R.id.buttonBlue),
            findViewById<Button>(R.id.buttonGreen),
            findViewById<Button>(R.id.buttonYellow)
        )
        buttons.forEach { it.visibility = View.VISIBLE }
    }

    private fun animateButton(button: Button) {
        button.isPressed = true
        Handler().postDelayed({ button.isPressed = false }, 500)
    }

    private fun playSimonSequence() {
        val handler = Handler()
        for (i in 0 until simonSequence.size) {
            val buttonIndex = simonSequence[i]
            val button = findViewById<Button>(getButtonId(buttonIndex))
            handler.postDelayed(
                {
                    animateButton(button)
                    playSound(buttonIndex)
                },
                (i + 1) * 1000L
            )
        }
    }

    private fun restartGame() {
        simonSequence.clear()
        userSequenceIndex = 0
        textHighScore.text = "Highscore: " + databaseHandler.getHighscore().toString()
        addRandomButtonToSimonSequence()
        playSimonSequence()
    }

    private fun addRandomButtonToSimonSequence() {
        val randomButtonIndex = (0 until soundResources.size - 1).random()
        simonSequence.add(randomButtonIndex)
    }

    private fun playSound(index: Int) {
        mediaPlayer.apply {
            reset()
            setDataSource(resources.openRawResourceFd(soundResources[index]))
            prepare()
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        defeatHandler?.removeCallbacksAndMessages(null)
        mediaPlayer.release()
    }

    private fun startSimonSays() {
        addRandomButtonToSimonSequence()
        playSimonSequence()
    }

    private fun getButtonId(index: Int): Int {
        return when (index) {
            0 -> R.id.buttonRed
            1 -> R.id.buttonBlue
            2 -> R.id.buttonGreen
            3 -> R.id.buttonYellow
            else -> throw IllegalArgumentException("Invalid button index")
        }
    }
}
