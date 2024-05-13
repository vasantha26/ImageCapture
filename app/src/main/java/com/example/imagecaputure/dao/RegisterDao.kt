package com.example.imagecaputure.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.imagecaputure.model.DataModel

@Dao
interface RegisterDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTask(task: DataModel)

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): LiveData<List<DataModel>>
}