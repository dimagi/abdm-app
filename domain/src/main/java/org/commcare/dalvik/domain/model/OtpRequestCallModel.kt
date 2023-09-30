package org.commcare.dalvik.domain.model

import timber.log.Timber

const val OTP_BLOCK_TIME = 15 * 60 * 1000

const val COUNTER_VAL = 40
data class OtpRequestCallModel(
    val id: String,
    var counter: Int,
    var blockedTS: Long = System.currentTimeMillis()
) {

    fun isBlocked() = counter >= COUNTER_VAL

    fun increaseOtpCounter() {
        counter += 1
        if(counter >= COUNTER_VAL){
            blockedTS = System.currentTimeMillis()
            Timber.d("----- OTP STATE ------ Blocking $id ---- $blockedTS")
        }
    }

    fun tryUnBlocking() : Boolean {
        if (isBlocked()) {
            val timeLeft = OTP_BLOCK_TIME - (System.currentTimeMillis() - blockedTS)
            if (timeLeft <= 0) {
                counter = 0
                blockedTS = System.currentTimeMillis()
                return true
            }
        }

        return false
    }

    fun reset() {
        counter = 0
        blockedTS = System.currentTimeMillis()
    }

    fun getTimeLeftToUnblock(): String {
        val timeLeft = OTP_BLOCK_TIME - (System.currentTimeMillis() - blockedTS)
        var minutesLeft = (timeLeft / 1000) / 60
        val secondsLeft = (timeLeft / 1000) % 60

        if(secondsLeft > 1){
            minutesLeft += 1
        }


        return "$minutesLeft minutes"
    }
}