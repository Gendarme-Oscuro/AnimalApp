package it.uniba.dib.sms222321;

import java.util.ArrayList;
import java.util.List;


public class Animal {

    String  name, age, animalType, owner;

    List<String> vaccinations ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getAnimalType() { return animalType; }

    public void setAnimalType(String animalType) {
        this.animalType = animalType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setVaccinations(List<String> vaccinations) {
        vaccinations = new ArrayList<>();
    }

    public void addVaccination(String vaccination) {
        vaccinations.add(vaccination);
    }

}
