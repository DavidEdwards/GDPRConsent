package dae.gdprconsent

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_gdpr_main.*

class ConsentActivity : AppCompatActivity() {

    private lateinit var adapter: ConsentPagerAdapter
    private lateinit var viewModel: ConsentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = intent.getIntExtra(Constants.KEY_THEME, R.style.GDPR_AppTheme_NoActionBar)
        if (theme != 0)
            setTheme(theme)
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ConsentViewModel::class.java)

        setContentView(R.layout.activity_gdpr_main)
        
        try {
            setSupportActionBar(toolbar)
        }
        catch (invalidTheme: IllegalStateException){
            throw IllegalStateException("Invalid theme! Theme must be a NoActionBar theme")
        }
        adapter = ConsentPagerAdapter(supportFragmentManager)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        container.adapter = adapter

        previous.setOnClickListener { _ ->
            previous()
        }

        next.setOnClickListener { _ ->
            next()
        }

        container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                refreshFABs()
            }
        })

        val mode = intent.getIntExtra(Constants.KEY_MODE, Constants.SHOW_MODES.MODE_SHOW_NOT_SEEN.modeID)
        var quickMode = mode == Constants.SHOW_MODES.MODE_SHOW_IF_REQUIRED.modeID || mode == Constants.SHOW_MODES.MODE_SHOW_NOT_SEEN.modeID
        val showNotSeen = mode == Constants.SHOW_MODES.MODE_SHOW_NOT_SEEN.modeID

        val dataset: ArrayList<ConsentRequest> = intent.extras.getParcelableArrayList(Constants.CONSENT_REQUESTS)
        for (item in dataset) {
            item.load(this)

            if (quickMode) {
                if (!item.isSeen) {
                    quickMode = false
                } else if (!item.isConsented && item.isRequired) {
                    quickMode = false
                }
            }
        }

        if (savedInstanceState == null) {
            if(showNotSeen) {
                val it = dataset.iterator()
                while(it.hasNext()) {
                    val request = it.next()
                    if(request.isSeen && (request.isConsented || !request.isRequired)) {
                        it.remove()
                    }
                }
            }

            viewModel.addConsentRequests(dataset)

            container.adapter?.notifyDataSetChanged()
            refreshFABs()
        }

        if (quickMode || dataset.size == 0) {
            testConsent()
            return
        } else {
            coordinator.visibility = View.VISIBLE
        }
    }

    private fun previous() {
        if (container.currentItem != 0) {
            container.currentItem = container.currentItem - 1
        }
    }

    private fun next() {
        val f = adapter.currentFragment as ConsentRequestDetailFragment
        if (container.currentItem == viewModel.consentRequests.size - 1) {
            testConsent()
        } else {
            if (!f.request.isConsented && f.binding.contentScroll.canScrollVertically(1)) {
                f.binding.contentScroll.smoothScrollBy(0, f.binding.contentScroll.height / 2)
            } else {
                container.currentItem = container.currentItem + 1
            }
        }
    }

    private fun testConsent() {
        val list = viewModel.consentRequests

        for (i in 0 until list.size) {
            if (list[i].isRequired && !list[i].isConsented) {
                coordinator.visibility = View.VISIBLE
                container.setCurrentItem(i, true)
                Snackbar.make(container, getString(R.string.gdpr_snack_required), Snackbar.LENGTH_LONG).show()
                return
            } else if (!list[i].isSeen) {
                coordinator.visibility = View.VISIBLE
                container.setCurrentItem(i, true)
                Snackbar.make(container, getString(R.string.gdpr_snack_new_consent), Snackbar.LENGTH_LONG).show()
                return
            }
        }

        done()
    }

    override fun onBackPressed() {
        val list = viewModel.consentRequests

        for (i in 0 until list.size) {
            if (list[i].isRequired && !list[i].isConsented) {
                setResult(Activity.RESULT_CANCELED)
                finish()
                return
            } else if (!list[i].isSeen) {
                setResult(Activity.RESULT_CANCELED)
                finish()
                return
            }
        }

        done()
    }

    private fun refreshFABs() {
        when {
            container.currentItem == viewModel.consentRequests.size - 1 -> {
                previous.visibility = if(viewModel.consentRequests.size > 1) View.VISIBLE else View.INVISIBLE
                next.setImageResource(R.drawable.ic_check_all_white_48dp)
            }
            container.currentItem == 0 -> {
                previous.visibility = View.INVISIBLE
                next.setImageResource(R.drawable.ic_chevron_right_white_48dp)
            }
            else -> {
                previous.visibility = View.VISIBLE
                next.setImageResource(R.drawable.ic_chevron_right_white_48dp)
            }
        }
    }

    private fun done() {
        val list = ArrayList<ConsentRequest>()

        val prefs = getSharedPreferences(Constants.PREF_GDPR, Context.MODE_PRIVATE)
        for(( key, _ ) in prefs.all) {
            if(key.startsWith("gdpr.")) {
                val request = ConsentRequest(key.substring(5))
                request.load(prefs)
                list.add(request)
            }
        }

        val intent = Intent()
        intent.putParcelableArrayListExtra(Constants.CONSENT_REQUESTS, list)
        ConsentHelper.populate(list)

        var isChanged = false
        val visibleList = viewModel.consentRequests
        for (item in visibleList) {
            if (item.hasChanged()) {
                isChanged = true
                break
            }
        }

        intent.putExtra(Constants.KEY_CONSENT_CHANGED, isChanged)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    inner class ConsentPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private var firstRun = true
        private lateinit var mCurrentFragment: Fragment

        val currentFragment: Fragment
            get() = mCurrentFragment

        override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
            if (firstRun || mCurrentFragment != obj) {
                firstRun = false
                mCurrentFragment = obj as Fragment
            }
            super.setPrimaryItem(container, position, obj)
        }

        override fun getItem(position: Int): Fragment {
            return ConsentRequestDetailFragment.newInstance(viewModel.consentRequests[position])
        }

        override fun getCount(): Int {
            return viewModel.consentRequests.size
        }
    }
}
