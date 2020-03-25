package marytts.language.ar;

import java.net.MalformedURLException;
import java.net.URL;

public class MishkalEnv {

  private static final String MISHKAL_DEFAULT_URL = "http://localhost:8080";
  private static final String MISHKAL_URL_ENVIRONMENT_VARIABLE = "MARY_TTS_MISHKAL_URL";
  private static String mishkalUrl;
  public static String getMishkalUrl() {
    if (mishkalUrl == null) {
      mishkalUrl = System.getenv(MISHKAL_URL_ENVIRONMENT_VARIABLE);
      if (mishkalUrl == null) {
        System.err.println("Missing environment variable " + MISHKAL_URL_ENVIRONMENT_VARIABLE + ", defaulting to " + MISHKAL_DEFAULT_URL);
        mishkalUrl = MISHKAL_DEFAULT_URL;
      } else {
        try {
          mishkalUrl = mishkalUrl.trim();
          new URL(mishkalUrl);
          if (!mishkalUrl.endsWith("/")) {
            mishkalUrl = mishkalUrl.concat("/");
          }
        } catch (MalformedURLException mue) {
          System.err.println("Invalid URL in environment variable " + MISHKAL_URL_ENVIRONMENT_VARIABLE + ", defaulting to " + MISHKAL_DEFAULT_URL);
          mishkalUrl = MISHKAL_DEFAULT_URL;
        }
      }
    }
    return mishkalUrl;
  }

}
