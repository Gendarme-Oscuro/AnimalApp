package it.uniba.dib.sms222321;

import java.util.HashMap;
import java.util.Map;

public class SaluteTable {
    private String descrizione;
    private String data;
    private String spesa;

    public SaluteTable() {
        // Required empty constructor for Firestore
    }

    public SaluteTable(String descrizione, String data, String spesa) {
        this.descrizione = descrizione;
        this.data = data;
        this.spesa = spesa;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSpesa() {
        return spesa;
    }

    public void setSpesa(String spesa) {
        this.spesa = spesa;
    }


}
