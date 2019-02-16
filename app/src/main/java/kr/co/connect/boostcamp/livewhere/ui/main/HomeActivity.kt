package kr.co.connect.boostcamp.livewhere.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import io.fabric.sdk.android.services.common.CommonUtils.hideKeyboard
import kotlinx.android.synthetic.main.fragment_search.*
import kr.co.connect.boostcamp.livewhere.BuildConfig
import kr.co.connect.boostcamp.livewhere.R
import kr.co.connect.boostcamp.livewhere.databinding.ActivityHomeBinding
import kr.co.connect.boostcamp.livewhere.ui.map.MapActivity
import kr.co.connect.boostcamp.livewhere.util.APPLICATION_EXIT
import kr.co.connect.boostcamp.livewhere.util.DELETE_RECENT_SEARCH
import kr.co.connect.boostcamp.livewhere.util.EMPTY_STRING_TEXT
import kr.co.connect.boostcamp.livewhere.util.SEARCH_TAG
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val HOME_CONTAINER_ID = R.id.fl_home_frame
    }

    private lateinit var binding: ActivityHomeBinding
    private lateinit var placesClient: PlacesClient
    private val homeViewModel: HomeViewModel by viewModel()
    private val searchViewModel: SearchViewModel by viewModel()
    private lateinit var currentFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        Places.initialize(applicationContext, BuildConfig.GooglePlacesApiKey)
        placesClient = Places.createClient(this)

        if (savedInstanceState == null) {
            startHomeFragment()
        }

        binding.apply {
            homeViewModel = this@HomeActivity.homeViewModel
            searchViewModel = this@HomeActivity.searchViewModel
            lifecycleOwner = this@HomeActivity
        }

        setGoogleClient()
        observeValues()
        initBookmark()
    }

    private fun setGoogleClient() {
        searchViewModel.setClient(placesClient)
    }

    private fun initBookmark() {
        homeViewModel.getBookmark()
    }

    private fun observeValues() {
        homeViewModel.searchBtnClicked.observe(this, Observer {
            startSearchFragment()
        })

        homeViewModel.sendAddress.observe(this, Observer {
            startMapActivity(homeViewModel.sendAddress.value)
        })

        searchViewModel.backBtnClicked.observe(this, Observer {
            hideKeyboard()
            startHomeFragment()
        })

        searchViewModel.mapBtnClicked.observe(this, Observer {
            startMapActivity()
        })

        searchViewModel.searchText.observe(this, Observer {
            if(searchViewModel.searchText.value.isNullOrEmpty()) {
                Toast.makeText(this, EMPTY_STRING_TEXT, Toast.LENGTH_LONG).show()
            } else {
                if(currentFragment.et_search_bar.text.toString() == searchViewModel.searchText.toString()) {
                    if(searchViewModel.autoCompleteList.value.isNullOrEmpty()) {
                        startMapActivity(searchViewModel.autoCompleteList.value!![0])
                    }
                } else {
                    startMapActivity(searchViewModel.searchText.value)
                }
            }
        })

        searchViewModel.showToast.observe(this, Observer {
            if(it) {
                Toast.makeText(this, DELETE_RECENT_SEARCH, Toast.LENGTH_LONG).show()
                searchViewModel.setToastDone()
            }
        })
    }

    private fun hideKeyboard() {
        currentFragment.et_search_bar.inputType = 0
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFragment.et_search_bar.windowToken, 0)
    }

    private fun startHomeFragment(){
        currentFragment = HomeFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .replace(HOME_CONTAINER_ID, currentFragment)
            .commit()
    }

    private fun startSearchFragment() {
        currentFragment = SearchFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .replace(HOME_CONTAINER_ID, currentFragment)
            .commit()
        searchViewModel.setVisibility()
    }

    private fun startMapActivity() {
        intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    private fun startMapActivity(text: String?) {
        if(text != null) {
            intent = Intent(this, MapActivity::class.java)
            intent.putExtra(SEARCH_TAG, text)
            startActivity(intent)
        } else {
            startMapActivity()
        }
    }

    override fun onBackPressed() {
        val toast = Toast.makeText(this, APPLICATION_EXIT, Toast.LENGTH_LONG)
        if(currentFragment is SearchFragment) {
            startHomeFragment()
        } else {
            if(homeViewModel.onBackPressed()) {
                toast.show()
            } else {
                finish()
                toast.cancel()
            }
        }
    }
}