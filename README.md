# PROXYDROID PLUS
## Description

- This is a modified version of proxydroid that supports tweaking configuration via command line.
- Now support from Android 5.0 to Android 14 ( tested on Android 15)
- Here is the link to the original version of proxydroid: [ProxyDroid](https://github.com/madeye/proxydroid)

## Usage
> Note: Root access is required

- Install ProxyDroid Plus apk
- Start the app for the first time to create the default configuration and make broadcast receiver work.
- Sending Broadcast intent via Adb or your Android Services/Apps.

## Command
### Profile Management

- Create a new profile and switch to it:
  ```
  adb shell am broadcast -a org.proxydroid.PROFILE_CHANGE --es action add
  ```
- Switch to the existing profile:
  ```
  adb shell am broadcast -a org.proxydroid.PROFILE_CHANGE --es action switch --ei profileId <profileId>
  ```
- Delete current profile:
  ```
  adb shell am broadcast -a org.proxydroid.PROFILE_CHANGE --es action delete 
  ```
- List all profiles:
  ```
  adb shell am broadcast -a org.proxydroid.PROFILE_CHANGE --es action list
  ```
- Rename current profile:
  ```
  adb shell am broadcast -a org.proxydroid.PROFILE_CHANGE --es action rename --es newName <newName>
  ```
### Proxy Configuration
- Set a property:
  ```
  adb shell am broadcast -a org.proxydroid.PROFILE_CONFIGURE <property> <value>
  ```

- List all properties:

  | Name           | Type           | Description                                                                                             |
    |----------------|----------------|---------------------------------------------------------------------------------------------------------|
  | proxyType      | string         | one of: http, https, http-tunnel, socks4, socks5                                                        |
  | host           | string         | Proxy host address                                                                                      |
  | port           | int            | Proxy port number                                                                                       |
  | isPAC          | bool           | Use PAC file. If true, the host prop will be used as PAC url                                            |
  | isGlobalProxy  | bool           | Set global proxy                                                                                        |
  | isAuth         | bool           | Use authentication for proxy                                                                            |
  | user           | string         | Proxy username                                                                                          |
  | password       | string         | Proxy password                                                                                          |
  | bypassAddrs    | string         | List of hosts to bypass the proxy, separated by pipe                                                    |
  | isNTLM         | bool           | Use NTLM authentication for proxy                                                                       |
  | domain         | string         | Domain name for NTLM 								                      |
  | proxyedApps    | string         | List of apps (package name) to use proxy, separated by pipe. If empty, all apps will use proxy          |
  | isBypassApps   | bool           | If true, the proxy will be bypassed for the apps in proxyedApps                                         |

### Connect Proxy
- Connect to the proxy:
  ```
  adb shell am broadcast -a org.proxydroid.TOGGLE_STATE --ez start true
  ```
- Disconnect from the proxy:
  ```
    adb shell am broadcast -a org.proxydroid.TOGGLE_STATE --ez start false
  ```

> **Note:** 
> - From Android 8.0, you need to use '-n org.proxydroid/.ProxyDroidCLI' to send broadcast
> - From Android 12.0, you need to open app before using broadcast to start proxying, due to background execution limits
