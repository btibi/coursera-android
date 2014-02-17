package course.labs.todomanager.comparators;

import course.labs.todomanager.ToDoItem;

public class DeadLineComparator implements java.util.Comparator<ToDoItem> {
    @Override
    public int compare(ToDoItem lhs, ToDoItem rhs) {
        return lhs.getDate().compareTo(rhs.getDate());
    }
}
