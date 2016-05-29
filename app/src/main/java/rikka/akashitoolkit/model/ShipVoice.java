package rikka.akashitoolkit.model;

/**
 * Created by Rikka on 2016/4/28.
 */
public class ShipVoice {

    /**
     * zh : 我是睦月！一鼓作气向前冲吧！
     * jp : 睦月です。はりきって、まいりましょー！
     * url : http://upload.kcwiki.moe/commons/a/a1/031-Intro.mp3
     * scene : 入手/登入时
     * voiceId : 1
     */

    private String zh;
    private String jp;
    private String url;
    private String scene;
    private int voiceId;

    public String getZh() {
        return zh;
    }

    public void setZh(String zh) {
        this.zh = zh;
    }

    public String getJp() {
        return jp;
    }

    public void setJp(String jp) {
        this.jp = jp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public int getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(int voiceId) {
        this.voiceId = voiceId;
    }

    @Override
    public String toString() {
        return "ShipVoice{" +
                "zh='" + zh + '\'' +
                ", jp='" + jp + '\'' +
                ", url='" + url + '\'' +
                ", scene='" + scene + '\'' +
                ", voiceId=" + voiceId +
                '}';
    }
}
