import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static int unbroken = 0;
    private static int broken = 0;
    private static final String URL = "http://91.210.252.240";
    private static final Set<String> urls = new HashSet<>();

    public static void main(String[] args) throws IOException {

        FileWriter outUnbroken = new FileWriter("unbroken.txt");
        FileWriter outBroken = new FileWriter("broken.txt");

        getContentPage(URL + "/broken-links/");

        outUnbroken.write(unbroken + "\n" + new Date().toString());
        outBroken.write(broken + "\n" + new Date().toString());

        outUnbroken.close();
        outBroken.close();
    }

    private static void getContentPage(String url) throws IOException {
        StringBuilder sb = new StringBuilder();
        HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
        int statusCode = http.getResponseCode();

        if (statusCode > 301) {
            broken++;

            System.err.printf("%s %d%n", url, statusCode);
            return;
        }

        try {
            InputStream is = http.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);
            char[] buffer = new char[256];
            int rc;

            while ((rc = reader.read(buffer)) != -1)
                sb.append(buffer, 0, rc);

            reader.close();

            unbroken++;

//            System.out.printf("%s %d%n", url, statusCode);

            getLinks(sb.toString());
        } catch (Exception e) {
            broken++;

//            System.err.printf("%s %d%n", url, statusCode);
        }
    }

    private static void getLinks(String content) throws IOException {
        Pattern pattern = Pattern.compile("<a.*href=\"(.*?)\".*>");
        Matcher matcher = pattern.matcher(content);

        int index = 0;
        while (matcher.find(index)) {
            index = matcher.end();

            String link = matcher.group(1);
            String url = (URL + "/broken-links/" + link);

            if (urls.contains(url) || urls.contains(link)) {
                continue;
            }

            urls.add(url);
            urls.add(link);

            if (link.startsWith("http")) {
                if (!link.contains(URL)) {
                    continue;
                }
                getContentPage(link);
            } else {
                getContentPage(url);
            }
        }
    }
}