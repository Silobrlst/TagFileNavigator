package tagfilenav;

import java.util.Collection;

public class EmptyPathListener implements PathListener {
    public void created(Path pathIn){}
    public void changedPath(Path pathIn){}
    public void changedPaths(){}
    public void renamed(Path pathIn){}
    public void addedTags(Path pathIn, Collection<Tag> tagsIn){}
    public void removedTag(Path pathIn, Tag tagIn){}
    public void removedTags(Path pathIn, Collection<Tag> tagsIn){}
    public void removedPath(Path pathIn) {}
}
