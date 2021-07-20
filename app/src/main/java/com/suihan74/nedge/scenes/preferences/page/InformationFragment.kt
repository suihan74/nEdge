package com.suihan74.nedge.scenes.preferences.page

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.suihan74.nedge.Application
import com.suihan74.nedge.R
import com.suihan74.nedge.databinding.FragmentInformationBinding
import com.suihan74.nedge.scenes.permissions.PermissionsValidationActivity
import java.time.LocalDateTime

class InformationFragment : Fragment() {
    companion object {
        fun createInstance() = InformationFragment()

        /** アプリ初公開年 */
        private const val FIRST_PUBLISHED_YEAR = 2020
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentInformationBinding.inflate(inflater, container, false)

        binding.appVersion.text = Application.instance.versionName

        binding.copyright.also {
            it.text = makeSpannedFromHtml(getString(
                R.string.copyright,
                LocalDateTime.now().let { now ->
                    buildString {
                        append(FIRST_PUBLISHED_YEAR)
                        val year = now.year
                        if (year != FIRST_PUBLISHED_YEAR) {
                            append("-")
                            if (year > FIRST_PUBLISHED_YEAR) append(year)
                        }
                    }
                }
            ))

            it.movementMethod = LinkMovementMethod.getInstance()
        }

        binding.showLicensesButton.setOnClickListener {
            val intent = Intent(activity, OssLicensesMenuActivity::class.java).apply {
                putExtra("title", "Licenses")
            }
            startActivity(intent)
        }

        binding.permissionsButton.setOnClickListener {
            val intent = Intent(activity, PermissionsValidationActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    // ------ //

    private fun makeSpannedFromHtml(htmlStr: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlStr, Html.FROM_HTML_MODE_LEGACY)!!
        }
        else {
            @Suppress("deprecation")
            Html.fromHtml(htmlStr)!!
        }
}
