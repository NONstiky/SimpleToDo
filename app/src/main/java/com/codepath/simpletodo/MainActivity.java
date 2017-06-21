package com.codepath.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // a numeric code to identify the edit activity
    public final static int EDIT_REQUEST_CODE =20;
    // keys used for passing data between activities
    public final static String ITEM_TEXT = "itemText";
    public final static String ITEM_POSITION = "itemPosition";
    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    ListView lvItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // obtain a reference to the ListView created with the layout
        lvItems = (ListView) findViewById(R.id.lvItems);
        // initialize the items list
        readItems();
        // initialize the adapter using the items list
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        // wire the adapter to the view
        lvItems.setAdapter(itemsAdapter);

        // add some mock items to the list
        //items.add("First todo item");
        //items.add("Second todo item");

        // setup the listener on creation
        setUpListViewListener();
    }

    public void onAddItem(View v){
        EditText etNewItem = (EditText) findViewById(R.id.etNewItem);
        String itemText = etNewItem.getText().toString();
        itemsAdapter.add(itemText);
        etNewItem.setText("");
        writeItems();
        Toast.makeText(getApplicationContext(),"Item added to list",Toast.LENGTH_SHORT).show();
    }

    private void setUpListViewListener(){
        Log.i("MainActivity", "Setting up listener on list view");
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.i("MainActivity", "Item removed from list: " + position);
                items.remove(position);
                itemsAdapter.notifyDataSetChanged();
                writeItems();
                return true;
            }
        });

        // set the ListView's regular click listener
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // first parameter is the context, second is the class of the activity to launch
                Intent i = new Intent(MainActivity.this, EditItemActivity.class);
                // put "extras" into the bundle for access in the edit activity
                i.putExtra(ITEM_TEXT, items.get(position));
                i.putExtra(ITEM_POSITION, position);
                // brings up the edit activity with the expectation of a result
                startActivityForResult(i, EDIT_REQUEST_CODE);
            }
        });

    }

    // handle results from edit activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // EDIT_REQUEST_CODE defined with constants
        if (resultCode == RESULT_OK && requestCode == EDIT_REQUEST_CODE) {
            // extract updated item value from result extras
            String updatedItem = data.getExtras().getString(ITEM_TEXT);
            // get the position of the item which was edited
            int position = data.getExtras().getInt(ITEM_POSITION, 0);
            // update the model with the new item text at the edited position
            items.set(position, updatedItem);
            // notify the adapter the model changed
            itemsAdapter.notifyDataSetChanged();
            //Add Change to persistance
            writeItems();
            // notify the user the operation completed OK
            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
        }
    }

    // returns the file in which the data is stored
    private File getDataFile() {
        return new File(getFilesDir(), "todo.txt");
    }

    // read the items from the file system
    private void readItems() {
        try {
            // create the array using the content in the file
            items = new ArrayList<String>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e) {
            // log the error
            Log.e("MainActivity", "Error reading file", e);
            // just load an empty list
            items = new ArrayList<>();
        }
    }

    // write the items to the filesystem
    private void writeItems() {
        try {
            // save the item list as a line-delimited text file
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            // print the error to the console
            Log.e("MainActivity", "Error writing file", e);
        }
    }
}
