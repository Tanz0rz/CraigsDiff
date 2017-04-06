package shadon.technologies.app.craigslistdiffchecker.service;

/**
 * Created by Monday on 7/31/2016.
 */
public class CraigSearch {
    public String name;
    public String url;

    public CraigSearch(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return name;
    }
}
