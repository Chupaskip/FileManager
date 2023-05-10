package com.example.filemanager

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.*
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filemanager.databinding.FragmentFilesBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File

class FilesFragment : Fragment(), FileAdapter.FileClickListener {
    private var _binding: FragmentFilesBinding? = null
    private val binding get() = _binding!!
    private lateinit var fileAdapter: FileAdapter
    private lateinit var mainActivity: MainActivity
    private val args: FilesFragmentArgs by navArgs()
    private lateinit var files: List<File>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentFilesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = requireActivity() as MainActivity
        fileAdapter = FileAdapter(this)
        binding.rvFiles.apply {
            adapter = fileAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
        files = if (args.directoryPath == "") {
            getFiles()
        } else
            getFiles(File(args.directoryPath))
        if (files.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
        }
        fileAdapter.submitList(files.sortedBy { file -> file.name })
        setupMenu()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : androidx.core.view.MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menu_sort_date -> {
                        if (menuItem.title == "Sort by date ↑") {
                            menuItem.title = "Sort by date ↓"
                            files = files.sortedBy { file -> file.lastModified() }
                        } else {
                            menuItem.title = "Sort by date ↑"
                            files = files.sortedByDescending { file -> file.lastModified() }
                        }
                    }
                    R.id.menu_sort_name -> {
                        if (menuItem.title == "Sort by name ↑") {
                            menuItem.title = "Sort by name ↓"
                            files = files.sortedBy { file -> file.name }
                        } else {
                            menuItem.title = "Sort by name ↑"
                            files = files.sortedByDescending { file -> file.name }
                        }
                    }
                    R.id.menu_sort_size -> {
                        if (menuItem.title == "Sort by size ↑") {
                            menuItem.title = "Sort by size ↓"
                            files = files.sortedBy { file -> file.length() }
                        } else {
                            menuItem.title = "Sort by size ↑"
                            files = files.sortedByDescending { file -> file.length() }
                        }
                    }
                    R.id.menu_sort_extension -> {
                        if (menuItem.title == "Sort by extension ↑") {
                            menuItem.title = "Sort by extension ↓"
                            files = files.sortedBy { file -> file.extension }
                        } else {
                            menuItem.title = "Sort by extension ↑"
                            files = files.sortedByDescending { file -> file.extension }
                        }
                    }
                    R.id.show_hide_updated_files -> {
                        if (menuItem.title == "Updated files") {
                            mainActivity.updatedFiles.also {
                                if (it.isEmpty()) {
                                    binding.tvEmpty.visibility = View.VISIBLE
                                    binding.tvEmpty.text =
                                        "Нет измененных файлов с последнего запуска"
                                }
                                files = mainActivity.updatedFiles
                            }
                            menuItem.title = "All files"
                        } else {
                            binding.tvEmpty.visibility = View.GONE
                            binding.tvEmpty.text =
                                "Пустая папка"
                            files = if (args.directoryPath == "") {
                                getFiles()
                            } else
                                getFiles(File(args.directoryPath))
                            if (files.isEmpty()) {
                                binding.tvEmpty.visibility = View.VISIBLE
                            }

                            menuItem.title = "Updated files"
                        }
                    }
                }
                if (menuItem.itemId != R.id.menu_sort)
                    fileAdapter.submitList(files) {
                        binding.rvFiles.scrollToPosition(0)
                    }
                return true
            }

        }, viewLifecycleOwner)
    }

    private fun getFiles(root: File = File(Environment.getExternalStorageDirectory().absolutePath)): List<File> {
        val files = mutableListOf<File>()
        if (root.isDirectory) {
            if (!root.name.startsWith('.') && root.absolutePath != args.directoryPath)
                files.add(root)
            val filesDir = root.listFiles() ?: emptyArray()
            for (file in filesDir) {
                if (!file.name.startsWith('.'))
                    files.add(file)
            }
        }
        return files.toList()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onFileClick(file: File, isLongClick: Boolean) {
        if (!file.isDirectory && isLongClick) {
            val snackbar = Snackbar.make(requireView(), "File is selected", Snackbar.LENGTH_LONG)
            snackbar.setAction("Share") {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "application/*"
                val uri = FileProvider.getUriForFile(requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file)
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                val chooserIntent = Intent.createChooser(shareIntent, "Share file")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                requireContext().startActivity(chooserIntent)
            }
            snackbar.show()
        }
        if (!file.isDirectory || file.absolutePath == args.directoryPath)
            return
        val action = FilesFragmentDirections.actionFilesFragmentSelf(file.absolutePath)
        findNavController().navigate(action)
    }

}