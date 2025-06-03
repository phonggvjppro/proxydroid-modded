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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.proxydroid.utils.Utils;

import java.util.Arrays;

public class ProxyDroidCLI extends BroadcastReceiver {
	public static final String TOGGLE_ACTION = "org.proxydroid.TOGGLE_STATE";
	public static final String CONFIG_ACTION = "org.proxydroid.PROFILE_CONFIGURE";
	public static final String PROFILE_ACTION = "org.proxydroid.PROFILE_CHANGE";
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent == null) return;
		if(TOGGLE_ACTION.equals(intent.getAction())) {
			boolean toggleStart = intent.getBooleanExtra("start", true);
			if (toggleStart) {
				if (!Utils.isWorking()) {
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

					Profile profile = new Profile();
					profile.getProfile(settings);
					try {
						Intent it = new Intent(context, ProxyDroidService.class);
						Bundle bundle = new Bundle();
						bundle.putString("host", profile.getHost());
						bundle.putString("user", profile.getUser());
						bundle.putString("bypassAddrs", profile.getBypassAddrs());
						bundle.putString("password", profile.getPassword());
						bundle.putString("domain", profile.getDomain());
						bundle.putString("certificate", profile.getCertificate());

						bundle.putString("proxyType", profile.getProxyType());
						bundle.putBoolean("isAutoSetProxy", profile.isAutoSetProxy());
						bundle.putBoolean("isBypassApps", profile.isBypassApps());
						bundle.putBoolean("isAuth", profile.isAuth());
						bundle.putBoolean("isNTLM", profile.isNTLM());
						bundle.putBoolean("isPAC", profile.isPAC());

						bundle.putInt("port", profile.getPort());

						it.putExtras(bundle);
						context.startService(it);
						setResultData("SUCCESS");

					} catch (Exception ignore) {
						// Nothing
						setResultData("FAILURE");
					}
				}
			} else {
				if (Utils.isWorking() && !Utils.isConnecting()) {
					try {
						context.stopService(new Intent(context, ProxyDroidService.class));
						setResultData("SUCCESS");
					} catch (Exception e) {
						setResultData("FAILURE");
					}
				}
			}
		} else if(CONFIG_ACTION.equals(intent.getAction())) {
			if(Utils.isWorking()) {
				return;
			}

			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			Profile profile = new Profile();
			profile.getProfile(settings);

			String host = intent.getStringExtra("host");
			if(host == null) host = profile.getHost();
			profile.setHost(host);

			int port = intent.getIntExtra("port", profile.getPort());
			if(port < 1) port = profile.getPort();
			profile.setPort(port);

			String user = intent.getStringExtra("user");
			if(user == null) user = profile.getUser();
			profile.setUser(user);

			String password = intent.getStringExtra("password");
			if(password == null) password = profile.getPassword();
			profile.setPassword(password);

			String domain = intent.getStringExtra("domain");
			if(domain == null) domain = profile.getDomain();
			profile.setDomain(domain);

			String certificate = intent.getStringExtra("certificate");
			if (certificate == null) certificate = profile.getCertificate();
			profile.setCertificate(certificate);

			String bypassAddrs = intent.getStringExtra("bypassAddrs");
			if (bypassAddrs == null) bypassAddrs = profile.getBypassAddrs();
			profile.setBypassAddrs(bypassAddrs);

			String proxyType = intent.getStringExtra("proxyType");
			if (proxyType == null || !Arrays.asList("http", "https", "http-tunnel", "socks4", "socks5").contains(proxyType)) {
				proxyType = profile.getProxyType();
			}
			profile.setProxyType(proxyType);

			boolean isAutoSetProxy = intent.getBooleanExtra("isAutoSetProxy", profile.isAutoSetProxy());
			profile.setAutoSetProxy(isAutoSetProxy);

			boolean isBypassApps = intent.getBooleanExtra("isBypassApps", profile.isBypassApps());
			profile.setBypassApps(isBypassApps);

			boolean isAuth = intent.getBooleanExtra("isAuth", profile.isAuth());
			profile.setAuth(isAuth);

			boolean isNTLM = intent.getBooleanExtra("isNTLM", profile.isNTLM());
			profile.setNTLM(isNTLM);

			boolean isPAC = intent.getBooleanExtra("isPAC", profile.isPAC());
			profile.setPAC(isPAC);

			profile.setProfile(settings);

		} else if(PROFILE_ACTION.equals(intent.getAction())) {
			if(Utils.isWorking()) return;

			String action = intent.getStringExtra("action");
			if("switch".equals(action)) {
				int profileId = intent.getIntExtra("profileId", 1);
				if(profileId < 1) profileId = 1;

				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
				String oldProfileId = settings.getString("profile", "1");

		        settings.edit().putString("profile", Integer.toString(profileId)).commit();
				boolean ret = Profile.ProfileUtils.switchProfile(oldProfileId, Integer.toString(profileId), context);
				if(ret) {
					setResultData("SUCCESS");
				} else {
					setResultData("FAILURE");
				}

			} else if("rename".equals(action)) {
				String profileId = PreferenceManager.getDefaultSharedPreferences(context).getString("profile", "1");

				String newName = intent.getStringExtra("newName");
				if(newName == null || newName.isEmpty()) {
					setResultData("INVALID_NAME");
					return;
				}
				newName = newName.replace("|", "");

				Profile.ProfileUtils.renameProfile(profileId, newName, context);

				setResultData("SUCCESS");
			} else if("delete".equals(action)) {
				String profileId = PreferenceManager.getDefaultSharedPreferences(context).getString("profile", "1");
				Profile.ProfileUtils.delProfile(profileId, context);
				setResultData("SUCCESS");

			} else if("add".equals(action)) {
				Profile.ProfileUtils.addProfile(context);
			}

			PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("profileChange", SystemClock.uptimeMillis()).commit();

		}
	}

}
