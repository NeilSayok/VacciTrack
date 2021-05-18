package neilsayok.github.vaccitracker.WorkManager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import neilsayok.github.vaccitrack.Helpers.VolleySingleton
import neilsayok.github.vaccitrack.R
import neilsayok.github.vaccitracker.Helpers.VaccineData
import org.json.JSONObject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class CheckVaccineWorker(var context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
    
    private val df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    
    private val CHANNEL_SEARCH_VACCINE = "CHANNEL_SEARCH_VACC"
    private val NOTIFCATION_ID_SEARCH_VACCINE = 1

    private val CHANNEL_FOUND_VACCINE = "CHANNEL_FOUND_VACCINE"
    private val NOTIFCATION_ID_FOUND_VACCINE = 2

    lateinit var nManagerCompat : NotificationManagerCompat
    lateinit var generalNotification: Notification
    lateinit var vaccineFoundNotfication: Notification
    
    var ageGrp: Int = 0
    var counter = 0
    
   
    private val vaccineAvailList = mutableListOf<VaccineData>()
    
    private val volleySingleton: VolleySingleton = VolleySingleton.getmInstance(context)
    



    override fun doWork(): Result {
        
        val sp = context.getSharedPreferences(context.getString(R.string.key_sp_name), AppCompatActivity.MODE_PRIVATE)

        nManagerCompat = NotificationManagerCompat.from(context)
        createNotificationChannel()
        showGeneralNotication()

        val calIns = Calendar.getInstance()
        val dayList = mutableListOf<String>()


        for(i in 0 until 7){
            dayList.add(df.format(calIns.time))
            calIns.add(Calendar.DAY_OF_MONTH,1)
        }



        val pinList = sp.getStringSet("PIN_LIST",HashSet<String>())
        ageGrp = sp.getInt("AGE_GRP",0)

        if (pinList != null) {
            if (pinList.isNotEmpty()){
            for(pin in pinList){
                for(day in dayList)
                checkVaccineAvailbility(pin,day)
            }
            }else{
                showGeneralNotication(context.getString(R.string.notif_head_search_comp),context.getString(
                    R.string.noti_msg_vacc_not_avail))
            }

        }



        return Result.success()
    }

    private fun checkVaccineAvailbility(pin: String, date: String){
        val url = context.getString(R.string.api_url)+"?pincode=$pin&date=$date"

        val stringRequest = StringRequest(Request.Method.GET,url,
            {
                try{
                    //Log.d("Response",it)
                    val jsonObj = JSONObject(it)
                    val centers = jsonObj.getJSONArray("centers")
                    val l = centers.length()
                    if (l > 0)
                        for (i in 0 until l) {
                            val centerDetails = centers.getJSONObject(i)
                            val sessions = centerDetails.getJSONArray("sessions")
                            val sessL = sessions.length()
                            val pCode = centerDetails.getString("pincode")
                            for (j in 0 until sessL) {
                                val sessObj = sessions.getJSONObject(j)
                                val availCap = sessObj.getInt("available_capacity")
                                val min_age_limit = sessObj.getInt("min_age_limit")
                                val date = sessObj.getString("date")



                                when (ageGrp) {
                                    0 -> {
                                        if (availCap > 0) {
                                            var x = 0
                                            for (i in vaccineAvailList) {
                                                if (i.pinCode == pCode && i.date == date) {
                                                    i.availiity += availCap
                                                    x++
                                                    break
                                                }
                                            }
                                            if (x == 0)
                                                vaccineAvailList.add(
                                                    VaccineData(
                                                        pCode,
                                                        min_age_limit,
                                                        availCap,
                                                        date
                                                    )
                                                )

                                        }
                                    }
                                    1 -> {
                                        if (availCap > 0 && min_age_limit >= 45) {
                                            var x = 0
                                            for (i in vaccineAvailList) {
                                                if (i.pinCode == pCode && i.date == date) {
                                                    i.availiity += availCap
                                                    x++
                                                    break
                                                }
                                            }
                                            if (x == 0)
                                                vaccineAvailList.add(
                                                    VaccineData(
                                                        pCode,
                                                        min_age_limit,
                                                        availCap,
                                                        date
                                                    )
                                                )

                                        }

                                    }
                                    2 -> {
                                        if (availCap > 0 && min_age_limit < 45) {
                                            var x = 0
                                            for (i in vaccineAvailList) {
                                                if (i.pinCode == pCode && i.date == date) {
                                                    i.availiity += availCap
                                                    x++
                                                    break
                                                }
                                            }
                                            if (x == 0)
                                                vaccineAvailList.add(
                                                    VaccineData(
                                                        pCode,
                                                        min_age_limit,
                                                        availCap,
                                                        date
                                                    )
                                                )

                                        }
                                    }
                                }


                            }
                        }

                    counter--
                    if (counter == 0) {
                        requestsExecuted()
                    }
            }catch(e: Exception){
                    e.printStackTrace()
                    counter = 0
                    requestsExecuted()
                }
            },{
                counter--
                if (counter == 0){
                    requestsExecuted()
                }
            })

        volleySingleton.addToRequestQue(stringRequest)
        counter++




    }

    private fun requestsExecuted(){
        if(vaccineAvailList.size <= 0)
            showGeneralNotication(context.getString(R.string.notif_head_search_comp),context.getString(
                            R.string.noti_msg_vacc_not_avail))
        else
            showVaccineFoundNotification(context.getString(R.string.vacci_msg_vacci_avail_at_pins))
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val searchVaccineNotificationChannel: NotificationChannel = NotificationChannel(CHANNEL_SEARCH_VACCINE,
                "Chanel 1", NotificationManager.IMPORTANCE_MIN)
            searchVaccineNotificationChannel.description = context.getString(R.string.vacc_desc_searching_for_vacc)

            val vaccineFoundNotificationChannel: NotificationChannel = NotificationChannel(CHANNEL_FOUND_VACCINE,
                "Chanel 2", NotificationManager.IMPORTANCE_HIGH)
            vaccineFoundNotificationChannel.vibrationPattern = longArrayOf(200,200,200)
            vaccineFoundNotificationChannel.description = context.getString(R.string.vacc_Desc_vacc_found)


            val manager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(searchVaccineNotificationChannel)
            manager.createNotificationChannel(vaccineFoundNotificationChannel)
        }
    }
    private fun showGeneralNotication(
        title: String =context.getString(R.string.noti_msg_searching_vaccines),
        message: String = context.getString(R.string.noti_msg_checking_vacc_availibility)
    ){

        var gerneralNotificationCompat = NotificationCompat.Builder(context, CHANNEL_SEARCH_VACCINE)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(message))
            .setSmallIcon(R.drawable.ic_vaccine)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            gerneralNotificationCompat.setPriority(NotificationManager.IMPORTANCE_MIN)
        }


        nManagerCompat.notify(NOTIFCATION_ID_SEARCH_VACCINE,gerneralNotificationCompat.build())
    }

    private fun showVaccineFoundNotification(message: String){
        var notificationCompat = NotificationCompat.Builder(context, CHANNEL_FOUND_VACCINE)
            .setContentTitle(context.getString(R.string.noti_title_vaccine_found))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_vaccine)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationCompat.setPriority(NotificationManager.IMPORTANCE_HIGH)
        }
        var s = context.getString(R.string.noti_body_header)+"\n"
        var prevPin = ""


        for(item in vaccineAvailList){
            if (item.pinCode != prevPin)
                s += "\n"
            s += "${context.getString(R.string.noti_body_pin)} :${item.pinCode} ${context.getString(R.string.noti_body_on)} ${item.date} ${context.getString(R.string.noti_body_availibility)}:${item.availiity} \n"
            prevPin = item.pinCode
        }

        notificationCompat.setStyle(NotificationCompat.BigTextStyle()
            .bigText(s))

        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(context.getString(R.string.cowin_url))
        val pendingIntent = PendingIntent.getActivity(context, 0, i, 0)
        notificationCompat.addAction(R.drawable.ic_vaccine, "Go To CoWin Site",pendingIntent)


        vaccineFoundNotfication = notificationCompat.build()
        nManagerCompat.notify(NOTIFCATION_ID_FOUND_VACCINE,vaccineFoundNotfication)
        nManagerCompat.cancel(NOTIFCATION_ID_SEARCH_VACCINE)
    }
}

//388580
//387210
//388430
