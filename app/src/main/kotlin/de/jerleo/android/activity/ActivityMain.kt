package de.jerleo.android.activity

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.jerleo.android.DateHelper
import de.jerleo.android.DialogHelper
import de.jerleo.android.DialogHelper.DialogCommand
import de.jerleo.android.DialogHelper.OnListChangedListener
import de.jerleo.android.R
import de.jerleo.android.adapter.AdapterBill
import de.jerleo.android.adapter.AdapterMeter
import de.jerleo.android.list.ListBill
import de.jerleo.android.list.ListMeter
import de.jerleo.database.Database
import de.jerleo.database.Database.Companion.instance
import de.jerleo.model.Home
import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.Executors

class ActivityMain : AppCompatActivity(), OnListChangedListener {

    private val progress: ProgressBar by lazy { findViewById(R.id.progressBar) }
    private val viewPager: ViewPager2 by lazy { findViewById(R.id.container) }
    private val tabLayout: TabLayout by lazy { findViewById(R.id.tabs) }
    private val pagerAdapter by lazy { PagerAdapter(this) }
    private val listMeter by lazy { ListMeter() }
    private val listBill by lazy { ListBill() }
    private val dialogHelper by lazy { DialogHelper() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.main)
        database = instance(this)
        viewPager.adapter = pagerAdapter
        val titles = arrayOf(getString(R.string.meters), getString(R.string.bills))
        TabLayoutMediator(tabLayout, viewPager) { tab, pos -> tab.text = titles[pos] }.attach()
    }

    inner class PagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment =
            if (position == 0) listMeter else listBill
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    override fun finish() {
        super.finish()
        database?.close()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_main, menu)
        invalidateOptionsMenu()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new_meter -> {
                viewPager.currentItem = 0
                startActivity(Intent(this, ActivityMeter::class.java))
            }
            R.id.action_new_bill -> {
                viewPager.currentItem = 1
                startActivity(Intent(this, ActivityBill::class.java))
            }
            R.id.action_import -> importPrepare()
            R.id.action_export -> exportPrepare()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onListChanged() {
        listMeter.listAdapter?.run { (this as AdapterMeter).notifyDataSetChanged() }
        listBill.listAdapter?.run { (this as AdapterBill).notifyDataSetChanged() }
    }

    private val exportActivity =
        registerForActivityResult(StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK)
                it.data?.run { Exporter(this.data!!).execute() }
        }

    private fun exportPrepare() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/json"
        val dateTime = DateHelper.timestamp()
        val filename = String.format("Export_%s.txt", dateTime)
        intent.putExtra(Intent.EXTRA_TITLE, filename)
        exportActivity.launch(intent)
    }

    private val importActivity =
        registerForActivityResult(StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK)
                it.data?.run { Importer().prepare(this.data!!) }
        }

    private fun importPrepare() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/*"
        importActivity.launch(intent)
    }

    private inner class Exporter(private val exportUri: Uri) : DialogCommand {

        override fun execute() {
            progress.visibility = View.VISIBLE
            Executors.newSingleThreadExecutor().execute {
                val data = home.exportJSON()
                try {
                    val context: Context = this@ActivityMain
                    exportUri.let {
                        context.contentResolver.openOutputStream(it)
                    }?.let {
                        it.write(data.toByteArray())
                        it.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                done()
            }
        }

        private fun done() {
            Handler(Looper.getMainLooper()).post {
                progress.visibility = View.INVISIBLE
                Toast.makeText(
                    this@ActivityMain, R.string.export_success,
                    Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_OK)
            }
        }
    }

    private inner class Importer : DialogCommand {

        private lateinit var imported: Home

        override fun execute() {
            progress.visibility = View.VISIBLE
            Executors.newSingleThreadExecutor().execute {
                database!!.rebuild(null)
                home.load(imported)
                done()
            }
        }

        private fun done() {
            Handler(Looper.getMainLooper()).post {
                onListChanged()
                progress.visibility = View.INVISIBLE
                Toast.makeText(
                    this@ActivityMain, R.string.import_success,
                    Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_OK)
            }
        }

        fun prepare(importUri: Uri) {
            try {
                val context = this@ActivityMain
                context.contentResolver.openInputStream(importUri)?.run {
                    imported = Home(this)
                    this.close()
                }
                if (imported.meters.isNotEmpty())
                    confirm()
                else
                    Toast.makeText(context, R.string.missing_entries, Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun confirm() {
            val dialog = dialogHelper.getAlertDialog(
                this@ActivityMain,
                R.string.import_title, R.string.confirm_import
            )
            dialog.show()
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).tag = this
        }
    }

    companion object {
        const val UNIT_FORMAT = "%,d %s"

        private val home: Home by lazy { Home.instance }
        private var database: Database? = null

        private val currency: Currency
            get() = Currency.getInstance(Locale.getDefault())

        val currencySymbol: String
            get() = currency.symbol
        val currencyFormat: String
            get() = "%." + currency.defaultFractionDigits + "f " + currency.symbol
        val decimalFormat: DecimalFormat
            get() = DecimalFormat(",###")
        val numberFormat: NumberFormat = DecimalFormat.getNumberInstance().apply {
            this.maximumFractionDigits = 5
            this.minimumFractionDigits = 2
        }
    }
}
