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

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlin.math.max

class TimerViewModel : ViewModel() {
    enum class State(val isCountDown: Boolean) {
        ON_STOP(false),
        ON_START(true),
        ON_PAUSE(true),
        ON_TIME_OVER(true),
        ;
    }
    private val _state = MutableStateFlow(State.ON_STOP)
    val state: Flow<State> = _state

    private val _startTime = MutableStateFlow(5)
    private val _passTime = MutableStateFlow(0)
    val passTime: Flow<Int> = state.flatMapLatest { state ->
        if (state != State.ON_STOP) _passTime else _startTime
    }

    private var job: Job? = null

    fun up(step: Int = 1) {
        if (_state.value == State.ON_PAUSE) {
            _passTime.value = _passTime.value + step
        } else {
            _startTime.value = _startTime.value + step
        }
    }

    fun down(step: Int = 1) {
        if (_state.value == State.ON_PAUSE) {
            _passTime.value = max(_passTime.value - step, 1)
        } else {
            _startTime.value = max(_startTime.value - step, 1)
        }
    }

    private suspend fun countDown(startTime: Int = _startTime.value) {
        job = viewModelScope.launch(Dispatchers.IO) {
            _state.value = State.ON_START
            repeat(startTime) { passTime ->
                val passed = startTime - passTime
                Log.d("timer", "passed=$passed")
                _passTime.value = passed
                delay(1000)
            }
            _passTime.value = 0
            _state.value = State.ON_TIME_OVER
        }
    }

    suspend fun start() {
        val startTime = if (_state.value != State.ON_STOP) {
            _passTime.value
        } else {
            _startTime.value
        }
        countDown(startTime)
    }

    fun pause() {
        _state.value = State.ON_PAUSE
        job?.cancel()
    }

    fun stop() {
        _state.value = State.ON_STOP
        job?.cancel()
    }
}
