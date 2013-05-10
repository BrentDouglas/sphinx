package io.machinecode.sphinx.browser.http;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class Http {

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";

    public static Request get(final String url) {
        return url.startsWith("https") ? new HttpsRequestImpl(GET, url) : new HttpRequestImpl(GET, url);
    }

    public static Request post(final String url) {
        return url.startsWith("https") ? new HttpsRequestImpl(POST, url) : new HttpRequestImpl(POST, url);
    }

    public static Request delete(final String url) {
        return url.startsWith("https") ? new HttpsRequestImpl(DELETE, url) : new HttpRequestImpl(DELETE, url);
    }

    public static Request request(final String method, final String url) {
        return url.startsWith("https") ? new HttpsRequestImpl(method, url) : new HttpRequestImpl(method, url);
    }

    public static class HttpRequestImpl implements Request {

        protected final String method;
        protected final String url;
        protected final Map<String, String> cookies = new HashMap<String, String>();
        protected final Map<String, String> data = new HashMap<String, String>();

        public HttpRequestImpl(final String method, final String url) {
            this.method = method;
            this.url = url;
        }

        @Override
        public Request cookie(final String name, final String value) {
            cookies.put(name, value);
            return this;
        }

        @Override
        public Request data(final String name, final String value) {
            data.put(name, value);
            return this;
        }

        @Override
        public Request data(final Map<String, String> data) {
            data.putAll(data);
            return this;
        }

        @Override
        public Response send() {
            try {
                final URL url = new URL(this.url);
                final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                for (final Map.Entry<String, String> entry : this.cookies.entrySet()) {
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }
                conn.setDoInput(true);
                conn.setRequestMethod(method);
                if (POST.equals(method)) {
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    boolean add = false;
                    final StringBuilder builder = new StringBuilder();
                    for (final Map.Entry<String, String> entry : this.data.entrySet()) {
                        if (add) {
                            builder.append('&');
                        }
                        add = true;
                        builder.append(entry.getKey())
                                .append("=")
                                .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    }
                    final byte[] data = builder.toString().getBytes();
                    conn.setRequestProperty("Content-Length", Integer.toString(data.length));
                    conn.setDoOutput(true);
                    final DataOutputStream stream = new DataOutputStream(conn.getOutputStream());
                    stream.write(data);
                }
                return processResponse(conn);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }

        protected Response processResponse(final HttpURLConnection conn) throws IOException {
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                InputStream stream = null;
                try {
                    stream = conn.getErrorStream();
                    throw new IOException(read(stream));
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }
            InputStream stream = null;
            try {
                stream = conn.getErrorStream();
                return new ResponseImpl(conn.getResponseCode(), stream, conn.getHeaderFields());
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }

        protected String read(final InputStream in) throws IOException {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            int b;
            while((b = in.read()) != -1) {
                out.write(b);
            }
            return out.toString();
        }
    }

    /**
     * @author Brent Douglas <brent.n.douglas@gmail.com>
     */
    public static class HttpsRequestImpl extends HttpRequestImpl {

        public HttpsRequestImpl(final String method, final String url) {
            super(method, url);
        }

        @Override
        public Response send() {
            try {
                final URL url = new URL(this.url);
                final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                for (final Map.Entry<String, String> entry : this.cookies.entrySet()) {
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }
                conn.setDoInput(true);
                conn.setRequestMethod(method);
                if (POST.equals(method)) {
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    boolean add = false;
                    final StringBuilder builder = new StringBuilder();
                    for (final Map.Entry<String, String> entry : this.data.entrySet()) {
                        if (add) {
                            builder.append('&');
                        }
                        add = true;
                        builder.append(entry.getKey())
                                .append("=")
                                .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    }
                    final byte[] data = builder.toString().getBytes();
                    conn.setRequestProperty("Content-Length", Integer.toString(data.length));
                    conn.setDoOutput(true);
                    final DataOutputStream stream = new DataOutputStream(conn.getOutputStream());
                    stream.write(data);
                }
                return processResponse(conn);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static class ResponseImpl implements Response {

        protected final int code;
        protected final InputStream data;
        protected final Map<String, List<String>> headers;

        public ResponseImpl(final int code, final InputStream data, final Map<String, List<String>> headers) {
            this.code = code;
            this.data = data;
            this.headers = headers;
        }

        @Override
        public int code() {
            return code;
        }

        @Override
        public InputStream data() {
            return data;
        }

        @Override
        public List<String> header(final String header) {
            return this.headers.get(header);
        }
    }
}
