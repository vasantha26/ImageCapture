package com.example.imagecaputure.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.imagecaputure.model.DataModel
import com.example.imagecaputure.repository.RegRepository


class RegViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RegRepository = RegRepository(application)
    private var regResponse: LiveData<List<DataModel>> = repository.getAllTasks()


    fun insertTask(task: DataModel) {
        repository.insertTask(task)
    }

    fun getAllRegResponse(): LiveData<List<DataModel>> {
        return regResponse
    }
}