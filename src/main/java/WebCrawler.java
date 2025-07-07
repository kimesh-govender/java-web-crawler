
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

public class WebCrawler {
    private static final int DEFAULT_MAX_DEPTH = 10;
    private static final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    public static void crawl(int maxDepth, String inputUrl) {
        try {
            String strippedUrl = UriUtil.stripUrl(inputUrl);
            String rootDomain = UriUtil.getUrlDomain(strippedUrl);

            System.out.println("Crawling pages for Domain [" + rootDomain + "] from Input URL [" + inputUrl + "] provided...");

            Set<String> allPages = Collections.synchronizedSet(new HashSet<>());

            RecursiveCrawler recursiveCrawler = new RecursiveCrawler(1, strippedUrl, maxDepth, rootDomain, allPages);
            forkJoinPool.invoke(recursiveCrawler);

            // Sort all pages found and then print to screen
            Set<String> sortedPages = new TreeSet<>(allPages);
            System.out.println("####################################################################################################");
            System.out.println("List of all pages found in Domain [" + rootDomain + "]:");
            for (String pageUrl : sortedPages) {
                System.out.println(pageUrl);
            }
            System.out.println("####################################################################################################");
        } catch (URISyntaxException e) {
            System.out.println("Invalid URL: " + inputUrl);
        }


    }

    public static void crawl(String inputUrl) {
        crawl(DEFAULT_MAX_DEPTH, inputUrl);
    }
}
