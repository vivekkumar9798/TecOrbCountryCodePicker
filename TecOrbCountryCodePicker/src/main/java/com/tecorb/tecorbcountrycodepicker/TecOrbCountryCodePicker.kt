package com.tecorb.tecorbcountrycodepicker

import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.TelephonyManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import java.util.Locale
import java.util.TimeZone
class TecOrbCountryCodePicker : RelativeLayout {
        private val DEFAULT_COUNTRY: String? = Locale.getDefault().country
        private var mBackgroundColor = defaultBackgroundColor

        private var mDefaultCountryCode = 0
        private var mDefaultCountryNameCode: String? = null

        //Util
        private var mPhoneUtil: PhoneNumberUtil? = null
        private var mPhoneNumberWatcher: PhoneNumberWatcher? = null
        var mPhoneNumberInputValidityListener:PhoneNumberInputValidityListener? = null
        private var mTvSelectedCountry: TextView? = null
        private var mRegisteredPhoneNumberTextView: TextView? = null
        private var mRlyHolder: RelativeLayout? = null
        private var mImvArrow: ImageView? = null
        private var mImvFlag: ImageView? = null
        private var mLlyFlagHolder: LinearLayout? = null
        private var mSelectedCountry: TecOrbCountry? = null
        private var mDefaultCountry: TecOrbCountry? = null
        private var mRlyClickConsumer: RelativeLayout? = null

        @get:Suppress("unused")
        private var countryCodeHolderClickListener: OnClickListener? = null

        private var mHideNameCode = false
        private var mShowFlag = true
        private var mShowFullName = false
        private val mUseFullName = false

          @set:Suppress("unused")
        var isSelectionDialogShowSearch: Boolean = true

        private var mPreferredCountries: List<TecOrbCountry>? = null

        private var mCountryPreference: String? = null
        private var mCustomMasterCountriesList: List<TecOrbCountry>? = null
     var customMasterCountries: String? = null
        var isKeyboardAutoPopOnSearch: Boolean = true
        private var mIsClickable = true
        private var mCountryCodeDialog: TecOrbCountryCodeDialog? = null
        private var mHidePhoneCode = false
        private var mTextColor = defaultContentColor
        var dialogTextColor: Int = defaultContentColor

        private var mTypeFace: Typeface? = null


        var isHintEnabled: Boolean = true
            private set

        var isPhoneAutoFormatterEnabled: Boolean = true
            private set

        private var mSetCountryByTimeZone = true

        private var mOnCountryChangeListener: OnCountryChangeListener? =null

        interface OnCountryChangeListener {
            fun onCountrySelected(selectedCountry: TecOrbCountry?)
        }

        interface PhoneNumberInputValidityListener {
            fun onFinish(ccp: TecOrbCountryCodePicker?, isValid: Boolean)
        }

        constructor(context: Context?) : super(context) {
            if (!isInEditMode) init(null)
        }

        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
            if (!isInEditMode) init(attrs)
        }

        constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
            if (!isInEditMode) init(attrs)
        }

        constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int, ) : super(context, attrs, defStyleAttr, defStyleRes) {
            if (!isInEditMode) init(attrs)
        }

        private fun init(attrs: AttributeSet?) {
            inflate(context, R.layout.layout_code_picker, this)

            mTvSelectedCountry = findViewById(R.id.selected_country_tv)
            mRlyHolder = findViewById(R.id.country_code_holder_rly)
            mImvArrow = findViewById(R.id.arrow_imv)
            mImvFlag = findViewById(R.id.flag_imv)
            mLlyFlagHolder = findViewById(R.id.flag_holder_lly)
            mRlyClickConsumer = findViewById(R.id.click_consumer_rly)

            applyCustomProperty(attrs)

            countryCodeHolderClickListener = OnClickListener {
                if (isClickable) {
                    if (mCountryCodeDialog == null) {
                        mCountryCodeDialog = TecOrbCountryCodeDialog(this@TecOrbCountryCodePicker)
                    }

                    mCountryCodeDialog!!.show()
                }
            }

            mRlyClickConsumer!!.setOnClickListener(countryCodeHolderClickListener)
        }

        private fun applyCustomProperty(attrs: AttributeSet?) {
            mPhoneUtil = PhoneNumberUtil.createInstance(context)
            val theme = context.theme
            val a = theme.obtainStyledAttributes(attrs, R.styleable.CountryCodePicker, 0, 0)

            try {
                mHidePhoneCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hidePhoneCode, false)
                mShowFullName = a.getBoolean(R.styleable.CountryCodePicker_ccp_showFullName, false)
                mHideNameCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hideNameCode, false)
                isHintEnabled = a.getBoolean(R.styleable.CountryCodePicker_ccp_enableHint, true)
                isPhoneAutoFormatterEnabled =a.getBoolean(R.styleable.CountryCodePicker_ccp_enablePhoneAutoFormatter, true)
                isKeyboardAutoPopOnSearch = a.getBoolean(R.styleable.CountryCodePicker_ccp_keyboardAutoPopOnSearch, true)
                customMasterCountries = a.getString(R.styleable.CountryCodePicker_ccp_customMasterCountries)
                refreshCustomMasterList()
                mCountryPreference = a.getString(R.styleable.CountryCodePicker_ccp_countryPreference)
                refreshPreferredCountries()
                applyCustomPropertyOfDefaultCountryNameCode(a)
                showFlag(a.getBoolean(R.styleable.CountryCodePicker_ccp_showFlag, true))
                applyCustomPropertyOfColor(a)
                val fontPath = a.getString(R.styleable.CountryCodePicker_ccp_textFont)
                if (fontPath != null && !fontPath.isEmpty()) setTypeFace(fontPath)
                val textSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_textSize, 0)
                if (textSize > 0) {
                    mTvSelectedCountry!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
                    setFlagSize(textSize)
                    setArrowSize(textSize)
                } else {
                    val dm = context.resources.displayMetrics
                    val defaultSize = Math.round(18 * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT))
                    setTextSize(defaultSize)
                }
                val arrowSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_arrowSize, 0)
                if (arrowSize > 0) setArrowSize(arrowSize)

                isSelectionDialogShowSearch = a.getBoolean(R.styleable.CountryCodePicker_ccp_selectionDialogShowSearch, true)
                isClickable = a.getBoolean(R.styleable.CountryCodePicker_ccp_clickable, true)

                mSetCountryByTimeZone = a.getBoolean(R.styleable.CountryCodePicker_ccp_setCountryByTimeZone, true)
                if (mDefaultCountryNameCode == null || mDefaultCountryNameCode!!.isEmpty()) {
                    setDefaultCountryFlagAndCode()
                }
            } catch (e: Exception) {
                Log.d(TAG, "exception = $e")
                if (isInEditMode) {
                    mTvSelectedCountry!!.text = context.getString(R.string.phone_code,context.getString(R.string.country_indonesia_number))
                } else {
                    mTvSelectedCountry!!.text = e.message
                }
            } finally {
                a.recycle()
            }
        }

        private fun applyCustomPropertyOfDefaultCountryNameCode(tar: TypedArray) {
            //default country
            mDefaultCountryNameCode = tar.getString(R.styleable.CountryCodePicker_ccp_defaultNameCode)
            if (mDefaultCountryNameCode == null || mDefaultCountryNameCode!!.isEmpty()) return

            if (mDefaultCountryNameCode!!.trim { it <= ' ' }.isEmpty()) {
                mDefaultCountryNameCode = null
                return
            }

            setDefaultCountryUsingNameCode(mDefaultCountryNameCode)
            selectedCountry = mDefaultCountry
        }

        private fun applyCustomPropertyOfColor(arr: TypedArray) {
            val textColor: Int
            textColor = if (isInEditMode) {
                arr.getColor(R.styleable.CountryCodePicker_ccp_textColor, defaultContentColor)
            } else {
                arr.getColor(R.styleable.CountryCodePicker_ccp_textColor, getColor(context, R.color.defaultTextColor))
            }
            if (textColor != 0) this.textColor = textColor

            dialogTextColor = arr.getColor(R.styleable.CountryCodePicker_ccp_dialogTextColor, defaultContentColor)

            mBackgroundColor = arr.getColor(R.styleable.CountryCodePicker_ccp_backgroundColor, Color.TRANSPARENT)

            if (mBackgroundColor != Color.TRANSPARENT) mRlyHolder!!.setBackgroundColor(
                mBackgroundColor
            )
        }

        private var defaultCountry: TecOrbCountry?
            get() = mDefaultCountry
            private set(defaultCountry) {
                mDefaultCountry = defaultCountry
            }

         var selectedCountry: TecOrbCountry?
            get() = mSelectedCountry
            set(selectedCountry) {
                var selectedCountry: TecOrbCountry? = selectedCountry
                mSelectedCountry = selectedCountry

                val ctx = context

                if (selectedCountry == null) {
                    selectedCountry = TecOrbCountryCodeUtils.getByCode(ctx, mPreferredCountries!!, mDefaultCountryCode)
                }

                if (mRegisteredPhoneNumberTextView != null) {
                    setPhoneNumberWatcherToTextView(
                        mRegisteredPhoneNumberTextView!!,
                        selectedCountry!!.iso.uppercase()
                    )
                }

                val phoneCode: String = selectedCountry!!.phoneCode
                if (mHideNameCode) {
                    mTvSelectedCountry!!.text = ctx.getString(R.string.phone_code, phoneCode)
                } else {
                    if (mShowFullName) {
                        val countryName: String = selectedCountry.name.uppercase()
                        if (mHidePhoneCode) {
                            mTvSelectedCountry!!.text = countryName
                        } else {
                            mTvSelectedCountry!!.text =
                                ctx.getString(
                                    R.string.country_full_name_and_phone_code, countryName,
                                    phoneCode
                                )
                        }
                    } else {
                        val iso: String = selectedCountry.iso.uppercase()
                        if (mHidePhoneCode) {
                            mTvSelectedCountry!!.text = iso
                        } else {
                            mTvSelectedCountry!!.text =
                                ctx.getString(R.string.country_code_and_phone_code, iso, phoneCode)
                        }
                    }
                }

                if (mOnCountryChangeListener != null) {
                    mOnCountryChangeListener!!.onCountrySelected(selectedCountry)
                }

                mImvFlag!!.setImageResource(TecOrbCountryCodeUtils.getFlagDrawableResId(selectedCountry))

                if (isHintEnabled) setPhoneNumberHint()
            }

        fun enablePhoneAutoFormatter(isEnable: Boolean) {
            isPhoneAutoFormatterEnabled = isEnable
            if (isEnable) {
                if (mPhoneNumberWatcher == null) {
                    mPhoneNumberWatcher = PhoneNumberWatcher(
                        selectedCountryNameCode
                    )
                }
            } else {
                mPhoneNumberWatcher = null
            }
        }


        fun refreshPreferredCountries() {
            if (mCountryPreference == null || mCountryPreference!!.length == 0) {
                mPreferredCountries = null
                return
            }

            val localCountryList: MutableList<TecOrbCountry> = ArrayList<TecOrbCountry>()
            for (nameCode in mCountryPreference!!.split(",".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()) {
                val country: TecOrbCountry = TecOrbCountryCodeUtils.getByNameCodeFromCustomCountries(context, mCustomMasterCountriesList, nameCode)!!
                if (country == null) continue
                //to avoid duplicate entry of country
                if (isAlreadyInList(country, localCountryList)) continue
                localCountryList.add(country)
            }

            mPreferredCountries = if (localCountryList.size == 0) {
                null
            } else {
                localCountryList
            }
        }


        fun refreshCustomMasterList() {
            if (customMasterCountries == null || customMasterCountries!!.length == 0) {
                mCustomMasterCountriesList = null
                return
            }

            val localCountries: MutableList<TecOrbCountry> = ArrayList<TecOrbCountry>()
            val split = customMasterCountries!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            for (i in split.indices) {
                val nameCode = split[i]
                val country: TecOrbCountry = TecOrbCountryCodeUtils.getByNameCodeFromAllCountries(context, nameCode) ?: continue
                if (isAlreadyInList(country, localCountries)) continue
                localCountries.add(country)
            }

            mCustomMasterCountriesList = if (localCountries.size == 0) {
                null
            } else {
                localCountries
            }
        }

        val customCountries: List<TecOrbCountry>
            get() = mCustomMasterCountriesList!!


        fun getCustomCountries(codePicker: TecOrbCountryCodePicker): List<TecOrbCountry> {
            codePicker.refreshCustomMasterList()
            return if (codePicker.customCountries == null || codePicker.customCountries!!.size <= 0) {
                TecOrbCountryCodeUtils.getAllCountries(codePicker.context)
            } else {
                codePicker.customCountries
            }
        }

        fun setCustomMasterCountriesList(customMasterCountriesList: List<TecOrbCountry>) {
            mCustomMasterCountriesList = customMasterCountriesList
        }

        val preferredCountries: List<TecOrbCountry?>?
            get() = mPreferredCountries

        private fun isAlreadyInList(country: TecOrbCountry?, countries: List<TecOrbCountry?>?): Boolean {
            if (country == null || countries == null) return false

            for (i in countries.indices) {
                if (countries[i]?.iso.equals(country.iso, ignoreCase = true)) {
                    return true
                }
            }

            return false
        }


        private fun detectCarrierNumber(fullNumber: String?, country: TecOrbCountry?): String? {
            val carrierNumber: String?
            if (country == null || fullNumber == null) {
                carrierNumber = fullNumber
            } else {
                val indexOfCode: Int = fullNumber.indexOf(country.phoneCode)
                if (indexOfCode == -1) {
                    carrierNumber = fullNumber
                } else {
                    carrierNumber =
                        fullNumber.substring(indexOfCode + country.phoneCode.length)
                }
            }
            return carrierNumber
        }

        fun setDefaultCountryUsingPhoneCode(defaultCountryCode: Int) {
            val defaultCountry: TecOrbCountry =
                TecOrbCountryCodeUtils.getByCode(context, mPreferredCountries!!, defaultCountryCode) ?: return
            mDefaultCountryCode = defaultCountryCode
            this.defaultCountry = defaultCountry
        }


        fun setDefaultCountryUsingNameCode(countryIso: String?) {
            val defaultCountry: TecOrbCountry =
                TecOrbCountryCodeUtils.getByNameCodeFromAllCountries(context, countryIso) ?: return
            mDefaultCountryNameCode = defaultCountry.iso
            this.defaultCountry = defaultCountry
        }

    val defaultCountryCode: String
            get() = mDefaultCountry!!.phoneCode
     val defaultCountryCodeAsInt: Int
            get() {
                var code = 0
                try {
                    code = defaultCountryCode.toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return code
            }

        @get:Suppress("unused")
        val defaultCountryCodeWithPlus: String
            get() = context.getString(R.string.phone_code, defaultCountryCode)

        @get:Suppress("unused")
        val defaultCountryName: String
            get() = mDefaultCountry!!.name

        val defaultCountryNameCode: String
            get() = mDefaultCountry!!.iso.uppercase()

        @Suppress("unused")
        fun resetToDefaultCountry() {
            setEmptyDefault()
        }

        val selectedCountryCode: String
            get() = mSelectedCountry!!.phoneCode

        val selectedCountryCodeWithPlus: String
            get() = context.getString(R.string.phone_code, selectedCountryCode)

        @get:Suppress("unused")
        val selectedCountryCodeAsInt: Int
            get() {
                var code = 0
                try {
                    code = selectedCountryCode.toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return code
            }

        val selectedCountryName: String
            get() = mSelectedCountry!!.name
        val selectedCountryNameCode: String
            get() = mSelectedCountry!!.iso.uppercase()

        @Suppress("unused")
        fun setCountryForPhoneCode(countryCode: Int) {
            val ctx = context
            val country: TecOrbCountry = TecOrbCountryCodeUtils.getByCode(ctx, mPreferredCountries!!, countryCode)!!
            if (country == null) {
                if (mDefaultCountry == null) {
                    mDefaultCountry = TecOrbCountryCodeUtils.getByCode(ctx, mPreferredCountries!!, mDefaultCountryCode)
                }
                selectedCountry = mDefaultCountry
            } else {
                selectedCountry = country
            }
        }

        @Suppress("unused")
        fun setCountryForNameCode(countryNameCode: String?) {
            val ctx = context
            val country: TecOrbCountry = TecOrbCountryCodeUtils.getByNameCodeFromAllCountries(ctx, countryNameCode)!!
            if (country == null) {
                if (mDefaultCountry == null) {
                    mDefaultCountry = TecOrbCountryCodeUtils.getByCode(ctx, mPreferredCountries!!, mDefaultCountryCode)
                }
                selectedCountry = mDefaultCountry
            } else {
                selectedCountry = country
            }
        }

        fun registerPhoneNumberTextView(textView: TextView?) {
            registeredPhoneNumberTextView = textView
            if (isHintEnabled) setPhoneNumberHint()
        }

        var registeredPhoneNumberTextView: TextView?
            get() = mRegisteredPhoneNumberTextView
            set(phoneNumberTextView) {
                mRegisteredPhoneNumberTextView = phoneNumberTextView
                if (isPhoneAutoFormatterEnabled) {
                    if (mPhoneNumberWatcher == null) {
                        mPhoneNumberWatcher = PhoneNumberWatcher(defaultCountryNameCode)
                    }
                    mRegisteredPhoneNumberTextView!!.addTextChangedListener(mPhoneNumberWatcher)
                }
            }

        private fun setPhoneNumberWatcherToTextView(textView: TextView, countryNameCode: String) {
            if (isPhoneAutoFormatterEnabled) {
                if (mPhoneNumberWatcher == null) {
                    mPhoneNumberWatcher = PhoneNumberWatcher(countryNameCode)
                    textView.addTextChangedListener(mPhoneNumberWatcher)
                } else {
                    if (!mPhoneNumberWatcher!!.previousCountryCode.equals(countryNameCode, ignoreCase = true)
                    ) {
                        mPhoneNumberWatcher = PhoneNumberWatcher(countryNameCode)
                    }
                }
            }
        }
     var fullNumber: String?
            get() {
                var fullNumber: String = mSelectedCountry!!.phoneCode
                if (mRegisteredPhoneNumberTextView == null) {
                    Log.w(TAG, context.getString(R.string.error_unregister_carrier_number))
                } else {
                    fullNumber += mRegisteredPhoneNumberTextView!!.text.toString()
                }
                return fullNumber
            }

            set(fullNumber) {
                val country: TecOrbCountry = TecOrbCountryCodeUtils.getByNumber(context, mPreferredCountries, fullNumber!!)!!
                selectedCountry = country
                val carrierNumber = detectCarrierNumber(fullNumber, country)
                if (mRegisteredPhoneNumberTextView == null) {
                    Log.w(TAG, context.getString(R.string.error_unregister_carrier_number))
                } else {
                    mRegisteredPhoneNumberTextView!!.text = carrierNumber
                }
            }
     val fullNumberWithPlus: String
            get() = context.getString(R.string.phone_code, fullNumber)
            var textColor: Int
            get() = mTextColor
            set(contentColor) {
                mTextColor = contentColor
                mTvSelectedCountry!!.setTextColor(contentColor)
                mImvArrow!!.setColorFilter(contentColor, PorterDuff.Mode.SRC_IN)
            }

        fun getBackgroundColor(): Int {
            return mBackgroundColor
        }

        override fun setBackgroundColor(backgroundColor: Int) {
            mBackgroundColor = backgroundColor
            mRlyHolder!!.setBackgroundColor(backgroundColor)
        }
        fun setTextSize(textSize: Int) {
            if (textSize > 0) {
                mTvSelectedCountry!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
                setArrowSize(textSize)
                setFlagSize(textSize)
            }
        }

        private fun setArrowSize(arrowSize: Int) {
            if (arrowSize > 0) {
                val params = mImvArrow!!.layoutParams as LayoutParams
                params.width = arrowSize
                params.height = arrowSize
                mImvArrow!!.layoutParams = params
            }
        }
        fun hideNameCode(hideNameCode: Boolean) {
            mHideNameCode = hideNameCode
            selectedCountry = mSelectedCountry
        }
        @Suppress("unused")
        fun setCountryPreference(countryPreference: String?) {
            mCountryPreference = countryPreference
        }

        fun setTypeFace(fontAssetPath: String?) {
            try {
                val typeFace = Typeface.createFromAsset(
                    context.assets, fontAssetPath
                )
                mTypeFace = typeFace
                mTvSelectedCountry!!.setTypeface(typeFace)
            } catch (e: Exception) {
                Log.d(TAG, "Invalid fontPath. $e")
            }
        }

        @Suppress("unused")
        fun setTypeFace(typeFace: Typeface?, style: Int) {
            try {
                mTvSelectedCountry!!.setTypeface(typeFace, style)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @set:Suppress("unused")
        var typeFace: Typeface?
            get() = mTypeFace
            set(typeFace) {
                mTypeFace = typeFace
                try {
                    mTvSelectedCountry!!.typeface = typeFace
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


        @Suppress("unused")
        fun setOnCountryChangeListener(onCountryChangeListener: OnCountryChangeListener?) {
            mOnCountryChangeListener = onCountryChangeListener
        }

        fun setFlagSize(flagSize: Int) {
            mImvFlag!!.layoutParams.height = flagSize
            mImvFlag!!.requestLayout()
        }

        fun showFlag(showFlag: Boolean) {
            mShowFlag = showFlag
            mLlyFlagHolder!!.visibility = if (showFlag) VISIBLE else GONE
        }

        fun showFullName(showFullName: Boolean) {
            mShowFullName = showFullName
            selectedCountry = mSelectedCountry
        }

        override fun isClickable(): Boolean {
            return mIsClickable
        }

        override fun setClickable(isClickable: Boolean) {
            mIsClickable = isClickable
            mRlyClickConsumer!!.setOnClickListener(if (isClickable) countryCodeHolderClickListener else null)
            mRlyClickConsumer!!.isClickable = isClickable
            mRlyClickConsumer!!.isEnabled = isClickable
        }

        var isHidePhoneCode: Boolean
            get() = mHidePhoneCode
            set(hidePhoneCode) {
                mHidePhoneCode = hidePhoneCode

                val ctx = context
                val phoneCode: String = mSelectedCountry!!.phoneCode

                // Reset the view
                if (mHideNameCode) {
                    mTvSelectedCountry!!.text = ctx.getString(R.string.phone_code, phoneCode)
                    return
                }

                if (mShowFullName) {
                    val name: String = mSelectedCountry!!.name.uppercase()
                    if (mHidePhoneCode) {
                        mTvSelectedCountry!!.text = name
                    } else {
                        val country = ctx.getString(
                            R.string.country_full_name_and_phone_code,
                            name,
                            phoneCode
                        )
                        mTvSelectedCountry!!.text = country
                    }
                } else {
                    val iso: String = mSelectedCountry!!.iso.uppercase()
                    if (mHidePhoneCode) {
                        mTvSelectedCountry!!.text = iso
                    } else {
                        mTvSelectedCountry!!.text =
                            ctx.getString(R.string.country_code_and_phone_code, iso, phoneCode)
                    }
                }
            }

         fun enableHint(hintEnabled: Boolean) {
            isHintEnabled = hintEnabled
            if (isHintEnabled) setPhoneNumberHint()
        }

        private fun setPhoneNumberHint() {
            // don't set phone number hint for null textView and country.
            if (mRegisteredPhoneNumberTextView == null || mSelectedCountry == null || mSelectedCountry!!.iso == null) {
                return
            }

            val iso: String = mSelectedCountry!!.iso.uppercase()
            val mobile: PhoneNumberUtil.PhoneNumberType = PhoneNumberUtil.PhoneNumberType.MOBILE
            val phoneNumber: Phonenumber.PhoneNumber = mPhoneUtil!!.getExampleNumberForType(iso, mobile)
            if (phoneNumber == null) {
                mRegisteredPhoneNumberTextView!!.hint = ""
                return
            }

            val hint: String = mPhoneUtil!!.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
            mRegisteredPhoneNumberTextView!!.hint = hint
        }

        private inner class PhoneNumberWatcher : PhoneNumberFormattingTextWatcher {
            private var lastValidity = false
            var previousCountryCode: String = ""
                private set

            @Suppress("unused")
            constructor() : super()

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            constructor(countryCode: String) : super(countryCode) {
                previousCountryCode = countryCode
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                super.onTextChanged(s, start, before, count)
                try {
                    var iso: String? = null
                    if (mSelectedCountry != null) iso =
                        mSelectedCountry!!.phoneCode.uppercase()
                    val phoneNumber: Phonenumber.PhoneNumber = mPhoneUtil!!.parse(s.toString(), iso)
                    iso = mPhoneUtil!!.getRegionCodeForNumber(phoneNumber)
                    if (iso != null) {
                    }
                } catch (ignored: NumberParseException) {
                }


                if (mPhoneNumberInputValidityListener != null) {

                    val validity: Boolean = this.isValid
                    if (validity != lastValidity) {
                        mPhoneNumberInputValidityListener!!.onFinish(this@TecOrbCountryCodePicker, validity)
                    }
                    lastValidity = validity
                }
            }
            val isValid: Boolean
                get() {
                    val phoneNumber: Phonenumber.PhoneNumber? = phoneNumber
                    return phoneNumber != null && mPhoneUtil!!.isValidNumber(phoneNumber)
                }
        }

        val number: String?
            get() {
                val phoneNumber: Phonenumber.PhoneNumber = phoneNumber ?: return null

                if (mRegisteredPhoneNumberTextView == null) {
                    Log.w(TAG, context.getString(R.string.error_unregister_carrier_number))
                    return null
                }

                return mPhoneUtil!!.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
            }
     val phoneNumber: Phonenumber.PhoneNumber?

            get() {
                try {
                    var iso: String? = null
                    if (mSelectedCountry != null) iso = mSelectedCountry!!.iso.uppercase()
                    if (mRegisteredPhoneNumberTextView == null) {
                        Log.w(TAG, context.getString(R.string.error_unregister_carrier_number))
                        return null
                    }
                    return mPhoneUtil!!.parse(mRegisteredPhoneNumberTextView!!.text.toString(), iso)
                } catch (ignored: NumberParseException) {
                    return null
                }
            }



        fun setPhoneNumberInputValidityListener(listener: PhoneNumberInputValidityListener?) {
            mPhoneNumberInputValidityListener = listener
        }


        private fun setDefaultCountryFlagAndCode() {
            val ctx = context
            val manager = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (manager == null) {
                Log.e(TAG, "Can't access TelephonyManager. Using default county code")
                setEmptyDefault(defaultCountryCode)
                return
            }

            try {
                val simCountryIso = manager.simCountryIso
                if (simCountryIso == null || simCountryIso.isEmpty()) {
                    val iso = manager.networkCountryIso
                    if (iso == null || iso.isEmpty()) {
                        enableSetCountryByTimeZone(true)
                    } else {
                        setEmptyDefault(iso)
                    }
                } else {
                    setEmptyDefault(simCountryIso)
                }
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error when getting sim country, error = $e"
                )
                setEmptyDefault(defaultCountryCode)
            }
        }

        private fun setEmptyDefault() {
            setEmptyDefault(null)
        }
        private fun setEmptyDefault(countryCode: String?) {
            var countryCode = countryCode
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode =
                    if (mDefaultCountryNameCode == null || mDefaultCountryNameCode!!.isEmpty()) {
                        if (DEFAULT_COUNTRY == null || DEFAULT_COUNTRY.isEmpty()) {
                            DEFAULT_ISO_COUNTRY
                        } else {
                            DEFAULT_COUNTRY
                        }
                    } else {
                        mDefaultCountryNameCode
                    }
            }

            if (isPhoneAutoFormatterEnabled && mPhoneNumberWatcher == null) {
                mPhoneNumberWatcher = PhoneNumberWatcher(countryCode!!)
            }

            setDefaultCountryUsingNameCode(countryCode)
            selectedCountry = defaultCountry
        }


        fun enableSetCountryByTimeZone(isEnabled: Boolean) {
            if (isEnabled) {
                if (mDefaultCountryNameCode != null && !mDefaultCountryNameCode!!.isEmpty()) return
                if (mRegisteredPhoneNumberTextView != null) return
                if (mSetCountryByTimeZone) {
                    val tz = TimeZone.getDefault()
                    val countryIsos: List<String> = TecOrbCountryCodeUtils.getCountryIsoByTimeZone(
                        context, tz.id
                    )

                    if (countryIsos == null) {
                        setEmptyDefault()
                    } else {
                        setDefaultCountryUsingNameCode(countryIsos[0])
                        selectedCountry = defaultCountry
                    }
                }
            }
            mSetCountryByTimeZone = isEnabled
        }

        companion object {
            private val TAG: String = TecOrbCountryCodePicker::class.java.simpleName

            private const val DEFAULT_ISO_COUNTRY = "ID"

            val defaultContentColor: Int = 0
                get() = field

            val defaultBackgroundColor: Int = Color.TRANSPARENT
                get() = field

            fun getColor(context: Context, id: Int): Int {
                val version = Build.VERSION.SDK_INT
                return if (version >= 23) {
                    context.getColor(id)
                } else {
                    context.resources.getColor(id)
                }
            }
        }
    }
