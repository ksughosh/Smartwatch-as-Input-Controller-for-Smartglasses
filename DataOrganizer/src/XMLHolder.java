import java.util.ArrayList;


public class XMLHolder {
    Coordinates source, destination;
    ArrayList<DataHolder> holder;
    DataHolder prev;

    XMLHolder(Coordinates s, Coordinates d){
        source = s;
        destination = d;
        holder = new ArrayList<>();
    }

    public void add(float x, float y, float relativeTime, float startTime){
        DataHolder d = new DataHolder(x, y, relativeTime, startTime);
        if (!d.isEqual(prev))
            holder.add(d);
        prev = d;
    }

    public void clearRedundantValues(XMLHolder d2) {
        for (int i = 0; i < holder.size() ; i++){
            DataHolder d = holder.get(i);
            for (int j = 0; j < d2.holder.size(); j++){
                DataHolder d_ = d2.holder.get(j);
                if (d.isEqual(d_)){
                    d2.holder.remove(d_);
                }
            }
        }
    }


    public Coordinates getSource(){
        return source;
    }

    public Coordinates getDestination(){
        return destination;
    }

    public ArrayList<DataHolder> getHolder(){
        return holder;
    }
}
