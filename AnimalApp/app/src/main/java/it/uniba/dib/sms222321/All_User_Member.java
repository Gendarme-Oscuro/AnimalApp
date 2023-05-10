package it.uniba.dib.sms222321;


import java.util.ArrayList;
import java.util.List;


public class All_User_Member {
    String name, uid, surname, age, userType, url;

    Long numAnimals = Long.valueOf(0);

    List<String> pets = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getNumAnimals() {
        return numAnimals;
    }

    public void setNumAnimals(Long numAnimals) {
        this.numAnimals = numAnimals;
    }

    public List<String> getPets() {
        return pets;
    }

    public void setPets(List<String> pets) {
        this.pets = pets;
    }



}
