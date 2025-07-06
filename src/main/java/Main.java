import com.google.common.net.InternetDomainName;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class Main {
    public static int MAX_DEPTH = 10;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter a URL: ");
            String inputUrl = scanner.nextLine();
            if (inputUrl.equals("exit")) {
                System.out.println("Exiting program...");
                break;
            }
            System.out.println("URLs for " + inputUrl + ": ");





            Set<String> allPages = new HashSet<>();

            try {
                URI strippedUri = stripUrl(inputUrl);

                System.out.println(strippedUri.toString());
                String host = strippedUri.getHost();
                System.out.println(host);
                InternetDomainName internetDomainName = InternetDomainName.from(host).topPrivateDomain();
                String rootDomain = internetDomainName.toString();

                System.out.println(rootDomain);
                crawl(MAX_DEPTH, strippedUri.toString(), rootDomain, allPages);
            } catch (URISyntaxException e) {
                System.out.println("Invalid URI");
            }

            for (String pageUrl : allPages) {
                System.out.println(pageUrl);
            }

            // In a real application, this would look up URLs based on inputUrl.
            // For now, we're just hardcoding a response.
//            List<String> hardcodedUrls = Arrays.asList(
//                    "http://sedna.com",
//                    "http://sedna.com/about-us",
//                    "http://sedna.com/terms-and-conditions",
//                    "http://sedna.com/careers"
//            );
//            for (String url : hardcodedUrls) {
//                System.out.println(url);
//            }
        }
    }

    private static void crawl(int depthRemaining, String url, String rootDomain, Set<String> pages) {
        try {
            String strippedUrl = stripUrl(url).toString();

            boolean isPageAbsent = pages.add(strippedUrl);

            if(!isPageAbsent || depthRemaining <= 0) return;
            Document doc = request(strippedUrl);

            if(doc != null) {
                int updatedDepthRemaining = depthRemaining - 1;
                System.out.println("Current Depth: " + (MAX_DEPTH - updatedDepthRemaining));

                Elements links = doc.select("a[href]");
                for(Element link : links) {
                    String nextUrl = link.absUrl("href");
                    if(nextUrl.contains(rootDomain)) {
                        crawl(updatedDepthRemaining, nextUrl, rootDomain, pages);
                    }
                }
            }

        } catch (URISyntaxException e) {
            System.out.println("Invalid URI");
        }
    }


    private static Document request(String url) {
        try {
            Connection conn = Jsoup.connect(url);
            Document doc = conn.get();

            if(conn.response().statusCode() == 200) {
                System.out.println("Link Requested: " + url);
                System.out.println("Page Title: " + doc.title());
                return doc;
            }

            return null;

        }
        catch(IOException e) {
            return null;
        }

    }

    private static URI stripUrl(String url) throws URISyntaxException {
        URI uri = new URI(url);

        String scheme = uri.getScheme();
        String host = uri.getHost();
        String path = uri.getPath();

        if(scheme == null || host == null || path == null)
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
