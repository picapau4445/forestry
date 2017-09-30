package forestry.counter.dto;

import java.math.BigDecimal;

public class Timber {
    int pref;
    int city;
    int forestGroup;
    int smallGroup;
    BigDecimal lat;
    BigDecimal log;
    String kind;
    int height;
    int dia;
    int volume;
    int send;

    public int getPref() {
        return pref;
    }

    public void setPref(int pref) {
        this.pref = pref;
    }

    public int getCity() {
        return city;
    }

    public void setCity(int city) {
        this.city = city;
    }

    public int getForestGroup() {
        return forestGroup;
    }

    public void setForestGroup(int forestGroup) {
        this.forestGroup = forestGroup;
    }

    public int getSmallGroup() {
        return smallGroup;
    }

    public void setSmallGroup(int smallGroup) {
        this.smallGroup = smallGroup;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLog() {
        return log;
    }

    public void setLog(BigDecimal log) {
        this.log = log;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getSend() {
        return send;
    }

    public void setSend(int send) {
        this.send = send;
    }

    public String toString() {
        return getKind() + " " + String.valueOf(getDia());
    }
}
