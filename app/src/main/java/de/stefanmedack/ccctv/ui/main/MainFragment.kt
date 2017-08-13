package de.stefanmedack.ccctv.ui.main

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v17.leanback.app.BackgroundManager
import android.support.v17.leanback.app.BrowseFragment
import android.support.v17.leanback.app.BrowseSupportFragment
import android.support.v17.leanback.widget.*
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.widget.Toast
import dagger.android.support.AndroidSupportInjection
import de.stefanmedack.ccctv.R
import de.stefanmedack.ccctv.util.plusAssign
import info.metadude.kotlin.library.c3media.models.Conference
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class MainFragment : BrowseSupportFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainViewModel

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity, viewModelFactory).get(MainViewModel::class.java)
        setupUi()
    }

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }

    private fun setupUi() {
        headersState = BrowseFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = ContextCompat.getColor(activity, R.color.fastlane_background)
        title = getString(R.string.browse_title)
        setOnSearchClickedListener {
            Toast.makeText(
                    activity, "implement Search", Toast.LENGTH_SHORT)
                    .show()
        }
        prepareEntranceTransition()

        disposable.add(viewModel.getConferences()
                .subscribeBy(// named arguments for lambda Subscribers
                        onSuccess = { render(it) },
                        // TODO proper error handling
                        onError = { it.printStackTrace() }
                ))
    }

    private fun render(mappedConferences: Map<String, List<Conference>>) {
        adapter = ArrayObjectAdapter(ListRowPresenter())
        (adapter as ArrayObjectAdapter) += mappedConferences
                .map { PageRow(HeaderItem(it.key)) }

        BackgroundManager.getInstance(activity).let {
            it.attach(activity.window)
            mainFragmentRegistry.registerFragment(PageRow::class.java,
                    PageRowFragmentFactory(it))
        }

        startEntranceTransition()
    }

    private class PageRowFragmentFactory internal constructor(
            private val backgroundManager: BackgroundManager
    ) : BrowseSupportFragment.FragmentFactory<Fragment>() {

        override fun createFragment(rowObj: Any): Fragment {
            val row = rowObj as Row
            backgroundManager.drawable = null
            return ConferenceGroupDetailFragment.create(row.headerItem.name)
        }
    }
}
