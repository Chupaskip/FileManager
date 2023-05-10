package com.example.filemanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.filemanager.databinding.ItemFileBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileAdapter(private val listener: FileClickListener) :
    ListAdapter<File, FileAdapter.FileViewHolder>(diffUtil) {

    inner class FileViewHolder(private val binding: ItemFileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File) {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = dateFormat.format(Date(file.lastModified()))
            binding.tvDateFile.text = date
            binding.tvNameFile.text = file.name
            @SuppressLint("SetTextI18n")
            if (!file.isDirectory) {
                binding.tvSizeFile.text =
                    "${String.format("%.2f", file.length() / 1024 / 1024.toDouble())} MB"
                binding.root.setOnClickListener {
                    openFile(file, "*/*")
                }
            } else {
                binding.root.setOnClickListener {
                    listener.onFileClick(file)
                }
                binding.ivFile.setImageResource(R.drawable.ic_directory)
            }
            when (file.extension) {
                "mp3" -> {
                    binding.ivFile.setImageResource(R.drawable.ic_music)
                    binding.root.setOnClickListener {
                        openFile(file, "audio/*")
                    }
                }
                "pdf" -> {
                    binding.ivFile.setImageResource(R.drawable.ic_pdf)
                    binding.root.setOnClickListener {
                        openFile(file, "application/pdf")
                    }
                }
                "txt" -> {
                    binding.ivFile.setImageResource(R.drawable.ic_text)
                    binding.root.setOnClickListener {
                        openFile(file, "text/plain")
                    }
                }
                "jpg", "jpeg" -> {
                    binding.ivFile.setImageResource(R.drawable.ic_jpg)
                    binding.root.setOnClickListener {
                        openFile(file, "image/jpeg")
                    }
                }
                "png" -> {
                    binding.ivFile.setImageResource(R.drawable.ic_png)
                    binding.root.setOnClickListener {
                        openFile(file, "image/png")
                    }
                }
            }
            binding.root.setOnLongClickListener {
                listener.onFileClick(file, true)
                true
            }
        }

        private fun openFile(file: File, type: String) {
            val uri =
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, type)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivity(context, intent, null)
        }

    }


    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<File>() {
            override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
                return oldItem == newItem
            }
        }
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        context = parent.context
        return FileViewHolder(ItemFileBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface FileClickListener {
        fun onFileClick(file: File, isLongClick: Boolean = false)
    }
}