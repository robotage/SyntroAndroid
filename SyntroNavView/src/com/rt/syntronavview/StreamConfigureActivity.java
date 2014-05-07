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

package com.rt.syntronavview;

import com.rt.syntronavview.StreamListFragment.ListSelectionListener;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class StreamConfigureActivity extends Activity implements ListSelectionListener {

	private FragmentManager fragmentManager = null;
	private final StreamListFragment streamListFragment = new StreamListFragment();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.stream_view);
		TextView tv = (TextView)findViewById(R.id.header);
		tv.setText(R.string.stream_list_header);
		fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.add(R.id.fragment_container, streamListFragment);
		fragmentTransaction.commit();
        Bundle extras = getIntent().getExtras();
        streamListFragment.setSources(extras.getStringArrayList(getString(R.string.stream_list)));		
	}
		
 	@Override
	public void onListSelection(String streamName) {
 		Intent intent = new Intent(this, NavViewControllerActivity.class);
 		intent.putExtra(getString(R.string.selected_stream), streamName);	
 		setResult(1, intent);
 		finish();
	}
}
