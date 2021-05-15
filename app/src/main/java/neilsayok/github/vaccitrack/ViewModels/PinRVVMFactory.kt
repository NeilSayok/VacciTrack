package neilsayok.github.vaccitracker.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PinRVVMFactory(): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PinRVViewModel::class.java)){
            return PinRVViewModel() as T
        }
        throw IllegalArgumentException ("UnknownViewModel")
    }
}