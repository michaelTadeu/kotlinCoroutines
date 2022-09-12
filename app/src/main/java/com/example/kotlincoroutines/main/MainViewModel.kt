package com.example.kotlincoroutines.main

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlincoroutines.utils.Utils
import kotlinx.coroutines.*

class MainViewModel(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    companion object {
        private const val TAG = "ViewModel"
        private const val INVALID_DATA = -1
        private const val DONE_LEFT_DATA = 10
        private const val DONE_RIGHT_DATA = 20
    }

    private val _leftData = mutableStateOf(INVALID_DATA)
    val leftData: Int get() = _leftData.value

    private val _rightData = mutableStateOf(INVALID_DATA)
    val rightData: Int get() = _rightData.value

    private var currentJob: Job? = null

    fun onButtonClick(useAsync: Boolean) {
        if(currentJob != null) return

        currentJob = viewModelScope.launch {
            Utils.log(TAG, "==== Corroutina criada no launch do botão ====")

            try {
                val job1 = launch {
                    Utils.log(TAG, "+++++ Sub-coroutine criada à esquerda +++++")
                    loadData(useAsync, start = 0, end = 9, data = _leftData)
                    _leftData.value = DONE_LEFT_DATA
                    Utils.log(TAG, "---Sub-launch à esquerda está finalizado ---")
                }

                val job2 = launch {
                    Utils.log(TAG, "+++++ Sub-coroutine criada à direita +++++")
                    loadData(useAsync, start = 10, end = 19, data = _rightData)
                    _rightData.value = DONE_RIGHT_DATA
                    Utils.log(TAG, "---Sub-launch à direita está finalizado ---")
                }

                job1.join()
                job2.join()
                currentJob = null
                Utils.log(TAG, "---Launch finalizado à esquerda e à direita---")

            } catch (e: Exception) {
                _leftData.value = INVALID_DATA
                _rightData.value = INVALID_DATA
                currentJob = null
                Utils.log(TAG, "---Coroutinas com erro---$e")
            }
        }

    }

    fun onCancelButtonClick() {
        if (currentJob == null) return

        viewModelScope.launch() {
            Utils.log(TAG, "---Corroutine cancelada---")
            currentJob!!.cancelAndJoin()
            Utils.log(TAG, "---Botão de cancelar coroutinas executado---")
        }
    }

    private suspend fun loadData(
        useAsync : Boolean,
        start: Int,
        end: Int,
        data: MutableState<Int>
    ) {
        withContext(dispatcher) {
            Utils.log(TAG, "Thread $dispatcher")
            for(index in start..end) {

                if (useAsync) {
                    val deferred = async {
                        Utils.log(TAG, "+++++++ Coroutine Async criada  - getData($index) +++++++")
                        getData(index)
                    }
                    data.value = deferred.await()

                } else {
                    data.value = getData(index)
                }
            }
        }
    }

    private suspend fun getData(input: Int) : Int {
        simulateLongRunningTask()
        return input
    }

    private suspend fun simulateLongRunningTask() {
        simulateBlockingThreadTask()
        simulateNonBlockingThreadTask()
    }

    private suspend fun simulateBlockingThreadTask() {
        repeat(10) {
            Thread.sleep(20)
            yield()
        }
    }

    private suspend fun simulateNonBlockingThreadTask() {
        delay(200)
    }
}
