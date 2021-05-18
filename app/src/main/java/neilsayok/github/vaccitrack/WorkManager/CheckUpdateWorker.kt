package neilsayok.github.vaccitrack.WorkManager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import neilsayok.github.vaccitrack.BuildConfig
import neilsayok.github.vaccitrack.Helpers.VolleySingleton
import neilsayok.github.vaccitrack.R
import org.json.JSONObject

class CheckUpdateWorker(var context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
    private val CHANNEL_REQUIRE_UPDATE = "REQUIRE_UPDATE"
    private val NOTIFCATION_ID_REQUIRE_UPDATE = 3
    lateinit var nManagerCompat : NotificationManagerCompat



    override fun doWork(): Result {

        createNotificationChannel()
        checkForUpdate()

        return Result.success()
    }


    private fun createNotificationChannel(){
        nManagerCompat = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val appUpdateNotificationChannel: NotificationChannel = NotificationChannel(CHANNEL_REQUIRE_UPDATE,
                "Chanel 3", NotificationManager.IMPORTANCE_HIGH)
            appUpdateNotificationChannel.description = "Checking App Update"




            val manager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(appUpdateNotificationChannel)
        }
    }


    fun checkForUpdate(){
        val url =context.getString(R.string.latest_release)
        val stringRequest = StringRequest(Request.Method.GET,url,
            {
                val jsonObject = JSONObject(it)


                if (BuildConfig.VERSION_NAME != jsonObject.getString("tag_name")){
                    var updateNotificatonBuilder = NotificationCompat.Builder(applicationContext,CHANNEL_REQUIRE_UPDATE)
                        .setContentTitle("An is available")
                        .setContentText("An app update is available. Please update to get all the Latest feaures")
                        .setColorized(true)
                        .setColor(Color.RED)
                        .setSmallIcon(R.drawable.ic_vaccine)

                    val x = Intent(Intent.ACTION_VIEW)
                    x.data = Uri.parse(jsonObject.getString("html_url"))
                    val pendingIntent1 = PendingIntent.getActivity(applicationContext, 1, x, 0)
                    updateNotificatonBuilder.addAction(R.drawable.ic_vaccine, "Update App",pendingIntent1)



                    val updateNotification = updateNotificatonBuilder.build()
                    nManagerCompat.notify(NOTIFCATION_ID_REQUIRE_UPDATE,updateNotification)


                }
            },{
                it.printStackTrace()
            });

        VolleySingleton.getmInstance(applicationContext).addToRequestQue(stringRequest)
    }


}