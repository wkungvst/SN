package kung.stocknews.Model;

/**
 * Created by wkung on 12/23/16.
 */
public class IndexCard {
    private String name;
    private String symbol;
    private String lprice;
    private String pchange;
    private String pchangep;
    private String hi;
    private String lo;



    public IndexCard(String name, String symbol, String lprice, String pchange, String pchangep){
        this.name = name;
        this.symbol = symbol;
        this.lprice = lprice;
        this.pchange = pchange;
        this.pchangep = pchangep;
    }

    public String getName(){
        return name;
    }

    public String getSymbol(){ return symbol;}

    public String getLastPrice(){return lprice;}

    public String getPriceChange(){ return pchange; }

    public String getPriceChangePercentage(){ return pchangep;}

    public String getHigh(){ return hi;}

    public String getLow(){ return lo;}

}
