package com.tecorb.tecorbcountrycodepicker

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView

class TecOrbCountryCodeDialog(private val mCountryCodePicker: TecOrbCountryCodePicker) : Dialog(mCountryCodePicker.context) {

    companion object {
        private const val TAG = "CountryCodeDialog"
    }

    private lateinit var mEdtSearch: EditText
    private lateinit var mTvNoResult: TextView
    private lateinit var mTvTitle: TextView
    private lateinit var mLvCountryDialog: ListView
    private lateinit var mRlyDialog: RelativeLayout

    private lateinit var masterCountries: List<TecOrbCountry>
    private lateinit var mFilteredCountries: List<TecOrbCountry>
    private lateinit var mInputMethodManager: InputMethodManager
    private lateinit var mArrayAdapter: TecOrbCountryCodeArrayAdapter
    private var mTempCountries: MutableList<TecOrbCountry>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_picker_dialog)
        setupUI()
        setupData()
    }

    private fun setupUI() {
        mRlyDialog = findViewById(R.id.dialog_rly)
        mLvCountryDialog = findViewById(R.id.country_dialog_lv)
        mTvTitle = findViewById(R.id.title_tv)
        mEdtSearch = findViewById(R.id.search_edt)
        mTvNoResult = findViewById(R.id.no_result_tv)
    }

    private fun setupData() {
        mCountryCodePicker.typeFace?.let { typeface ->
            mTvTitle.typeface = typeface
            mEdtSearch.typeface = typeface
            mTvNoResult.typeface = typeface
        }

        if (mCountryCodePicker.getBackgroundColor() != TecOrbCountryCodePicker.defaultBackgroundColor) {
            mRlyDialog.setBackgroundColor(mCountryCodePicker.getBackgroundColor())
        }

        if (mCountryCodePicker.dialogTextColor != TecOrbCountryCodePicker.defaultBackgroundColor) {
            val color = mCountryCodePicker.dialogTextColor
            mTvTitle.setTextColor(color)
            mTvNoResult.setTextColor(color)
            mEdtSearch.setTextColor(color)
            mEdtSearch.setHintTextColor(adjustAlpha(color, 0.7f))
        }

        mCountryCodePicker.refreshCustomMasterList()
        mCountryCodePicker.refreshPreferredCountries()
        masterCountries = mCountryCodePicker!!.getCustomCountries(mCountryCodePicker)

        mFilteredCountries = getFilteredCountries()
        setupListView(mLvCountryDialog)

        val ctx = mCountryCodePicker.context
        mInputMethodManager = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setSearchBar()
    }

    private fun setupListView(listView: ListView) {
        mArrayAdapter = TecOrbCountryCodeArrayAdapter(context, mFilteredCountries, mCountryCodePicker)

        if (!mCountryCodePicker.isSelectionDialogShowSearch) {
            val params = listView.layoutParams as RelativeLayout.LayoutParams
            params.height =LayoutParams.WRAP_CONTENT
            listView.layoutParams = params
        }

        val listener = AdapterView.OnItemClickListener { parent, view, position, id ->
            if (mFilteredCountries == null) {
                Log.e(TAG, "No filtered countries found! This should not happen, please report!")
                return@OnItemClickListener
            }

            if (mFilteredCountries.size <= position || position < 0) {
                Log.e(TAG, "Something wrong with the ListView. Please report this!")
                return@OnItemClickListener
            }

            val country = mFilteredCountries[position]
            if (country == null) return@OnItemClickListener
            mCountryCodePicker!!.selectedCountry =country
            mInputMethodManager.hideSoftInputFromWindow(mEdtSearch.windowToken, 0)
            dismiss()
        }
        listView.onItemClickListener = listener
        listView.adapter = mArrayAdapter
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun setSearchBar() {
        if (mCountryCodePicker.isSelectionDialogShowSearch) {
            setTextWatcher()
        } else {
            mEdtSearch.visibility = View.GONE
        }
    }

    private fun setTextWatcher() {
        mEdtSearch?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyQuery(s.toString())
            }
        })

        if (mCountryCodePicker.isKeyboardAutoPopOnSearch) {
            mInputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    private fun applyQuery(query: String) {
        mTvNoResult.visibility = View.GONE
        var queryLower = query.lowercase()

        if (queryLower.isNotEmpty() && queryLower[0] == '+') {
            queryLower = queryLower.substring(1)
        }

        mFilteredCountries = getFilteredCountries(queryLower)

        if (mFilteredCountries.isEmpty()) {
            mTvNoResult.visibility = View.VISIBLE
        }

        mArrayAdapter.notifyDataSetChanged()
    }

    private fun getFilteredCountries(): List<TecOrbCountry> {
        return getFilteredCountries("")
    }

    private fun getFilteredCountries(query: String): List<TecOrbCountry> {
        if (mTempCountries == null) {
            mTempCountries = ArrayList()
        } else {
            mTempCountries!!.clear()
        }

        val preferredCountries = mCountryCodePicker.preferredCountries
        if (preferredCountries != null && preferredCountries.isNotEmpty()) {
            for (country in preferredCountries) {
                if (country!!.isEligibleForQuery(query)) {
                    mTempCountries!!.add(country)
                }
            }

            if (mTempCountries!!.isNotEmpty()) {
                mTempCountries!!.add(null!!) // Add separator for preferred countries
            }
        }

        for (country in masterCountries) {
            if (country.isEligibleForQuery(query)) {
                mTempCountries!!.add(country)
            }
        }
        return mTempCountries!!
    }
}
