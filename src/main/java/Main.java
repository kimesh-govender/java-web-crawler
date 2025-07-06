import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

public class Main {

    public static void main(String[] args) {
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter a URL: ");
            String inputUrl = scanner.nextLine();
            if (inputUrl.equals("exit")) {
                System.out.println("Exiting program...");
                break;
            }

            Set<String> allPages = Collections.synchronizedSet(new HashSet<>());

            try {
                String strippedUrl = UriUtil.stripUrl(inputUrl);
                String rootDomain = UriUtil.getUrlDomain(strippedUrl);

                System.out.println("Crawling pages for Domain [" + rootDomain + "] from Input URL [" + inputUrl + "] provided...");

                WebCrawler webCrawler = new WebCrawler(WebCrawler.DEFAULT_MAX_DEPTH, strippedUrl, rootDomain, allPages);
                forkJoinPool.invoke(webCrawler);

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
    }
}
