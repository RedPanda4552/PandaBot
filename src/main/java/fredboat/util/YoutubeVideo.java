/*
 * MIT License
 *
 * Copyright (c) 2016 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package fredboat.util;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.github.redpanda4552.PandaBot.Main;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeVideo {

    private static final Logger log = LoggerFactory.getLogger(YoutubeVideo.class);

    String id = null;
    String name = null;
    String duration = null;//Youtube has strange duration strings suchs as PT2H3M33S
    String description = null;
    String channelId = null;
    String channelTitle = null;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDuration() {
        return duration;
    }

    public String getDescription() {
        return description;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getCannelTitle() {
        return channelTitle;
    }

    public int getDurationHours() {
        Pattern pat = Pattern.compile("(\\d+)H");
        Matcher matcher = pat.matcher(duration);

        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1));
        } else {
            return 0;
        }
    }

    public int getDurationMinutes() {
        Pattern pat = Pattern.compile("(\\d+)M");
        Matcher matcher = pat.matcher(duration);

        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1));
        } else {
            return 0;
        }
    }

    public int getDurationSeconds() {
        Pattern pat = Pattern.compile("(\\d+)S");
        Matcher matcher = pat.matcher(duration);

        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1));
        } else {
            return 0;
        }
    }

    public String getDurationFormatted() {
        if (getDurationHours() == 0) {
            return forceTwoDigits(getDurationMinutes()) + ":" + forceTwoDigits(getDurationSeconds());
        } else {
            return forceTwoDigits(getDurationHours()) + ":" + forceTwoDigits(getDurationMinutes()) + ":" + forceTwoDigits(getDurationSeconds());
        }
    }

    public String getChannelUrl() {
        return "https://www.youtube.com/channel/" + channelId;
    }

    public String getChannelThumbUrl() {
        try {
            JSONObject json = Unirest.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&fields=items(snippet/thumbnails)")
                    .queryString("id", channelId)
                    .queryString("key", Main.config.get("youtube-api-key"))
                    .asJson()
                    .getBody()
                    .getObject();

            log.debug("Channel thumb response", json);

            return json.getJSONArray("items")
                    .getJSONObject(0)
                    .getJSONObject("snippet")
                    .getJSONObject("thumbnails")
                    .getJSONObject("default")
                    .getString("url");
        } catch (UnirestException e) {
            log.error("Failed to get channel thumbnail", e);
            return null;
        }
    }

    private String forceTwoDigits(int i) {
        if (i < 10) {
            return "0" + i;
        } else {
            return String.valueOf(i);
        }
    }

    @Override
    public String toString() {
        return "[YoutubeVideo:" + id + "]";
    }

}
