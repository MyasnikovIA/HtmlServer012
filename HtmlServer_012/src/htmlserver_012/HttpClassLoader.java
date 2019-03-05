package htmlserver_012;


import java.net.URL;
import java.net.URLClassLoader;


public class HttpClassLoader extends URLClassLoader {

    /**
     * @param urls, to carryforward the existing classpath.
     */
    public HttpClassLoader(URL[] urls) {
        super(urls);
    }

    @Override
    /**
     * add ckasspath to the loader.
     */
    public void addURL(URL url) {
        super.addURL(url);
    }

}
