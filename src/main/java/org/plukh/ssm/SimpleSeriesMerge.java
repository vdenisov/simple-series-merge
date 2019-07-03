package org.plukh.ssm;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SimpleSeriesMerge {
    private String seriesName;
    private int season;

    private EpisodeNames episodeNames;
    private TrackAssembly track;
    private String outputFile;

    private String defaultLanguage;

    public SimpleSeriesMerge(String seriesName, int season, EpisodeNames episodeNames,
                             TrackAssembly track, String outputFile, String defaultLanguage) {
        this.seriesName = seriesName;
        this.season = season;
        this.episodeNames = episodeNames;
        this.track = track;
        this.outputFile = outputFile;
        this.defaultLanguage = defaultLanguage;
    }

    public int merge() throws IOException {
        System.err.println("Starting mkvmerge...");

        String cmd = prepareCommandLine();
        System.err.println("Command line: " + cmd);
        Process proc = Runtime.getRuntime().exec(cmd);
        Utils.startGobblers(proc);

        while (proc.isAlive()) {
            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                //Do nothing
            }
        }

        System.err.println("mkvmerge finished, result code: " + proc.exitValue());

        return proc.exitValue();
    }

    private String prepareCommandLine() throws IOException {
        String exe = "mkvmerge.exe";
        String outputArgs = generateOutputFileArgs();
        String videoArgs = generateVideoTrackArgs();
        String audioArgs = generateAudioTrackArgs();
        String subtitleArgs = generateSubtitleTrackArgs();
        String titleArgs = generateTitleArgs();
        String chapterArgs = generateChaptersArgs();

        return exe + " " + outputArgs + " " + videoArgs + " " + audioArgs + " " + subtitleArgs + " " + titleArgs + " " + chapterArgs;
    }

    private String generateOutputFileArgs() {
        return "--output " + "\"" + outputFile + "\"";
    }

    private String generateVideoTrackArgs() throws IOException {
        VideoTrack videoTrack = track.getVideoTrack();

        return "--language " + videoTrack.getTrackNumber() + ":" + videoTrack.getTrackLanguage(defaultLanguage) + " " +
                "--track-name \"" + videoTrack.getTrackNumber() + ":" + videoTrack.getTrackName(seriesName, season,
                track.getEpisode(), episodeNames, defaultLanguage) + "\" " +
                "--default-track " + videoTrack.getTrackNumber() + ":" + boolString(videoTrack.isDefaultTrack()) + " " +
                "--forced-track " + videoTrack.getTrackNumber() + ":no " +
                "--video-tracks " + videoTrack.getTrackNumber() + " --no-audio --no-subtitles --no-track-tags --no-global-tags" +
                (chaptersAreExternal() ? " --no-chapters " : " ") +
                "\"" + videoTrack.getFileName() + "\"";
    }

    private String generateAudioTrackArgs() {
        List<AudioTrack> audioTracks = track.getAudioTracks();

        StringBuilder sb = new StringBuilder();
        for (AudioTrack audioTrack : audioTracks) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("--language " + audioTrack.getTrackNumber() + ":" + audioTrack.getTrackLanguage(defaultLanguage) + " " +
                    "--track-name \"" + audioTrack.getTrackNumber() + ":" + audioTrack.getTrackName(seriesName,
                    season, track.getEpisode(), episodeNames, defaultLanguage) + "\" " +
                    "--default-track " + audioTrack.getTrackNumber() + ":" + boolString(audioTrack.isDefaultTrack()) + " " +
                    "--forced-track " + audioTrack.getTrackNumber() + ":no " +
                    "--audio-tracks " + audioTrack.getTrackNumber() + " --no-video --no-subtitles --no-track-tags " +
                    "--no-global-tags --no-chapters " +
                    "\"" + audioTrack.getFileName() + "\"");
        }

        return sb.toString();
    }

    private String generateSubtitleTrackArgs() {
        List<SubtitleTrack> subtitleTracks = track.getSubtitleTracks();

        StringBuilder sb = new StringBuilder();
        for (SubtitleTrack subtitleTrack : subtitleTracks) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("--language " + subtitleTrack.getTrackNumber() + ":" + subtitleTrack.getTrackLanguage
                    (defaultLanguage) + " " +
                    "--track-name \"" + subtitleTrack.getTrackNumber() + ":" + subtitleTrack.getTrackName(seriesName,
                    season, track.getEpisode(), episodeNames, defaultLanguage) + "\" " +
                    "--default-track " + subtitleTrack.getTrackNumber() + ":" + boolString(subtitleTrack
                    .isDefaultTrack()) + " " +
                    "--forced-track " + subtitleTrack.getTrackNumber() + ":no " +
                    "--subtitle-tracks " + subtitleTrack.getTrackNumber() + " --no-video --no-audio --no-track-tags " +
                    "--no-global-tags --no-chapters " +
                    "\"" + subtitleTrack.getFileName() + "\"");
        }

        return sb.toString();
    }

    private String generateTitleArgs() {
        return "--title \"" + track.getVideoTrack().getTrackName(seriesName, season, track.getEpisode(), episodeNames, defaultLanguage) + "\"";
    }

    private String generateChaptersArgs() throws IOException {
        String chapterFile = track.getChapterFile();

        if (StringUtils.isEmpty(chapterFile)) return "";

        //See if chapter files are in mkv or external file
        if (chaptersAreExternal()) {
            //We don't support embedded chapters in external MKV files so far...
            return "--chapters \"" + chapterFile + "\"";
        } else {
            System.err.println("Chapters are embedded in original MKV, will be copied automatically");
            return "";
        }
    }

    private boolean chaptersAreExternal() throws IOException {
        String chapterFile = track.getChapterFile();
        return StringUtils.isNotEmpty(chapterFile) && !Files.isSameFile(Paths.get(chapterFile), Paths.get(track.getVideoTrack().getFileName()));
    }

    private String boolString(boolean isDefault) {
        return isDefault ? "yes" : "no";
    }
}
