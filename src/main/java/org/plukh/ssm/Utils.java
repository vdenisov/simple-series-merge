package org.plukh.ssm;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Utils {
    public static class StreamGobbler extends Thread {
        protected InputStream in;
        protected PrintStream out;
        protected boolean closeOutputStream;

        // reads everything from input stream until empty, printing to provided output
        StreamGobbler(InputStream in, OutputStream out) {
            this(in, new PrintStream(out));
        }

        public StreamGobbler(InputStream in, PrintStream out) {
            this(in, out, false);
        }

        public StreamGobbler(InputStream in, OutputStream out, boolean closeOutputStream) {
            this.in = in;
            this.out = new PrintStream(out);
            this.closeOutputStream = closeOutputStream;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null)
                    out.println(line);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                if (closeOutputStream) out.close();
            }
        }
    }

    private static final Map<String, String> langCodeToLanguageMap = new HashMap<>();
    private static final Map<String, String> languageToLangCodeMap = new HashMap<>();

    public static void init() throws IOException {
        Properties langs = new Properties();
        langs.load(Utils.class.getResourceAsStream("/languages.properties"));
        for (String langCode : langs.stringPropertyNames()) {
            langCode = langCode.toLowerCase();
            String language = langs.getProperty(langCode);
            langCodeToLanguageMap.put(langCode, language);
            languageToLangCodeMap.put(language.toLowerCase(), langCode);
        }
    }

    public static String getPaddedNumber(int num) {
        return String.format("%02d", num);
    }

    public static String getLanguage(String langCode) {
        return langCodeToLanguageMap.get(langCode.toLowerCase());
    }

    public static String getLangCode(String language) {
        return languageToLangCodeMap.get(language.toLowerCase());
    }

    public static void startGobblers(Process process) {
        StreamGobbler out = new StreamGobbler(process.getInputStream(), System.out);
        StreamGobbler err = new StreamGobbler(process.getErrorStream(), System.err);
        out.start();
        err.start();
    }
}
