package org.plukh.ssm;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AudioTrack extends Track {
    private static final Set<String> EXTENSIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("ac3", "dts", "mp3", "aac", "mka")));

    protected AudioTrack(String fileName, int trackNumber) {
        super(fileName, trackNumber);
    }

    protected AudioTrack(String fileName, int trackNumber, String language, String codec) {
        super(fileName, trackNumber, language, codec);
    }

    @Override
    public String getTrackName(String seriesName, int season, int episode, EpisodeNames episodeNames,
                               String defaultLanguage) {
        return Utils.getLanguage(getTrackLanguage(defaultLanguage)) + " " + detectAudioTrackCodec();
    }

    public static Set<String> getSupportedExtensions() {
        return EXTENSIONS;
    }

    private String detectAudioTrackCodec() {
        //See if codec is explicitly set
        if (StringUtils.isNotEmpty(codec)) return codec.toUpperCase();

        //Not set explicitly, try to guess by checking extension
        String extension = FilenameUtils.getExtension(fileName);
        //TODO: implement detection of audio type in MKV container
        if (extension.equalsIgnoreCase("mkv")) throw new NotImplementedException("Using audio files from MKV containers not implemented yet");

        return extension.toUpperCase();
    }
}
