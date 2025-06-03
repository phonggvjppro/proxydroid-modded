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

import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.proxydroid.utils.Constraints;
import org.proxydroid.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class BypassListActivity extends AppCompatActivity implements
		OnClickListener, OnItemClickListener, OnItemLongClickListener {

	private static final String TAG = BypassListActivity.class.getName();

	private static final int MSG_ERR_ADDR = 0;
	private static final int MSG_ADD_ADDR = 1;
	private static final int MSG_EDIT_ADDR = 2;
	private static final int MSG_DEL_ADDR = 3;
	private static final int MSG_PRESET_ADDR = 4;
	private static final int MSG_IMPORT_ADDR = 5;
	private static final int MSG_EXPORT_ADDR = 6;

	private ListAdapter adapter;
	private ArrayList<String> bypassList;
	private final Profile profile = new Profile();

	private final ActivityResultLauncher<Intent>  importReqResultLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result -> {
				if (result.getResultCode() == RESULT_OK) {
					if (result.getData() == null)
						return;
					final String path = result.getData().getStringExtra(Constraints.FILE_PATH);
					if (path == null || path.equals(""))
						return;

					final AlertDialog ad = new AlertDialog.Builder(this)
							.setMessage(getString(R.string.importing))
							.setCancelable(true)
							.create();
					ad.show();

					final Handler h = new Handler(Looper.getMainLooper()) {
						@Override
						public void handleMessage(Message msg) {
							refreshList();
							ad.dismiss();
						}
					};

					new Thread() {
						@Override
						public void run() {
							Process su;
							try {
								su = Runtime.getRuntime().exec(Utils.getRootShell());
								DataOutputStream os = new DataOutputStream(su.getOutputStream());
								BufferedReader reader = new BufferedReader(new InputStreamReader(su.getInputStream()));

								os.writeBytes("cat " + path + "\n");
								os.writeBytes("exit\n");
								os.flush();
								su.waitFor();

								bypassList.clear();
								String line;
								while ((line = reader.readLine()) != null) {
									String addr = Profile.validateAddr(line);
									if (addr != null)
										bypassList.add(addr);
								}

								os.close();
								reader.close();
							} catch (Exception e) {
								Log.e(TAG, "error to invoke root shell", e);
								return;
							}


							FileInputStream input;
							try {
								input = new FileInputStream(path);
								BufferedReader br = new BufferedReader(
										new InputStreamReader(input));
								bypassList.clear();
								while (true) {
									String line = br.readLine();
									if (line == null)
										break;

								}
								br.close();
								input.close();
							} catch (FileNotFoundException e) {
								Log.e(TAG, "error to open file", e);
							} catch (IOException e) {
								Log.e(TAG, "error to read file", e);
							}
							h.sendEmptyMessage(MSG_IMPORT_ADDR);
						}
					}.start();
				}
			}
	);

	final Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			String addr;
			switch (msg.what) {
			case MSG_ERR_ADDR:
				Toast.makeText(BypassListActivity.this, R.string.err_addr,
						Toast.LENGTH_LONG).show();
				break;
			case MSG_ADD_ADDR:
				if (msg.obj == null)
					return;
				addr = (String) msg.obj;
				bypassList.add(addr);
				break;
			case MSG_EDIT_ADDR:
				if (msg.obj == null)
					return;
				addr = (String) msg.obj;
				bypassList.set(msg.arg1, addr);
				break;
			case MSG_DEL_ADDR:
				bypassList.remove(msg.arg1);
				break;
			case MSG_PRESET_ADDR:
				String[] list = Constraints.PRESETS[msg.arg1];
				reset(list);
				return;
			case MSG_EXPORT_ADDR:
				if (msg.obj == null)
					return;
				Toast.makeText(BypassListActivity.this,
						getString(R.string.exporting) + " " + (String) msg.obj,
						Toast.LENGTH_LONG).show();
				return;
			}
			refreshList();
			super.handleMessage(msg);
		}
	};

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		if(id == R.id.addBypassAddr) {
			editAddr(MSG_ADD_ADDR, -1);
		} else if(id == R.id.presetBypassAddr) {
			presetAddr();
		} else if(id == R.id.importBypassAddr) {
			importAddr();
		} else if(id == R.id.exportBypassAddr) {
			exportAddr();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.bypass_list);
		TextView addButton = findViewById(R.id.addBypassAddr);
		addButton.setOnClickListener(this);

		TextView presetButton = findViewById(R.id.presetBypassAddr);
		presetButton.setOnClickListener(this);

		TextView importButton = findViewById(R.id.importBypassAddr);
		importButton.setOnClickListener(this);

		TextView exportButton = findViewById(R.id.exportBypassAddr);
		exportButton.setOnClickListener(this);

		refreshList();


	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		editAddr(MSG_EDIT_ADDR, position);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		delAddr(position);
		return true;
	}

	private void presetAddr() {
		AlertDialog ad = new AlertDialog.Builder(this)
				.setTitle(R.string.preset_button)
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								/* User clicked Cancel so do some stuff */
							}
						})
				.setSingleChoiceItems(R.array.presets_list, -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (which >= 0
										&& which < Constraints.PRESETS.length) {
									Message msg = new Message();
									msg.what = MSG_PRESET_ADDR;
									msg.arg1 = which;
									handler.sendMessage(msg);
								}
								dialog.dismiss();
							}
						}).create();
		ad.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	private void importAddr() {
		importReqResultLauncher.launch(new Intent(this, FileChooser.class));
	}

	private void exportAddr() {
		if (profile == null)
			return;

		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(
				R.layout.alert_dialog_text_entry, null);
		final EditText path = textEntryView.findViewById(R.id.text_edit);

		path.setText(Utils.getDataPath(this) + "/" + profile.getHost() + ".opt");

		AlertDialog ad = new AlertDialog.Builder(this)
				.setTitle(R.string.export_button)
				.setView(textEntryView)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (path.getText() == null)
									dialog.dismiss();
								new Thread() {
									@Override
									public void run() {
										Process su;
										try {
											su = Runtime.getRuntime().exec(Utils.getRootShell());
											DataOutputStream os = new DataOutputStream(su.getOutputStream());
											os.writeBytes("rm -f " + path.getText() + "\n");

											os.writeBytes("cat > " + path.getText() + "<<EOF\n");
											for (String addr : bypassList) {
												addr = Profile.validateAddr(addr);
												if (addr != null)
													os.writeBytes(addr + "\n");
											}
											os.writeBytes("EOF\n");
											os.writeBytes("exit\n");
											os.flush();

											su.waitFor();
									        os.close();
										} catch (Exception e) {
											Log.e(TAG, "error to invoke root shell", e);
										}

										Message msg = new Message();
										msg.what = MSG_EXPORT_ADDR;
										msg.obj = path.getText().toString();
										handler.sendMessage(msg);
									}
								}.start();

							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								/* User clicked cancel so do some stuff */
							}
						}).create();
		ad.show();
	}

	private void delAddr(final int idx) {

		final String addr = bypassList.get(idx);

		AlertDialog ad = new AlertDialog.Builder(this)
				.setTitle(addr)
				.setMessage(R.string.bypass_del_text)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								/* User clicked OK so do some stuff */
								Message msg = new Message();
								msg.what = MSG_DEL_ADDR;
								msg.arg1 = idx;
								msg.obj = addr;
								handler.sendMessage(msg);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								/* User clicked Cancel so do some stuff */
							}
						}).create();

		ad.show();
	}

	private void editAddr(final int msg, final int idx) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(
				R.layout.alert_dialog_text_entry, null);
		final EditText addrText = textEntryView.findViewById(R.id.text_edit);

		if (msg == MSG_EDIT_ADDR)
			addrText.setText(bypassList.get(idx));
		else if (msg == MSG_ADD_ADDR)
			addrText.setText("0.0.0.0/0");

		AlertDialog ad = new AlertDialog.Builder(this)
				.setTitle(R.string.bypass_edit_title)
				.setView(textEntryView)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								/* User clicked OK so do some stuff */

								new Thread() {
									@Override
									public void run() {
										EditText addrText = textEntryView.findViewById(R.id.text_edit);
										String addr = addrText.getText()
												.toString();
										addr = Profile.validateAddr(addr);
										if (addr != null) {
											Message m = new Message();
											m.what = msg;
											m.arg1 = idx;
											m.obj = addr;
											handler.sendMessage(m);
										} else {
											handler.sendEmptyMessage(MSG_ERR_ADDR);
										}
									}
								}.start();

							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked cancel so do some stuff */
							}
						}).create();
		ad.show();
	}

	private void reset(final String[] list) {

		final AlertDialog ad = new AlertDialog.Builder(this)
				.setMessage(getString(R.string.reseting))
				.setCancelable(true)
				.create();
		ad.show();

		final Handler h = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				refreshList();
				ad.dismiss();
			}
		};

		new Thread() {
			@Override
			public void run() {
				bypassList.clear();
				for (String addr : list) {
					addr = Profile.validateAddr(addr);
					if (addr != null)
						bypassList.add(addr);
				}
				h.sendEmptyMessage(0);
			}
		}.start();
	}

	private void refreshList() {

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);

		profile.getProfile(settings);

		if (bypassList != null) {
			profile.setBypassAddrs(Profile.encodeAddrs(bypassList
					.toArray(new String[0])));
			profile.setProfile(settings);
		}

		String[] addrs = Profile.decodeAddrs(profile.getBypassAddrs());
		bypassList = new ArrayList<String>();

		Collections.addAll(bypassList, addrs);

		final LayoutInflater inflater = getLayoutInflater();

		adapter = new ArrayAdapter<String>(this, R.layout.bypass_list_item,
				R.id.bypasslistItemText, bypassList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				String addr;
				if (convertView == null) {
					// Inflate a new view
					convertView = inflater.inflate(R.layout.bypass_list_item,
							parent, false);
				}

				TextView item = (TextView) convertView
						.findViewById(R.id.bypasslistItemText);
				addr = bypassList.get(position);
				if (addr != null)
					item.setText(addr);

				return convertView;
			}
		};

		ListView list = findViewById(R.id.BypassListView);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
	}
}
