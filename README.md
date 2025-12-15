# 2025/15/15
修正ファイル
。。。/Bodycamera\ba\build.gradle
10行から
 //設定
        buildConfigField "String", "DEVICE_API_BASE_URL", "\"http://10.200.2.29:5000\""
        buildConfigField "String", "DEVICE_API_AUTHMODE_PATH", "\"/api/device/getAuthMode\""
.../[text](ba/src/main/java/com/bodycamera/ba/)
baの中に
activity
   DeviceApiClient.kt(add new)
   DeviceSerialHelper.kt(add new)
   VeinResultActivity.kt
   Face3Activity.kt
   TopActivity.kt
layout
  activity_vein_result.xml
values 
  strings.xml
