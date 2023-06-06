package it.uniba.dib.sms222321;

import java.util.ArrayList;

public class RequestMember {

    String url;
    String name;
    String surname;
    String company_name;
    String id;
    String key;
    String request;
    String time;

    private ArrayList<String> photoUrls;

    public RequestMember(){

    }

    public RequestMember(String url, ArrayList<String> photoUrls, String name, String surname, String company_name, String id, String key, String request, String time) {
        this.url = url;
        this.photoUrls = new ArrayList<>();
        this.name = name;
        this.surname = surname;
        this.company_name = company_name;
        this.id = id;
        this.key = key;
        this.request = request;
        this.time = time;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(ArrayList<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
