package com.teamPrime.sm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.teamPrime.sm.history.HistoryItem;

public class HistoryActivity extends ListActivity {
	private static final String SHARED_PREF_NAME = "SafeMode - History";
	
	private ArrayAdapter<HistoryItem> adapter;
	private static List<HistoryItem> items;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		populateItems();
		adapter = new ArrayAdapter<HistoryItem>(this,android.R.layout.simple_list_item_1,items);
		setListAdapter(adapter);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		saveItems();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
    	menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Clear History");
        return result;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	SharedPreferences data = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
    	Editor edit = data.edit();
    	edit.putInt("numItems", 0);
    	edit.commit();
    	items.clear();
        return super.onOptionsItemSelected(item);
    }
	
	private void populateItems(){
		SharedPreferences data = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
		items = new ArrayList<HistoryItem>();
		int numItems = data.getInt("numItems", 0);
		for (int i = 0; i<numItems; i++){
			ObjectInputStream ois = null;
			try{
				ByteArrayInputStream bais = new ByteArrayInputStream(data.getString("item"+i, "").getBytes());
				if (bais.available()>0){
					ois = new ObjectInputStream(bais);
					items.add((HistoryItem)ois.readObject());
				}
			} catch (ClassNotFoundException ex){
				ex.printStackTrace();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void saveItems(){
		if (items == null || items.isEmpty()) return;
		SharedPreferences data = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
		Editor edit = data.edit();
		edit.putInt("numItems", items.size());
		int i = 0;
		for (HistoryItem hi : items){
			ObjectOutputStream oos = null;
			try{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(baos);
				oos.writeObject(hi);
				edit.putString("item"+(i++), new String(baos.toByteArray()));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		edit.commit();
	}
	
	public static int addItem(Context c, HistoryItem hi){
		SharedPreferences data = c.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
		int numItems = data.getInt("numItems", -1);
		Editor edit = data.edit();
		ObjectOutputStream oos = null;
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(hi);
			edit.putString("item"+(++numItems), new String(baos.toByteArray()));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return numItems;
	}
}
