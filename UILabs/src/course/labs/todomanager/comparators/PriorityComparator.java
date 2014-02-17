package course.labs.todomanager.comparators;

import course.labs.todomanager.ToDoItem;

public class PriorityComparator implements java.util.Comparator<ToDoItem> {

    @Override
    public int compare(ToDoItem lhs, ToDoItem rhs) {
        int priorityCompare = Integer.valueOf(lhs.getPriority().getPosition()).compareTo(Integer.valueOf(rhs.getPriority().getPosition()));
        return priorityCompare == 0 ? lhs.getDate().compareTo(rhs.getDate()) : priorityCompare;
    }
}
