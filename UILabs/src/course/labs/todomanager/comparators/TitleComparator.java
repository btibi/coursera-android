package course.labs.todomanager.comparators;


import course.labs.todomanager.ToDoItem;

public class TitleComparator implements java.util.Comparator<ToDoItem> {
    @Override
    public int compare(ToDoItem lhs, ToDoItem rhs) {
        return lhs.getTitle().compareTo(rhs.getTitle());
    }
}
