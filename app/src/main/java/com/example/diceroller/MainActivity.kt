package com.example.diceroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.diceroller.ui.theme.DiceRollerTheme
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.runtime.*
import kotlin.random.Random
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.blur
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiceRollerTheme {
                Scaffold( modifier = Modifier.fillMaxSize() ) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

    var diceNumber by remember { mutableStateOf(1) }
    var isRolling by remember { mutableStateOf(false) }

    val rotation = remember { Animatable(0f) }
    val offsetX = remember { Animatable(0f) }

    val context = LocalContext.current

    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.dice_roll)
    }
    var player1Name by remember { mutableStateOf("") }
    var player2Name by remember { mutableStateOf("") }
    var isGameStarted by remember { mutableStateOf(false) }

    var maxTurns by remember { mutableStateOf(10) }
    var turnCount by remember { mutableStateOf(0) }

    var currentPlayer by remember { mutableStateOf(1) }

    var player1Score by remember { mutableStateOf(0) }
    var player2Score by remember { mutableStateOf(0) }

    var winnerText by remember { mutableStateOf("") }

    fun resetGame() {
        player1Score = 0
        player2Score = 0
        turnCount = 0
        winnerText = ""
        currentPlayer = 1
        player1Name = ""
        player2Name = ""
        isGameStarted = false
    }

    LaunchedEffect(isRolling) {
        if (isRolling && turnCount < maxTurns) {

            launch {
                repeat(10) {
                    offsetX.animateTo(15f, tween(50))
                    offsetX.animateTo(-15f, tween(50))
                }
                offsetX.animateTo(0f)
            }

            mediaPlayer.start()

            rotation.animateTo(
                targetValue = 1440f,
                animationSpec = tween(1000)
            )

            diceNumber = rollDice()

            // Add score to current player
            if (currentPlayer == 1) {
                player1Score += diceNumber
                currentPlayer = 2
            } else {
                player2Score += diceNumber
                currentPlayer = 1
            }

            turnCount++

            if (turnCount == maxTurns) {
                winnerText = when {
                    player1Score > player2Score -> "$player1Name Wins 🎉"
                    player2Score > player1Score -> "$player2Name Wins 🎉"
                    else -> "It's a Draw 🤝"
                }
            }

            rotation.snapTo(0f)
            isRolling = false
        }
    }



    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (!isGameStarted) {

            // 🔹 FIRST SCREEN (Name Input Screen)

            Text("Enter Player Names")

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = player1Name,
                onValueChange = { player1Name = it },
                label = { Text("Player 1 Name") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = player2Name,
                onValueChange = { player2Name = it },
                label = { Text("Player 2 Name") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (player1Name.isNotBlank() && player2Name.isNotBlank()) {
                        isGameStarted = true
                    }
                }
            ) {
                Text("Start Game")
            }

        } else {

            // 🔹 SECOND SCREEN (Your Existing Dice UI)

            Text("Turns Left: ${maxTurns - turnCount}")

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(horizontalAlignment = Alignment.Start) {
                    Text(player1Name)
                    Text("Score: $player1Score")
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(player2Name)
                    Text("Score: $player2Score")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Current Turn: ${if (currentPlayer == 1) player1Name else player2Name}")

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                val imageResource = when (diceNumber) {
                    1 -> R.drawable.dice_1
                    2 -> R.drawable.dice_2
                    3 -> R.drawable.dice_3
                    4 -> R.drawable.dice_4
                    5 -> R.drawable.dice_5
                    else -> R.drawable.dice_6
                }

                Image(
                    painter = painterResource(id = imageResource),
                    contentDescription = "Dice",
                    modifier = Modifier
                        .size(150.dp)
                        .blur(if (isRolling) 8.dp else 0.dp)
                        .graphicsLayer {
                            rotationY = rotation.value
                            translationX = offsetX.value
                            cameraDistance = 12f * density
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Rolled: $diceNumber")

            Spacer(modifier = Modifier.height(16.dp))

            if (turnCount < maxTurns) {

                RollDiceButton {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                150,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        vibrator.vibrate(150)
                    }

                    isRolling = true
                }

            } else {

                Text(winnerText)

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { resetGame() }) {
                    Text("Start New Game")
                }

            }

        }
    }



    }


@Composable
fun RollDiceButton(onClickAction: () -> Unit) {
    Button(
        onClick = onClickAction,
        shape = RoundedCornerShape(20.dp)

    ) {
        Text("Roll Dice")
    }
}

fun rollDice(): Int {
    return Random.nextInt(1, 7)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DiceRollerTheme {
        Greeting("Android")
    }
}