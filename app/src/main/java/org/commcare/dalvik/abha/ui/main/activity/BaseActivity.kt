package org.commcare.dalvik.abha.ui.main.activity

import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import org.commcare.dalvik.abha.R

abstract class BaseActivity<B : ViewBinding>(val bindingInflater: (layoutInflater: LayoutInflater) -> B) :
    AppCompatActivity() {

    var mBinding: B? = null

    lateinit var navController: NavController


    val binding: B
        get() = mBinding as B


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = bindingInflater.invoke(LayoutInflater.from(this))
        if (mBinding == null) {
            throw IllegalAccessException()
        }
        setContentView(binding.root)

    }


    override fun onSupportNavigateUp(): Boolean {
        val navController =
            Navigation.findNavController(this, getNavHostId())

        return if (navController.navigateUp() || super.onSupportNavigateUp())
            true
        else {
            finish()
            true
        }
    }

    abstract fun getNavHostId(): Int

}