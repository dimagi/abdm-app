package org.commcare.dalvik.data.network

enum class HqError(val code: String, val message: String) {

    Error_500("HIS-500",
        "An unexpected error has occurred. Please try again sometime."),
    Error_1007("HIS-1007",
        "The user you are searching for is disabled."),
    Error_1008("HIS-1008",
        "No user account can be found with the Health ID provided."),
    ERROR_1015("HIS-1015",
        "A Health ID already exists with these details. Click here to login to your Health ID."),
    ERROR_1023("HIS_1023",
        "Please wait for 30 seconds before sending another OTP request."),
    ERROR_1029("HIS-1029",
    "The provided Health ID number #healthIdNumber is already registered with Aadhaar."),
    ERROR_1039("HIS-1039",
    "You have exceeded the maximum limit of failed attempts. Please try to login using other  modes or try again in 12 hours."),
    ERROR_1052("HIS-1052",
    "The mobile user provided by you is already linked to 10 other Health IDs. Please  provide a different Mobile Number."),
    ERROR_1056("HIS-1056", "OTP has expired. Please re-send."),
    ERROR_2007("HIS-2007", "Aadhaar suspended by competent authority."),
    ERROR_2008("HIS-2008 ","Aadhaar canceled(Aadhaar is not in authenticable status)."),
    ERROR_2009("HIS-2009"," Aadhaar suspended(Aadhaar is not in authenticatable status)."),
    ERROR_2016("HIS-2016","Aadhaar Number/Virtual ID is invalid."),
    ERROR_2017("HIS-2017","You have requested multiple OTPs in this transaction. Please try again in 30 minutes."),
    ERROR_2022("HIS-2022","Invalid OTP value."),
    ERROR_3005("HIS-3005","Aadhaar number does not have a mobile number.")
}