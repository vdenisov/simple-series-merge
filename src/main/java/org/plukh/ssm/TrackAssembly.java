package org.plukh.ssm;

import java.util.LinkedList;
import java.util.List;

public class TrackAssembly {
    private VideoTrack videoTrack;
    private List<AudioTrack> audioTracks;
    private List<SubtitleTrack> subtitleTracks;
    private String chapterFile;
    private int episode;

    public TrackAssembly() {
        audioTracks = new LinkedList<>();
        subtitleTracks = new LinkedList<>();
    }

    public VideoTrack getVideoTrack() {
        return videoTrack;
    }

    public void setVideoTrack(VideoTrack videoTrack) {
        this.videoTrack = videoTrack;
    }

    public List<AudioTrack> getAudioTracks() {
        return audioTracks;
    }

    public void setAudioTracks(List<AudioTrack> audioTracks) {
        this.audioTracks = audioTracks;
    }

    public List<SubtitleTrack> getSubtitleTracks() {
        return subtitleTracks;
    }

    public void setSubtitleTracks(List<SubtitleTrack> subtitleTracks) {
        this.subtitleTracks = subtitleTracks;
    }

    public String getChapterFile() {
        return chapterFile;
    }

    public void setChapterFile(String chapterFile) {
        this.chapterFile = chapterFile;
    }

    public TrackAssembly withVideoTrack(final VideoTrack videoTrack) {
        this.videoTrack = videoTrack;
        return this;
    }

    public TrackAssembly withAudioTracks(final List<AudioTrack> audioTracks) {
        this.audioTracks = audioTracks;
        return this;
    }

    public TrackAssembly withSubtitleTracks(final List<SubtitleTrack> subtitleTracks) {
        this.subtitleTracks = subtitleTracks;
        return this;
    }

    public TrackAssembly withChapterFile(final String chapterFile) {
        this.chapterFile = chapterFile;
        return this;
    }

    public int getEpisode() {
        return episode;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }

    public TrackAssembly withEpisode(final int episode) {
        this.episode = episode;
        return this;
    }
}
