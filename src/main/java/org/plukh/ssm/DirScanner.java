package org.plukh.ssm;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.*;

public class DirScanner {
    public static final Set<String> VIDEO_FILE_EXTENSIONS = new HashSet<>(
            Arrays.asList("mkv", "mp4", "avi"));

    public static final Set<String> AUDIO_FILE_EXTENSIONS = new HashSet<>(
            Arrays.asList("ac3", "dts", "aac", "ogg", "mp3"));

    public static final Set<String> SUBTITLE_FILE_EXTENSIONS = new HashSet<>(
            Arrays.asList("srt", "ass", "sup"));

    public static final Set<String> CHAPTER_FILE_EXTENSIONS = new HashSet<>(
            Arrays.asList("txt"));

    public static TrackAssembly scanDirectory(String dir) {
        TrackAssembly track = new TrackAssembly();

        //Detect episode
        File dirFile = new File(dir);
        int episode = Integer.parseInt(dirFile.getName());
        track.setEpisode(episode);

        File[] trackFiles = dirFile.listFiles();
        if (trackFiles == null) {
            throw new UnexpectedException("Can't read directory " + dir);
        }

        for (File trackFile : trackFiles) {
            processTrackFile(trackFile, track);
        }

        System.err.println("Track assembly prepared");

        return track;
    }

    private static void processTrackFile(File trackFile, TrackAssembly track) {
        //Detect file type
        String ext = FilenameUtils.getExtension(trackFile.getName()).toLowerCase();

        if (VIDEO_FILE_EXTENSIONS.contains(ext)) processVideoTrack(trackFile, track);
        else
        if (AUDIO_FILE_EXTENSIONS.contains(ext)) processAudioTrack(trackFile, track);
        else
        if (SUBTITLE_FILE_EXTENSIONS.contains(ext)) processSubtitleTrack(trackFile, track);
        else
        if (CHAPTER_FILE_EXTENSIONS.contains(ext)) processChapterTrack(trackFile, track);
    }

    private static void processVideoTrack(File trackFile, TrackAssembly track) {
        VideoTrack videoTrack = new VideoTrack(trackFile.getAbsolutePath(), 0);
        track.setVideoTrack(videoTrack);
    }

    private static void processAudioTrack(File trackFile, TrackAssembly track) {
        AudioTrack audioTrack = new AudioTrack(trackFile.getAbsolutePath(), 0);
        track.getAudioTracks().add(audioTrack);
    }

    private static void processSubtitleTrack(File trackFile, TrackAssembly track) {
        SubtitleTrack subtitleTrack = new SubtitleTrack(trackFile.getAbsolutePath(), 0);
        track.getSubtitleTracks().add(subtitleTrack);
    }

    private static void processChapterTrack(File trackFile, TrackAssembly track) {
        track.setChapterFile(trackFile.getAbsolutePath());
    }
}
