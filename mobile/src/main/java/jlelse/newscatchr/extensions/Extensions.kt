/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.extensions

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.content.res.AppCompatResources
import android.text.Html
import android.text.Spanned
import com.google.gson.Gson
import jlelse.newscatchr.appContext
import jlelse.newscatchr.backend.Feed
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import javax.xml.parsers.SAXParserFactory

fun String.convertOpmlToFeeds() = tryOrNull {
	mutableListOf<Feed>().apply {
		SAXParserFactory.newInstance().newSAXParser().xmlReader.apply {
			contentHandler = object : DefaultHandler() {
				@Throws(SAXException::class)
				override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
					if (qName.equals("outline", ignoreCase = true) && attributes.getValue("xmlUrl") != null) {
						add(Feed(
								title = attributes.getValue("title"),
								feedId = attributes.getValue("xmlUrl"),
								saved = true
						))
					}
				}
			}
			parse(this@convertOpmlToFeeds)
		}
	}.toTypedArray()
}

fun Any.toJson(): String = Gson().toJson(this)

fun String.jsonArray(): JSONArray? = tryOrNull { JSONArray(this) }

fun String.jsonObject(): JSONObject? = tryOrNull { JSONObject(this) }

fun String.buildExcerpt(words: Int) = split(" ").toMutableList().filter { it.notNullOrBlank() && it != "\n" }.take(words).joinToString(separator = " ", postfix = "...").trim()

fun String?.notNullOrBlank() = !isNullOrBlank()

fun <T> Array<out T>?.notNullAndEmpty() = this != null && isNotEmpty()

fun <T> Collection<T>?.notNullAndEmpty() = this != null && isNotEmpty()

fun <T> Set<T>?.notNullAndEmpty() = this != null && isNotEmpty()

fun Array<out String?>.removeBlankStrings() = mutableListOf<String>().apply { this@removeBlankStrings.filter { it.notNullOrBlank() }.forEach { add(it!!) } }.toTypedArray()

fun Set<String?>.removeBlankStrings() = mutableSetOf<String>().apply { this@removeBlankStrings.filter { it.notNullOrBlank() }.forEach { add(it!!) } }.toSet()

fun String.cleanHtml(): String? = if (notNullOrBlank()) Jsoup.clean(this, Whitelist.basic().addTags("h2", "h3", "h4", "h5", "h6")) else this

fun String.toHtml(): Spanned = if (android.os.Build.VERSION.SDK_INT < 24) {
	@Suppress("DEPRECATION")
	Html.fromHtml(this)
} else {
	Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
}

fun <T> tryOrNull(code: () -> T): T? = try {
	code()
} catch(e: Exception) {
	e.printStackTrace()
	null
}

fun <T> tryOrNull(arg: Boolean?, code: () -> T): T? = if (arg ?: false) tryOrNull(code) else null

fun sharedPref(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)

fun Int.resDim() = tryOrNull { appContext?.resources?.getDimension(this) }

fun Int.resStr() = tryOrNull { appContext?.resources?.getString(this) }

fun Int.resStrArr(): Array<out String>? = tryOrNull { appContext?.resources?.getStringArray(this) }

fun Int.resIntArr() = tryOrNull { appContext?.resources?.getIntArray(this) }

fun Int.resDrw(context: Context?, color: Int?) = tryOrNull {
	AppCompatResources.getDrawable(context ?: appContext!!, this)?.apply {
		if (color != null) DrawableCompat.setTint(this, color)
	}
}

fun Int.resClr(context: Context?) = tryOrNull { ContextCompat.getColor(context ?: appContext!!, this) }