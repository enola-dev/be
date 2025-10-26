package ch.vorburger.jvmtools;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

final class Utils {

    static URI toURI(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL.toURI() failed: " + url, e);
        }
    }

    static URL toURL(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new IllegalArgumentException("URI.toURL() failed: " + uri, e);
        }
    }

    static List<URI> toURIs(Set<URL> urls) {
        return urls.stream().map(Utils::toURI).toList();
    }

    private Utils() {}
}
