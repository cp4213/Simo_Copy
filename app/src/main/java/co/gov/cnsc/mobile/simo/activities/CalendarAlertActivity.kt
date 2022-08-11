package co.gov.cnsc.mobile.simo.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.extensions.toFormat
import co.gov.cnsc.mobile.simo.models.Alert
import co.gov.cnsc.mobile.simo.network.RestAPI
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.github.kittinunf.fuel.core.Request
import kotlinx.android.synthetic.main.activity_calendar_alert.*
import kotlinx.android.synthetic.main.fragment_my_alerts.*
import java.text.SimpleDateFormat
import java.util.*


class CalendarAlertActivity  : SIMOActivity(){

    //var calendarView: MCalendarView? = null
    //private val ADD_NOTE = 44
    private var request: Request? = null
    //private var mEventDays: List<EventDay> = ArrayList()
    //val RESULT = "result"
    //val events: MutableList<EventDay> = ArrayList()
    var calendarView: com.applandeo.materialcalendarview.CalendarView? = null
   // private val ADD_NOTE = 44
    //private var mEventDays: List<EventDay> = java.util.ArrayList()
    //val RESULT = "result"
    val events: MutableList<EventDay> = java.util.ArrayList()
    val format ="yyyy-MM-dd"
    var pos =-1
    //val format ="yyyy-MM-dd"
   // private var calendarAdapter: CalendarAdapter? = null
    private var alerts = ArrayList<Alert>()


    private var month : Map<String,String>? =null
   // private var dates : MutableMap<String,month>? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_alert)
        showToolbarBack()
        setTextTitleToolbar(R.string.alertcalendar)
        calendarView = findViewById<CalendarView>(R.id.calendarView)
        var calendar = Calendar.getInstance()


       /* Prueba de fecha X
       val trydate="2022-07-09"
        events.add(EventDay(string2Calendar(trydate,format), R.drawable.dotselected))
        calendarView?.setEvents(events)
        */

        //events.add(EventDay(calendar, R.drawable.dotselected))


        calendarView?.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val clickedDayCalendar = eventDay.calendar
                calendar=eventDay.calendar
                for(i in alerts) {
                    if(i.notification?.dateShowCalendar == calendar.toFormat(format)){
                        rowAsunto.value=i.notification.subject
                        rowReception.value=i.notification.dateShowCalendar
                        pos=alerts.indexOf(i)
                        break
                    }else{
                        rowAsunto.value=""
                        rowReception.value=""
                        pos=-1
                    }
                }
                calendarAlertlayout.setOnClickListener(){
                    if (pos!=-1){
                        goToDetailAlert(pos,alerts.get(pos))
                    }
                }
            }

        })

        /*calendarView?.setOnDayClickListener(OnDayClickListener { eventDay ->
            val clickedDayCalendar: Calendar = eventDay.calendar
            Log.d("DEV","hola "+ eventDay.calendar+ " ")
            calendar=eventDay.calendar
            for(i in alerts) {
                if(i.notification?.dateShowCalendar == calendar.toFormat(format)){
                    rowAsunto.value=i.notification.subject
                    rowReception.value=i.notification.dateShowCalendar
                    pos=alerts.indexOf(i)
                    break
                }else{
                    rowAsunto.value=""
                    rowReception.value=""
                    pos=-1
                }
            }
            calendarAlertlayout.setOnClickListener(){
                if (pos!=-1){
                    goToDetailAlert(pos,alerts.get(pos))
                }
            }

        })*/
        getAlerts()
    }

    private fun getAlerts() {
        swipeRefresh?.isRefreshing = true
        request = RestAPI.getAlerts({ alerts ->
            this.alerts=alerts as ArrayList<Alert>
            configureUI(alerts)
        }, { fuelError ->


        })
    }

    private fun configureUI( alerts: ArrayList<Alert>) {
        //Pinta todas las alertas en el calendario
        for(i in alerts) {
            events.add(EventDay(string2Calendar(i.notification?.dateShowCalendar,format),
                R.drawable.dotselected))
        }
        Log.d("DEV","HolaConfig")
        calendarView?.setEvents(events)
    }

    private fun string2Calendar(dateString: String?, format: String?): Calendar {
        val cal = Calendar.getInstance()
        if (format != null && !format.isEmpty() && dateString != null && !dateString.isEmpty()) {
            val sdf = SimpleDateFormat(format, Locale.ENGLISH)
            val date = sdf.parse(dateString)
            cal.time = date
        }
        return cal
    }

    private fun goToDetailAlert(position: Int, alert: Alert?) {
        val intent = Intent(this, DetailAlertActivity::class.java)
        intent.putExtra("alert", alert)
        intent.putExtra("position", position)
        startActivityForResult(intent, SIMOApplication.REQUEST_CODE_DETAIL_ALERT)
    }
}

