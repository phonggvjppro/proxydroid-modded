/* proxydroid - Global / Individual Proxy App for Android
 * Copyright (C) 2011 Max Lv <max.c.lv@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 *                            ___====-_  _-====___
 *                      _--^^^#####//      \\#####^^^--_
 *                   _-^##########// (    ) \\##########^-_
 *                  -############//  |\^^/|  \\############-
 *                _/############//   (@::@)   \\############\_
 *               /#############((     \\//     ))#############\
 *              -###############\\    (oo)    //###############-
 *             -#################\\  / VV \  //#################-
 *            -###################\\/      \//###################-
 *           _#/|##########/\######(   /\   )######/\##########|\#_
 *           |/ |#/\#/\#/\/  \#/\##\  |  |  /##/\#/  \/\#/\#/\#| \|
 *           `  |/  V  V  `   V  \#\| |  | |/#/  V   '  V  V  \|  '
 *              `   `  `      `   / | |  | | \   '      '  '   '
 *                               (  | |  | |  )
 *                              __\ | |  | | /__
 *                             (vvv(VVV)(VVV)vvv)
 *
 *                              HERE BE DRAGONS
 *
 */

package org.proxydroid;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.proxydroid.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProxyDroidWidgetProvider extends AppWidgetProvider {

	public static final String PROXY_SWITCH_ACTION = "org.proxydroid.ProxyDroidWidgetProvider.PROXY_SWITCH_ACTION";
	public static final String CHANGE_CONFIG_ACTION = "org.proxydroid.ProxyDroidWidgetProvider.CHANGE_CONFIG_ACTION";
	public static final String SERVICE_NAME = "org.proxydroid.ProxyDroidService";

	public static final String TAG = "ProxyDroidWidgetProvider";

	@SuppressLint("LongLogTag")
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;


	}

	@SuppressLint("LongLogTag")
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if(!Utils.isRoot()) {
			setResultCode(-1);
			setResultData("No root access");
			return;
		}
//		if (intent.getAction().equals(PROXY_SWITCH_ACTION)) {
//
//
//			Log.d(TAG, "Proxy switch action");
//			// do some really cool stuff here
//			Boolean hasExtra = intent.hasExtra("switch");
//			Integer action = intent.getIntExtra("switch", 0);
//			if(action!=0 && action !=1) action = 1;
//			if ((hasExtra && action ==0) || (!hasExtra && Utils.isWorking())) {
//				// Service is working, so stop it
//				try {
//					context.stopService(new Intent(context,
//							ProxyDroidService.class));
//				} catch (Exception e) {
//					// Nothing
//				}
//
//			} else {
//
//				// Service is not working, then start it
//				SharedPreferences settings = PreferenceManager
//						.getDefaultSharedPreferences(context);
//				Profile mProfile = new Profile();
//				mProfile.getProfile(settings);
//
//				Intent it = new Intent(context, ProxyDroidService.class);
//				Bundle bundle = new Bundle();
//				bundle.putString("host", mProfile.getHost());
//				bundle.putString("user", mProfile.getUser());
//				bundle.putString("bypassAddrs", mProfile.getBypassAddrs());
//				bundle.putString("password", mProfile.getPassword());
//				bundle.putString("domain", mProfile.getDomain());
//				bundle.putString("proxyType", mProfile.getProxyType());
//				bundle.putString("certificate", mProfile.getCertificate());
//
//				bundle.putBoolean("isAutoSetProxy", mProfile.isAutoSetProxy());
//				bundle.putBoolean("isBypassApps", mProfile.isBypassApps());
//				bundle.putBoolean("isAuth", mProfile.isAuth());
//				bundle.putBoolean("isNTLM", mProfile.isNTLM());
//				bundle.putBoolean("isDNSProxy", mProfile.isDNSProxy());
//				bundle.putBoolean("isPAC", mProfile.isPAC());
//
//				bundle.putInt("port", mProfile.getPort());
//				it.putExtras(bundle);
//				context.startService(it);
//
//			}
//
//		}
//		if(intent.getAction().equals(CHANGE_CONFIG_ACTION)) {
//			setResultData("Success");
//			if(intent.getExtras() != null) {
//				if(Utils.isWorking()) {
//					try {
//						context.stopService(new Intent(context,
//								ProxyDroidService.class));
//					} catch (Exception e) {
//						// Nothing
//					}
//				}
//				SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(context);
//				SharedPreferences.Editor editor = setting.edit();
//				if(intent.hasExtra("setprofile")) {
//					String profile = intent.getStringExtra("setprofile");
//					if(profile!=null &&!profile.isEmpty()) {
//						List<String> profileEntries = new LinkedList<String>(Arrays.asList(setting.getString("profileEntries", "").split("\\|")));
//						profileEntries.remove("New Profile");
//						List<String> profileValues = new LinkedList<String>( Arrays.asList(setting.getString("profileValues", "").split("\\|")));
//						if(profileEntries.contains(profile)) {
//							editor.putString("profile", profileValues.get(profileEntries.indexOf(profile)));
//							editor.commit();
//						} else {
//							editor.putString("profile", "0"); editor.commit();
//							String index = Integer.toString(Integer.valueOf(profileValues.get(profileValues.size()-2))+1);
//							editor.putString("profile"+index, profile);
//							profileEntries.add(profile); profileEntries.add("New Profile");
//							profileValues.add(profileValues.size()-2, index);
//							editor.commit();
//							editor.putString("profile", index);
//							editor.commit();
//						}
//
//					}
//				}
//				if(intent.hasExtra("host")) {
//					String host = intent.getStringExtra("host");
//					if(host!=null) editor.putString("host", host);
//				}
//				if(intent.hasExtra("port")) {
//					Integer port = intent.getIntExtra("port", -1);
//					if(port>=0) editor.putString("port", port.toString());
//				}
//				if(intent.hasExtra("username")) {
//					String username = intent.getStringExtra("username");
//					if(username!=null) editor.putString("user", username);
//				}
//				if(intent.hasExtra("password")) {
//					String pass = intent.getStringExtra("password");
//					if(pass!=null) editor.putString("password", pass);
//				}
//				if(intent.hasExtra("proxytype")) {
//					String proxytype = intent.getStringExtra("proxytype");
//					if(proxytype!=null && (proxytype.equalsIgnoreCase("HTTP") || proxytype.equalsIgnoreCase("HTTPS") ||
//							proxytype.equalsIgnoreCase("HTTP-Tunnel") || proxytype.equalsIgnoreCase("SOCKS4") || proxytype.equalsIgnoreCase("SOCKS5")))
//						editor.putString("proxyType", proxytype.toLowerCase());
//				}
//				if(intent.hasExtra("setbypassAddress")) {
//					String bypassaddrs = intent.getStringExtra("setbypassAddress");
//					if(bypassaddrs!=null) {
//						editor.putString("bypassAddrs", Profile.encodeAddrs(bypassaddrs.split(",")));
//					}
//				}
//				if(intent.hasExtra("addbypassAddress")) {
//					String bypassaddrs = intent.getStringExtra("addbypassAddress");
//					if(bypassaddrs!=null) {
//						List<String> laddress =new LinkedList<String>(Arrays.asList(Profile.decodeAddrs(setting.getString("bypassAddrs", ""))));
//						for (String add : bypassaddrs.split(",")) {
//							if (!laddress.contains(add.trim())) laddress.add(add.trim());
//						}
//						String[] la = new String[laddress.size()]; laddress.toArray(la);
//						editor.putString("bypassAddrs", Profile.encodeAddrs(la));
//					}
//				}
//				if(intent.hasExtra("delbypassAddress")) {
//					String bypassaddrs = intent.getStringExtra("delbypassAddress");
//					if(bypassaddrs!=null) {
//						List<String> laddress =new LinkedList<String>(Arrays.asList(Profile.decodeAddrs(setting.getString("bypassAddrs", ""))));
//						for (String add : bypassaddrs.split(",")) {
//							if (laddress.contains(add.trim())) laddress.remove(add.trim());
//						}
//						String[] la = new String[laddress.size()]; laddress.toArray(la);
//						editor.putString("bypassAddrs", Profile.encodeAddrs(la));
//					}
//				}
//				if(intent.hasExtra("isAuth")) {
//					Boolean isAuth = intent.getBooleanExtra("isAuth", false);
//					editor.putBoolean("isAuth", isAuth);
//				}
//				if(intent.hasExtra("domain")) {
//					String domain = intent.getStringExtra("domain");
//					if(domain!=null) editor.putString("domain", domain);
//				}
//				if(intent.hasExtra("isBypassApps")) {
//					Boolean isbypassapp = intent.getBooleanExtra("isBypassApps", false);
//					editor.putBoolean("isBypassApps", isbypassapp);
//				}
//				if(intent.hasExtra("isNTLM")) {
//					Boolean isntlm = intent.getBooleanExtra("isNTLM", false);
//					editor.putBoolean("isNTLM", isntlm);
//				}
//				if(intent.hasExtra("isPAC")) {
//					Boolean ispac = intent.getBooleanExtra("isPAC", false);
//					editor.putBoolean("isPAC", ispac);
//				}
//				if(intent.hasExtra("setproxyedapp")) {
//					String proxyedapp = intent.getStringExtra("setproxyedapp");
//					if(proxyedapp!=null) {
//						String finalproxyedapp = TextUtils.join("|", getAppfromPackage(context, proxyedapp.split(",")));
//						editor.putString("Proxyed", finalproxyedapp);
//					}
//				}
//				if(intent.hasExtra("addproxyedapp")) {
//					String proxyedapp = intent.getStringExtra("addproxyedapp");
//					if(proxyedapp!=null) {
//						List<String> lapp =  getAppfromPackage(context, proxyedapp.split(","));
//						List<String>  aapp =new LinkedList<String>(Arrays.asList(setting.getString("Proxyed", "").split("\\|")));
//						for(String app : lapp) if(!aapp.contains(app)) aapp.add(app);
//						String finalproxyedapp = TextUtils.join("|", aapp);
//						editor.putString("Proxyed", finalproxyedapp);
//					}
//				}
//				if(intent.hasExtra("delproxyedapp")) {
//					String proxyedapp = intent.getStringExtra("delproxyedapp");
//					if(proxyedapp!=null) {
//						List<String> lapp =  getAppfromPackage(context, proxyedapp.split(","));
//						List<String>  aapp =new LinkedList<String>(Arrays.asList(setting.getString("Proxyed", "").split("\\|")));
//						for(String app : lapp) if(aapp.contains(app.trim())) aapp.remove(app);
//						String finalproxyedapp = TextUtils.join("|", aapp);
//						editor.putString("Proxyed", finalproxyedapp);
//					}
//				}
//				if(intent.hasExtra("isBypassApps")) {
//					Boolean isbypass = intent.getBooleanExtra("isBypassApps", false);
//					editor.putBoolean("isBypassApps", isbypass);
//				}
//				if(intent.hasExtra("isGlobalProxy")) {
//					Boolean isautoset = intent.getBooleanExtra("isGlobalProxy", false);
//					editor.putBoolean("isAutoSetProxy", isautoset);
//				}
//				if(intent.hasExtra("certificate")) {
//					String certificate = intent.getStringExtra("certificate");
//					if(certificate != null) {
//						editor.putString("certificate", certificate);
//					}
//				}
//				if(intent.hasExtra("setssid")) {
//					String lssid = intent.getStringExtra("setssid");
//					if(lssid!=null) {
//						List<String> listssid = new ArrayList<>();
//						for(String s:lssid.split(",")){
//							listssid.add(s.trim());
//						}
//						lssid = TextUtils.join(" , ", listssid);
//						editor.putString("ssid", lssid);
//						editor.commit();
//					}
//				}
//
//				editor.commit();
//				RemoteViews views = new RemoteViews(context.getPackageName(),
//						R.layout.proxydroid_appwidget);
//				try {
//					views.setImageViewResource(R.id.serviceToggle, R.drawable.ing);
//
//					AppWidgetManager awm = AppWidgetManager.getInstance(context);
//					awm.updateAppWidget(awm.getAppWidgetIds(new ComponentName(
//							context, ProxyDroidWidgetProvider.class)), views);
//				} catch (Exception ignore) {
//					// Nothing
//				}
//			}
//		}
	}

	private List<String> getAppfromPackage(Context context,String[] inp_packages) {
		List<String> appusername = new ArrayList<String>();
		PackageManager packages = context.getPackageManager();
		List<ApplicationInfo> lAppInfo = packages.getInstalledApplications(0);
		for(String pkg : inp_packages) {
			for(ApplicationInfo app : lAppInfo) {
				if(app.packageName.equals(pkg.trim())) appusername.add(packages.getNameForUid(app.uid));
			}
		}
		return appusername;
	}
}
