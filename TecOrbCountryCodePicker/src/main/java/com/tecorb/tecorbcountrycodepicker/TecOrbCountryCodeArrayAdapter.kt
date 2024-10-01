package com.tecorb.tecorbcountrycodepicker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

class TecOrbCountryCodeArrayAdapter(
    private val mContext: Context,
    countries: List<TecOrbCountry>,
    private val mCountryCodePicker: TecOrbCountryCodePicker
) : ArrayAdapter<TecOrbCountry>(mContext, 0, countries) {

    private class ViewHolder(
        val rlyMain: RelativeLayout,
        val tvName: TextView,
        val tvCode: TextView,
        val imvFlag: ImageView,
        val llyFlagHolder: LinearLayout,
        val viewDivider: View
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val country = getItem(position)
        val viewHolder: ViewHolder

        var itemView = convertView
        if (itemView == null) {
            val inflater = LayoutInflater.from(context)
            itemView = inflater.inflate(R.layout.item_country, parent, false)

            viewHolder = ViewHolder(
                itemView.findViewById(R.id.item_country_rly),
                itemView.findViewById(R.id.country_name_tv),
                itemView.findViewById(R.id.code_tv),
                itemView.findViewById(R.id.flag_imv),
                itemView.findViewById(R.id.flag_holder_lly),
                itemView.findViewById(R.id.preference_divider_view)
            )
            itemView.tag = viewHolder
        } else {
            viewHolder = itemView.tag as ViewHolder
        }

        setData(country, viewHolder)
        return itemView!!
    }

    private fun setData(country: TecOrbCountry?, viewHolder: ViewHolder) {
        if (country == null) {
            viewHolder.viewDivider.visibility = View.VISIBLE
            viewHolder.tvName.visibility = View.GONE
            viewHolder.tvCode.visibility = View.GONE
            viewHolder.llyFlagHolder.visibility = View.GONE
        } else {
            viewHolder.viewDivider.visibility = View.GONE
            viewHolder.tvName.visibility = View.VISIBLE
            viewHolder.tvCode.visibility = View.VISIBLE
            viewHolder.llyFlagHolder.visibility = View.VISIBLE

            val ctx = viewHolder.tvName.context
            val countryNameAndCode = ctx.getString(R.string.country_name_and_code, country.name, country.iso.uppercase())
            viewHolder.tvName.text = countryNameAndCode

            if (mCountryCodePicker.isHidePhoneCode) {
                viewHolder.tvCode.visibility = View.GONE
            } else {
                viewHolder.tvCode.text = ctx.getString(R.string.phone_code, country.phoneCode)
            }

            mCountryCodePicker.typeFace?.let { typeface ->
                viewHolder.tvCode.typeface = typeface
                viewHolder.tvName.typeface = typeface
            }

            viewHolder.imvFlag.setImageResource(TecOrbCountryCodeUtils.getFlagDrawableResId(country))

            val color = mCountryCodePicker.dialogTextColor
            if (color != TecOrbCountryCodePicker.defaultContentColor) {
                viewHolder.tvCode.setTextColor(color)
                viewHolder.tvName.setTextColor(color)
            }
        }
    }
}
