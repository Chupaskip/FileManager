package com.example.filemanager

import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.example.filemanager.database.FileDatabase
import com.example.filemanager.database.toFile
import com.example.filemanager.database.toFileEntity
import com.example.filemanager.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private var readPermissionGranted = false
    lateinit var manageExternalStorageLauncher: ActivityResultLauncher<Intent>
    lateinit var readExternalStorageLauncher: ActivityResultLauncher<String>
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var fileDataBase: FileDatabase
    val updatedFiles = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileDataBase = FileDatabase(this)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        @RequiresApi(Build.VERSION_CODES.R)
        manageExternalStorageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                isPermissionGranted(Environment.isExternalStorageManager())
            }

        readExternalStorageLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                isPermissionGranted(isGranted)
            }
        requestPermission()
        if (readPermissionGranted) {
            isPermissionGranted(true)
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val savedFiles = fileDataBase.getFileDao().getSavedFiles()
            for (savedFile in savedFiles) {
                val file = savedFile.toFile()
                if (file.exists() && file.lastModified() > savedFile.lastModified) {
                    updatedFiles.add(file)
                }
            }
            saveFiles()
        }
    }

    private fun isPermissionGranted(isGranted: Boolean) {
        if (isGranted) {
            val currentDestination = navController.currentDestination
            if (currentDestination != null && currentDestination.id == R.id.filesFragment) {
                return
            }
            val action =
                PermissionFragmentDirections.actionPermissionFragmentToFilesFragment()
            navController.navigate(action)
        } else {
            Toast.makeText(this,
                "Нужно разрешение для просмотра файлов",
                Toast.LENGTH_SHORT).show()
            requestPermission()
        }
    }

    private fun requestPermission() {
        setInfoAboutPermissions()
        if (!readPermissionGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                val intent =
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                manageExternalStorageLauncher.launch(intent)
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                readExternalStorageLauncher.launch(READ_EXTERNAL_STORAGE)
            }
        }
        setInfoAboutPermissions()
    }

    private fun setInfoAboutPermissions() {
        val hasReadPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else (ContextCompat.checkSelfPermission(applicationContext,
            READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        readPermissionGranted = hasReadPermission
    }

    private fun saveFiles(root: File = File(Environment.getExternalStorageDirectory().absolutePath)) {
        lifecycleScope.launch {
            if (root.isDirectory) {
                if (!root.name.startsWith('.'))
                    Log.d("FileManager", root.name)
                val files = root.listFiles() ?: emptyArray()
                for (file in files) {
                    if (file.isDirectory) {
                        saveFiles(file)
                    } else {
                        if (!file.name.startsWith('.')) {
                            fileDataBase.getFileDao().insertFile(file.toFileEntity())
                            Log.d("FileFrom${root.name}",
                                "${file.hashCode()} ${file.absolutePath}")
                        }
                    }
                }
            }
        }
    }
}