# PROXYDROID PLUS
## Description

- This is a modified version of proxydroid that supports tweaking configuration via command line.
- Here is the link to the original version of proxydroid: [ProxyDroid](https://github.com/madeye/proxydroid)

## Usage
> Note: Root access is required

- Install ProxyDroid Plus apk
- Sending Broadcast intent via Adb or your Android Services/Apps.
- Example:

  - Turn on/off proxydroid service
     ```
     adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.PROXY_SWITCH_ACTION --ei switch 0 
     ## use 0 to turn off and 1 to turn on
     ```
  - Change profile 
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es setprofile "you_profile_name"
    ## If your profile doesn't exist, it will automatically create a new profile with such name.
    ```
  - Change host
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es host "host_string"
    ```
  - Change port
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --ei port port_value
    ## port value must be an integer
    ```
  - Change proxyType
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es proxytype "proxy_type_value"
    ## proxy_type_value must be one of the following values: HTTP, HTTPS, SOCKS4, SOCKS5, HTTP-Tunnel
    ```
  - Change authenticate mode
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --ez isAuth true|false
    ```
  - Change username
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es username "abcxyz"
    ```
  - Change password
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es password "abcxyz"
    ```
  - Change autoconnect mode
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --ez isAutoSetProxy true|false
    ``` 
  - Change PAC mode
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --ez isPAC true|false
    ``` 
  - Change bound or never bound ssid list
    - You must restart app to see change.
    - If the number of ssids is more than 1, they must be separated by commas
    - Alternatively, you can use the following defaults: "WIFI", "WIFI/2G/3G" or "2G/3G"
    - Set bound ssid list
      ```
      adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es setssid "WIFI, wifi_chua, 2G/3G"
      ```
    - Set never bound ssid list
      ```
      adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es setexcludedssid "WIFI, wifi_chua, 2G/3G"
      ```
    - Add bound ssid list
      ```
      adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es addssid "trachanhsuoi"
      ```
    - Add never bound ssid list
      ```
      adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es addexcludedssid "trachanhsuoi"
      ```
    - Remove bound ssid list
      ```
      adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es delssid "trachanhsuoi, 2G/3G"
      ```
    - Remove never bound ssid list
      ```
      adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es delexcludedssid "trachanhsuoi, 2G/3G"
      ```
    
  - Change bypass address list
    - Set bypass address list
      ```
      adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es setssid "WIFI, wifi_chua, 2G/3G"
      ```
    - Add bypass address list
      ```
      adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es setexcludedssid "WIFI, wifi_chua, 2G/3G"
      ```
    - Remove bypass address list
      ```
      adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es addssid "trachanhsuoi"
      ```
  - Change NTLM mode
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --ez isNTLM true|false
    ```
  - Change domain
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es domain "abcxyz.org"
    ```
  - Change certificate
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es certificate "abcxyzghik......"
    ```
  - Change global proxy mode
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --ez isGlobalProxy true|false
    ```
  - Change bypass app mode
    ```
    adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --ez isBypassApps true|false
    ```
  - Change bypass app list
    - you must use package name instead of app name
    - Set Bypass or proxyed app list
      ```
      adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es setproxyedapp "com.facebook.katana, org.proxydroid"
      ```
    - Add bypass or proxyed app list
      ```
      adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es addproxyedapp "com.facebook.katana, org.proxydroid"
      ```
    - Remove bypass or proxyed app list
      ```
      adb shell am broadcast -a org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION --es delproxyedapp "com.facebook.katana, org.proxydroid"
      ```
      
    
