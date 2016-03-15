package bfergus.Terra_Wallpapers.Model;

import java.io.Serializable;


public class Reddit_Children_Data implements Serializable {
    public String name;
    public String title;
    public String url;

    public Reddit_Children_Data(String name, String title, String url) {
        this.name = name;
        this.title = title;
        this.url = url;
    }
}
