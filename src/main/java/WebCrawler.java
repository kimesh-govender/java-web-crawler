import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class WebCrawler extends RecursiveTask<Boolean> {
    public static int DEFAULT_MAX_DEPTH = 10;

    final private int depthRemaining;
    final private String url;
    final private String rootDomain;
    final private Set<String> pages;

    public WebCrawler(int depthRemaining, String url, String rootDomain, Set<String> pages) {
        this.depthRemaining = depthRemaining;
        this.url = url;
        this.rootDomain = rootDomain;
        this.pages = pages;
    }

    @Override
    protected Boolean compute() {
        try {
            String strippedUrl = UriUtil.stripUrl(url);

            boolean isPageAbsent = pages.add(strippedUrl);

            if (!isPageAbsent || depthRemaining <= 0) return true;

            ArrayList<WebCrawler> nestedWebCrawlers = createNestedCrawlers(strippedUrl);

            nestedWebCrawlers.forEach(ForkJoinTask::fork);
            nestedWebCrawlers.forEach(ForkJoinTask::join);

            return true;

        } catch (URISyntaxException e) {
            System.out.println("Invalid URL: " + url);
            return true;
        }
    }

    private ArrayList<WebCrawler> createNestedCrawlers(String strippedUrl) {
        ArrayList<WebCrawler> nestedWebCrawlers = new ArrayList<>();

        Document doc = UriUtil.requestPage(strippedUrl);

        if (doc != null) {
            int updatedDepthRemaining = depthRemaining - 1;
            //System.out.println("Current Depth: " + (DEFAULT_MAX_DEPTH - updatedDepthRemaining));

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String nestedUrl = link.absUrl("href");
                try {
                    String nestedUrlDomain = UriUtil.getUrlDomain(nestedUrl);
                    if (nestedUrlDomain.equals(rootDomain)) {
                        nestedWebCrawlers.add(new WebCrawler(updatedDepthRemaining, nestedUrl, rootDomain, pages));
                    }
                } catch (URISyntaxException e) {
                    //System.out.println("Ignoring invalid nested URL: " + nestedUrl);
                }

            }
        }

        return nestedWebCrawlers;
    }
}
