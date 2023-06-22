package it.uniba.dib.sms222321;


import java.util.ArrayList;
import java.util.List;

/*
Classe di supporto per gestire i dati degli utenti
 */

public class All_User_Member {
    String name;
    String uid;
    String surname;
    String company_name;
    String age;
    String userType;
    String url;
    long numAnimals;

    long numRequests;
    List<String> pets;

    List<String> pokedex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getCompany_name() {return company_name;}
    public void setCompany_name(String company_name) {this.company_name = company_name;}
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

    public long getNumAnimals() {
        return numAnimals;
    }

    public long getNumRequests() {
        return numRequests;
    }

    public void setNumRequests(long numRequests) {
        this.numRequests = numRequests;
    }

    public void setNumAnimals(long numAnimals) {
        this.numAnimals = numAnimals;
    }

    public List<String> getPets() {
        return pets;
    }

    public void setPets(List<String> pets) {
        this.pets = pets;
    }

    public void addPet(String petName) {
        if (pets == null) {
            pets = new ArrayList<>();
        }
        pets.add(petName);
    }

    public List<String> getPokedex() {
        return pokedex;
    }

    public void setPokedex(List<String> pokedex) {
        this.pokedex = pokedex;
    }

    public void addPokedex(String idanimal) {
        if (pokedex == null) {
            pokedex = new ArrayList<>();
        }
        pokedex.add(idanimal);
    }




}
