import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class RecursiveCrawler extends RecursiveTask<Boolean> {
    private static final Logger logger = LoggerFactory.getLogger(RecursiveCrawler.class);

    final private int maxDepth;
    final private int currentDepth;
    final private String url;
    final private String rootDomain;
    final private Set<String> pages;

    public RecursiveCrawler(int currentDepth, String url, int maxDepth, String rootDomain, Set<String> pages) {
        this.currentDepth = currentDepth;
        this.url = url;
        this.maxDepth = maxDepth;
        this.rootDomain = rootDomain;
        this.pages = pages;
    }

    @Override
    protected Boolean compute() {
        try {
            String strippedUrl = UriUtil.stripUrl(url);

            boolean isPageAbsent = pages.add(strippedUrl);

            if (!isPageAbsent || isDepthExceeded()) return true;

            ArrayList<RecursiveCrawler> nestedCrawlers = createNestedCrawlers(strippedUrl);

            nestedCrawlers.forEach(ForkJoinTask::fork);
            nestedCrawlers.forEach(ForkJoinTask::join);

            return true;

        } catch (URISyntaxException e) {
            logger.warn("Invalid URL in web crawler: {}", url);
            return true;
        }
    }

    private ArrayList<RecursiveCrawler> createNestedCrawlers(String strippedUrl) {
        ArrayList<RecursiveCrawler> nestedCrawlers = new ArrayList<>();

        Document doc = UriUtil.requestPage(strippedUrl);

        if (doc != null) {
            logger.trace("Current Depth: {}", currentDepth);

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String nestedUrl = link.absUrl("href");
                try {
                    String nestedUrlDomain = UriUtil.getUrlDomain(nestedUrl);
                    if (nestedUrlDomain.equals(rootDomain)) {
                        nestedCrawlers.add(new RecursiveCrawler(currentDepth + 1, nestedUrl, maxDepth, rootDomain, pages));
                    }
                } catch (URISyntaxException e) {
                    logger.debug("Ignoring invalid nested URL: {}", nestedUrl);
                }

            }
        }

        return nestedCrawlers;
    }

    private boolean isDepthExceeded() {
        return currentDepth > maxDepth;
    }
}
