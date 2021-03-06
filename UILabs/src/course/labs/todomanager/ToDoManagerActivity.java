package course.labs.todomanager;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import course.labs.todomanager.ToDoItem.Priority;
import course.labs.todomanager.ToDoItem.Status;

import java.io.*;
import java.text.ParseException;
import java.util.Date;

public class ToDoManagerActivity extends ListActivity {

    // Add a ToDoItem Request Code
    private static final int ADD_TODO_ITEM_REQUEST = 0;

    private static final String FILE_NAME = "TodoManagerActivityData.txt";
    private static final String TAG = "Lab-UserInterface";

    // IDs for menu items
    private static final int MENU_DELETE = Menu.FIRST;
    private static final int MENU_DUMP = Menu.FIRST + 1;

    // Ids for context menu items
    private static final int CONTEXT_MENU_DELETE = Menu.FIRST;

    ToDoListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Create a new TodoListAdapter for this ListActivity's ListView
        mAdapter = new ToDoListAdapter(getApplicationContext());

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new TabChangeListener(mAdapter);
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_order_by_title).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_order_by_dead_line).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_order_by_priority).setTabListener(tabListener));

        // Put divider between ToDoItems and FooterView
        ListView listView = getListView();

        registerForContextMenu(listView);

        listView.setFooterDividersEnabled(true);

        LayoutInflater inflater = getLayoutInflater();
        TextView footerView = (TextView) inflater.inflate(R.layout.footer_view, listView, false);

        listView.addFooterView(footerView);

        footerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                log("Entered footerView.OnClickListener.onClick()");

                Intent intent = new Intent(ToDoManagerActivity.this, AddToDoActivity.class);
                startActivityForResult(intent, ADD_TODO_ITEM_REQUEST);

            }
        });

        setListAdapter(mAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        log("Entered onActivityResult()");

        if (requestCode == ADD_TODO_ITEM_REQUEST && resultCode == RESULT_OK) {
            ToDoItem toDoItem = new ToDoItem(data);
            mAdapter.add(toDoItem);
        }

    }

    // Do not modify below here

    @Override
    public void onResume() {
        super.onResume();

        // Load saved ToDoItems, if necessary
        if (mAdapter.getCount() == 0)
            loadItems();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save ToDoItems
        saveItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, "Delete all");
        menu.add(Menu.NONE, MENU_DUMP, Menu.NONE, "Dump to log");
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(mAdapter.getItem(info.position).getTitle());
        menu.add(Menu.NONE, CONTEXT_MENU_DELETE, CONTEXT_MENU_DELETE, R.string.del_item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case CONTEXT_MENU_DELETE:
                mAdapter.remove(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_DELETE:
                mAdapter.clear();
                return true;
            case MENU_DUMP:
                dump();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void dump() {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            String data = ((ToDoItem) mAdapter.getItem(i)).toLog();
            log("Item " + i + ": " + data.replace(ToDoItem.ITEM_SEP, ","));
        }
    }

    // Load stored ToDoItems
    private void loadItems() {
        BufferedReader reader = null;
        try {
            FileInputStream fis = openFileInput(FILE_NAME);
            reader = new BufferedReader(new InputStreamReader(fis));

            String title = null;
            String priority = null;
            String status = null;
            Date date = null;

            while (null != (title = reader.readLine())) {
                priority = reader.readLine();
                status = reader.readLine();
                date = ToDoItem.FORMAT.parse(reader.readLine());
                mAdapter.add(new ToDoItem(title, Priority.valueOf(priority),
                        Status.valueOf(status), date));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Save ToDoItems to file
    private void saveItems() {
        PrintWriter writer = null;
        try {
            FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    fos)));

            for (int idx = 0; idx < mAdapter.getCount(); idx++) {

                writer.println(mAdapter.getItem(idx));

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != writer) {
                writer.close();
            }
        }
    }

    private void log(String msg) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, msg);
    }

    private class TabChangeListener implements ActionBar.TabListener {
        private final ToDoListAdapter mAdapter;

        public TabChangeListener(ToDoListAdapter mAdapter) {
            this.mAdapter = mAdapter;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            switch (tab.getPosition()) {
                case 1:
                    mAdapter.orderByDeadLine();
                    return;
                case 2:
                    mAdapter.orderByPriorityAndDeadLine();
                    return;
                default:
                    mAdapter.orderByTitle();
                    return;
            }
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            //none
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            //none
        }
    }
}