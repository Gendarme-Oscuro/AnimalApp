package it.uniba.dib.sms222321;

import java.util.ArrayList;
import java.util.List;

public class Animal {
    private String name; // Nome dell'animale
    private String age; // Età dell'animale
    private String animalType; // Tipo di animale
    private String owner; // Proprietario dell'animale
    private String url; // URL dell'immagine dell'animale
    private String biografia; // Biografia dell'animale
    private List<SaluteTable> vaccinations; // Lista delle vaccinazioni dell'animale
    private List<SaluteTable> dewormings; // Lista degli sverminamenti dell'animale
    private List<SaluteTable> visits; // Lista delle visite dell'animale
    private List<SaluteTable> food; // Lista del cibo dell'animale
    private List<SaluteTable> other; // Lista di altre informazioni sulla salute dell'animale
    private double spesaTotale; // Spesa totale sostenuta per l'animale

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

    public String getAnimalType() {
        return animalType;
    }

    public void setAnimalType(String animalType) {
        this.animalType = animalType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public List<SaluteTable> getVaccinations() {
        return vaccinations;
    }

    public void setVaccinations(List<SaluteTable> vaccinations) {
        this.vaccinations = vaccinations;
    }

    public void addVaccination(SaluteTable vaccination) {
        if (vaccinations == null) {
            vaccinations = new ArrayList<>();
        }
        vaccinations.add(vaccination);
    }

    public List<SaluteTable> getDewormings() {
        return dewormings;
    }

    public void setDeworming(List<SaluteTable> deworming) {
        this.dewormings = deworming;
    }

    public void addDeworming(SaluteTable dewormingEntry) {
        if (dewormings == null) {
            dewormings = new ArrayList<>();
        }
        dewormings.add(dewormingEntry);
    }

    public List<SaluteTable> getVisits() {
        return visits;
    }

    public void setVisits(List<SaluteTable> visits) {
        this.visits = visits;
    }

    public void addVisit(SaluteTable visit) {
        if (visits == null) {
            visits = new ArrayList<>();
        }
        visits.add(visit);
    }

    public List<SaluteTable> getFood() {
        return food;
    }

    public void setFood(List<SaluteTable> food) {
        this.food = food;
    }

    public void addFood(SaluteTable foodEntry) {
        if (food == null) {
            food = new ArrayList<>();
        }
        food.add(foodEntry);
    }

    public List<SaluteTable> getOther() {
        return other;
    }

    public void setOther(List<SaluteTable> other) {
        this.other = other;
    }

    public void addOther(SaluteTable otherEntry) {
        if (other == null) {
            other = new ArrayList<>();
        }
        other.add(otherEntry);
    }

    public double getSpesaTotale() {
        return spesaTotale ;
    }

    public void setSpesaTotale(double spesaTotale) {
        this.spesaTotale = spesaTotale;
    }
}
