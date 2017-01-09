package kung.stocknews.Model;

/**
 * Created by wkung on 1/9/17.
 */
public class AutoStockObject{
    String name;
    String ticker;
    public AutoStockObject(String n, String t){
        name = n; ticker = t;
    }

    public String getName(){
        return name;
    }

    public String getTicker(){
        return ticker;
    }
}