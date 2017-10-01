package forestry.counter.dto;

import java.math.BigDecimal;
import java.util.Date;

public class Timber {
    int user;
    int pref;
    int city;
    int forestGroup;
    int smallGroup;
    BigDecimal lat;
    BigDecimal lon;
    String kind = "";
    int height;
    int dia;
    int volume;
    int send;
    Date regDate;
    Date sendDate;

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

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

    public String getLatString() {
        return (this.lat != null) ? this.lat.toString():"";
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public void setLat(String lat) {
        if(lat == null || lat.equals("")) {
            this.lat = null;
        } else {
            this.lat = new BigDecimal(lat);
        }
    }

    public BigDecimal getLon() {
        return lon;
    }

    public String getLonString() {
        return (this.lon != null) ? this.lon.toString():"";
    }

    public void setLon(BigDecimal lon) {
        this.lon = lon;
    }

    public void setLon(String lon) {
        if(lon == null || lon.equals("")) {
            this.lon = null;
        } else {
            this.lon = new BigDecimal(lon);
        }
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

    public Date getRegDate() {
        return regDate;
    }

    public String getRegDateString() {
        return (this.regDate != null) ? this.regDate.toString():"";
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public void setRegDate(String regDate) {
        if(regDate == null || regDate.equals("")) {
            this.regDate = null;
        } else {
            this.regDate = new Date(regDate);
        }
    }

    public Date getSendDate() {
        return sendDate;
    }

    public String getSendDateString() {
        return (this.sendDate != null) ? this.sendDate.toString():"";
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public void setSendDate(String sendDate) {
        if(sendDate == null || sendDate.equals("")) {
            this.sendDate = null;
        } else {
            this.sendDate = new Date(sendDate);
        }
    }

    public String toString() {
        return getKind() + " " + String.valueOf(getDia());
    }
}
