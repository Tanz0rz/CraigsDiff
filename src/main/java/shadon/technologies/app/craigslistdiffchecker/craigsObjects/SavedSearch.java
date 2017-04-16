package shadon.technologies.app.craigslistdiffchecker.craigsObjects;

/**
 * Created by Monday on 7/31/2016.
 */
public class SavedSearch {
    public String name;
    public String url;

    public SavedSearch(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return name;
    }
}
