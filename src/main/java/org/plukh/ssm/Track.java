package org.plukh.ssm;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Track {
    protected static final Pattern SUFFIX_PATTERN = Pattern.compile("\\w*");

    protected final String baseName;
    protected final String fileName;
    protected final int trackNumber;
    protected final String language;
    protected final String codec;
    protected final String variant;

    protected boolean defaultTrack;
    protected final List<String> suffixes;

    protected Track(String baseName, String fileName, int trackNumber) {
        this(baseName, fileName, trackNumber, null, null);
    }

    protected Track(String baseName, String fileName, int trackNumber, String language, String codec) {
        this(baseName, fileName, trackNumber, language, codec, null);
    }

    protected Track(String baseName, String fileName, int trackNumber, String language, String codec, String variant) {
        this.baseName = baseName;
        this.fileName = fileName;
        this.trackNumber = trackNumber;
        this.language = language;
        this.codec = codec;
        this.variant = variant;

        parseSuffixes();
    }

    private void parseSuffixes() {
        String suffixString = FilenameUtils.getBaseName(fileName).substring(baseName.length());
        Matcher m = SUFFIX_PATTERN.matcher(suffixString);

        ImmutableList.Builder<String> suffixesBuilder = ImmutableList.builder();
        while (m.matches()) {
            suffixesBuilder.add(m.group());
        }

        suffixes = suffixesBuilder.build();
    }

    public String getTrackLanguage(String defaultLanguage) {
        //See if language is set already (by, i.e., external detector)
        if (StringUtils.isEmpty(language)) {
            //It's not - try to guess from file name
            language = getLanguageByFileName();
            //If still empty, set to default
            if (StringUtils.isEmpty(language)) language = defaultLanguage;
        }

        return language;
    }

    public abstract String getTrackName(String seriesName, int season, int episode, EpisodeNames episodeNames,
                                        String defaultLanguage);

    protected String getLanguageByFileName() {
        String f = FilenameUtils.getBaseName(fileName);

        int pos = f.lastIndexOf(' ') + 1;

        return pos > 0 ? f.substring(pos) : null;
    }

    public String getFileName() {
        return fileName;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public boolean isDefaultTrack() {
        return defaultTrack;
    }

    public void setDefaultTrack(boolean defaultTrack) {
        this.defaultTrack = defaultTrack;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getVariant() {
        return variant;
    }

    @Override
    public String toString() {
        return "Track{" +
            "fileName='" + fileName + '\'' +
            ", trackNumber=" + trackNumber +
            ", defaultTrack=" + defaultTrack +
            ", language='" + language + '\'' +
            ", codec='" + codec + '\'' +
            ", variant='" + variant + '\'' +
            '}';
    }
}
