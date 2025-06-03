package org.proxydroid;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.ListFragment;

import org.proxydroid.utils.Constraints;
import org.proxydroid.utils.Option;
import org.proxydroid.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileChooser extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(androidx.appcompat.R.style.Theme_AppCompat_DayNight);
		FrameLayout frameLayout = new FrameLayout(this);
		frameLayout.setId(View.generateViewId());
		setContentView(frameLayout, new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT));

		if(savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(frameLayout.getId(), new FileChooserFragment())
					.commit();
		}

	}


	public static class FileChooserFragment extends ListFragment {

		private File currentDir;
		private FileArrayAdapter adapter;


		private void fill(File f) {

			requireActivity().setTitle(getString(R.string.current_dir) + ": " + f.getName());
			List<Option> dir = new ArrayList<Option>();
			List<Option> fls = new ArrayList<Option>();
			try {
				String[] files = Utils.runRootCommandResult("find " + f.getAbsolutePath() + " -maxdepth 1 -type f").split("\n");
				String[] dirs = Utils.runRootCommandResult("find " + f.getAbsolutePath() + " -maxdepth 1 -type d").split("\n");

				for(String s : dirs) {
					if(s.equals(f.getAbsolutePath())) continue;
					if (s.isEmpty()) continue;
					File ff = new File(s);
					if (ff.isDirectory())
						dir.add(new Option(ff.getName(),
								getString(R.string.folder), ff.getAbsolutePath()));

				}
				for (String s : files) {
					if (s.isEmpty()) continue;
					File ff = new File(s);
					fls.add(new Option(ff.getName(),
							getString(R.string.file_size) + ": " + ff.length() + " bytes", ff
							.getAbsolutePath()));
				}
			} catch (Exception e) {

			}
			Collections.sort(dir);
			Collections.sort(fls);
			dir.addAll(fls);
			dir.add(0,new Option("..", getString(R.string.parent_dir), f
								.getParent()));
			adapter = new FileArrayAdapter(requireActivity(), R.layout.file_view,
					dir);
			this.setListAdapter(adapter);
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			currentDir = new File(Utils.getDataPath(requireActivity()));
			fill(currentDir);
		}

		private void onFileClick(Option o) {
			File inputFile = new File(o.getPath());
			if (inputFile.exists() && inputFile.length() < 32 * 1024) {
				Toast.makeText(requireActivity(), getString(R.string.file_toast) + o.getPath(),
						Toast.LENGTH_SHORT).show();
				Intent i = new Intent();
				i.putExtra(Constraints.FILE_PATH, o.getPath());
				requireActivity().setResult(RESULT_OK, i);
			} else {
				Toast.makeText(requireActivity(), getString(R.string.file_error) + o.getPath(),
						Toast.LENGTH_SHORT).show();
				requireActivity().setResult(RESULT_CANCELED);
			}

			requireActivity().finish();
		}

		@Override
		public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
			super.onListItemClick(l, v, position, id);
			Option o = adapter.getItem(position);
			if (o == null || o.getPath() == null) return;
			if (o.getData().equalsIgnoreCase(getString(R.string.folder))
					|| o.getData().equalsIgnoreCase(getString(R.string.parent_dir))) {
				currentDir = new File(o.getPath());
				fill(currentDir);
			} else {
				onFileClick(o);
			}
		}
	}



}