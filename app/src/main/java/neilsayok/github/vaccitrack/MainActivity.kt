package neilsayok.github.vaccitrack


import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import neilsayok.github.vaccitrack.RecyclerViewHelpers.PinRVAdapter
import neilsayok.github.vaccitrack.WorkManager.CheckUpdateWorker
import neilsayok.github.vaccitracker.ViewModels.PinRVVMFactory
import neilsayok.github.vaccitracker.ViewModels.PinRVViewModel
import neilsayok.github.vaccitracker.WorkManager.CheckVaccineWorker
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    lateinit var pinInput : TextInputEditText
    lateinit var addToRvBtn : Button
    lateinit var pinRecyclerView: RecyclerView
    private val pinRegex = Regex("^[1-9][0-9]{5}$")
    lateinit var  anim: Animation
    lateinit var rvViewModel: PinRVViewModel
    lateinit var rvAdapter: PinRVAdapter
    lateinit var pinList: ArrayList<String>
    lateinit var sp: SharedPreferences
    lateinit var radioGroup: RadioGroup
    lateinit var radB1: RadioButton
    lateinit var radB2: RadioButton
    lateinit var radB3: RadioButton
    lateinit var openCowinSite: Button
    lateinit var startMonitorBtn: Button
    lateinit var checkOnceBtn: Button
    lateinit var base: View
    lateinit var constraints: Constraints
    lateinit var snack: Snackbar
    lateinit var gotoForm: Button
    lateinit var checkAppUpdateWorker:PeriodicWorkRequest






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        initalize()
        rvInitializer()
        radioGroupHandler()
        checkAppUpdateWorkerStart()
    }

    private fun checkAppUpdateWorkerStart() {
        checkAppUpdateWorker  = PeriodicWorkRequestBuilder<CheckUpdateWorker>(12,
        TimeUnit.HOURS,
        1,TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(checkAppUpdateWorker)
    }

    private fun initalize() {
        pinInput = findViewById(R.id.pinInput)
        addToRvBtn = findViewById(R.id.addPinBtn)
        pinRecyclerView = findViewById(R.id.pinRV)
        radioGroup = findViewById(R.id.radioGroup)
        radB1 = findViewById(R.id.radioAllAgeGrp)
        radB2 = findViewById(R.id.radioAgeGrp45)
        radB3 = findViewById(R.id.radioAgeGrp18)
        openCowinSite = findViewById(R.id.openCowinWebsite)
        startMonitorBtn = findViewById(R.id.startMoitorBtn)
        checkOnceBtn = findViewById(R.id.checkOnceBtn)
        base = findViewById(R.id.base_scroll_view)
        gotoForm = findViewById(R.id.gotoForm)

        anim = AnimationUtils.loadAnimation(applicationContext, R.anim.shake)
        sp = getSharedPreferences(getString(R.string.key_sp_name), MODE_PRIVATE)
        constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()


        addToRvBtn.setOnClickListener(onClickListener)
        openCowinSite.setOnClickListener(onClickListener)
        startMonitorBtn.setOnClickListener(onClickListener)
        checkOnceBtn.setOnClickListener(onClickListener)
        gotoForm.setOnClickListener(onClickListener)

        snack = Snackbar.make(base,getString(R.string.mainact_snack_txt),Snackbar.LENGTH_INDEFINITE)
        snack.setAction(getString(R.string.dissmiss)) {
            snack.dismiss()
        }
        val snackbarView: View = snack.view
        val snackTextView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        snackTextView.maxLines = 10
    }

    private fun rvInitializer(){
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
        pinRecyclerView.layoutManager = staggeredGridLayoutManager

        val factory = PinRVVMFactory()
        rvViewModel = ViewModelProvider(this,factory).get(PinRVViewModel::class.java)
        val l = sp.getStringSet("PIN_LIST",HashSet<String>())
        pinList = ArrayList(l)

        rvViewModel.setList(pinList)

        rvAdapter = PinRVAdapter(rvViewModel,pinList,applicationContext)
        pinRecyclerView.adapter = rvAdapter

        rvViewModel.pinList.observe(this, {
            rvAdapter.updateList(it)
            sp.edit().putStringSet(getString(R.string.key_sp_pin_list), HashSet<String>(pinList)).apply()

        })

        rvViewModel.isPinInList.observe(this, {
            it.getContentIfNotHandled()?.let {
                if(it){
                    Toast.makeText(applicationContext,getString(R.string.toast_txt_pin_present),Toast.LENGTH_LONG).show()
                }
            }
        })


    }

    private fun radioGroupHandler(){
        when(sp.getInt(getString(R.string.key_sp_age_grp),0)){
            0->radB1.isChecked = true
            1->radB2.isChecked = true
            2->radB3.isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.radioAllAgeGrp->{
                    sp.edit().putInt(getString(R.string.key_sp_age_grp),0).apply()
                }
                R.id.radioAgeGrp45->{
                    sp.edit().putInt(getString(R.string.key_sp_age_grp),1).apply()
                }
                R.id.radioAgeGrp18->{
                    sp.edit().putInt(getString(R.string.key_sp_age_grp),2).apply()
                }
            }
        }
    }


    private val onClickListener : View.OnClickListener = View.OnClickListener {
        when(it.id){
            R.id.addPinBtn->{
                if (pinInput.text.isNullOrEmpty() || !pinRegex.matches(pinInput.text.toString())){
                    pinInput.startAnimation(anim)
                    Toast.makeText(applicationContext,getString(R.string.toast_txt_valid_pin), Toast.LENGTH_LONG).show()
                }else{
                    rvViewModel.addPin(pinInput.text.toString())
                    pinInput.text!!.clear()
                }
            }
            R.id.openCowinWebsite->{
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(getString(R.string.cowin_url))
                startActivity(i)
            }
            R.id.startMoitorBtn->{

                WorkManager.getInstance(applicationContext).cancelAllWork()

                val oneTimeChecker = OneTimeWorkRequestBuilder<CheckVaccineWorker>()
                    .setConstraints(constraints)
                    .build()
                val checkVaccineRequest = PeriodicWorkRequestBuilder<CheckVaccineWorker>(3,
                    TimeUnit.HOURS,
                    1,TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .build()
                WorkManager.getInstance(applicationContext).enqueue(oneTimeChecker)
                WorkManager.getInstance(applicationContext).enqueue(checkVaccineRequest)
                WorkManager.getInstance(applicationContext).enqueue(checkAppUpdateWorker)
                snack.show()

            }
            R.id.checkOnceBtn->{

                val checkVaccineRequest = OneTimeWorkRequestBuilder<CheckVaccineWorker>()
                    .setConstraints(constraints)
                    .build()
                WorkManager.getInstance(applicationContext).enqueue(checkVaccineRequest)
                snack.show()

            }
            R.id.gotoForm->{
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(getString(R.string.form_link))
                startActivity(i)
            }
        }
    }
}