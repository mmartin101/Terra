package bfergus.Terra_Wallpapers.Model;

import java.io.Serializable;

public class Reddit_API_Model implements Serializable {
    public Reddit_Data data;
    public String kind;

    public Reddit_API_Model(Reddit_Data data, String kind) {
       this.data = data;
        this.kind = kind;
    }

}
