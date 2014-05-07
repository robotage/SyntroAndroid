//
//  Copyright (c) 2014 richards-tech.
//
//  This file is part of SyntroNet
//
//  SyntroNet is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  SyntroNet is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with SyntroNet.  If not, see <http://www.gnu.org/licenses/>.
//

package com.rt.syntroview;

import java.util.ArrayList;
import java.util.Collections;

import com.pansenti.syntroview.R;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class StreamListFragment extends ListFragment {
	static private final String TAG = "StreamListFragment";
	ListSelectionListener mListener = null;

	private ArrayList<String> streamList = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	
	public interface ListSelectionListener {
		public void onListSelection(String streamName);
	}

	public void setSources(ArrayList<String> sources) {
		streamList = sources;
		Collections.sort(streamList, String.CASE_INSENSITIVE_ORDER);
		adapter.clear();
		adapter.addAll(streamList);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (ListSelectionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnArticleSelectedListener");
		}
		Log.d(TAG, "onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.list_item, streamList);
		setListAdapter(adapter);
		Log.d(TAG, "onActivityCreated");
	}

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id) {
		getListView().setItemChecked(pos, true);
		mListener.onListSelection(streamList.get(pos));
	}
}