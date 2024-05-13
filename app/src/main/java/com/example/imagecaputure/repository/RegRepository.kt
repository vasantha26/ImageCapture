package com.example.imagecaputure.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.imagecaputure.dao.AppDatabase
import com.example.imagecaputure.model.DataModel
import com.example.imagecaputure.dao.RegisterDao
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegRepository(application: Application) {

    private var registerDao: RegisterDao
    private val database: AppDatabase = AppDatabase.getInstance(application)
    private var regResponseList: LiveData<List<DataModel>>

    init {
        registerDao = database.registerDao()
        regResponseList = registerDao.getAllTasks()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun insertTask(task: DataModel) {
        GlobalScope.launch(Dispatchers.Default) {
            registerDao.insertTask(task)
        }
    }

    fun getAllTasks(): LiveData<List<DataModel>> {
        return regResponseList
    }
}