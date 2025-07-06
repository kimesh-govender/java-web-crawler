import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
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
            String strippedUrl = stripUrl(url).toString();

            boolean isPageAbsent = pages.add(strippedUrl);

            if (!isPageAbsent || depthRemaining <= 0) return true;

            ArrayList<WebCrawler> nestedWebCrawlers = createNestedCrawlers(strippedUrl);

            nestedWebCrawlers.forEach(ForkJoinTask::fork);
            nestedWebCrawlers.forEach(ForkJoinTask::join);

            return true;

        } catch (URISyntaxException e) {
            System.out.println("Invalid URI");
            return true;
        }
    }

    private ArrayList<WebCrawler> createNestedCrawlers(String strippedUrl) {
        ArrayList<WebCrawler> nestedWebCrawlers = new ArrayList<>();

        Document doc = requestPage(strippedUrl);

        if (doc != null) {
            int updatedDepthRemaining = depthRemaining - 1;
            System.out.println("Current Depth: " + (DEFAULT_MAX_DEPTH - updatedDepthRemaining));

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String nestedUrl = link.absUrl("href");
                if (nestedUrl.contains(rootDomain)) {
                    nestedWebCrawlers.add(new WebCrawler(updatedDepthRemaining, nestedUrl, rootDomain, pages));
                }
            }
        }

        return nestedWebCrawlers;
    }


    private Document requestPage(String url) {
        try {
            Connection conn = Jsoup.connect(url);
            Document doc = conn.get();

            if (conn.response().statusCode() == 200) {
                System.out.println("Link Requested: " + url);
                System.out.println("Page Title: " + doc.title());
                return doc;
            }

            return null;

        } catch (IOException e) {
            return null;
        }

    }

    public static URI stripUrl(String url) throws URISyntaxException {
        URI uri = new URI(url);

        String scheme = uri.getScheme();
        String host = uri.getHost();
        String path = uri.getPath();

        if (scheme == null || host == null || path == null)
            throw new URISyntaxException(url, "Invalid scheme, host or path");

        String pathNoTrailingSlashes = StringUtils.removeEnd(path, "/");

        return new URI(
                uri.getScheme(),
                uri.getHost(),
                pathNoTrailingSlashes,
                null  // Set fragment to null to remove the anchor
        );

    }
}
