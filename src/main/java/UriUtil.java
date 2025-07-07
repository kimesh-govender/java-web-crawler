import com.google.common.net.InternetDomainName;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class UriUtil {
    private static final Logger logger = LoggerFactory.getLogger(UriUtil.class);

    public static Document requestPage(String url) {
        try {
            Connection conn = Jsoup.connect(url);
            Document doc = conn.get();

            if (conn.response().statusCode() == 200) {
                logger.info("Page URL Requested: {}", url);
                logger.info("Page Title: {}", doc.title());
                return doc;
            }

            return null;

        } catch (IOException e) {
            return null;
        }

    }

    public static String stripUrl(String url) throws URISyntaxException {
        URI uri = createValidUri(url);

        String pathNoTrailingSlashes = StringUtils.removeEnd(uri.getPath(), "/");

        URI strippedUri = new URI(
                uri.getScheme(),
                uri.getHost(),
                pathNoTrailingSlashes,
                null  // Set fragment to null to remove the anchor
        );

        return strippedUri.toString();
    }

    public static String getUrlDomain(String url) throws URISyntaxException {
        URI uri = createValidUri(url);

        String host = uri.getHost();
        InternetDomainName internetDomainName = InternetDomainName.from(host).topPrivateDomain();
        return internetDomainName.toString();
    }

    public static URI createValidUri(String url) throws URISyntaxException {
        URI uri = new URI(url);

        String scheme = uri.getScheme();
        String host = uri.getHost();
        String path = uri.getPath();

        if (scheme == null || host == null || path == null)
            throw new URISyntaxException(url, "Invalid scheme, host or path");

        return uri;
    }

}
