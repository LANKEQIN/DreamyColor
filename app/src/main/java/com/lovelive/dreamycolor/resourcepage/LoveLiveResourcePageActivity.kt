package com.lovelive.dreamycolor.resourcepage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.lovelive.dreamycolor.R

class LoveLiveResourcePageActivity : AppCompatActivity() {

    private var fragment: LoveLiveResourcePageFragment? = null

    companion object {
        const val TAG = "LoveLiveResourceActivity"
    }

    override fun getIntent(): Intent {
        return super.getIntent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lovelive_resource_root_layout)
        handleIntent(savedInstanceState)
    }

    private fun handleIntent(savedInstanceState: Bundle?) {
        if (intent != null) {

            attachFragment()

        } else {
            finish("invalid intent")
        }
    }

    private fun attachFragment() {

        try {
            fragment = LoveLiveResourcePageFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.lovelive_resource_page_container, fragment!!)
                .commitAllowingStateLoss()
        } catch(e: Exception) {
            finish("fail to attachFragment")
        }

    }

    private fun finish(msg: String?) {
        Log.w(TAG, msg ?: "unknown finish")
        finish()
    }
}