package neilsayok.github.vaccitracker.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RadioBtnViewModel: ViewModel() {
    val isChecked = MutableLiveData<Boolean>()

    public fun setIsChecked(checked: Boolean){
        isChecked.value = checked
    }
}