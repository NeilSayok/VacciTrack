package neilsayok.github.vaccitracker.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}


class PinRVViewModel() : ViewModel() {
    val pinList = MutableLiveData<ArrayList<String>>()
    var mList = arrayListOf<String>()
    private val _ispininlist = MutableLiveData<Event<Boolean>>()

    val isPinInList : LiveData<Event<Boolean>>
        get() = _ispininlist

    fun pinIsInList(itemId: Boolean) {
        _ispininlist.value = Event(itemId)  // Trigger the event by setting a new Event as a new value
    }

    fun addPin(newPin:String){
        if (newPin !in mList)
        {
            mList.add(newPin)
            pinList.value = mList
            pinIsInList(false)

        }else{
            pinIsInList(true)
        }
    }

    fun setList(mPinList:ArrayList<String>){
        mList = mPinList
        //pinList.value = mList
    }

    fun removePin(pin:String){
        if (pin in mList)
        {
            mList.remove(pin)
            pinList.value = mList
            //Log.d("Viewmodel Remove", pin)
        }
    }
}