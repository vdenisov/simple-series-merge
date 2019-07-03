package org.plukh.ssm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MkvTrackInfo {
    public enum TrackType {
        VIDEO, AUDIO, SUBTITLES, CHAPTERS
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, String> CODEC_RENAMES = ImmutableMap.of(
        "AC-3", "AC3"
    );

    private int trackNumber;
    private TrackType trackType;
    private String language;
    private final String codec;

    public MkvTrackInfo(int trackNumber, TrackType trackType, String codec, String language) {
        this.trackNumber = trackNumber;
        this.trackType = trackType;
        this.codec = codec;
        this.language = language;
    }

    public static List<MkvTrackInfo> parse(InputStream in, String defaultLanguage) throws IOException {
        JsonNode parsedJson = objectMapper.readTree(in);
        List<MkvTrackInfo> tracks = new LinkedList<>();

        //Chapters
        if (parsedJson.get("chapters") != null && parsedJson.get("chapters").size() > 0) {
            tracks.add(new MkvTrackInfo(0, TrackType.CHAPTERS, null, null));
        }

        //Tracks
        if (parsedJson.get("tracks") != null) {
            for (JsonNode track : parsedJson.get("tracks")) {
                int trackNumber = track.get("id").asInt();
                TrackType trackType = TrackType.valueOf(track.get("type").asText().toUpperCase());

                String codec = track.get("codec").asText();
                if (codec != null) {
                    codec = CODEC_RENAMES.getOrDefault(codec, codec);
                }

                JsonNode languageNode = track.get("properties").get("language");
                String language = languageNode == null ? null : languageNode.asText();

                //Adjust for undefined language
                if (language == null || language.equals("und")) language = defaultLanguage;

                tracks.add(new MkvTrackInfo(trackNumber, trackType, codec, language));
            }
        }

        return tracks;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public TrackType getTrackType() {
        return trackType;
    }

    public String getLanguage() {
        return language;
    }

    public String getCodec() {
        return codec;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MkvTrackInfo)) return false;

        MkvTrackInfo info = (MkvTrackInfo) o;

        if (trackNumber != info.trackNumber) return false;
        if (codec != null ? !codec.equals(info.codec) : info.codec != null) return false;
        if (language != null ? !language.equals(info.language) : info.language != null) return false;
        if (trackType != info.trackType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = trackNumber;
        result = 31 * result + trackType.hashCode();
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (codec != null ? codec.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "[Track ID: " + trackNumber + ", type: " + trackType + ", codec: " + codec + ", language: " + language + "]";
    }
}
