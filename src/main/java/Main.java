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
import java.util.concurrent.ForkJoinPool;

public class Main {

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

            ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
            Set<String> allPages = Collections.synchronizedSet(new HashSet<>());

            try {
                URI strippedUri = WebCrawler.stripUrl(inputUrl);

                System.out.println(strippedUri.toString());
                String host = strippedUri.getHost();
                System.out.println(host);
                InternetDomainName internetDomainName = InternetDomainName.from(host).topPrivateDomain();
                String rootDomain = internetDomainName.toString();

                System.out.println(rootDomain);
                WebCrawler webCrawler = new WebCrawler(WebCrawler.DEFAULT_MAX_DEPTH, strippedUri.toString(), rootDomain, allPages);
                forkJoinPool.invoke(webCrawler);
            } catch (URISyntaxException e) {
                System.out.println("Invalid URI");
            }

            for (String pageUrl : allPages) {
                System.out.println(pageUrl);
            }
        }
    }
}
