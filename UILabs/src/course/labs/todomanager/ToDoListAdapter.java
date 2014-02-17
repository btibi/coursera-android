package course.labs.todomanager;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import course.labs.todomanager.ToDoItem.Status;

import java.util.ArrayList;
import java.util.List;

public class ToDoListAdapter extends BaseAdapter {

    // List of ToDoItems
    private final List<ToDoItem> mItems = new ArrayList<ToDoItem>();

    private final Context mContext;

    private static final String TAG = "Lab-UserInterface";

    public ToDoListAdapter(Context context) {
        mContext = context;
    }

    // Add a ToDoItem to the adapter
    // Notify observers that the data set has changed

    public void add(ToDoItem item) {
        mItems.add(item);
        notifyDataSetChanged();
    }

    // Clears the list adapter of all items.

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void remove(int pos) {
        mItems.remove(pos);
        notifyDataSetChanged();
    }

    // Returns the number of ToDoItems

    @Override
    public int getCount() {
        return mItems.size();
    }

    // Retrieve the number of ToDoItems

    @Override
    public ToDoItem getItem(int pos) {
        return mItems.get(pos);
    }

    // Get the ID for the ToDoItem
    // In this case it's just the position

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    //Create a View to display the ToDoItem
    // at specified position in mItems

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ToDoItem toDoItem = (ToDoItem) getItem(position);

        // from todo_item.xml.
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.todo_item, parent, false);
        view.setLongClickable(true);

        refreshColorAndImage(toDoItem, view);

        // Remember that the data that goes in this View
        // corresponds to the user interface elements defined
        // in the layout file
        final TextView titleView = (TextView) view.findViewById(R.id.titleView);
        titleView.setText(toDoItem.getTitle());

        final CheckBox statusView = (CheckBox) view.findViewById(R.id.statusCheckBox);
        statusView.setChecked(toDoItem.getStatus() == Status.DONE);
        statusView.setOnCheckedChangeListener(new StatusChangeListener(toDoItem, view));

        final Spinner priorityView = (Spinner) view.findViewById(R.id.priorityView);
        priorityView.setSelection(toDoItem.getPriority().getPosition());
        priorityView.setOnItemSelectedListener(new PrioritySelectedListener(toDoItem));

        final TextView dateView = (TextView) view.findViewById(R.id.dateView);
        dateView.setText(ToDoItem.FORMAT.format(toDoItem.getDate()));

        // Return the View you just created
        return view;
    }

    private void refreshColorAndImage(ToDoItem toDoItem, View view) {
        TextView lineButton = (TextView) view.findViewById(R.id.lineView);
        lineButton.setBackgroundColor(toDoItem.getStatus() == Status.DONE ? Color.GREEN : Color.RED);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setVisibility(toDoItem.isAlert() ? View.VISIBLE : View.INVISIBLE);
    }

    private void log(String msg) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, msg);
    }

    private class StatusChangeListener implements OnCheckedChangeListener {
        private final ToDoItem toDoItem;
        private final View view;

        public StatusChangeListener(ToDoItem toDoItem, View view) {
            this.toDoItem = toDoItem;
            this.view = view;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            log("Entered onCheckedChanged()");

            // is called when the user toggles the status checkbox
            toDoItem.setStatus(isChecked ? Status.DONE : Status.NOTDONE);
            refreshColorAndImage(toDoItem, view);
        }
    }

    private class PrioritySelectedListener implements AdapterView.OnItemSelectedListener {
        private final ToDoItem toDoItem;

        public PrioritySelectedListener(ToDoItem toDoItem) {
            this.toDoItem = toDoItem;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            toDoItem.setPriority(ToDoItem.Priority.valueFromPosition(position));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //none
        }
    }
}
