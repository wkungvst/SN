package kung.stocknews.Model;

/**
 * Created by wkung on 12/23/16.
 */
public class IndexCard {
    private String name;
    private String value;

    public IndexCard(String name, String value){
        this.name = name;
        this.value = value;
    }

    public String getName(){
        return name;
    }

    public String getValue(){
        return value;
    }
}
