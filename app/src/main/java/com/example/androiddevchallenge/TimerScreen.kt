/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androiddevchallenge.ui.theme.typography
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@Composable
fun TimerScreen(timerViewModel: TimerViewModel = viewModel()) {
    val second: Int by timerViewModel.passTime.collectAsState(initial = 0)
    val state: TimerViewModel.State by timerViewModel.state.collectAsState(initial = TimerViewModel.State.ON_STOP)
    val color: Color by animateColorAsState(
        targetValue = when (second) {
            2 -> Color.Green
            1 -> Color.Yellow
            0 -> Color.Red
            else -> Color.Unspecified
        }
    )

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Timer")
                }
            )
        }
    ) {
        Surface(color = MaterialTheme.colors.background) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimerText(
                    second = second,
                    color = color
                )
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    IconButton(
                        onClick = { timerViewModel.down() },
                        enabled = state == TimerViewModel.State.ON_STOP
                    ) {
                        Icon(Icons.Filled.Remove, "add")
                    }
                    TimerButton(
                        state = state,
                        onClickStart = {
                            scope.launch {
                                timerViewModel.start()
                            }
                        },
                        onClickPause = {
                            timerViewModel.pause()
                        },
                        onClickStop = {
                            timerViewModel.stop()
                        }
                    )
                    IconButton(
                        onClick = { timerViewModel.up() },
                        enabled = state == TimerViewModel.State.ON_STOP
                    ) {
                        Icon(Icons.Filled.Add, "add")
                    }
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun TimerButton(
    state: TimerViewModel.State,
    onClickStart: () -> Unit,
    onClickPause: () -> Unit,
    onClickStop: () -> Unit
) {
    val onClick: () -> Unit = when (state) {
        TimerViewModel.State.ON_START -> onClickPause
        TimerViewModel.State.ON_STOP,
        TimerViewModel.State.ON_PAUSE -> onClickStart
        TimerViewModel.State.ON_TIME_OVER -> { {} }
    }
    val icon = when (state) {
        TimerViewModel.State.ON_TIME_OVER,
        TimerViewModel.State.ON_START -> Icons.Filled.Pause
        TimerViewModel.State.ON_STOP,
        TimerViewModel.State.ON_PAUSE -> Icons.Filled.PlayArrow
    }

    Column {
        AnimatedVisibility(visible = state != TimerViewModel.State.ON_TIME_OVER) {
            IconButton(onClick = onClick) {
                Icon(icon, "countdown")
            }
        }
        AnimatedVisibility(visible = state.isCountDown) {
            IconButton(onClick = onClickStop) {
                Icon(Icons.Filled.Stop, "stop")
            }
        }
    }
}

@Composable
fun TimerText(second: Int?, color: Color) {
    val text = second?.let {
        second.toString()
    } ?: stringResource(id = R.string.no_time)
    Row {
        Text(
            text = text,
            style = typography.h1.copy(color = color),
        )
    }
}
