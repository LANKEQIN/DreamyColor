package com.lovelive.dreamycolor.resourcepage

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lovelive.dreamycolor.R

class LoveLiveResourcePageFragment : Fragment() {

    companion object {
        fun newInstance() = LoveLiveResourcePageFragment()
    }

    private val viewModel: LoveLiveResourcePageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.lovelive_resouce_page_common_container, container, false)
    }
}