package org.plukh.ssm;

import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.plukh.ssm.MkvTrackInfo.TrackType.*;

public class MkvTrackInfoTest {

    @Test
    public void testChapters() throws Exception {
        //Given
        InputStream in = this.getClass().getResourceAsStream("chapters.json");

        //When
        List<MkvTrackInfo> tracks = MkvTrackInfo.parse(in, "eng");

        //Then
        assertThat(tracks)
            .hasSize(1)
            .element(0)
                .isEqualTo(new MkvTrackInfo(0, CHAPTERS, null, null));
    }

    @Test
    public void testNoChapters() throws Exception {
        //Given
        InputStream in = this.getClass().getResourceAsStream("no_chapters.json");

        //When
        List<MkvTrackInfo> tracks = MkvTrackInfo.parse(in, "eng");

        //Then
        assertThat(tracks).isEmpty();
    }

    @Test
    public void testLanguage() throws Exception {
        //Given
        InputStream in = this.getClass().getResourceAsStream("language.json");

        //When
        List<MkvTrackInfo> tracks = MkvTrackInfo.parse(in, "rus");

        //Then
        assertThat(tracks)
            .hasSize(1)
            .element(0)
            .isEqualTo(new MkvTrackInfo(2, AUDIO, "AAC", "eng"));
    }

    @Test
    public void testNoLanguage() throws Exception {
        //Given
        InputStream in = this.getClass().getResourceAsStream("no_language.json");

        //When
        List<MkvTrackInfo> tracks = MkvTrackInfo.parse(in, "rus");

        //Then
        assertThat(tracks)
            .hasSize(1)
            .element(0)
            .isEqualTo(new MkvTrackInfo(2, AUDIO, "AAC", "rus"));
    }

    @Test
    public void testUndefinedLanguage() throws Exception {
        //Given
        InputStream in = this.getClass().getResourceAsStream("undefined_language.json");

        //When
        List<MkvTrackInfo> tracks = MkvTrackInfo.parse(in, "rus");

        //Then
        assertThat(tracks)
            .hasSize(1)
            .element(0)
            .isEqualTo(new MkvTrackInfo(2, AUDIO, "AAC", "rus"));
    }

    @Test
    public void test30MinutesOrLess() throws Exception {
        //Given
        InputStream in = this.getClass().getResourceAsStream("30 Minutes Or Less.json");

        //When
        List<MkvTrackInfo> tracks = MkvTrackInfo.parse(in, "rus");

        //Then
        assertThat(tracks)
            .containsExactlyInAnyOrder(
                new MkvTrackInfo(0, CHAPTERS, null, null),
                new MkvTrackInfo(0, VIDEO, "MPEG-4p10/AVC/h.264", "eng"),
                new MkvTrackInfo(1, AUDIO, "DTS", "eng"),
                new MkvTrackInfo(2, AUDIO, "AAC", "eng"),
                new MkvTrackInfo(3, SUBTITLES, "SubRip/SRT", "eng"),
                new MkvTrackInfo(4, SUBTITLES, "SubRip/SRT", "eng")
            );
    }

    @Test
    public void test2010() throws Exception {
        //Given
        InputStream in = this.getClass().getResourceAsStream("2010.json");

        //When
        List<MkvTrackInfo> tracks = MkvTrackInfo.parse(in, "rus");

        //Then
        assertThat(tracks)
            .containsExactlyInAnyOrder(
                new MkvTrackInfo(0, VIDEO, "MPEG-4p10/AVC/h.264", "eng"),
                new MkvTrackInfo(1, AUDIO, "AC3", "rus"),
                new MkvTrackInfo(2, AUDIO, "DTS", "eng"),
                new MkvTrackInfo(3, SUBTITLES, "SubRip/SRT", "eng")
            );
    }

    @Test
    public void testSpaceBattleShipYamato() throws Exception {
        //Given
        InputStream in = this.getClass().getResourceAsStream("Space Battleship Yamato.json");

        //When
        List<MkvTrackInfo> tracks = MkvTrackInfo.parse(in, "rus");

        //Then
        assertThat(tracks)
            .containsExactlyInAnyOrder(
                new MkvTrackInfo(0, VIDEO, "MPEG-4p10/AVC/h.264", "jpn"),
                new MkvTrackInfo(1, AUDIO, "AC3", "rus"),
                new MkvTrackInfo(2, AUDIO, "AC3", "jpn"),
                new MkvTrackInfo(3, SUBTITLES, "SubRip/SRT", "rus")
            );
    }
}